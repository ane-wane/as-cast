package fr.stack.peersampling;

import peersim.transport.Transport;
import peersim.core.Node;
import peersim.config.FastConfig;



/**
 * Class that makes sure that any received message is legit received
 * by the current link despite the event-like functioning. The other
 * way would be to remove the events generated when the communication
 * link dies.
 * 
 */
public class Link {

    // to generate globally unique ID for each link
    public static int nth = 0;
    
    public final int id;
    public int counter;

    public final Node from;
    public final Node to;
    public final Integer PID;
    
    public Link(Node from, Node to, Integer PID) {
	this.counter = 0;
	this.id = (int) Link.nth / 2; // one id shared by 2 links created at same time
	this.from = from;
	this.to = to;
	this.PID = PID;
	
	Link.nth += 1;
	
    }

    public boolean isValidForReceipt(IMessage m){
	return m.fromID() == this.id;
    }

    public void send(IMessage m){
	this.counter += 1;
	m.setCounter(this.counter);
	m.setID(this.id);
	Transport t = (Transport) from.getProtocol(FastConfig.getTransport(PID));
        t.send(from, to, m, PID);
    }
    
}
