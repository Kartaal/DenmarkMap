package bfst19.Line;

import bfst19.Route_parsing.ResizingArray;

import java.util.ArrayList;
import java.util.function.LongSupplier;
public class OSMWay implements LongSupplier {
	ResizingArray<OSMNode> ways;
	long id;

	public OSMWay(long id) {
		this.id = id;
		ways = new ResizingArray<>();
	}

	@Override
	public long getAsLong() {
		return id;
	}

	public OSMNode getFirst() {
		return ways.get(0);
	}

	public OSMNode getLast() {
		return ways.get(ways.size()-1);
	}

	//TODO All of this refactoring headache, right now it's very tightly connected to ArrayList
	//Refactoring the ArrayList out of inheritance
	// following methods needed for the rest of the code to work

	public void add(OSMNode osmNode) {
		ways.add(osmNode);
	}

	public int size() {
		return ways.size();
	}

	public OSMNode get(int i) {
		return ways.get(i);
	}



}
