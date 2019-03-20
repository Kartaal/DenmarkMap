package bfst19.osmdrawing.KDTree;

import bfst19.osmdrawing.OSMWay;
import bfst19.osmdrawing.WayType;

public class Edge { // TODO: add necessary information here


    private final OSMWay path; // TODO: OSMRelation instead?
    private final String roadname;
    private final WayType wayType;
    private final float centerX, centerY;

    public Edge(OSMWay path, String roadname, WayType wayType) {
        this.path = path;
        if (wayType == WayType.COASTLINE){ //Coastlines dont have a name
            this.roadname = null;
        } else {
        this.roadname = roadname;
        }
        this.wayType = wayType;

        float xMin = path.get(0).getLon();
        float xMax = path.get(0).getLon();
        float yMin = path.get(0).getLat();
        float yMax = path.get(0).getLat();

        // go through path list, and find min and max coords (methods check between arguments and returns min/max of those)
        // maybe change to screen coords at some point
        for (int i = 1; i < path.size(); i++) {
            xMin = Math.min(xMin, path.get(i).getLon());
            xMax = Math.max(xMax, path.get(i).getLon());
            yMin = Math.min(yMin, path.get(i).getLat());
            yMax = Math.max(yMax, path.get(i).getLat());
        }

        this.centerX = (xMin + xMax) / 2;
        this.centerY = (yMin + yMax) / 2;
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

    float getCenterX() {
        return centerX;
    }

    float getCenterY() {
        return centerY;
    }


}
