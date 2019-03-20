package bfst19.osmdrawing.KDTree;

import bfst19.osmdrawing.OSMWay;
import bfst19.osmdrawing.WayType;

public class Edge {


    private final OSMWay path; //TODO: OSMRelation instead?
    private final String roadname;
    private final WayType wayType;
    private final float centerX, centerY;

    public Edge(OSMWay path, String roadname, WayType wayType, float centerX, float centerY) {
        this.path = path;
        this.roadname = roadname;
        this.wayType = wayType;
        this.centerX = centerX;
        this.centerY = centerY;
    }


    //Getters

    public OSMWay getPath() {
        return path;
    }

    public String getRoadname() {
        return roadname;
    }

    public WayType getWayType() {
        return wayType;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }


}
