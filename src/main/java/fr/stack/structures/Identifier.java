package fr.stack.structures;



public class Identifier implements Comparable<Identifier> {

    public final long id;
    public final int counter;

    public Identifier (long id, int counter) {
        this.id = id;
        this.counter = counter;
    }

    public Identifier(Identifier id) {
	this.id = id.id;
	this.counter = id.counter;
    }

    public static Identifier NOTHING() {
	return new Identifier(-1, -1);
    }

    public int compareTo(Identifier o) {
        if (this.id < o.id)
            return -1;
        if (this.id > o.id)
            return 1;
        if (this.counter < o.counter)
            return -1;
        if (this.counter > o.counter)
            return 1;
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return this.compareTo((Identifier) obj) == 0;
    }

    public String toString(){
        return String.format("(%s, %s)", this.id, this.counter);
    }

    public boolean isNothing(){
	return this.id == -1 && this.counter == -1;
    }
}
