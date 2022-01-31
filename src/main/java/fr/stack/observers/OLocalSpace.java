package fr.stack.observers;

import peersim.core.Control;
import peersim.core.Network;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Locale;

import fr.stack.partitioners.xascast.CrossASPartitioner;
import fr.stack.partitioners.ascast.ASPartitioner;



/** Monitor the space taken by the elements of xAS-cast. **/
public class OLocalSpace implements Control {

    private static final String PAR_PROT = "protocol";
    private final int protocol;



    public OLocalSpace (String prefix) {
	Locale.setDefault(new Locale("en", "US")); // to use "." in floats
        this.protocol = Configuration.getPid(prefix + "." + PAR_PROT);
    }
    
    public boolean execute () {
	long sumVectorSize = 0;

	double sumWeights = 0.;
	long sumLocalPath = 0;
	long sumLocal = 0;

	long sumGlobal = 0;
	long sumGlobalPath = 0;

	ArrayList<ASPartitioner> smallests = new ArrayList<ASPartitioner>();
	int minPath = Integer.MAX_VALUE;
	
        for (int i = 0; i < Network.size(); i++) {
            ASPartitioner p = (ASPartitioner) Network.get(i).getProtocol(this.protocol);
	    sumLocal += p.best.isNothing() ? 0 : 1;
	    sumLocalPath += p.best.path.size();
	    sumVectorSize += p.versions.versions.size();
	    sumWeights += p.best.isNothing() ? 0 : p.best.weight;

	    if (!p.best.isNothing() && p.best.path.size() < minPath) {
		smallests = new ArrayList<ASPartitioner>();
		minPath = p.best.path.size();
	    }
	    
	    if (!p.best.isNothing() && p.best.path.size() == minPath) {
		smallests.add(p);
	    }
	    
	    if (p instanceof CrossASPartitioner) {
		CrossASPartitioner q = (CrossASPartitioner) p;
		sumGlobal += q.globalBest.isNothing() ? 0 : 1;
		sumGlobalPath += q.globalBest.path.size();
	    }
        }
	
	String report = String.format(
				      "OLocalSpace: %s ; v= %.2f ; l= %.2f%% ; g= %.2f%% ; lP= %.2f ; gP= %.2f; w= %.2f",
				      CommonState.getTime(),
				      (double)sumVectorSize/Network.size(),
				      (double)sumLocal/Network.size()*100,
				      (double)sumGlobal/Network.size()*100,
				      sumLocal==0 ? 0. : (double)sumLocalPath/sumLocal,
				      (double)sumGlobalPath/Network.size(),
				      sumLocal==0 ? 0. : sumWeights/(double)sumLocal);
	System.out.println(report);

	// if (CommonState.getTime() > 9000) {
	//     for (ASPartitioner p : smallests) {
	// 	String path = "";
	// 	for (Node n : p.best.path) 
	// 	    path += "; " + n.getID();
		
	// 	System.out.println("@@@@@@ " + p.node.getID() +  " @@@@@@ path: " + path );
	//     }
	// }

	
        return false;
    }

}
