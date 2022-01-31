package fr.stack.partitioners.xascast;

import peersim.core.CommonState;
import peersim.core.Node;
import java.util.Objects;
import java.util.HashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.stack.structures.Identifier;
import fr.stack.peersampling.Weights;
import fr.stack.controllers.CAutonomousSystems;
import fr.stack.peersampling.IMessage;
import fr.stack.partitioners.ascast.ASPartitioner;
import fr.stack.partitioners.ascast.MAdd;
import fr.stack.partitioners.ascast.MDel;
import fr.stack.partitioners.IPartitioner;
import fr.stack.peersampling.MessagesToSend;
    


/**
 * Cross autonomous systems (xAS-) partitioner. Nodes do not share
 * every content with all processes of the network. Instead, members
 * of an autonomous system index a content only when they replicated
 * it, i.e., a member is a source. When no source exist in the AS
 * anymore, the index disappears to reflect this change.
 **/
public class CrossASPartitioner extends ASPartitioner implements IPartitioner {

    public MAdd globalBest;

    public static Logger log = LogManager.getLogger("CrossASPartitioner");
    


    public CrossASPartitioner () {
	super();
	this.globalBest = MAdd.NOTHING();
    }
    
    public CrossASPartitioner (String prefix) {
	super(prefix);
	this.globalBest = MAdd.NOTHING();
    }



    @Override
    public void nextCycle(Node node, int protocolID) {
	// Check invariant
	if (!best.isNothing()) {
	    for (Identifier idPath : best.path) {
		if (!CAutonomousSystems.fromSameASWithID(id, idPath.id)) {
		    log.error("@{}, local {} contains nodes from a different AS", id, best);
		    System.exit(1);
		}
	    }
	}
	if (!globalBest.isNothing()) {
	    boolean throughAnotherAS = false;
	    for (Identifier idPath : globalBest.path)  {
		if (!CAutonomousSystems.fromSameASWithID(id, idPath.id)) {
		    throughAnotherAS = true;
		}
	    }
	    if (!throughAnotherAS) {
		log.error("@{}, global {} did not go through different AS", id, globalBest);
		System.exit(1);
	    }
	}
	
	if (!globalBest.isNothing() && !best.isNothing() && best.isBetterThan(globalBest)) {
	    log.error("@{}, global {} should be better than local {}… but it's not", id, globalBest, best);
	    System.exit(1);
	}

	if (best.isNothing() && !globalBest.isNothing()) {
	    log.error("@{}, global should not exist {} for local does not", id, globalBest);
	    System.exit(1);
	}
	
	super.nextCycle(node, protocolID);
    }
    
    @Override
    public void processEvent(Node receiver, int protocolId, Object message) {
	// #A drop messages that come from dead links (simulator specific)
	if (!this.peerSampling.isValidForReceipt((IMessage) message))
	    return;

	// #1 message is from another AS, it changes its meaning
	if ((message instanceof MAdd) && !(message instanceof MGAdd)) {
	    MAdd a = (MAdd) message;
	    if (!CAutonomousSystems.fromSameAS(this.node, a.last)) {
		message = new MGAdd(a);
		MGAdd mgadd = (MGAdd) message;
		peerSampling.sendTo(this.justChangedMGAdd(a.last, mgadd));
	    }
	}
	
	// #B register message for observers to exploit
	this.monitor.received(message);

	// #C update the partition if need be
	// #2 process messages depending on the AS it comes from.
	if (message instanceof MGAdd) { // MGAdd must precede MAdd
	    MGAdd mgadd = (MGAdd) message;
	    peerSampling.sendTo(this.receiveGAdd(mgadd.last, mgadd));
	} else if (message instanceof MAdd) {
	    MAdd madd = (MAdd) message;
	    peerSampling.sendTo(this.receiveAdd(madd.last, madd));
	} else if (message instanceof MDel) {
	    MDel mdel = (MDel) message;
	    peerSampling.sendTo(this.receiveDel(mdel.last, mdel));
	}
    }



    /**
     * New functionality to make the AS index the content even if it
     * does not have an actual replica.
     **/
    public void showInterest(Node n) {
	this.counter += 1;
	// (TODO) change reserved value of NOTHING
	// high value but not enough to cause overflow if incremented…
	this.peerSampling.sendTo(this.receiveAdd(this.node, MAdd.FAKE(id, counter)));
    }
    
    @Override
    public MessagesToSend receiveAdd(Node q, MAdd a) {
	MessagesToSend messages = new MessagesToSend();

	if (versions.isStaleAdd(a) && q.equals(globalBest.last)) {
	    counter += 1;
	    MDel d = new MDel(id, counter);
	    messages.send(this.receiveDel(q, d));
	} else {
	    messages.send(super.receiveAdd(q, a));
	}
	
	if (best.isBetterThan(globalBest) || best.isNothing()) {
	    // global only exists if lower than local, hence erase global value when local is smaller
	    globalBest = MAdd.NOTHING();
	}
	
	return messages;
    }

