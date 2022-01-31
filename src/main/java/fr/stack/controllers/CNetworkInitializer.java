package fr.stack.controllers;

import peersim.core.Control;
import peersim.core.Network;
import peersim.core.CommonState;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Node;
import peersim.core.Linkable;

import fr.stack.peersampling.IDynamicNetwork;
import fr.stack.peersampling.Weights;



/**
 * Starts from the linkable of protocols to initialize peer sampling
 * views. Links are bidirectional.
 */
public class CNetworkInitializer implements Control {

    public static final String PAR_PROT = "protocol";
    public final int protocol;

    public static final String PAR_INITLINK = "initlink";
    public final int initlink;
    
    public static final String PAR_WEIGHT_FROM = "weight_from";
    public final int weightFrom;
    public static final String PAR_WEIGHT_TO = "weight_to";
    public final int weightTo;



    public CNetworkInitializer(String prefix) {
        this.protocol = Configuration.getPid(prefix + "." + PAR_PROT);
        this.initlink = Configuration.getPid(prefix + "." + PAR_INITLINK);
        
        
        Weights w = new Weights(Network.size()); // UGLY but w/e
        this.weightFrom = Configuration.getInt(prefix + "." + PAR_WEIGHT_FROM, 1);
        this.weightTo = Configuration.getInt(prefix + "." + PAR_WEIGHT_TO, 2); // [from; to[
        Weights.uniformRandomBetween(this.weightFrom, this.weightTo);
    }
    
    public boolean execute () {
        for (int i = 0; i < Network.size(); ++i ) {
            IDynamicNetwork n = (IDynamicNetwork) Network.get(i).getProtocol(this.protocol);
            Linkable l = (Linkable) Network.get(i).getProtocol(this.initlink);
            for (int j = 0; j < l.degree(); ++j) {
                Node m = l.getNeighbor(j);
                n.getPeerSampling().add(m); // bidirectionnal n->m and m->n
            }
        }
        return false;
    }
    
}
