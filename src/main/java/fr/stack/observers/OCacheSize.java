package fr.stack.observers;

import fr.stack.replicator.Cache;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.CommonState;
import peersim.core.Control;



public class OCacheSize implements Control {

    public static final String PAR_PID = "pid";
    public int pid;


    
    public OCacheSize (String prefix) {
	this.pid = Configuration.getPid(prefix + "." + PAR_PID);
    }
    
    public boolean execute () {
	double sumCacheSize = 0;
	int sumSupZero = 0;
	int sumFavorite = 0;
	int max = 0;
	for (int i = 0; i < Network.size(); ++i) {
	    Node n = Network.get(i);
	    Cache c = (Cache) n.getProtocol(pid);

	    sumCacheSize += c.localContents.size();
	    sumFavorite += c.localContents.contains(c.favorite) ? 1 : 0;
	    sumSupZero += c.localContents.size() > 0 ? 1 : 0;
	    max = Math.max(max, c.localContents.size());
	}

	System.out.println(String.format("OCacheSize: %s; avg= %.2f | max= %s | nb= %s | fav= %s",
					 CommonState.getIntTime(),
					 sumSupZero > 0 ? sumCacheSize/sumSupZero : 0.,
					 max,
					 sumSupZero,
					 sumFavorite));
	
	return false;
    }
}
