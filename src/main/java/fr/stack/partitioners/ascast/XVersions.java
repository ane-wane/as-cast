package fr.stack.partitioners.ascast;

import fr.stack.structures.Versions;
import fr.stack.structures.Identifier;



/**
 * A simple extension of the vector of versions to include easier way
 * to register messages specific to this partitioning protocol.
 */
public class XVersions extends Versions {
    
    public XVersions () {
	super();
    }

    public boolean isStaleAdd(MAdd a) {
	for (Identifier id : a.path) {
	    if (this.isStale(id))
		return true;
	}
	return false;
    }

    /**
     * Returns a Delete message corresponding to the stale message the
     * closer from the root. Null if there is no stale message.
     **/
    public MDel getHigherStale(MAdd a) {
	for (Identifier id : a.path) {
	    if (this.isStale(id))
		return new MDel(id.id, versions.get(id.id));
	}
	return null;
    }
    
    public void updateWithAdd(MAdd a) {
	for (Identifier id : a.path) {
	    this.update(id);
	}
    }

    public void updateWithDel(MDel d) {
	this.update(d.id);
    }
}
