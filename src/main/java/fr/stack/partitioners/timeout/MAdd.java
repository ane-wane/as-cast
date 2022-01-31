package fr.stack.partitioners.timeout;

import fr.stack.structures.Identifier;



public class MAdd {

    public final Identifier id;
    public final double weight;


    
    public MAdd (long id, int counter, double weight) {
	this.id = new Identifier(id, counter);
        this.weight = weight;
    }

    public MAdd (Identifier id, double weight) {
	this.id = new Identifier(id);
	this.weight = weight;
    }

    public MAdd fwd(double weight) {
	return new MAdd(this.id, this.weight + weight);
    }
}
