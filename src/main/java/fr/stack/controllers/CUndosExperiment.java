package fr.stack.controllers;

import peersim.core.Control;
import peersim.util.ExtendedRandom;
import peersim.core.CommonState;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;

import fr.stack.partitioners.IAdder;
import fr.stack.peersampling.IDynamicNetwork;
import fr.stack.peersampling.PeerSampling;



/**
 * When an edge crashes or a node leave, all information that it
 * propagated becomes stale. All subsequent nodes must realize that
 * and eventually converge toward their closest partition despite
 * concurrent operations, undo, etc.
 */
public class CUndosExperiment implements Control {

    private static final String PAR_PROT = "protocol";
    private final int protocol;

    // The node that will be added and be a source
    private static final String PAR_NODE = "node";
    private final Node node;

    // The number of links that are added to random nodes with designated latencies
    private static final String PAR_EDGES = "edges";
    private final Integer edges;
    
    // The number of edges that crashes
    private static final String PAR_CRASHES = "crashes";
    private final int crashes;

    // and when.
    private static final String PAR_CRASHESAT = "crashAt";
    private final int crashesAt;
    
    private static ExtendedRandom erng;

    public boolean isInit = false;


    
    public CUndosExperiment(String prefix) {
	CUndosExperiment.erng = new ExtendedRandom(CommonState.r.getLastSeed());
	this.protocol = Configuration.getPid(prefix + "." + PAR_PROT);
	int nodeID = Configuration.getInt(prefix + "." + PAR_NODE);
	this.node = Network.get(nodeID);
	this.edges = Configuration.getInt(prefix + "." + PAR_EDGES);
	this.crashes = Configuration.getInt(prefix + "." + PAR_CRASHES);
	this.crashesAt = Configuration.getInt(prefix + "." + PAR_CRASHESAT);

    }


    
    public boolean execute () {

	if (!isInit) {
	    IDynamicNetwork p = (IDynamicNetwork) this.node.getProtocol(this.protocol);
	    PeerSampling ps = p.getPeerSampling();
	    
	    for (int i = 0; i < edges; ++i) {
		int toID = erng.nextInt(Network.size());
		Node to = Network.get(toID);
		ps.add(to);
	    }
	    
	    IAdder source = (IAdder) this.node.getProtocol(this.protocol);
	    source.add();
	    CDynamicPartitions.addersSet.add(this.node);

	    isInit = true;
	}
	
	if (CommonState.getIntTime() == this.crashesAt) {
	    IDynamicNetwork p = (IDynamicNetwork) this.node.getProtocol(this.protocol);
	    PeerSampling ps = p.getPeerSampling();
	    
	    for (int i = 0; i < crashes; ++i) {
		Node to = ps.neighbors().get(0);
		ps.rem(to);
	    }
	}
	
	return false;
    }
    
}
