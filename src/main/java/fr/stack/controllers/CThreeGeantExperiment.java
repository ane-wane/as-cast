package fr.stack.controllers;

import peersim.core.Control;
import peersim.util.ExtendedRandom;
import peersim.core.CommonState;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;


import fr.stack.partitioners.IAdder;
import fr.stack.partitioners.IDeller;



/**
 * Experiment to show that xAS-cast enables dynamic adaptation of
 * indexing depending on whether or not the AS hosts a replica of
 * specific content. There are three interconnected GEANT ASes (an
 * european network).
 **/
public class CThreeGeantExperiment implements Control {

    private static final String PAR_PROT = "protocol";
    private final int protocol;

    private static ExtendedRandom erng;

    public Node s0 = null;
    public Node s1 = null;
    public Node s2 = null;
    public Node s3 = null;
    public Node s4 = null;

    

    
    public CThreeGeantExperiment(String prefix) {
	new CAutonomousSystems();
	erng = new ExtendedRandom(CommonState.r.getLastSeed());
	this.protocol = Configuration.getPid(prefix + "." + PAR_PROT);
    }


    public boolean execute () {

	int INTERVAL = 1000;
	
	if (CommonState.getIntTime() == INTERVAL) {
	    int rn = erng.nextInt(CAutonomousSystems.asnToNodes.get("AS-0").size());
	    s0 = (Node) CAutonomousSystems.asnToNodes.get("AS-0").toArray()[rn];
	    IAdder a = (IAdder) s0.getProtocol(protocol);
	    a.add();
	    CDynamicPartitions.addersSet.add(s0);
	}

	if (CommonState.getIntTime() == INTERVAL*2) {
	    int rn = erng.nextInt(CAutonomousSystems.asnToNodes.get("AS-1").size());
	    s1 = (Node) CAutonomousSystems.asnToNodes.get("AS-1").toArray()[rn];
	    IAdder a = (IAdder) s1.getProtocol(protocol);
	    a.add();
	    CDynamicPartitions.addersSet.add(s1);
	}

	if (CommonState.getIntTime() == INTERVAL*3) {
	    int rn = erng.nextInt(CAutonomousSystems.asnToNodes.get("AS-2").size());
	    s2 = (Node) CAutonomousSystems.asnToNodes.get("AS-2").toArray()[rn];
	    IAdder a = (IAdder) s2.getProtocol(protocol);
	    a.add();
	    CDynamicPartitions.addersSet.add(s2);
	    
	    IDeller d = (IDeller) s0.getProtocol(protocol);
	    d.del();
	    CDynamicPartitions.addersSet.remove(s0);
	}

	if (CommonState.getIntTime() == INTERVAL*4) {
	    int rn = erng.nextInt(CAutonomousSystems.asnToNodes.get("AS-0").size());
	    s3 = (Node) CAutonomousSystems.asnToNodes.get("AS-0").toArray()[rn];
	    IAdder a = (IAdder) s3.getProtocol(protocol);
	    a.add();
	    CDynamicPartitions.addersSet.add(s3);

	    IDeller d = (IDeller) s1.getProtocol(protocol);
	    d.del();
	    CDynamicPartitions.addersSet.remove(s1);
	}

	if (CommonState.getIntTime() == INTERVAL*5) {
	    int rn = erng.nextInt(CAutonomousSystems.asnToNodes.get("AS-1").size());
	    s4 = (Node) CAutonomousSystems.asnToNodes.get("AS-1").toArray()[rn];
	    IAdder a = (IAdder) s4.getProtocol(protocol);
	    a.add();
	    CDynamicPartitions.addersSet.add(s4);
	}

	if (CommonState.getIntTime() == INTERVAL*6) {
	    IDeller d = (IDeller) s3.getProtocol(protocol);
	    d.del();
	    CDynamicPartitions.addersSet.remove(s3);
	    d = (IDeller) s4.getProtocol(protocol);
	    d.del();
	    CDynamicPartitions.addersSet.remove(s4);
	    d = (IDeller) s2.getProtocol(protocol);
	    d.del();
	    CDynamicPartitions.addersSet.remove(s2);
	}

	
	return false;
    }
    
}
