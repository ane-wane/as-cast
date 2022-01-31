package fr.stack.controllers;

import peersim.core.Control;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.util.ExtendedRandom;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceGML;
import java.io.IOException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Arrays;

import fr.stack.structures.Pair;
import fr.stack.peersampling.IDynamicNetwork;
import fr.stack.peersampling.PeerSampling;
import fr.stack.transport.LatencyTransport;
import fr.stack.peersampling.Weights;
import fr.stack.partitioners.IAdder;
import fr.stack.partitioners.IDeller;



/**
 * Load a number of .gml files and link them with communication links.
 * Then, at specific times, it creates or deletes a partition in one
 * of these clusters.
 */
public class CTopologyExperiment implements Control {

    private static final String PAR_PROT = "protocol";    
    private final int protocol;

    private static final String PAR_FILE = "file";
    private final String file;

    // number of time we load the file
    private static final String PAR_N = "n";
    private final int n;

    // number of links to add between clusters
    private static final String PAR_LINKS = "links"; 
    private final int links;

    // latency of intercluster links
    private static final String PAR_LATENCY = "latency";
    private final int latency;

    // number of adders 
    private static final String PAR_ADDERS = "adders";
    private final int adders;

    private static int iNode = 0;
    private boolean isInit = false;
    
    private static ExtendedRandom erng;

    private static ArrayList<ArrayList<peersim.core.Node>> clusters;
    private static ArrayList<Pair<peersim.core.Node, peersim.core.Node>> interClusterEdges;
    

   
    public CTopologyExperiment(String prefix) {
	this.protocol = Configuration.getPid(prefix + "." + PAR_PROT);
	this.file = Configuration.getString(prefix + "." + PAR_FILE);
	this.n = Configuration.getInt(prefix + "." + PAR_N);
	this.links = Configuration.getInt(prefix + "." + PAR_LINKS);
	this.latency = Configuration.getInt(prefix + "." + PAR_LATENCY);
	this.adders = Configuration.getInt(prefix + "." + PAR_ADDERS);
	
	CTopologyExperiment.erng = new ExtendedRandom(CommonState.r.getLastSeed());

	if (Objects.isNull(CTopologyExperiment.clusters)) {
	    CTopologyExperiment.clusters = new ArrayList<>();
	}
	if (Objects.isNull(CTopologyExperiment.interClusterEdges)){
	    CTopologyExperiment.interClusterEdges = new ArrayList<>();
	}

	new CAutonomousSystems();
    }

