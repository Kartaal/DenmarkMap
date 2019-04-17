package bfst19.Line;

import java.util.ArrayList;
import java.util.function.LongSupplier;
public class OSMWay extends ArrayList<OSMNode> implements LongSupplier {
	ArrayList<OSMNode> ways;
	long id;

	public OSMWay(long id) {
		this.id = id;
		ways = new ArrayList<>();
	}

	@Override
	public long getAsLong() {
		return id;
	}

	public OSMNode getFirst() {
		return get(0);
	}

	public OSMNode getLast() {
		return get(size()-1);
	}


	//TODO All of this refactoring headache, right now it's very tightly connected to ArrayList
	//Refactoring the ArrayList out of inheritance
	// following methods needed for the rest of the code to work
	/*
	public void add(OSMNode osmNode) {
		ways.add(osmNode);
	}

	public int size() {
		return ways.size();
	}

	public OSMNode get(int i) {
		return ways.get(i);
	}

	*/
}
