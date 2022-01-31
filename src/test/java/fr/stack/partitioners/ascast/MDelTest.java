package fr.stack.partitioners.ascast;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.util.ArrayList;
import peersim.core.Node;

import fr.stack.structures.Identifier;



@DisplayName("Delete messages should be simple.")
public class MDelTest {

    @Test
    @DisplayName("A delete of the preceding add works.")
    public void addThenDelete() {
	FakeNode n1 = new FakeNode(0L);
	
	MAdd best = new MAdd(0L, 1, 0.);
	best = best.fwd(n1, 1, 0.);
	MDel delete = new MDel(0, 2);
	Assertions.assertTrue(delete.shouldDel(best));
    }
    
}
