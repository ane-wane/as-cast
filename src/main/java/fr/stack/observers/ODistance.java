package fr.stack.observers;

import peersim.core.Control;
import peersim.core.Network;
import peersim.config.Configuration;
import peersim.core.CommonState;

import fr.stack.partitioners.IPartitioner;



public class ODistance implements Control {
    
    private static final String PAR_PROT = "protocol";
    private final int protocol;



    public ODistance (String prefix) {
        this.protocol = Configuration.getPid(prefix + "." + ODistance.PAR_PROT);
    }

    public boolean execute () {
        double sum = 0;
        double max = 0;
        for (int i = 0; i < Network.size(); i++) {
            IPartitioner p = (IPartitioner) Network.get(i).getProtocol(this.protocol);
            sum += p.getBestDistance();
            max = Math.max(p.getBestDistance(), max);
        }

        System.out.println(String.format("ODistance: %s ; max= %s ; avg= %s",
                                         CommonState.getTime(),
                                         max,
                                         sum / Network.size()));
        return false;
    }
    

}
