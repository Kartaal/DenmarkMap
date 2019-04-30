package bfst19.Line;

import bfst19.Route_parsing.ResizingArray;

import java.util.ArrayList;
import java.util.function.LongSupplier;
public class OSMWay {
	ResizingArray<OSMNode> ways;
	int id;

	public OSMWay(int id) {
		this.id = id;
		ways = new ResizingArray<>();
	}

	public long getId() {
		return id;
	}

	public OSMNode get(int index){
	    return ways.get(index);
    }

	public OSMNode getFirst() {
		return get(0);
	}

	public OSMNode getLast() {
		return get(ways.size()-1);
	}

    public int size() {
    return ways.size();
	}

    public void add(OSMNode osmNode) {
	    ways.add(osmNode);
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
