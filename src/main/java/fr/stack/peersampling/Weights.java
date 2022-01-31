package fr.stack.peersampling;

import peersim.util.ExtendedRandom;
import peersim.core.CommonState;



public class Weights {
    
    // weights are integers, for they might be useful for latency
    public static int[][] weights;
    private static ExtendedRandom erng;


    
    public Weights (int numberOfPeers) {
        Weights.weights = new int[numberOfPeers][numberOfPeers];
        Weights.erng = new ExtendedRandom(CommonState.r.getLastSeed());
    }

    public Weights (int numberOfPeers, long seed) { // (for testing purpose)
	Weights.weights = new int[numberOfPeers][numberOfPeers];
        Weights.erng = new ExtendedRandom(seed);
    }
    
    public static void uniformRandomBetween(int from, int to) {
        for (int i = 0; i < weights.length; ++i) {
            for (int j = 0; j < weights[i].length; ++j) {
		if (i != j) {
		    weights[i][j] = erng.nextInt(to - from) + from; // next int for now
		    weights[j][i] = weights[i][j];
		} else {
		    weights[i][j] = 0;
		}
            }
        }
    }
    
    public static int get(long from, long to) {
        return weights[(int) from][(int) to];
    }
}
