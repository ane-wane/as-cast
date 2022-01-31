package fr.stack.observers;

import peersim.core.Control;
import peersim.core.Node;
import peersim.core.Network;
import peersim.config.Configuration;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.HashSet;

import fr.stack.controllers.CDynamicPartitions;
import fr.stack.partitioners.IPartitioner;
import fr.stack.partitioners.ascast.ASPartitioner;
import fr.stack.peersampling.PeerSampling;
import fr.stack.peersampling.Weights;
    


public class OFinalCheck implements Control {

    private static final String PAR_PROT = "protocol";
    private final int protocol;

    // for processing purpose
    private static HashMap<Node, HashMap<Node, Double>> sourcesToProcesses = new HashMap<>();
    

    
    public OFinalCheck (String prefix) {
        this.protocol = Configuration.getPid(prefix + "." + PAR_PROT);
    }
    
    public boolean execute () {
        System.out.println("OFinalCheck: Checking…");        

	for (Node source : CDynamicPartitions.addersSet) {
	    sourcesToProcesses.put(source, weightedDijkstra(source));
	}
	
        for (int i = 0; i < Network.size(); ++i) {
	    Node toProcess = Network.get(i);
	    
            Double minDistance = Double.MAX_VALUE;
	    Node closestSource = null;
            for (Node source: CDynamicPartitions.addersSet) {
		if (minDistance >=  sourcesToProcesses.get(source).get(toProcess)) {
		    minDistance = sourcesToProcesses.get(source).get(toProcess);
		    closestSource = source;
		}
            }
            
            IPartitioner p = (IPartitioner) toProcess.getProtocol(this.protocol);
            if (p.getBestDistance() != minDistance){
                System.out.println(String.format("OFinalCheck: (FAIL) peer %s has a distance %s instead of %s to %s.",
						 toProcess.getID(),  p.getBestDistance(),
						 minDistance, closestSource.getID()));

                ASPartitioner debug  = (ASPartitioner) Network.get(i).getProtocol(this.protocol);
                System.out.println(String.format("current = %s", debug.best.toString()));
		for (long id : debug.versions.versions.keySet()) {
		    System.out.print(String.format("%s => %s; ", id, debug.versions.versions.get(id)));
		}
		System.out.println();
            }
        }

        System.out.println("OFinalCheck: Checked!");
        return false;
    }


    public HashMap<Node, Double> weightedDijkstra(Node start) {
	HashMap<Node, Double> distances = new HashMap<>();
	
	for (int i = 0; i < Network.size(); ++i )
	    distances.put(Network.get(i), Double.POSITIVE_INFINITY);

	distances.put(start, 0.);

	HashSet<Node> queue = new HashSet<Node>(distances.keySet());
	
	while (!queue.isEmpty()) {
	    Node minDistanceNode = min(queue, distances);
	    queue.remove(minDistanceNode);

	    PeerSampling ps = ((ASPartitioner) minDistanceNode.getProtocol(this.protocol)).peerSampling;
		
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
