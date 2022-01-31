package fr.stack.observers;

import peersim.core.Control;
import peersim.core.Network;
import peersim.config.Configuration;
import peersim.core.CommonState;
import java.util.Objects;

import fr.stack.peersampling.IDynamicNetwork;
import fr.stack.peersampling.PeerSampling;



public class ODegree implements Control {

    public static final String PAR_PROT = "protocol";
    public final int protocol;


    
    public ODegree (String prefix) {
        this.protocol = Configuration.getPid(prefix + "." + PAR_PROT);
    }

    public boolean execute () {
        int min = Integer.MAX_VALUE;
        int max = 0;
        int sum = 0;
        for (int i = 0; i < Network.size(); ++i) {
            IDynamicNetwork protocol = (IDynamicNetwork) Network.get(i).getProtocol(this.protocol);
            PeerSampling ps = protocol.getPeerSampling();
            if (!Objects.isNull(ps)) {
                sum += ps.neighbors.size();
                min = Math.min(min, ps.neighbors.size());
                max = Math.max(max, ps.neighbors.size());
            }
        }
        System.out.println(String.format("CDegree: %s ; min= %s ; avg= %s ; max= %s ; total= %s",
                                         CommonState.getTime(),
                                         min, (double) sum/Network.size(), max, sum));
        return false;
    }
    
}