    @Override
    public MessagesToSend receiveDel(Node q, MDel d) {
	MessagesToSend messages = new MessagesToSend();

	//	logMessage(id, d);

	if (d.shouldDel(best) || d.id.id == id) {
	    best = MAdd.NOTHING();
	    if (this.isGateway() && !globalBest.isNothing()) {
		globalBest = MAdd.NOTHING(); // serve to avoid infinite loops when going to receiveDel again
		counter += 1;
		MDel c = new MDel(id, counter);
		messages.send(receiveDel(q, c));
	    } else {
		globalBest = MAdd.NOTHING();
		for (Node n : peerSampling.neighbors()) {
		    if (!q.equals(n)) {
			MDel f = d.fwd(this.node);
			messages.send(n, f);
		    }
		}
	    }
	} else if (d.shouldDel(globalBest)) {
	    globalBest = MAdd.NOTHING();
	    for (Node n : peerSampling.neighbors()) {
		if (!q.equals(n)) {
		    MDel f = d.fwd(this.node);
		    messages.send(n, f);
		}
	    }
	} else { // echo α's
	    if (!best.isNothing()) {
		MAdd l = best.fwd(this.node, counter, Weights.get(id, q.getID()));
		messages.send(q, l);
	    }
	    if (!globalBest.isNothing()) {
		// global after local, so it is kept if local is kept
		MAdd g = new MGAdd(globalBest.fwd(this.node, counter, Weights.get(id, q.getID()))); 
		messages.send(q, g); // MGAdd 
	    }
	}

	versions.updateWithDel(d);
		    
	return messages;
    }

    public MessagesToSend justChangedMGAdd(Node q, MGAdd a) {
	MessagesToSend messages = new MessagesToSend();
	
	log.info("@{} A message just crossed AS from {}.", id, a.last.getID());
	logMessage(id, a);
	if (!bestOfTheBest().isNothing() && !bestOfTheBest().isLooping(q)) {
	    // compete with other AS (echo)
	    MGAdd f = new MGAdd(bestOfTheBest().fwd(this.node, counter, Weights.get(id, q.getID())));
	    messages.send(q, f);
	}
	
	return messages;
    }
    
    public MessagesToSend receiveGAdd(Node q, MGAdd a) {
	MessagesToSend messages = new MessagesToSend();
	
	log.info("@{} Just received xα from {}.", id, a.last.getID());
	if (versions.isStaleAdd(a) && q.equals(globalBest.last)) {
	    log.info("@{}: cancel from global {}.", id, a);
	    counter += 1;
	    MDel c = new MDel(id, counter);
	    c = c.fwd(this.node);
	    messages.send(this.receiveDel(q, c));
	} else if (!versions.isStaleAdd(a) && // no staleness
		   !a.isLooping(this.node) && // no loops allowed
		   !best.isNothing() && // there is a local interest
		   a.isBetterThan(best) && // compete with local
		   a.isBetterThan(globalBest)) { // compete with to global
	    // log.info("@{},  {} replaced by {}", this.node.getID(), globalBest, a);
	    globalBest = a.clone();

	    for (Node n : peerSampling.neighbors()) {
		MGAdd f = new MGAdd(a.fwd(this.node, counter, Weights.get(id, n.getID())));
		messages.send(n, f);
	    }
	}

	versions.updateWithAdd(a);
	
	return messages;
    }
    

    
    public MAdd bestOfTheBest() {
	return globalBest.isBetterThan(best) ? globalBest : best;
    }
    
    @Override 
    public long getBestPartition() {
        return bestOfTheBest().id.id;
    }

    @Override 
    public double getBestDistance() {
	return bestOfTheBest().isFake() ? Double.POSITIVE_INFINITY : bestOfTheBest().weight;
    }
    
    @Override
    public Object clone() {
        return new CrossASPartitioner();
    }

    public boolean isGateway() {
	for (Node neighbor : peerSampling.neighbors.keySet()) 
	    if (!CAutonomousSystems.fromSameAS(this.node, neighbor)) 
		return true; // ugly in "for loop" but w/e it works
	return false;
    }

    public void logMessage(long id, Object m) {
	if (this.node.getID() == id) {
	    log.info("@{} in asn-{}: local:  {}", id, CAutonomousSystems.nodeToAsn.get(id), best);
	    log.info("@{} in asn-{}: global: {}", id, CAutonomousSystems.nodeToAsn.get(id), globalBest);
	    if (m instanceof MAdd) {
		MAdd madd = (MAdd) m;
		log.info("@{}: {}", id, madd);
	    } else if (m instanceof MDel) {
		MDel mdel = (MDel) m;
		log.info("@{}: {}", id, mdel);
	    }
	}
    }
}
