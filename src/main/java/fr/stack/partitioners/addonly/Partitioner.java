package fr.stack.partitioners.addonly;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.edsim.EDProtocol;
import peersim.cdsim.CDProtocol;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.transport.Transport;
import java.util.Objects;

import fr.stack.partitioners.IAdder;
import fr.stack.partitioners.IPartitioner;
import fr.stack.partitioners.MonitorMessages;



/**
 * A simple partitioner protocol that only allows a process to become
 * source. A process cannot revoke its self-appointed status of
 * source. Processes efficiently converge towards consistent
 * partitioning without the need of complex data structures or
 * communication patterns, using scoped broadcast. 
 */
public class Partitioner implements EDProtocol, CDProtocol,
                                    IPartitioner, IAdder {
    
    public static String PAR_PID = "pid";
    public static Integer PID;
    public Node node;
    
    public double best;
    public long idOfBest;

    public MonitorMessages monitor;
    

        
    public Partitioner () {
        this.best = Double.MAX_VALUE;
	this.monitor = new MonitorMessages();
    }
    
    public Partitioner (String prefix) {
        Partitioner.PID = Configuration.getPid(prefix + "." + Partitioner.PAR_PID);
        this.best = Double.MAX_VALUE;
	this.monitor = new MonitorMessages();
    }
    

    public void nextCycle(Node node, int protocolID) {
        this.lazyLoadNode(node);
    }
    
    public void processEvent(Node node, int protocolId, Object message) {
        this.lazyLoadNode(node);

	this.monitor.received(message);
	
        if (message instanceof MAdd) {
            this.receiveAdd((MAdd) message);
        }
    }
        
    public void add() {
        this.receiveAdd(new MAdd(this.node.getID(), 0));
    }

                         
    public void receiveAdd(MAdd m) {
        if (m.weight < this.best) {
            this.best = m.weight;
            this.idOfBest = m.id;
                        
            Linkable l = (Linkable) this.node.getProtocol(FastConfig.getLinkable(PID));
            Transport t = (Transport) this.node.getProtocol(FastConfig.getTransport(PID));
            for (int i = 0; i < l.degree(); ++i){
                Node neighbor  = l.getNeighbor(i);
                t.send(this.node, neighbor,
                       new MAdd(m.id, m.weight+t.getLatency(this.node, neighbor)),
                       PID);
            }
        }
    }


    
    @Override
    public Object clone() {
        return new Partitioner();
    }

    public void lazyLoadNode(Node n) {
        if (Objects.isNull(this.node)) {
            this.node = n;
        }
    }

    public double getBestDistance() {
        return this.best;
    }

    public long getBestPartition() {
        return this.idOfBest;
    }

    public MonitorMessages getMonitor() {
	return this.monitor;
    }
}
