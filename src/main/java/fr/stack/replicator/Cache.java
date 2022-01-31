package fr.stack.replicator;

import fr.stack.partitioners.IAdder;
import fr.stack.partitioners.IDeller;
import fr.stack.partitioners.IPartitioner;
import fr.stack.controllers.CDynamicPartitions;
import fr.stack.controllers.CAutonomousSystems;
import fr.stack.structures.Pair;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Arrays;
import peersim.edsim.EDProtocol;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.config.Configuration;
import peersim.core.CommonState;



/**
 * This is a protocol that retrieves a content, and replicates it when
 * judged necessary.
 */
public class Cache implements EDProtocol, CDProtocol {

    public static String PAR_PID = "pid";
    public static Integer PID; // ID of the partitioner

    public static String PAR_SIZE = "size";
    public Integer size;

    // The one content that we monitor, rest of content are for deletes to occur
    public static String PAR_FAVORITE = "favorite";
    public String favorite;

    public static String PAR_REMOVEAFTER = "removeafter";
    public Integer removeAfter;

    public Node node; // our own identity
    public ArrayList<String> localContents;
    public ArrayList<Integer> ages;
    
    public static Logger log = LogManager.getLogger("Cache");
    


    public Cache () {
	this.size = 1;
	this.favorite = "";
	this.removeAfter = -1;
	this.localContents = new ArrayList<>();
	this.ages = new ArrayList<>();
    }
    
    public Cache (String prefix) {
	PID = Configuration.getPid(prefix + "." + PAR_PID);
        this.size = Configuration.getInt(prefix + "." + PAR_SIZE, 1);
	this.favorite = Configuration.getString(prefix + "." + PAR_FAVORITE);
	this.removeAfter = Configuration.getInt(prefix + "." + PAR_REMOVEAFTER, -1);
	this.localContents = new ArrayList<>();
	this.ages = new ArrayList<>();
    }

    public void nextCycle(Node node, int protocolID) {
	// NOTHING ? (TODO) or maybe clean local structure depending
	// on the type of caching mechanism.
        this.lazyLoadNode(node);

	for (int i = 0; i < this.ages.size(); ++i) {
	    this.ages.set(i, this.ages.get(i) - 1); // start old
	}

	while (this.ages.size() > 0 && this.ages.get(0) <= 0) {
	    this.delContent(this.localContents.get(0));
	}
	
	if (CommonState.getIntTime() > 20000) {
	    if (this.localContents.contains(this.favorite)) {
		System.out.println("AFZAOFAZFHF");
		Pair<Double, Double> position = CAutonomousSystems.nodeToPosition.get(this.node);
		System.out.println(position.first +  " ; ;; ; " + position.second);
	    }
	}
	
    }

    public void processEvent(Node receiver, int protocolId, Object message) {
	// NOTHING ? (TODO) maybe download from other replicators.
	this.lazyLoadNode(receiver); 
    }


    
    public Double get(String id) {
	if (this.localContents.contains(id)) {
	    int at = this.localContents.indexOf(id);
	    this.localContents.remove(at);
	    this.ages.remove(at);
	    this.localContents.add(id); // refresh position and age
	    this.ages.add(this.removeAfter);
	    return 0.;
	}
	
	// #A (TODO) perform shortest path to get closest ?
	// Double distance = -1.;
	// if (favorite.equals(id)) {
	// 	IPartitioner partitioner = (IPartitioner) this.node.getProtocol(PID);
	// 	distance = partitioner.getBestDistance();
	// 	// (TODO) do something with this
	// }
	// #B (TODO) if got it, add it to cache. maybe not instant
	this.addContent(id);
	// return distance;
	return 1.;
    }
    
	public void addContent(String id) {
	assert (!localContents.contains(id));
	// #1 add it to local content
	this.localContents.add(id); // refresh position and age
	this.ages.add(this.removeAfter);
	
	// #3 remove the oldest from cache when too many elements
	while (this.localContents.size() > this.size) {
	    this.delContent(this.localContents.get(0));
	}
	
	// #2 advertise it through xas-cast
	if (this.favorite.equals(id)) {
	    log.info("@{} creates a new replica…", this.node.getID());
	    String asn = CAutonomousSystems.nodeToAsn.get(this.node.getID());
	    log.info("@{} in asn {} with {} other nodes.", this.node.getID(), asn, CAutonomousSystems.asnToNodes.get(asn).size());
	    
	    CDynamicPartitions.addersSet.add(this.node);
	    IAdder partitioner = (IAdder) this.node.getProtocol(PID);
	    partitioner.add();
	}
    }
    
    public void delContent(String id) {
	assert (localContents.contains(id));
	// #1 remove it from local content
	int at = this.localContents.indexOf(id);
	this.localContents.remove(at);
	this.ages.remove(at);
	
	// #2 advertise its removal using xas-cast
	if (this.favorite.equals(id)) {
	    log.info("@{} removes its replica…", this.node.getID());
	    CDynamicPartitions.addersSet.remove(this.node);
	    IDeller partitioner = (IDeller) this.node.getProtocol(PID);
	    partitioner.del();
	}
    }
    


    public void lazyLoadNode(Node n) {
        if (Objects.isNull(this.node)) {
            this.node = n;
        }
    }

    @Override
    public Object clone() {
	Cache cloned = new Cache();
	cloned.size = this.size;
	cloned.favorite = this.favorite;
	cloned.removeAfter = this.removeAfter;
        return cloned;
    }
    
}
