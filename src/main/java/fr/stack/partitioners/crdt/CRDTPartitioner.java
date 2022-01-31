package fr.stack.partitioners.crdt;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.edsim.EDProtocol;
import peersim.cdsim.CDProtocol;
import peersim.core.Linkable;
import peersim.transport.Transport;
import peersim.core.Node;
import java.util.Objects;

import fr.stack.partitioners.IPartitioner;
import fr.stack.partitioners.IAdder;
import fr.stack.partitioners.IDeller;
import fr.stack.partitioners.MonitorMessages;
import fr.stack.structures.Identifier;
import fr.stack.peersampling.Weights;



/**
 * Protocol that uses a set CRDT and full optimistic replication to
 * eventually converge towards consistent partitioning. While dynamic
 * consistent partitioning is easy to prove, it does not use scoped
 * broadcast. Every process needs to receive all broadcast
 * messages. The traffic generated is high, but it actually depends on
 * protocols operations.  When the system becomes quiescent, processes
 * eventually stop broadcasting and forwarding messages.
 */
public class CRDTPartitioner implements EDProtocol, CDProtocol,
                                        IPartitioner, IAdder, IDeller {
    
    public static String PAR_PID = "pid";
    public static Integer PID;
    public Node node;

    public int counter;
    public CRDTSet set;

    public MonitorMessages monitor;
        

        
    public CRDTPartitioner () {
        this.set = new CRDTSet();
        this.counter = 0;
	this.monitor = new MonitorMessages();
    }
    
    public CRDTPartitioner (String prefix) {
        CRDTPartitioner.PID = Configuration.getPid(prefix + "." + CRDTPartitioner.PAR_PID);
        this.set = new CRDTSet();
        this.counter = 0;
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
        } else if (message instanceof MDel) {
            this.receiveDel((MDel) message);
        }
    }

    public void del() {
        this.receiveDel(new MDel(this.node.getID(), this.counter));
    }

    public void receiveDel(MDel m) {
        Identifier id = new Identifier(m.id, m.counter);        
        if (!this.set.rem.contains(id)){
            this.set.rem(id);
            Linkable l = (Linkable) this.node.getProtocol(FastConfig.getLinkable(PID));
            Transport t = (Transport) this.node.getProtocol(FastConfig.getTransport(PID));
            for (int i = 0; i < l.degree(); ++i){
                Node neighbor  = l.getNeighbor(i);
                t.send(this.node, neighbor, m, PID);
            }            
        }
    }
    
    public void add() {
        this.counter += 1;
        this.receiveAdd(new MAdd(this.node.getID(), this.counter, 0));
    }

                         
    public void receiveAdd(MAdd m) {
        if ((!this.set.contains(m.id)) || this.set.improve(m.id, m.weight)) {
            
            this.set.add(m.id, m.weight);
            Linkable l = (Linkable) this.node.getProtocol(FastConfig.getLinkable(PID));
            Transport t = (Transport) this.node.getProtocol(FastConfig.getTransport(PID));
            for (int i = 0; i < l.degree(); ++i){
                Node neighbor  = l.getNeighbor(i);
                t.send(this.node, neighbor,
		       m.fwd(Weights.get(this.node.getID(), neighbor.getID())),
                       PID);
            }            
            
        }
    }
    

    
    @Override
    public Object clone() {
        return new CRDTPartitioner();
    }

    public void lazyLoadNode(Node n) {
        if (Objects.isNull(this.node)) {
            this.node = n;
        }
    }
    
    public double getBestDistance() {
        return this.set.best();
    }

    public long getBestPartition() {
        return this.set.bestId();
    }

    public MonitorMessages getMonitor(){
	return this.monitor;
    }
}
