package fr.stack.controllers;

import fr.stack.replicator.Cache;
import fr.stack.structures.Pair;
import fr.stack.transport.LatencyInferror;
import fr.stack.controllers.CAutonomousSystems;

import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import peersim.core.Network;
import peersim.core.Control;
import peersim.core.Node;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.util.ExtendedRandom;
import peersim.core.CommonState;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceGML;



public class CWorkload implements Control {
    
    private static final String PAR_PROT = "protocol";
    private final int protocol;
    
    private static final String PAR_FILE = "file";
    private final String file;

    // start the workload at given from (kind of an offset)
    private static final String PAR_FROM = "from";
    private Integer from;
    private static final String PAR_LOOP = "loop";
    private final Integer loop;
    
    private static final String PAR_CONTENT = "content";
    private final String content;

    private static final String PAR_CONTENTS = "contents"; // number of contents
    private Integer contents = 1;
    
    private boolean isInit = false;
    private ArrayList<org.graphstream.graph.Node> events;

    private static ExtendedRandom erng;
    public static Logger log = LogManager.getLogger("CWorkload");
    
    public CWorkload(String prefix) {
	this.protocol = Configuration.getPid(prefix + "." + PAR_PROT);
	this.file = Configuration.getString(prefix + "." + PAR_FILE, "");
	this.content = Configuration.getString(prefix + "." + PAR_CONTENT, "");
	this.contents = Configuration.getInt(prefix + "." + PAR_CONTENTS, 1);
	this.from = Configuration.getInt(prefix + "." + PAR_FROM);
	this.loop = Configuration.getInt(prefix + "." + PAR_LOOP, -1);
	CWorkload.erng = new ExtendedRandom(CommonState.r.getLastSeed());
    }

    public boolean execute() {
	// #0 if end of loop, reset
	if (loop>0 && (CommonState.getIntTime() == (from + loop))) {
	    this.from = CommonState.getIntTime();
	    this.isInit = false;
	}
	
	// #1 it loads the file the first time
	if (!isInit && !file.equals("")) {
	    events = new ArrayList<>();
	    
	    Graph g = new DefaultGraph("g");
	    FileSource fs = new FileSourceGML();		
	    fs.addSink(g);		
	    try {
		fs.begin(file);
		while (fs.nextEvents()) { }
		fs.end();	    
	    } catch (IOException e) {
		e.printStackTrace();
		System.exit(1);
	    } finally {
		fs.removeSink(g);
	    }

	    Iterator<org.graphstream.graph.Node> it = g.nodes().iterator();
	    while (it.hasNext()) {
		this.events.add(it.next());
	    }

	    log.info("The log file {} contained {} events.", file, events.size());
	    isInit = true;
	    
	    Comparator c =  new Comparator<org.graphstream.graph.Node> () {
		    @Override
		    public int compare (org.graphstream.graph.Node n1, org.graphstream.graph.Node n2) {
			Integer d1 = Integer.valueOf(n1.getAttribute("Date").toString());
			Integer d2 = Integer.valueOf(n2.getAttribute("Date").toString());
			return d1 - d2;
		    }
		}; // To be sure it's sorted, the well execution depends on it
	    
	    Collections.sort(this.events, c);
	}

	
	

	// #2 then it executes its instructions
	boolean isCurrentDate = true;
	while (!Objects.isNull(events) && !events.isEmpty() && isCurrentDate) {
	    org.graphstream.graph.Node nGML = events.get(0);
	    Integer date = Integer.valueOf(nGML.getAttribute("Date").toString());	    
	    Double longitude = Double.valueOf(nGML.getAttribute("Longitude").toString());
	    Double latitude = Double.valueOf(nGML.getAttribute("Latitude").toString());
	    // String country = nGML.getAttribute("Country").toString();
	    
	    LatencyInferror li = new LatencyInferror(0);
	    if ((date + this.from) <= CommonState.getIntTime()) {
		// process the event
		events.remove(0);
		// #A get closest node from AS to the end user performing request
		double minLatency = Double.POSITIVE_INFINITY;
		Node closest = null;
		for (Node n : CAutonomousSystems.nodeToPosition.keySet()) {
		    Pair<Double, Double> position = CAutonomousSystems.nodeToPosition.get(n);
		    double latency = li.infer(latitude, position.second, longitude, position.first);
		    if (minLatency > latency) {
			minLatency = latency;
			closest = n;
		    }
		}
		
		Cache cache = (Cache) closest.getProtocol(this.protocol);
		if (this.contents == 1) {
		    cache.get(this.content);
		} else {
		    int rn = erng.nextInt(this.contents);
		    cache.get(String.format("%s-%s", this.content, rn));
		}
		
	    } else {
		isCurrentDate = false;
	    }
	}
	return false;
    }
    
}
