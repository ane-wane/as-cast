package fr.stack.peersampling;

import peersim.core.Node;



public interface IDynamicNetwork {

    public PeerSampling getPeerSampling();
    
    public void onEdgeUp(Node newNeighbor);
    public void onEdgeDown(Node ripNeighbor);

    // (TODO) onPeerDown ? 
    // (TODO) sendTo ? 
}
