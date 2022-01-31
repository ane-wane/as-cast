package fr.stack.peersampling;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import fr.stack.partitioners.ascast.FakeNode;
import fr.stack.partitioners.ascast.MAdd;
import fr.stack.partitioners.ascast.MDel;
    


@DisplayName("Testing the structure that registers messages")
public class MessagesToSendTest {

    @Test
    @DisplayName("Simple distinct elements with MAdd.")
    public void simpleDistinctMAdd () {
	FakeNode n0 = new FakeNode(0);
	MessagesToSend mts = new MessagesToSend();
	MAdd a = new MAdd(n0.getID(), 1, 0.);
	mts.add(n0, a);
	Assertions.assertTrue(mts.neighborToMessages.get(n0).size() == 1);
	
	MAdd aa = new MAdd(n0.getID(), 1, 0.);
	mts.add(n0, aa);
	Assertions.assertTrue(mts.neighborToMessages.get(n0).size() == 2);

	mts.distinct();
	Assertions.assertEquals(1, mts.neighborToMessages.get(n0).size());
    }

    // @Test
    // @DisplayName("Simple distinct elements with MDel.")
    // public void simpleDistinctMDel () {
    // 	FakeNode n0 = new FakeNode(0);
    // 	MessagesToSend mts = new MessagesToSend();
    // 	MDel d = new MDel(n0.getID(), 2);
    // 	mts.add(n0, d);

    // 	MDel dd = new MDel(n0.getID(), 2);
    // 	mts.add(n0, dd);
    // 	Assertions.assertTrue(mts.neighborToMessages.get(n0).size() == 2);

    // 	mts.distinct();
    // 	Assertions.assertEquals(1, mts.neighborToMessages.get(n0).size());

    // 	MAdd a = new MAdd(n0.getID(), 1, 0.);
    // 	MDel c = a.cancel();
    // 	c = c.fwd(n0);
    // 	mts.add(n0, c);
    // 	Assertions.assertEquals(2, mts.neighborToMessages.get(n0).size());
    // 	mts.distinct();
    // 	Assertions.assertEquals(2, mts.neighborToMessages.get(n0).size());

    // 	MDel cc = a.cancel();
    // 	cc = cc.fwd(n0);
    // 	mts.add(n0, cc);
    // 	Assertions.assertEquals(3, mts.neighborToMessages.get(n0).size());
	
    // 	mts.distinct();
    // 	Assertions.assertEquals(2, mts.neighborToMessages.get(n0).size());
    // }
    
}

