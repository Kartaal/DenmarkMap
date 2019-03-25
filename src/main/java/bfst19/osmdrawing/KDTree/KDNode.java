package bfst19.osmdrawing.KDTree;

import bfst19.osmdrawing.*;

public class KDNode { // TODO: add necessary fields here

    private final BoundingBoxable path; // TODO: OSMRelation instead?
    private final String roadname;
    private final WayType wayType;


    public KDNode(BoundingBoxable path, String roadname, WayType wayType) {
        this.path = path;
        if (wayType == WayType.COASTLINE){ //Coastlines dont have a name
            this.roadname = null;
        } else {
        this.roadname = roadname;
        }
        this.wayType = wayType;

    }

    // Getter methods

    public BoundingBoxable getPath() {
        return path;
    }

    public String getRoadname() {
        return roadname;
    }

    public WayType getWayType() {
        return wayType;
    }

    float getCenterX() {
        return path.getCenterX();
    }

    float getCenterY() {
        return path.getCenterY();
    }


}
