package fr.stack.partitioners.ascast;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.util.ArrayList;



@DisplayName("Testing add messages, just in case…")
public class MAddTest {

    @Test
    @DisplayName("Is this looping?")
    public void shouldDetectLoop () {
	FakeNode receiver = new FakeNode(42);
	FakeNode receiver2 = new FakeNode(12);

	MAdd a = new MAdd(42L, 1, 0.);
	MAdd f = a.fwd(receiver, 2, 1.);
	
	Assertions.assertTrue(f.isLooping(receiver));
	Assertions.assertTrue(!f.isLooping(receiver2));
    }
    

    
}
