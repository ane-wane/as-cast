package fr.stack.controllers;

import peersim.core.Control;
import peersim.core.Network;
import peersim.core.CommonState;
import peersim.config.Configuration;
import peersim.util.ExtendedRandom;
import peersim.config.FastConfig;
import peersim.core.Node;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
    
import fr.stack.partitioners.IAdder;
import fr.stack.partitioners.IDeller;



public class CDynamicPartitions implements Control {

    private static final String PAR_PROT = "protocol";    
    private final int protocol;

    private static final String PAR_TYPE = "type";
    private final String type; // ADD DEL

    private static final String PAR_NUMBER = "number";
    private final int number;
    
    private static ExtendedRandom erng;

    public static ArrayList<Node> addersSet;
    

    
    public CDynamicPartitions (String prefix) {        
        this.protocol = Configuration.getPid(prefix + "." + PAR_PROT);
        this.type = Configuration.getString(prefix + "." + PAR_TYPE, "ADD");
        this.number = Configuration.getInt(prefix + "."  + PAR_NUMBER, 0);
        
        CDynamicPartitions.erng = new ExtendedRandom(CommonState.r.getLastSeed());
        if (Objects.isNull(CDynamicPartitions.addersSet))
            CDynamicPartitions.addersSet = new ArrayList<>();
    }
    
    public boolean execute () {
        if (this.type.equals("ADD")) {            
            // #1 add new elements to the set of adders
            HashSet<Node> addAddersSet = new HashSet<>();
            while (addAddersSet.size() < this.number) {
                int random = erng.nextInt(Network.size());
                if (!CDynamicPartitions.addersSet.contains(Network.get(random))) {
                    addAddersSet.add(Network.get(random)); // local
                }
            }
            for (Node adder : addAddersSet) {
                IAdder p = (IAdder) adder.getProtocol(this.protocol);
                CDynamicPartitions.addersSet.add(adder); // global
                p.add();
            }
            System.out.println(String.format("CDynamicPartitions: adders %s",
                                             Arrays.toString(addAddersSet.toArray())));
        } else if (this.type.equals("DEL")) {
            // #2 remove partitions starting from adders
            HashSet<Node> dellersSet = new HashSet<>();
            Iterator<Node> iterAdders = CDynamicPartitions.addersSet.iterator();
            for (int i = 0; i < Math.min(CDynamicPartitions.addersSet.size(), this.number); ++i) {
                Node deller = iterAdders.next();
                dellersSet.add(deller);
                IDeller p = (IDeller) deller.getProtocol(this.protocol);
                p.del();
            }
            for (Node deller : dellersSet) {
                CDynamicPartitions.addersSet.remove(deller);
            }
            
            System.out.println(String.format("CDynamicPartitions: dellers %s",
                                             Arrays.toString(dellersSet.toArray())));
        }
        
        return false;
    }
    
}
