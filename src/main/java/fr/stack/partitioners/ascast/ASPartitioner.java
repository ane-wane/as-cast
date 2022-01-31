package fr.stack.partitioners.ascast;

import peersim.core.CommonState;
import peersim.config.Configuration;
import peersim.edsim.EDProtocol;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.stack.structures.Pair;
import fr.stack.partitioners.MonitorMessages;
import fr.stack.partitioners.IAdder;
import fr.stack.partitioners.IDeller;
import fr.stack.partitioners.IPartitioner;
import fr.stack.structures.Versions;
import fr.stack.structures.Identifier;
import fr.stack.peersampling.Weights;
import fr.stack.peersampling.IDynamicNetwork;
import fr.stack.peersampling.PeerSampling;
import fr.stack.peersampling.IMessage;
import fr.stack.peersampling.MessagesToSend;



public class ASPartitioner implements EDProtocol, CDProtocol,
					  IDynamicNetwork,
					  IPartitioner, IAdder, IDeller {

    public static String PAR_PID = "pid";
    public static Integer PID;

    public static String PAR_LINKABLE = "linkable";
    public static Integer LINKABLE;
   
    public Node node;
    public Long id;
    public int counter = 0;
    public XVersions versions = new XVersions(); // simplified version of epoch vector...
    public PeerSampling peerSampling = new PeerSampling();
    
    public MAdd best = MAdd.NOTHING();

    public MonitorMessages monitor = new MonitorMessages();

    public static Logger log = LogManager.getLogger("ASPartitioner");
    


    public ASPartitioner () { // everything init above
    }

    public ASPartitioner (Node n) { // (convenient to test the class)
	this.node = n;
	this.id = n.getID();
    }

    public ASPartitioner addNeighbor (Node n) { // (for testing purpose only)
	this.peerSampling.neighbors.put(n, null);
	return this;
    }
    
    public ASPartitioner (String prefix) {
        ASPartitioner.PID = Configuration.getPid(prefix + "." + PAR_PID);
        ASPartitioner.LINKABLE = Configuration.getPid(prefix + "." + PAR_LINKABLE);
    }


    
    public void nextCycle(Node node, int protocolID) {
        this.lazyLoadNode(node); 
    }

    public void processEvent(Node receiver, int protocolId, Object message) {
        this.lazyLoadNode(receiver);
	
	// #A drop messages that come from dead links (simulator specific)
	if (!this.peerSampling.isValidForReceipt((IMessage) message)) 
	    return;
    	
	// #B register message for observers to exploit
	this.monitor.received(message);
	
	// #C update the partition if need be
	if (message instanceof MAdd) {
	    MAdd a = (MAdd) message;
	    this.peerSampling.sendTo(this.receiveAdd(a.last, a));
	} else if (message instanceof MDel) {
	    MDel d = (MDel) message;
	    this.peerSampling.sendTo(this.receiveDel(d.last, d));
	}
    }
    


    public void add() {
	this.peerSampling.sendTo(this._add());
    }

    public void del() {
	this.peerSampling.sendTo(this._del());
    }

    
    public MessagesToSend _add() {
	counter += 1;
	return this.receiveAdd(this.node, new MAdd(id, counter, 0));
    }
    
    public MessagesToSend _del() {
        counter += 1;
	return this.receiveDel(this.node, new MDel(id, counter));
    }

    public MessagesToSend receiveAdd(Node q, MAdd a) {
	MessagesToSend messages = new MessagesToSend();
	
	if (versions.isStaleAdd(a) && q.equals(this.best.last)) {
	    // #A might be unconsistency, resolve this
	    counter += 1;
	    MDel d = new MDel(id, counter);
	    messages.send(this.receiveDel(q, d)); // ∆
	} else if (!versions.isStaleAdd(a) && !a.isLooping(this.node) && a.isBetterThan(this.best)) {
	    // #B better partition detected
	    this.best = a;
	    for (Node n : this.peerSampling.neighbors()) {
		MAdd f = a.fwd(this.node, counter, Weights.get(id, n.getID())); // α
		messages.send(n, f);
	    }
	}
	
	versions.updateWithAdd(a); // always register the add to not go backwards
	
	return messages;
    }


    public MessagesToSend receiveDel(Node q, MDel d) {
	MessagesToSend messages = new MessagesToSend();

	if (d.shouldDel(best) || d.id.id == id) {
	    this.best = MAdd.NOTHING();
	    for (Node n: peerSampling.neighbors())
		if (!q.equals(n)) { // removes ~half ∂ and some α
		    messages.send(n, d.fwd(this.node)); // #1 forward ∆ or ∂
		}
	} else if (!best.isNothing()) { //  && !d.hasCommonAncestor(best) && !best.isLooping(q)) {
	    messages.send(q, this.best.fwd(this.node, counter, Weights.get(id, q.getID()))); // #2 echo our α
	}
	
	versions.updateWithDel(d);
	
	return messages;
    }
    


    public void onEdgeUp(Node newNeighbor) {
	assert (!newNeighbor.equals(this.node));
	
	// #2 send current partition if we have one
	if (!this.best.isNothing()){
	    // Might create a shortcut. Inform each other of current partition.
	    MAdd f = this.best.fwd(this.node, counter, Weights.get(this.node.getID(), newNeighbor.getID()));
	    this.peerSampling.sendTo(newNeighbor, f);
	}
    }
    
    public void onEdgeDown(Node rip) {
	log.info("Edge CRASHED between {} and {}.", this.node.getID(), rip.getID());
	
	if (!this.best.isNothing() &&
	    !this.best.isSource() &&  // not the source ourselves
	    this.best.last.equals(rip)) { // and our best comes from removed edge
	    counter += 1;
	    MDel c = new MDel(this.node.getID(), counter);
	    log.info("@{}: edge down to {}, undoing {}", this.node.getID(), rip.getID(), c);
	    this.peerSampling.sendTo(this.receiveDel(rip, c));
	}
    }
    


    public void lazyLoadNode(Node n) {
        if (Objects.isNull(this.node)) {
            this.node = n;
	    this.id = n.getID();
            this.peerSampling = ((PeerSampling) this.node.getProtocol(ASPartitioner.LINKABLE))
                .setNode(this.node).setPID(ASPartitioner.PID);
        }
    }

    public MonitorMessages getMonitor() {
	return this.monitor;
    }
    
    public long getBestPartition() {
        return this.best.id.id;
    }

    public double getBestDistance() {
	return this.best.weight;
    }

    public PeerSampling getPeerSampling() {
        return this.peerSampling;
    }
    
    @Override
    public Object clone() {
        return new ASPartitioner();
    }

    public void logMessage(long id, Object m) {
	if (this.node.getID() == id) {
		System.out.println("====================================================");
	    log.info("@{}: local: {};", id,  best);
	    if (m instanceof MAdd) {
		MAdd madd = (MAdd) m;
		log.info("@{}: ADD {}", id, madd);
	    } else if (m instanceof MDel) {
		MDel mdel = (MDel) m;
		log.info("@{}: DEL {}", id, mdel);
	    }
	}
    }

    
}
