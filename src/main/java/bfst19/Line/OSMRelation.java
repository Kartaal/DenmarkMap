package bfst19.Line;

import bfst19.ResizingArray;

import java.util.Iterator;

//container for relations used in merge or others
public class OSMRelation implements Iterable<OSMWay> {
	private ResizingArray<OSMWay> ways;

	public OSMRelation() {
		ways = new ResizingArray<>();

	}

	public void add(OSMWay member) {
		ways.add(member);
	}

	public Iterator<OSMWay> iterator() {
		return ways.iterator();
	}
}