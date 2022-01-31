package fr.stack.partitioners.ascast;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import peersim.core.Node;
import peersim.core.CommonState;
    
import fr.stack.peersampling.MessagesToSend;
import fr.stack.peersampling.IMessage;
import fr.stack.peersampling.PeerSampling;
import fr.stack.peersampling.Weights;
import fr.stack.structures.Pair;



@DisplayName("Class of scenarios that must be taken into account by ASPartitioner.")
public class ASPartitionerTest {

    public static int K = 10;
    public static long SEED = 1;
    public static FakeNode[] n;
    public static ASPartitioner[] p;
    
    public ASPartitionerTest () {
	n = new FakeNode[K];
	p = new ASPartitioner[K];
	for (int i = 0 ; i < K ; ++i) {
	    n[i] = new FakeNode((long) i);
	}
	
	new Weights (K, SEED);
    }

    @BeforeEach
    public void reset () {
	for (int i = 0; i < K; ++i) {
	    p[i] = new ASPartitioner(n[i]);
	}
	Weights.uniformRandomBetween(1, 2); // always 1, i.e., equivalent to hop count
    }
    
    @Test
    @DisplayName("'0' becomes a source all alone.")
    public void isSource() {
	MessagesToSend ms = p[0]._add();
	Assertions.assertTrue(ms.isEmpty());
	Assertions.assertTrue(p[0].best.isSource());
    }

    @Test
    @DisplayName("'0' becomes a source and advertises it to '1'.")
    public void isSourceWithFriends() {
	p[0].addNeighbor(n[1]); p[1].addNeighbor(n[0]);
	MessagesToSend ms = p[0]._add();
	Assertions.assertTrue(!ms.isEmpty());
	Pair<Node, IMessage> m = ms.next();
	Assertions.assertTrue(ms.isEmpty());
	Assertions.assertTrue(m.first.equals(n[1]) && m.second instanceof MAdd);
	MessagesToSend ms2 = p[1].receiveAdd(n[0], (MAdd) m.second);
	Assertions.assertTrue(!ms2.isEmpty());
	Assertions.assertTrue(p[1].best.weight == 1 && p[1].best.id.equals(p[0].best.id));
    }

    @Test
    @DisplayName("Cadarache case: 0 -> 1 -> 2, 2 cannot know about ∂0 because of 1, 2 cannot go in α9 because ∂9. This highlights the need for undo.")
    public void detectionWithThree() {
	p[0].addNeighbor(n[1]);
	p[1].addNeighbor(n[0]).addNeighbor(n[2]).addNeighbor(n[9]);
	p[2].addNeighbor(n[1]).addNeighbor(n[9]);
	p[9].addNeighbor(n[1]).addNeighbor(n[2]);
	
	Weights.weights[0][1] = 2;
	Weights.weights[1][0] = 2;
	
	MessagesToSend ma0 = p[0]._add(); // α0
	MessagesToSend md0 = p[0]._del(); // α0 -> ∂0
	MessagesToSend ma0_b = p[1].receiveAdd(n[0], (MAdd) ma0.get(n[1]).remove(0)); // @1 α0
	
	MessagesToSend ma9 = p[9]._add();
	MessagesToSend md9 = p[9]._del(); // || α9 -> ∂9

	MessagesToSend ma9_b = p[2].receiveAdd(n[9], (MAdd) ma9.get(n[2]).remove(0));
	MessagesToSend md9_b = p[2].receiveDel(n[9], (MDel) md9.get(n[2]).remove(0)); // @2 α9 -> ∂9
	
	MessagesToSend ma9_c = p[1].receiveAdd(n[9], (MAdd) ma9.get(n[1]).remove(0)); // @1 α0 -> α9
	Assertions.assertTrue(p[1].best.id.id == 9);

	MessagesToSend ma0_c = p[2].receiveAdd(n[1], (MAdd) ma0_b.get(n[2]).remove(0)); // @1 α9 -> ∂9 -> α0
	Assertions.assertTrue(p[2].best.id.id == 0);

	MessagesToSend md0_b = p[1].receiveDel(n[0], (MDel) md0.get(n[1]).remove(0)); // @1 don't care about ∂0, echo ourselves to 0 and that's it
	Pair<Node, IMessage> echo = md0_b.next();
	Assertions.assertTrue(md0_b.isEmpty()); // (because next() consumes a message)
	Assertions.assertTrue(echo.first.getID() == 0 && echo.second instanceof MAdd);

	MessagesToSend ma9_d = p[2].receiveAdd(n[1], (MAdd) ma9_c.get(n[2]).remove(0)); // @2 cannot deliver α2 since already received, acknowledge possible issue
	Assertions.assertTrue(p[2].best.isNothing());
	for (Node n: p[2].peerSampling.neighbors())
	    ma9_d.next();
	Assertions.assertTrue(ma9_d.isEmpty()); // 1 message for each neighbor
    }

    @Test
    @DisplayName("Tricky Cadarache case with two nodes looping 0 -> 1 -> 0. This highlights the need for α to go to their parent.")
    public void detectionWithTwo () {
	// (TODO) ? 
    }
    

	

}
