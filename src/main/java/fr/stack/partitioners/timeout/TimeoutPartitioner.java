package fr.stack.partitioners.timeout;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.edsim.EDProtocol;
import peersim.cdsim.CDProtocol;
import peersim.core.Linkable;
import peersim.transport.Transport;
import peersim.core.Node;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

import fr.stack.partitioners.IPartitioner;
import fr.stack.partitioners.IAdder;
import fr.stack.partitioners.IDeller;
import fr.stack.partitioners.MonitorMessages;
import fr.stack.structures.Identifier;
import fr.stack.structures.Versions;
import fr.stack.peersampling.Weights;



/**
 * A protocol based on timeout and cycles to remove stale control
 * information. A source advertises itself every cycles. Processes
 * receiving such notification update their data structure. If they
 * did not receive a notification after TIMEOUT time, they assume the
 * source has revoked its status. Pros: the principle is very easy to
 * comprehend, and it handles tricky operations such as dynamic
 * partitions and dynamic networks. Cons: the traffic is constant
 * whatever the operations of the protocol, TIMEOUT is difficult to
 * set properly for it depends on the network topology, relying on
 * physical time may impair consistency.
 */
public class TimeoutPartitioner implements EDProtocol, CDProtocol,
                                           IPartitioner, IAdder, IDeller {
    
    public static String PAR_PID = "pid";
    public static Integer PID;
    public Node node;

    public static String PAR_TIMEOUT = "timeout";
    public static Integer TIMEOUT;
    
    public int counter;
    public Versions versions;
    public TreeMap<Identifier, Double> bests;
    public TreeMap<Identifier, Integer> timeouts;

    public MonitorMessages monitor;
    

        
    public TimeoutPartitioner () {
        this.resetLocals();
    }
    
    public TimeoutPartitioner (String prefix) {
        PID = Configuration.getPid(prefix+"."+PAR_PID);
        TIMEOUT = Configuration.getInt(prefix+"."+PAR_TIMEOUT);
        this.resetLocals();
    }

    private void resetLocals () {
        this.bests = new TreeMap<>();
        this.counter = 0;
        this.versions = new Versions();
        this.bests = new TreeMap<>();
        this.timeouts = new TreeMap<>();
	this.monitor = new MonitorMessages();
    }
    

    
    public void nextCycle(Node node, int protocolID) {
        this.lazyLoadNode(node);

        // #A Advertise myself if I am a source
        if (this.counter % 2 == 1) {
            this.add();
        }
        
        // #B every cycle, remove unheard-of bests since delta cycles
        TreeSet<Identifier> toRemoves = new TreeSet<>();
	
        for (Identifier id : this.timeouts.keySet()) {
            if (this.timeouts.get(id) <= 1) {
                toRemoves.add(id);
            } else {
                this.timeouts.put(id, this.timeouts.get(id) - 1);
            }
        }
	
        for (Identifier toRemove : toRemoves) {
            this.timeouts.remove(toRemove);
            this.bests.remove(toRemove);
        }
    }
    
    public void processEvent(Node node, int protocolId, Object message) {
        this.lazyLoadNode(node);

	this.monitor.received(message);

        if (message instanceof MAdd) {
            this.receiveAdd((MAdd) message);
        }
    }

    public void add() {
        this.counter += 1;
        this.receiveAdd(new MAdd(this.node.getID(), this.counter, 0));
    }
    
    public void receiveAdd(MAdd m) {
	
	if (this.versions.isStale(m.id)) {
	    return;
	} else if (this.versions.isBrandNew(m.id)) {
	    // cleaning structures from useless data
            TreeSet<Identifier> toRemoves = new TreeSet<>();
            for (Identifier oldId : this.bests.keySet()) 
                if (oldId.id == m.id.id)
                    toRemoves.add(oldId);
            
            for (Identifier toRemove: toRemoves) {
                this.bests.remove(toRemove);
                this.timeouts.remove(toRemove);
            }
            
            if (this.getBestDistance() > m.weight) {
		this.versions.update(m.id);
		
                this.bests.put(m.id, m.weight);
                this.timeouts.put(m.id, TimeoutPartitioner.TIMEOUT);                
                this.broadcastAdd(m);
            }
	    
	    
	} else if (this.getBestDistance() > m.weight) { // current version
	    this.bests.put(m.id, m.weight);
	    this.timeouts.put(m.id, TIMEOUT);
	    this.broadcastAdd(m);
	}
	
    }
    
    public void broadcastAdd(MAdd m) {
        Linkable l = (Linkable) this.node.getProtocol(FastConfig.getLinkable(PID));
        Transport t = (Transport) this.node.getProtocol(FastConfig.getTransport(PID));
        for (int i = 0; i < l.degree(); ++i){
            Node neighbor  = l.getNeighbor(i);
            t.send(this.node, neighbor,
		   m.fwd(Weights.get(this.node.getID(), neighbor.getID())),
                   PID);
        }
    }
    
    public void del() {
        // Just delete ourself from the set of bests (should be a
        // simple speedup, for timeouts do the rest)
        Identifier ourId = new Identifier(this.node.getID(), this.counter);
        this.bests.remove(ourId);
        this.timeouts.remove(ourId);

        this.counter += 1;
        this.versions.update(new Identifier(this.node.getID(), this.counter));
    }


    
    @Override
    public Object clone() {
        return new TimeoutPartitioner();
    }

    public void lazyLoadNode(Node n) {
        if (Objects.isNull(this.node)) {
            this.node = n;
        }
    }

    public Identifier getBest() {
        // UGLY !!!!
        TreeSet<Long> sourceIds = new TreeSet<Long>();
        for (Identifier id : this.bests.keySet())
            sourceIds.add(id.id);
        
        TreeSet<Identifier> bestSources = new TreeSet<Identifier>();
        for (Long sourceId : sourceIds) {
            int higherCounter = 0;
            for (Identifier id : this.bests.keySet()) {
                if (id.id == sourceId && higherCounter < id.counter) {
                    higherCounter = id.counter;
                }
            }
            bestSources.add(new Identifier(sourceId, higherCounter));
        }
        
        double bestDistance = Double.MAX_VALUE;
        Identifier bestIdentifier = null;
        for (Identifier source : bestSources) {
            if (this.bests.get(source) < bestDistance) {
                bestDistance = this.bests.get(source);
                bestIdentifier = source;
            }
        }
        
        return bestIdentifier;
    }
    
    public double getBestDistance() {
        Identifier bestId = this.getBest();
        if (Objects.isNull(bestId)) {
            return Double.MAX_VALUE;
        } else {            
            return this.bests.get(bestId);
        }
    }

    public long getBestPartition() {
        Identifier bestId = this.getBest();
        if (Objects.isNull(bestId)) {
            return -1;
        } else {            
            return bestId.id;
        }
    }

    public MonitorMessages getMonitor() {
	return this.monitor;
    }
    
}
