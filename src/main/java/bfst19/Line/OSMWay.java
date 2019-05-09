package bfst19.Line;

import bfst19.ResizingArray;

public class OSMWay {
    private ResizingArray<OSMNode> ways;
    private int id;

    public OSMWay(int id) {
        this.id = id;
        ways = new ResizingArray<>();
    }

    public long getId() {
        return id;
    }

    public OSMNode get(int index) {
        return ways.get(index);
    }

    public OSMNode getFirst() {
        return get(0);
    }

    public OSMNode getLast() {
        return get(ways.size() - 1);
    }

    public int size() {
        return ways.size();
    }

    public void add(OSMNode osmNode) {
        ways.add(osmNode);
    }
}
