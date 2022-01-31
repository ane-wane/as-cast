package fr.stack.controllers;

import peersim.core.Control;
import peersim.core.Network;
import peersim.core.CommonState;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Node;
import peersim.core.Linkable;



/**
 * Modifies linkable to put links in both directions: if there exists
 * a link A->B, then we add a link B->A.
 */
public class CBidirectionalLinks implements Control {

    private static final String PAR_PROT = "protocol";    
    private final int protocol;

    public CBidirectionalLinks (String prefix) {
        this.protocol = Configuration.getPid(prefix + "." + PAR_PROT);
    }

    public boolean execute () {
        if (CommonState.getTime() > 0)
            return false;
        
        // #A bidirectionnal links
        for (int i = 0; i < Network.size(); ++i) {
            Linkable l = (Linkable) Network.get(i).getProtocol(FastConfig.getLinkable(this.protocol));
            for (int j=0; j < l.degree(); ++j){
                Node neighbor = l.getNeighbor(j);
                Linkable neighborLinkable = (Linkable) neighbor.getProtocol(FastConfig.getLinkable(this.protocol));
                neighborLinkable.addNeighbor(Network.get(i));
            }
        }

        return false;
    }

}
