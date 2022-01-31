package fr.stack.controllers;

import fr.stack.structures.Pair;
import fr.stack.peersampling.IDynamicNetwork;
import fr.stack.peersampling.PeerSampling;
import fr.stack.peersampling.Weights;
import fr.stack.transport.LatencyInferror;
import fr.stack.transport.LatencyTransport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.io.IOException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceGML;
import org.graphstream.algorithm.ConnectedComponents;



/**
 * Loads a GML file that contains AS information, localisation as
 * well, and stores it as static.
 */
public class CAutonomousSystems implements Control {

    private static final String PAR_PROT = "protocol";
    private int protocol;
    
    private static final String PAR_FILE = "file";
    private String file;

    public static HashMap<String, HashSet<peersim.core.Node>> asnToNodes;
    public static HashMap<Long, String> nodeToAsn;
    public static HashMap<String, HashSet<String>> asnToNeighbors;
    public static HashMap<peersim.core.Node, Pair<Double, Double>> nodeToPosition;
    
    private static int iNode;

    public static Logger log = LogManager.getLogger("CAutonomousSystems");


    
    public CAutonomousSystems () {
	if (Objects.isNull(asnToNodes)) { // lazy loading of statics
	    asnToNodes = new HashMap<>();
	    nodeToAsn = new HashMap<>();
	    asnToNeighbors = new HashMap<>();
	    nodeToPosition = new HashMap<>();
	    iNode = 0;
	}
    }
    
    public CAutonomousSystems (String prefix) {
	if (Objects.isNull(asnToNodes)) { // lazy loading of statics
	    asnToNodes = new HashMap<>();
	    nodeToAsn = new HashMap<>();
	    asnToNeighbors = new HashMap<>();
	    nodeToPosition = new HashMap<>();
	    iNode = 0;
	}
	
	this.protocol = Configuration.getPid(prefix + "." + PAR_PROT);
	this.file = Configuration.getString(prefix + "." + PAR_FILE);
    }

    public boolean execute() {
	loadFile(file, protocol);
	return false;
    }
    


    public static boolean fromSameAS(peersim.core.Node n1, peersim.core.Node n2) {
	return fromSameASWithID(n1.getID(), n2.getID());
    }

    public static boolean fromSameASWithID(long id1, long id2) {
	return nodeToAsn.get(id1).equals(nodeToAsn.get(id2));
    }


    
    public static void loadFile (String filename, int PID) {
	log.info("Loading file {}…", filename);

	LatencyInferror latencyInferror = new LatencyInferror(2); // min 2ms
	
	HashMap<peersim.core.Node, String> nodeToGMLId = new HashMap<>();
	HashMap<String, peersim.core.Node> gmlIdToNode = new HashMap<>();
	
	// #1 load graph from file
	Graph g = new DefaultGraph("g");
	FileSource fs = new FileSourceGML();

	fs.addSink(g);
	
	try {
	    fs.begin(filename);
	    while (fs.nextEvents()) { }
	    fs.end();	    
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    fs.removeSink(g);
	}
	
	ConnectedComponents cc = new ConnectedComponents();
	cc.init(g);
	log.info("Number of connected components in gml file: {}", cc.getConnectedComponentsCount());
	
	// #2 put the graph in the simulator
	// #A nodes
	Double latitude = 0.;
	Double longitude = 0.;
	Iterator<Node> itNode = g.nodes().iterator();
	int fixes = 0;
	while (itNode.hasNext()) {
	    Node n = itNode.next();
	    // (fill the missing latitude longitude with previous)
	    if (Objects.isNull(n.getAttribute("Latitude"))) { // both 
		n.setAttribute("Latitude", latitude);
		n.setAttribute("Longitude", longitude);
		++fixes;
	    }

	    // get asn 
	    if (!Objects.isNull(n.getAttribute("asn"))) {
		String asn = n.getAttribute("asn").toString();
		if (!asnToNodes.containsKey(asn)) {
		    asnToNodes.put(asn, new HashSet<peersim.core.Node>());
		    asnToNeighbors.put(asn, new HashSet<String>());
		}
		asnToNodes.get(asn).add(Network.get(iNode));
		nodeToAsn.put(Network.get(iNode).getID(), asn);
	    }
	    
	    nodeToPosition.put(Network.get(iNode),
			       new Pair<>(Double.valueOf(n.getAttribute("Longitude").toString()),
					  Double.valueOf(n.getAttribute("Latitude").toString())));
	    gmlIdToNode.put(n.getId(), Network.get(iNode));
	    nodeToGMLId.put(Network.get(iNode), n.getId());
	    ++iNode;
	}
	if (fixes > 0) {
	    log.warn("Fixed {} locations.", fixes);
	}
	
	// #B edges
	Iterator<Edge> itEdges = g.edges().iterator();
	int nbInterASEdge = 0;
	while (itEdges.hasNext()) {
	    Edge e = itEdges.next();
	    Node from = e.getNode0();
	    Node to = e.getNode1();
	    peersim.core.Node nA = gmlIdToNode.get(from.getId());
	    peersim.core.Node nB = gmlIdToNode.get(to.getId());
	    
	    IDynamicNetwork protocol = (IDynamicNetwork) nA.getProtocol(PID);
	    PeerSampling ps = protocol.getPeerSampling();
	    ps.add(nB);

	    // #C getting the latency of each link, inferring it if necessary
	    Object weight = e.getAttribute("weight");
	    double latency = 0;
	    if (Objects.isNull(weight)) {
		// infer it, from distance
		double latA = Double.parseDouble(e.getNode0().getAttribute("Latitude").toString());
		double longA = Double.parseDouble(e.getNode0().getAttribute("Longitude").toString());
		double latB = Double.parseDouble(e.getNode1().getAttribute("Latitude").toString());
		double longB = Double.parseDouble(e.getNode1().getAttribute("Longitude").toString());
		latency = latencyInferror.infer(latA, latB, longA, longB);
	    } else {
		latency = Double.parseDouble(e.getAttribute("weight").toString());
	    }

	    LatencyTransport.lags[(int) nA.getID()][(int) nB.getID()] = (int) latency;
	    LatencyTransport.lags[(int) nB.getID()][(int) nA.getID()] = (int) latency;	    
	    Weights.weights[(int) nA.getID()][(int) nB.getID()] = (int) latency;
	    Weights.weights[(int) nB.getID()][(int) nA.getID()] = (int) latency;
	    
	    if (!nodeToAsn.get(nA.getID()).equals(nodeToAsn.get(nB.getID()))) {
		++nbInterASEdge;
		String asnA = nodeToAsn.get(nA.getID());
		String asnB = nodeToAsn.get(nB.getID());
		asnToNeighbors.get(asnA).add(asnB);
		asnToNeighbors.get(asnB).add(asnA);
	    }
	}
	
	log.info("Loaded a graph of {} nodes and {} edges.", g.nodes().count(), g.edges().count());
	log.info("Nodes are distributed among {} ASes.", asnToNodes.size());
	log.info("Amongst all edges are {} inter-ASes edges.", nbInterASEdge);
    }
    
}
