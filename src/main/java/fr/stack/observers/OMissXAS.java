package fr.stack.observers;

import peersim.core.Control;
import peersim.core.Node;
import peersim.core.Network;
import peersim.config.Configuration;
import peersim.core.CommonState;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Arrays;

import fr.stack.controllers.CAutonomousSystems;
import fr.stack.partitioners.ascast.ASPartitioner;
import fr.stack.partitioners.xascast.CrossASPartitioner;
import fr.stack.controllers.CDynamicPartitions;
import fr.stack.partitioners.IPartitioner;
import fr.stack.peersampling.PeerSampling;
import fr.stack.peersampling.Weights;



/**
 * OMiss but extended to a interconnected networks instead of one big
 * network.
 */
public class OMissXAS implements Control {

    public static final String PAR_PROT = "protocol";
    public static int protocol;

    private static HashMap<Node, HashMap<Node, Double>> sourcesToProcesses = new HashMap<>();



    public OMissXAS(String prefix) {
	protocol = Configuration.getPid(prefix + "." + PAR_PROT);
	CDynamicPartitions.addersSet = new ArrayList<>();
    }

    public boolean execute () {
        int noSource = 0; // have a source that does not exist anymore
        int wrongSource = 0; // not the best source
        double cumulativeErrorDistance = 0.; // overhead

	if (PeerSampling.changed) { // must reprocess everything
	    sourcesToProcesses = new HashMap<>();
	}
	
	Set<Node> newSources = new HashSet<Node>(CDynamicPartitions.addersSet);
	newSources.removeAll(sourcesToProcesses.keySet());
	Set<Node> removedSources = sourcesToProcesses.keySet();
	removedSources.removeAll(CDynamicPartitions.addersSet);

	newSources = new HashSet<Node>(CDynamicPartitions.addersSet); // redo for all sources
	if (PeerSampling.changed || !newSources.isEmpty() || !removedSources.isEmpty()) {
	    for (Node source : newSources) {
		ASPartitioner casp = (ASPartitioner) source.getProtocol(this.protocol);
		if (casp.best.weight == 0.) { // not a fake source
		    sourcesToProcesses.put(source, weightedDijkstra(source));
		    PeerSampling.changed = false;
		}
	    }
	}
	
        for (int i = 0; i < Network.size(); ++i) {
	    Node toProcess = Network.get(i);
	    
            Double minDistance = Double.POSITIVE_INFINITY;
	    Node closestSource = null;
            for (Node source : CDynamicPartitions.addersSet) {
		ASPartitioner casp = (ASPartitioner) source.getProtocol(this.protocol);
		if (casp.best.weight == 0.) { // not a fake source
		    if (minDistance > sourcesToProcesses.get(source).get(toProcess)) {
			minDistance = sourcesToProcesses.get(source).get(toProcess);
			closestSource = source;
		    }
		}
            }
	    
            IPartitioner p = (IPartitioner) Network.get(i).getProtocol(this.protocol);

	    
            if (p.getBestDistance() != minDistance) {
                if (p.getBestDistance() < minDistance ||
                    p.getBestDistance() == Double.POSITIVE_INFINITY) {
                    // #A reference a dead partition or none
                    noSource += 1;

		    if (CommonState.getIntTime() > 50000000) {
			CrossASPartitioner casp = (CrossASPartitioner) Network.get(i).getProtocol(this.protocol);
			String asn = CAutonomousSystems.nodeToAsn.get(casp.node.getID());
			String asnSource = CAutonomousSystems.nodeToAsn.get(closestSource.getID());
			System.out.println("@@@" + Network.get(i).getID() +
					   " FROM asn-" + asn + "  ; ; ; G== " + casp.globalBest +
					   "   ||| L==" + casp.best + "||||||| INSTEAD OF "+ minDistance + " TO " + closestSource.getID() + " ASN-"+ asnSource);
		    }

                }
                if (p.getBestDistance() > minDistance &&
                    p.getBestDistance() != Double.POSITIVE_INFINITY) {
                    wrongSource += 1;
                    cumulativeErrorDistance += p.getBestDistance() - minDistance;

		    // if (CommonState.getIntTime() > 2950) {
		    // 	CrossASPartitioner casp = (CrossASPartitioner) Network.get(i).getProtocol(this.protocol);
		    // 	String asn = CAutonomousSystems.nodeToAsn.get(casp.node.getID());
		    // 	String asnSource = CAutonomousSystems.nodeToAsn.get(closestSource.getID());
		    // 	System.out.println("@@@" + Network.get(i).getID() +
		    // 			   " FROM asn-" + asn + "  ; ; ; G== " + casp.globalBest +
		    // 			   "   ||| L==" + casp.best + "||||||| INSTEAD OF "+ minDistance + " TO " + closestSource.getID() + " ASN-"+ asnSource);
		    // 	System.out.println("@@@" + Network.get(i).getID() + "||||| NIEGHBHIREIHEORRRRS  = "
		    // 	+ Arrays.toString(casp.peerSampling.neighborsAsID().toArray()));
		    // }
                }
            }
        }
	
        System.out.println(String.format("OMissXAS: %s ; no= %s ; wrong= %s ; cumulative= %s",
                                         CommonState.getTime(),
                                         noSource, wrongSource,
                                         cumulativeErrorDistance));
        
        return false;
    }


    public static HashMap<Node, Double> weightedDijkstra(Node start) {
	HashMap<Node, Double> distances = new HashMap<>();
	
	for (int i = 0; i < Network.size(); ++i )
	    distances.put(Network.get(i), Double.POSITIVE_INFINITY);
	
	distances.put(start, 0.);

	HashSet<Node> queue = new HashSet<Node>(distances.keySet());
	
	while (!queue.isEmpty()) {
	    Node minDistanceNode = min(queue, distances);
	    queue.remove(minDistanceNode);
	    
	    if (Objects.isNull(minDistanceNode)) {
		return distances;
	    }
	    
	    PeerSampling ps = ((ASPartitioner) minDistanceNode.getProtocol(OMissXAS.protocol)).peerSampling;
		
	    for (Node neighbor : ps.neighbors()) {
		
		// only consider links and nodes that belong to an AS replicating
		String asn = CAutonomousSystems.nodeToAsn.get(neighbor.getID());
		boolean isASInterested = false;
		for (Node asNeighbor : CAutonomousSystems.asnToNodes.get(asn)) {
		    if (CDynamicPartitions.addersSet.contains(asNeighbor)) {
			isASInterested = true; // no opti w/e
		    }
		}		
		if (!isASInterested)
		    continue;
		
		double sumOfWeights = distances.get(minDistanceNode) +
		    Weights.get(minDistanceNode.getID(), neighbor.getID());

		if (sumOfWeights < distances.get(neighbor)) {
		    distances.put(neighbor, sumOfWeights);
		}
	    }
	}
	
	return distances;
    }

    public static Node min(HashSet<Node> queue, HashMap<Node, Double> distances) {
	// (TODO) better would be sorted by distance but w/e
	// might slow down when 10k peers though
	double distance = Double.POSITIVE_INFINITY;
	Node minDistanceNode = null;
	for (Node node : queue) {
	    if (distances.get(node) < distance) {
		distance = distances.get(node);
		minDistanceNode = node;	
	    }
	}
	
	return minDistanceNode;
    }

    
}
