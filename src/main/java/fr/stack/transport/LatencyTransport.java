package fr.stack.transport;

import peersim.transport.Transport;
import peersim.core.Node;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.edsim.EDSimulator;
import peersim.util.ExtendedRandom;
import peersim.config.Configuration;



public class LatencyTransport implements Transport {

    public static int[][] lags;
    private static ExtendedRandom erng;

    private static final String PAR_FROM = "from";
    private static final String PAR_TO = "to";
    private static int from;
    private static int to;


    
    public LatencyTransport (String prefix) {
        LatencyTransport.lags = new int[Network.size()][Network.size()];
        LatencyTransport.erng = new ExtendedRandom(CommonState.r.getLastSeed());

	LatencyTransport.from = Configuration.getInt(prefix + "." + LatencyTransport.PAR_FROM, 1); // [1;
	LatencyTransport.to = Configuration.getInt(prefix + "." + LatencyTransport.PAR_TO, 2); // ;2(

	// #1 default latencies follows uniform distribution
	LatencyTransport.uniformRandomBetween(LatencyTransport.from, LatencyTransport.to);
	
	// #2 (TODO) load a file containing latency
    }


    public static void uniformRandomBetween(int from, int to) {
        for (int i = 0; i < lags.length; ++i) {
            for (int j = 0; j < lags[i].length; ++j) {
		if (i != j) {
		    lags[i][j] = erng.nextInt(to - from) + from; // next int for now
		    lags[j][i] = lags[i][j];
		} else {
		    lags[i][j] = 0;
		}
            }
        }
    }

    
    public long getLatency (Node src, Node dest) {
	return LatencyTransport.lags[(int)src.getID()][(int)dest.getID()];
    }
    
    public void send(Node src, Node dest, Object msg, int pid) {
	EDSimulator.add(getLatency(src, dest), msg, dest, pid);
    }

    public Object clone() {
	return this;
    }
    
}
