package fr.stack.partitioners.ascast;

import peersim.core.Node;
import peersim.core.Protocol;



/**
 * A fake node to test our protocols without running peersim.
 */
public class FakeNode implements Node {

    public final long id;
    
    public FakeNode (long id) {
	this.id = id;
    }

    public long getID() {
	return id;
    }



    // The rest does nothing    
    public Object clone() {
	return null;
    }
    
    public int getIndex() {
	return -1;
    }
    
    public Protocol getProtocol(int i) {
	return null;
    }
    
    public int protocolSize() {
	return -1;
    }
    
    public void setIndex(int index) {	
    }

    public int getFailState() {
	return 0;
    }
    
    public boolean isUp() {
	return true;
    }
    
    public void setFailState(int failState) {
    }
    
}
