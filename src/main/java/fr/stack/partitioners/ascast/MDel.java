package fr.stack.partitioners.ascast;

import fr.stack.structures.Identifier;
import fr.stack.peersampling.IMessage;

import peersim.core.Node;
import java.util.ArrayList;
import java.util.Objects;



public class MDel implements IMessage {

    public int linkID;
    public int linkCounter;

    public Node last;
    // identifier of ∂ that also determines the α to delete
    public final Identifier id;



    public MDel (long id, int counter) {	
	this.id = new Identifier(id, counter);
    }
    
    public MDel (Identifier id) {
	this.id = new Identifier(id);
    }
    
    public MDel fwd(Node forwarder) {
	MDel f = this.clone();
	f.last = forwarder;
	return f;
    }

    public MDel clone() {
	MDel clone = new MDel(this.id);
	clone.last = this.last;
	return clone;
    }
    
    public String toString() {
	return String.format("(DEL %s; %s)",
			     id,
			     Objects.isNull(last) ? "": last.getID());
    }

    @Override
    public int hashCode() {
	return Objects.hash(this.id.toString());
    }
    
    @Override
    public boolean equals(Object o) {
	if (!(o instanceof MDel))
	    return false;
	
	MDel d = (MDel) o;
	
	if (!this.id.equals(d.id))
	    return false;
	
	return true;
    }

    public Identifier target () {
	return new Identifier(id.id, id.counter - 1);
    }
    
    public boolean shouldDel (MAdd best) {
	for (Identifier idBest : best.path) {
	    if (idBest.id == id.id && idBest.counter < id.counter) {
		return true;
	    }
	}

	return false;
    }

    public boolean hasCommonAncestor(MAdd best) {
	return false;
    }

    public boolean hasCommonPrefix(MAdd best) {
	return false;
    }
    
    public boolean isUndo() {
	return false;
    }
    


    public int fromID() { return this.linkID; }
    public void setID(int id) { this.linkID = id; }
    public int getCounter() { return this.linkCounter; }
    public void setCounter(int counter) { this.linkCounter = counter; }
    
}
