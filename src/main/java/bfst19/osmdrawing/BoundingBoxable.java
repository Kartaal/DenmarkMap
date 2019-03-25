package bfst19.osmdrawing;

import javafx.geometry.BoundingBox;

public interface BoundingBoxable {
    public float getCenterX();

    public float getCenterY();

    public BoundingBox getBB();
}