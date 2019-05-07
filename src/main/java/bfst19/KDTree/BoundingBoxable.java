package bfst19.KDTree;


import bfst19.Line.OSMNode;

/**
 * Represents elements that can be stored in KDTrees based on their center point,
 * have a BoundingBox and contain OSMNodes to be retrieved.
 */
public interface BoundingBoxable {
    float getCenterX();

    float getCenterY();

    BoundingBox getBB();

    OSMNode[] getNodes();
}