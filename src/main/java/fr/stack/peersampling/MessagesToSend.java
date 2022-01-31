package fr.stack.peersampling;

import fr.stack.structures.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import peersim.core.Node;
import java.util.Iterator;
import java.lang.Iterable;



/**
 * Structure that stores the messages to send and to which neighbor, so
 * the class PeerSampling can send them. 
 */
public class MessagesToSend implements Iterable<Pair<Node, IMessage>>, Iterator<Pair<Node, IMessage>> {

    public HashMap<Node, ArrayList<IMessage>> neighborToMessages;

    public MessagesToSend () {
	this.neighborToMessages = new HashMap<>();
    }

    public void add(Node to, IMessage m) {
	if (!this.neighborToMessages.containsKey(to))
	    this.neighborToMessages.put(to, new ArrayList<IMessage>());
	
	this.neighborToMessages.get(to).add(m);
    }

    public void add(MessagesToSend others) {
	for (Pair<Node, IMessage> pair : others) {
	    this.add(pair.first, pair.second);
	}
    }

    public ArrayList<IMessage> get(Node n) {
	return neighborToMessages.get(n);
    }

    public void send(Node to, IMessage m) { // alias
	this.add(to, m);
    }

    public void send(MessagesToSend others) { // alias
	this.add(others);
    }

    public boolean hasNext() {
	return neighborToMessages.size() > 0;
    }

    public int size() {
	int sum = 0;
	for (Node to : this.neighborToMessages.keySet()) {
	    sum += this.neighborToMessages.get(to).size();
	}
	return sum;
    }

    public boolean isEmpty() {
	return !hasNext();
    }

    public Pair<Node, IMessage> next() {
	for (Node to : this.neighborToMessages.keySet()) {
	    IMessage message = this.neighborToMessages.get(to).remove(0);
	    if (this.neighborToMessages.get(to).size() == 0)
		this.neighborToMessages.remove(to);
	    return new Pair(to, message);
	}
	return null;
    }

    public Iterator<Pair<Node, IMessage>> iterator() {
	return this; // (TODO) clone
    }

    public MessagesToSend distinct () {
	for (Node to : this.neighborToMessages.keySet()) {
	    HashSet<IMessage> distinct = new HashSet<IMessage>(this.neighborToMessages.get(to));
	    this.neighborToMessages.put(to, new ArrayList(distinct));
	}
	return this;
    }
    
}
