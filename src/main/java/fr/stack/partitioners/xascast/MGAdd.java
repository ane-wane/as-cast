package fr.stack.partitioners.xascast;

import fr.stack.partitioners.ascast.MAdd;

import peersim.core.Node;
import java.util.Arrays;



class MGAdd extends MAdd {
    
    public MGAdd (MAdd a) {
	super (a.id, a.weight, a.path);
	last = a.last;
    }

    @Override
    public MAdd fwd(Node forwarder, int counter, double weight) {
	return new MGAdd(super.fwd(forwarder, counter, weight));
    }

    @Override
    public String toString() {
	return String.format("(X-ADD %s; %s; %s)",
			     id,
			     weight,
			     Arrays.toString(path.toArray()));
    }
    
}
