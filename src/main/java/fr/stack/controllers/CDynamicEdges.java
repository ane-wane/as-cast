package fr.stack.controllers;

import peersim.core.Control;
import peersim.core.Network;
import peersim.core.CommonState;
import peersim.config.Configuration;
import peersim.util.ExtendedRandom;
import peersim.config.FastConfig;
import peersim.core.Node;
import java.util.Iterator;

import fr.stack.peersampling.IDynamicNetwork;
import fr.stack.peersampling.PeerSampling;



public class CDynamicEdges implements Control {
    
    private static final String PAR_PROT = "protocol";    
    private final int protocol;

    private static final String PAR_TYPE = "type";
    private final String type; // ADD or REM

    private static final String PAR_NUMBER = "number";
    private final int number;

    private static ExtendedRandom erng;
    

    
    public CDynamicEdges (String prefix) {
        this.protocol = Configuration.getPid(prefix +"."+ PAR_PROT);

        this.type = Configuration.getString(prefix +"."+ PAR_TYPE, "ADD"); // ADD or REM
        this.number = Configuration.getInt(prefix +"."+ PAR_NUMBER, 0);
        CDynamicEdges.erng = new ExtendedRandom(CommonState.r.getLastSeed());
    }
    
    public boolean execute () {
        int currentNumber = 0;
        // /!\ it could inifinite loop, for it tries to add things at
        // random without verifying it is possible in the first place.
        while (currentNumber < this.number) {
            int randomA = erng.nextInt(Network.size());
            Node randomNodeA = Network.get(randomA);
            
            if (this.type.equals("ADD")) {
                currentNumber = this.add(randomNodeA) ? currentNumber + 2 : currentNumber;
            } else if (this.type.equals("REM")) {
                currentNumber = this.rem(randomNodeA) ? currentNumber + 2 : currentNumber;
            } else {
                System.out.println("CDynamicEdges: Error in dynamicity types.");
                return true;
            }
        }

        return false;
    }
    
    private boolean add(Node nA) {
        IDynamicNetwork protocol = (IDynamicNetwork) nA.getProtocol(this.protocol);
        PeerSampling ps = protocol.getPeerSampling();
        int randomB = erng.nextInt(Network.size());
        Node nB = Network.get(randomB);
	if (nB.equals(nA))
	    return false; // quick patch so there are no self-loops
        // (TODO) call the corresponding handler in running protocol
        return ps.add(nB);
    }

    private boolean rem(Node nA) {
        IDynamicNetwork protocol = (IDynamicNetwork) nA.getProtocol(this.protocol);
        PeerSampling ps = protocol.getPeerSampling();
        
        int randomB = erng.nextInt(ps.neighbors.size());
        if (ps.neighbors.size() < 2) {
            System.out.println("CDynamicEdges: Node fully disconnected.");
            return true;
        }
        
        Iterator<Node> neighborIt = ps.neighbors().iterator();
        Node neighbor = neighborIt.next(); // every peer should have a neighbor
        for (int i = 0; i < randomB; ++i) {
            neighbor = neighborIt.next();
        }
        // (TODO) call the corresponding handler in running protocol
        return ps.rem(neighbor);
    }
    
}
