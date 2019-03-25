package bfst19.osmdrawing.KDTree;

import bfst19.osmdrawing.*;

public class KDNode {

    private BoundingBoxable path;
    private double split;
    private boolean vertical;
    private KDNode nodeL; //child
    private KDNode nodeR; //child



    public KDNode(BoundingBoxable path) {
        this.path = path;
    }

    // Getter methods

    public BoundingBoxable getPath() {
        return path;
    }

    float getCenterX() {
        return path.getCenterX();
    }

    float getCenterY() {
        return path.getCenterY();
    }


}
