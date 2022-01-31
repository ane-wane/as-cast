package fr.stack.structures;

import java.util.TreeMap;



public class Versions {

    public TreeMap<Long, Integer> versions;

    public Versions () {
	this.versions = new TreeMap<Long, Integer>();
    }

    public boolean isStale(Identifier id) {
	// when no key, we assume lowest counter 0
	return this.versions.containsKey(id.id) && id.counter < this.versions.get(id.id);
    }

    public void update(Identifier id) {
	if (!this.versions.containsKey(id.id))
	    this.versions.put(id.id, 0);

	this.versions.put(id.id, Math.max(this.versions.get(id.id), id.counter));
    }

    public boolean isBrandNew(Identifier id) {
	return !this.versions.containsKey(id.id) || this.versions.get(id.id) < id.counter;
    }
}