    public boolean execute () {
	
	if (!isInit) {
	    isInit = true;
	    
	    // #A load N independant clusters
	    for (int i = 0; i < this.n; ++i) {
		this.loadFile(i);
	    }
	    
	    // #B chain clusters with intercluster links
	    for (int i = 0; i < clusters.size()-1; ++i) {
		for (int j = 0; j < links; ++j) {
		    int from = erng.nextInt(clusters.get(i).size());
		    int to = erng.nextInt(clusters.get(i+1).size());
		    peersim.core.Node nA = clusters.get(i).get(from);
		    peersim.core.Node nB = clusters.get(i+1).get(to);
		    interClusterEdges.add(new Pair(nA, nB));

		    IDynamicNetwork protocol = (IDynamicNetwork) nA.getProtocol(this.protocol);
		    PeerSampling ps = protocol.getPeerSampling();
		    ps.add(nB);
		}
	    }	    

	    for (Pair<peersim.core.Node, peersim.core.Node> edge : interClusterEdges) {
		peersim.core.Node nA = edge.first;
		peersim.core.Node nB = edge.second;	
		LatencyTransport.lags[(int) nA.getID()][(int) nB.getID()] = latency;
		LatencyTransport.lags[(int) nB.getID()][(int) nA.getID()] = latency;
		Weights.weights[(int) nA.getID()][(int) nB.getID()] = latency;
		Weights.weights[(int) nB.getID()][(int) nA.getID()] = latency;
	    }

	    if (iNode < Network.size()) {
		System.out.println(String.format("/!\\ The number of nodes loaded is lower (%s) than expected (%s)...",
						 iNode, Network.size()));
	    } else if (iNode > Network.size()) {
		System.out.println(String.format("/!\\ The number of nodes loaded is higher (%s) than expected (%s)...",
						 iNode, Network.size()));
	    }

	}

	// #C @50 add partition(s)
	if (CommonState.getIntTime() == 50) {
	    int nbAddersBefore = CDynamicPartitions.addersSet.size();
	    int i = 0;
	    while (CDynamicPartitions.addersSet.size() < nbAddersBefore + adders) {
		int addId = erng.nextInt(clusters.get(i%clusters.size()).size());
		peersim.core.Node addNode = clusters.get(i%clusters.size()).get(addId);
		if (!CDynamicPartitions.addersSet.contains(addNode)) {
		    CDynamicPartitions.addersSet.add(addNode);
		    System.out.println(String.format("Add new partition @%s.", addNode.getID()));
		    IAdder pAdd = (IAdder) addNode.getProtocol(this.protocol);
		    pAdd.add();
		    ++i;
		}
	    }
	}

	// #D remove interlinks
	if (CommonState.getIntTime() == 850) {
	    for (int i = 0; i < interClusterEdges.size(); ++i) {
		Pair<peersim.core.Node, peersim.core.Node> edgeToDelete = CTopologyExperiment.interClusterEdges.get(i);
		IDynamicNetwork protocol = (IDynamicNetwork) edgeToDelete.first.getProtocol(this.protocol);
		PeerSampling ps = protocol.getPeerSampling();
		ps.rem(edgeToDelete.second);
	    }
	}

	// #E add interlinks
	if (CommonState.getIntTime() == 1700) {
	    for (int i = 0; i < interClusterEdges.size(); ++i) {
		Pair<peersim.core.Node, peersim.core.Node> edgeToAdd = CTopologyExperiment.interClusterEdges.get(i);
		IDynamicNetwork protocol = (IDynamicNetwork) edgeToAdd.first.getProtocol(this.protocol);
		PeerSampling ps = protocol.getPeerSampling();
		ps.add(edgeToAdd.second);
	    }
	}

	
	return false;
    }


    
    public void loadFile (int k) {
	HashMap<peersim.core.Node, String> nodeToGMLId = new HashMap<>();
	HashMap<String, peersim.core.Node> gmlIdToNode = new HashMap<>();
	
	// #1 load graph from file
	Graph g = new DefaultGraph("g");
	FileSource fs = new FileSourceGML();

	fs.addSink(g);	
	
	try {
	    fs.begin(this.file);
	    while (fs.nextEvents()) { }
	    fs.end();	    
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    fs.removeSink(g);
	}
	
	// #2 put the graph in the simulator
	// #A nodes
	Iterator<Node> itNode = g.nodes().iterator();

	String asn = "AS-" + k;
	CAutonomousSystems.asnToNodes.put(asn, new HashSet());
	
	while (itNode.hasNext()) {
	    Node n = itNode.next();
	    gmlIdToNode.put(n.getId(), Network.get(iNode));
	    nodeToGMLId.put(Network.get(iNode), n.getId());
	    CAutonomousSystems.asnToNodes.get(asn).add(Network.get(iNode));
	    CAutonomousSystems.nodeToAsn.put(Network.get(iNode).getID(), asn);
	    ++iNode;
	}
	
	// #B edges
	Iterator<Edge> itEdges = g.edges().iterator();
	while (itEdges.hasNext()) {
	    Edge e = itEdges.next();
	    Node from = e.getNode0();
	    Node to = e.getNode1();
	    peersim.core.Node nA = gmlIdToNode.get(from.getId());
	    peersim.core.Node nB = gmlIdToNode.get(to.getId());
	    
	    IDynamicNetwork protocol = (IDynamicNetwork) nA.getProtocol(this.protocol);
	    PeerSampling ps = protocol.getPeerSampling();
	    ps.add(nB);

	    double latency = Double.parseDouble(e.getAttribute("weight").toString());
	    LatencyTransport.lags[(int) nA.getID()][(int) nB.getID()] = (int) latency;
	    LatencyTransport.lags[(int) nB.getID()][(int) nA.getID()] = (int) latency;	    
	    Weights.weights[(int) nA.getID()][(int) nB.getID()] = (int) latency;
	    Weights.weights[(int) nB.getID()][(int) nA.getID()] = (int) latency;
	}


	// System.out.println(Arrays.toString( nodeToGMLId.keySet().toArray()));
	
	CTopologyExperiment.clusters.add(new ArrayList<>(nodeToGMLId.keySet()));
    }
}
