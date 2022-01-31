package fr.stack.partitioners.crdt;

import java.util.TreeSet;
import java.util.TreeMap;

import fr.stack.structures.Identifier;



public class CRDTSet {

    public TreeSet<Identifier> add;
    public TreeSet<Identifier> rem;

    public TreeMap<Identifier, Double> distances;


    
    public CRDTSet() {
        this.add = new TreeSet();
        this.rem = new TreeSet();
        this.distances = new TreeMap();
    }

    public void add(Identifier id, Double distance) {
        if (!this.rem.contains(id)) {
            this.add.add(id);
            Double currentDistance = this.distances.containsKey(id) ?
                this.distances.get(id) : Double.MAX_VALUE;
            this.distances.put(id, Math.min(currentDistance, distance));
        }
    }

    public void rem(Identifier id) {
        this.rem.add(id);
        if (this.distances.containsKey(id))
            this.distances.remove(id);
    }

    public boolean contains(Identifier id){
        return this.add.contains(id) || this.rem.contains(id);
    }

    public boolean improve(Identifier id, Double distance) {
        return !this.rem.contains(id) &&
            this.add.contains(id) &&
            this.distances.get(id) > distance;
    }

    public double best() {
        double min = Double.MAX_VALUE;
        for (Identifier id : this.add) {
            if (!this.rem.contains(id)){
                min = Math.min(min, this.distances.get(id));
            }
        };
        return min;
    }

    public long bestId() {
        double min = Double.MAX_VALUE;
        long bestId = -1;
        for (Identifier id : this.add) {
            if (!this.rem.contains(id)){
                min = Math.min(min, this.distances.get(id));
                bestId = id.id;
            }
        };
        return bestId;
    }

}
