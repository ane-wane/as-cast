package fr.stack.observers;

import peersim.core.Control;
import peersim.core.Network;
import peersim.config.Configuration;
import peersim.core.CommonState;
import java.util.Locale;

import fr.stack.partitioners.IPartitioner;
import fr.stack.partitioners.IAdder;
import fr.stack.partitioners.IDeller;



public class OTraffic implements Control {

    private static final String PAR_PROT = "protocol";
    private final int protocol;



    public OTraffic (String prefix) {
	Locale.setDefault(new Locale("en", "US")); // to use "." in floats
        this.protocol = Configuration.getPid(prefix + "." + OTraffic.PAR_PROT);
    }
    
    public boolean execute () {
        long sum = 0;
        long sumAdd = 0;
        long sumDel = 0;
	long sumUndo = 0;
	long sumXAdd = 0;
	
        for (int i = 0; i < Network.size(); i++) {
            IPartitioner p = (IPartitioner) Network.get(i).getProtocol(this.protocol);	    
	    if (p.getMonitor().typeToNbMessages.containsKey("MAdd"))
		sumAdd += p.getMonitor().typeToNbMessages.get("MAdd");
	    if (p.getMonitor().typeToNbMessages.containsKey("MDel")) {
		sumDel += p.getMonitor().typeToNbMessages.get("MDel");
	    }
	    if (p.getMonitor().typeToNbMessages.containsKey("MUndo")) {
		sumUndo += p.getMonitor().typeToNbMessages.get("MUndo");
	    }
	    if (p.getMonitor().typeToNbMessages.containsKey("MGAdd"))
		sumXAdd += p.getMonitor().typeToNbMessages.get("MGAdd");
        }
	
	String report = String.format("OTraffic: %s ; α= %.4f ; δ= %.4f ; Δ= %.4f ; xα= %.4f",  // ; xδ= %.2f",
				      CommonState.getTime(),
				      (double)sumAdd/Network.size(),
				      (double)sumDel/Network.size(),
				      (double)sumUndo/Network.size(),
				      (double) sumXAdd / Network.size());
				      //				      (double)sumXDel/Network.size());
	
	System.out.println(report);
	
        return false;
    }

}
