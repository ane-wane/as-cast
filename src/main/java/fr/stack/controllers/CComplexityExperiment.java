package fr.stack.controllers;

import peersim.core.Control;
import peersim.core.Network;
import peersim.core.CommonState;
import peersim.config.Configuration;
import peersim.util.ExtendedRandom;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.HashMap;

import fr.stack.partitioners.IPartitioner;
import fr.stack.partitioners.IAdder;
import fr.stack.partitioners.IDeller;
import fr.stack.observers.OMiss;
import fr.stack.controllers.CDynamicPartitions;



public class CComplexityExperiment implements Control {

    private static final String PAR_PROT = "protocol";    
    private final int protocol;

    private static final String PAR_NUMBER = "number";
    private static int maxNumber;
    private static int number;

    private static ExtendedRandom erng;

    private static HashMap<Node, HashMap<Node, Double>> sourcesToProcesses = new HashMap<>();



    
    public CComplexityExperiment (String prefix) {
	this.protocol = Configuration.getPid(prefix + "." + PAR_PROT);
	this.number = Configuration.getInt(prefix + "."  + PAR_NUMBER, 0);
	this.maxNumber = number;
	CComplexityExperiment.erng = new ExtendedRandom(CommonState.r.getLastSeed());
		
	CDynamicPartitions.addersSet = new ArrayList<Node>();
    }

    public boolean execute () {

	// #1 check if we are in consistent partitioning
	boolean consistent = true;

	int j = 0;
	while (consistent && j < Network.size()) {
	    Node n = Network.get(j);
	    Double minDistance = Double.POSITIVE_INFINITY;
	    IPartitioner p = (IPartitioner) n.getProtocol(this.protocol);
	    
	    for (int i = 0; i < CDynamicPartitions.addersSet.size(); ++i) {
		Node s = CDynamicPartitions.addersSet.get(i);
		minDistance = Math.min(minDistance, sourcesToProcesses.get(s).get(n));
	    }
	    
	    consistent = p.getBestDistance() == minDistance;
	    ++j;
	}


	// #2 if we are, add another source and process distances
	if (consistent && number > 0) {
	    boolean added = false;

	    while (!added) {
		int random = erng.nextInt(Network.size());
		if (!CDynamicPartitions.addersSet.contains(Network.get(random))) {
		    CDynamicPartitions.addersSet.add(Network.get(random)); // local
		    added = true;
		}
	    }

	    number = number - 1;

	    Node newSource = CDynamicPartitions.addersSet.get(CDynamicPartitions.addersSet.size()-1);
	    
	    sourcesToProcesses.put(newSource, OMiss.weightedDijkstra(newSource));
		
	    System.out.println(String.format("CComplexity: %s; adding %s as source [%s/%s].",
					     CommonState.getTime(), newSource.getID(),
					     CDynamicPartitions.addersSet.size(), maxNumber));
	    
	    IAdder p = (IAdder) newSource.getProtocol(this.protocol);
	    p.add();
	    
	} else if (consistent && number == 0 && CDynamicPartitions.addersSet.size() > 0)  {
	    // #3 if max sources, start removing from the begining
	    Node toRemove = CDynamicPartitions.addersSet.get(0);
	    
	    sourcesToProcesses.remove(toRemove);
	    CDynamicPartitions.addersSet.remove(toRemove);
	    
	    System.out.println(String.format("CComplexity: %s; removing %s as source [%s/%s].",
					     CommonState.getTime(), toRemove.getID(),
					     CDynamicPartitions.addersSet.size(), maxNumber));
	    
	    IDeller p = (IDeller) toRemove.getProtocol(this.protocol);
	    p.del();
	}

	return false;
    }


}
