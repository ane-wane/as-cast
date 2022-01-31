package fr.stack.peersampling;

import fr.stack.structures.Pair;

import peersim.core.Node;
import peersim.core.Protocol;
import peersim.core.Linkable;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;



public class PeerSampling implements Protocol, Linkable {

    public Node node;
    public Integer pid;
    
    public HashMap<Node, Link> neighbors = new HashMap<Node, Link>();

    // Global variable to know if graph should be reprocessed
    public static boolean changed = true; 
    


    public PeerSampling () {
    }

    public PeerSampling (String prefix) {
    }
    
    public PeerSampling (Node node, Integer pid) {
        this.pid = pid;
        this.node = node;
    }

    public PeerSampling setNode(Node node) {
        this.node = node;
        return this;
    }

    public PeerSampling setPID(int pid) {
        this.pid = pid;
        return this;
    }
    
    public boolean add(Node n) {
        if (!this.neighbors.containsKey(n)) {
            IDynamicNetwork thisProtocol = (IDynamicNetwork) this.node.getProtocol(this.pid);
            IDynamicNetwork otherProtocol = (IDynamicNetwork) n.getProtocol(this.pid);
            PeerSampling other = otherProtocol.getPeerSampling();
	    Link lt = new Link(this.node, n, this.pid); // t->o
	    Link lo = new Link(n, this.node, this.pid); // o->t
	    this.neighbors.put(n, lt);
            other.neighbors.put(this.node, lo);

	    PeerSampling.changed = true;
	    
            thisProtocol.onEdgeUp(n);
            otherProtocol.onEdgeUp(this.node);
	    
            return true;
        }
        return false;
    }

    public boolean rem(Node n) {
        if (this.neighbors.containsKey(n)) {
            IDynamicNetwork thisProtocol = (IDynamicNetwork) this.node.getProtocol(this.pid);
            IDynamicNetwork otherProtocol = (IDynamicNetwork) n.getProtocol(this.pid);
            PeerSampling other = otherProtocol.getPeerSampling();
            this.neighbors.remove(n);
            other.neighbors.remove(this.node);
	    
            PeerSampling.changed = true;
	    
            thisProtocol.onEdgeDown(n);
            otherProtocol.onEdgeDown(this.node);
	    
            return true;
        }
        return false;
    }



    public boolean isValidForReceipt(IMessage m) {
	boolean valid = false;

	Iterator<Node> it = this.neighbors.keySet().iterator();
	while (!valid && it.hasNext()){
	    Node neighbor = it.next();
	    valid = this.neighbors.get(neighbor).isValidForReceipt(m);
	}
	
	return valid;
    }

    public void sendTo(Node neighbor, IMessage message) {
	assert (this.neighbors.containsKey(neighbor));
	this.neighbors.get(neighbor).send(message);
    }

    public void sendTo(MessagesToSend messages) {
	for (Pair<Node, IMessage> pair : messages)
	    this.sendTo(pair.first, pair.second); 
    }

    public ArrayList<Node> neighbors() {
	return new ArrayList<Node>(this.neighbors.keySet());
    }

    public ArrayList<Long> neighborsAsID () {
	ArrayList<Long> result = new ArrayList<Long>();
	for (Node n : neighbors.keySet()) {
	    result.add(n.getID());
	}
	return result;
    }
    


    public boolean addNeighbor(Node node) {
        return this.add(node);
    }

    public boolean contains(Node node) {
        return this.neighbors.containsKey(node);
    }

    public int degree() {
        return this.neighbors.size();
    }

    public Node getNeighbor(int i) {
        List<Node> intoList = new ArrayList<Node>(this.neighbors.keySet());
        return intoList.get(i);
    }

    public void pack() {
        // (TODO) ???? 
    }

    public void onKill() {
        this.neighbors = null;
    }

    public Object clone() {
        return new PeerSampling();
    }
}
