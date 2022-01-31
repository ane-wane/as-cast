package fr.stack.partitioners.ascast;

import fr.stack.structures.Identifier;
import fr.stack.peersampling.IMessage;

import peersim.core.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;



public class MAdd implements IMessage {

    public int linkID;
    public int linkCounter;
        
    public final Identifier id;
    public double weight;
    public ArrayList<Identifier> path = new ArrayList<>();
    public Node last;



    public MAdd(long id, int counter, double weight) {
	this.id = new Identifier(id, counter);
	this.weight = weight;
    }
    
    public MAdd(Identifier id, double weight) {
	this.id = new Identifier(id);
	this.weight = weight;
    }
    
    public MAdd(Identifier id, double weight, ArrayList<Identifier> path) {
	this.id = new Identifier(id);
	this.weight = weight;
	for (Identifier peerId : path) {
	    this.path.add(new Identifier(peerId));
	}
    }
    
    public MAdd(long id, int counter, double weight, ArrayList<Identifier> path) {
	this.id = new Identifier(id, counter);
	this.weight = weight;
	for (Identifier peerId : path) {
	    this.path.add(new Identifier(peerId));
	}
    }

    
    public MAdd fwd(Node forwarder, int counter, double weight) {
	MAdd f = this.clone();
	f.weight += weight;
	f.path.add(new Identifier(forwarder.getID(), counter));
	f.last = forwarder;
	return f;
    }

    public MAdd clone() {
	MAdd clone = new MAdd(this.id, this.weight, this.path);
	clone.last = this.last;
	return clone;
    }
    
    public boolean isLooping(Node receiver) {
	for (Identifier idPath : this.path) {
	    if (idPath.id == receiver.getID())
		return true;
	}

	return false;
    }
    
    public String toString() {
	return String.format("(ADD %s; %s; %s)",
			     id,
			     weight,
			     Arrays.toString(path.toArray()));
    }

    public int compareTo(MAdd o) {
	if (this.weight > o.weight) {
	    return -1;
	} else if (this.weight < o.weight) {
	    return 1;
	} else {
	    if (this.id.id < o.id.id) {
		return -1;
	    } else if (this.id.id > o.id.id) {
		return 1;
	    }
	}
	return 0;
    }

    public boolean isBetterThan(MAdd o) {
	return this.compareTo(o) > 0;
    }

    public boolean isVersion(Identifier id) {
	return this.id.equals(id);
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof MAdd)) 
	    return false;
	
	MAdd a = (MAdd) o;
	
	if (!this.id.equals(a.id))
	    return false;

	if (this.weight != a.weight)
	    return false;
	
	if (!this.path.equals(a.path))
	    return false;
	
	return true;
    }

    @Override
    public int hashCode() {
	return Objects.hash(toString());
    }

    public boolean isSameObjectButLEQVersionThan(Identifier id) {
	return this.id.id == id.id && this.id.counter <= id.counter;
    }

    public boolean isNothing() {
	return this.id.equals(Identifier.NOTHING());
    }
    
    public static MAdd NOTHING() {  
	return new MAdd(Identifier.NOTHING(), Double.POSITIVE_INFINITY);
    }

    public boolean isSource() {
	return weight == 0.;
    }

    public static MAdd FAKE(long id, int c) {
	return new MAdd(id, c, Double.MAX_VALUE / 2); // /2 because we need to accumulate weight still
    }

    public boolean isFake() {
	return this.weight >= (Double.MAX_VALUE / 2);
    }



    public int fromID() { return this.linkID; }
    public void setID(int id) { this.linkID = id; }
    public int getCounter() { return this.linkCounter; }
    public void setCounter(int counter) { this.linkCounter = counter; }

}
