package bfst19.Line;

import java.util.ArrayList;
import java.util.Iterator;

//container for relations used in merge or others
public class OSMRelation implements Iterable<OSMWay> {
	ArrayList<OSMWay> ways;

	public OSMRelation() {
		ways = new ArrayList<>();

	}

	public void add(OSMWay member) {
		ways.add(member);
	}

	public Iterator<OSMWay> iterator() {
		return ways.iterator();
	}
}