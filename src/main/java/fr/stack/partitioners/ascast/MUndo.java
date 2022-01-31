package fr.stack.partitioners.ascast;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Arrays;
import peersim.core.Node;

import fr.stack.structures.Identifier;



/**
 * Structure of messages that cancels, or *undo*es messages. Very
 * similar to MDel in its functioning, it has additional behavior that
 * are finally worth differentiating.
 */
public class MUndo extends MDel {

    public ArrayList<Long> addPath = new ArrayList<>();
    public ArrayList<Long> undoPath = new ArrayList<>();

    public Identifier detector;
    
    public MUndo (long id, int counter) {
	super(id, counter);
    }

    public MUndo (Identifier id) {
	super(id);
    }

    public MUndo (Identifier id, ArrayList<Long> addPath) {
	super(id);
	for (Long peerId : addPath)
	    this.addPath.add(peerId);
    }
    
    public MUndo (long id, int counter, ArrayList<Long> addPath) {
	super(id, counter);
	for (Long peerId : addPath)
	    this.addPath.add(peerId);
    }

    public MUndo (Identifier idDetector, MAdd best) {
	super(best.id);
	this.detector = new Identifier(idDetector);
    	for (Identifier peerId : best.path)
	    this.addPath.add(peerId.id);
    }



    @Override
    public MUndo fwd(Node forwarder) {
	MUndo u = this.clone();
	u.undoPath.add(forwarder.getID());
	u.last = forwarder;
	return u;
    }

    public MUndo clone() {
	MUndo clone = new MUndo(this.id);
	for (Long id: this.addPath)
	    clone.addPath.add(id);
	for (Long id: this.undoPath)
	    clone.undoPath.add(id);
	clone.last = this.last;
	clone.detector = new Identifier(this.detector);
	return clone;
    }

    public boolean isLooping(Node receiver) {
	return this.addPath.contains(receiver.getID()) || this.undoPath.contains(receiver.getID());
    }

    @Override
    public String toString() {
	return String.format("(UNDO FROM %s of %s; %s - %s)",
			     detector,
			     id,
			     Arrays.toString(addPath.toArray()),
			     Arrays.toString(undoPath.toArray()));
    }

    @Override
    public int hashCode() {
	return Objects.hash(this.toString());
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof MUndo))
	    return false;

	MUndo u = (MUndo) o;

	if (!this.id.equals(u.id))
	    return false;
	
	if (!this.addPath.equals(u.addPath) || !this.undoPath.equals(u.undoPath))
	    return false;

	return true;
    }

    @Override
    public Identifier target () {
	return new Identifier(id);
    }

    @Override
    public boolean shouldDel (MAdd best) {
	for (Identifier id : best.path) {
	    if (detector.id == id.id && detector.counter > id.counter)
		return true;
	}
	return false;
	
	// if (!best.id.equals(this.target()))
	//     return false;

	// if (best.isNothing()) // nothing cannot be undone
	//     return false;

	// if (best.isSource()) // source can be deleted and that's it
	//     return false;
	
	// if (!best.last.equals(this.last))
	//     return false;

	// boolean sameSignature = best.path.size() == addPath.size() + undoPath.size();
	// if (!sameSignature)
	//     return false;
	
	// int i = addPath.size() + undoPath.size() - 1; // likely difference in tail
	// while (sameSignature && i >= 0) {
	//     if (i > addPath.size() - 1)
	// 	sameSignature = best.path.get(i).equals(undoPath.get(i-addPath.size()));
	//     else
	// 	sameSignature = best.path.get(i).equals(addPath.get(i));
	//     if (!sameSignature)
	// 	return false;
	//     --i;
	// }
	
	// return true;
    }


    @Override
    public boolean hasCommonAncestor(MAdd best) {
	// for (Long l : best.path) {
	//     if (this.undoPath.contains(l))
	// 	return true;
	// }
	return false;
    }

    @Override
    public boolean hasCommonPrefix(MAdd best) {
	// if (undoPath.isEmpty())
	//     return false;

	// int i = 0;
	// boolean found = false;
	// while (!found && i < best.path.size()) {
	//     if (undoPath.get(0).equals(best.path.get(i))) {
	// 	found = true;
	//     } else {
	// 	++i;
	//     }
	// }
	// /// start = i
	// if (i >= best.path.size())
	//     return false;

	// boolean same = true;
	// int j = 0;
	// while (same && i < best.path.size() && j < undoPath.size()) {
	//     if (!best.path.get(i).equals(undoPath.get(j))) {
	// 	same = false;
	//     } else {
	// 	++i;
	// 	++j;
	//     }
	// }

	// for (int k = j; k < undoPath.size(); ++k) {
	//     if (best.path.contains(undoPath.get(k)))
	// 	return false;
	// }

	return true;
    }
    
    @Override
    public boolean isUndo() {
	return true;
    }
    
}
