package bfst19.KDTree;


import bfst19.Line.OSMNode;

public interface BoundingBoxable {
    float getCenterX();

    float getCenterY();

    BoundingBox getBB();

    OSMNode[] getNodes();
}