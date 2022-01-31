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

import fr.stack.partitioners.ascast.ASPartitioner;
import fr.stack.controllers.CDynamicPartitions;
import fr.stack.partitioners.IPartitioner;
import fr.stack.peersampling.PeerSampling;
import fr.stack.peersampling.Weights;



public class OMiss implements Control {

    public static final String PAR_PROT = "protocol";
    public static int protocol;

    private static HashMap<Node, HashMap<Node, Double>> sourcesToProcesses = new HashMap<>();



    public OMiss(String prefix) {
        OMiss.protocol = Configuration.getPid(prefix + "." + PAR_PROT);
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
	    
	if (PeerSampling.changed || !newSources.isEmpty()) {
	    for (Node source : newSources) {
		sourcesToProcesses.put(source, weightedDijkstra(source));
		PeerSampling.changed = false;
	    }
	}
	
        for (int i = 0; i < Network.size(); ++i) {
	    Node toProcess = Network.get(i);
	    
            Double minDistance = Double.POSITIVE_INFINITY;
            for (Node source : CDynamicPartitions.addersSet) {
		minDistance = Math.min(minDistance, sourcesToProcesses.get(source).get(toProcess));
            }
            
            IPartitioner p = (IPartitioner) Network.get(i).getProtocol(this.protocol);
            if (p.getBestDistance() != minDistance) {
                if (p.getBestDistance() < minDistance ||
                    p.getBestDistance() == Double.POSITIVE_INFINITY) {
                    // #A reference a dead partition, or none, or
                    // # through a path that was shorter but does not
                    // # exist anymore.
                    noSource += 1;
                }
                if (p.getBestDistance() > minDistance &&
                    p.getBestDistance() != Double.POSITIVE_INFINITY) {
		    // #B reference a partition that is farther, or
		    // # the good partition with longer path
                    wrongSource += 1;
                    cumulativeErrorDistance += p.getBestDistance() - minDistance;
                }
            }
        }
	
        System.out.println(String.format("OMiss: %s ; no= %s ; wrong= %s ; cumulative= %s",
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
	    
	    PeerSampling ps = ((ASPartitioner) minDistanceNode.getProtocol(OMiss.protocol)).peerSampling;
		
	    for (Node neighbor : ps.neighbors()) {
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
