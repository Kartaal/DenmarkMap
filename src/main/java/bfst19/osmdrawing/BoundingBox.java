package bfst19.osmdrawing;

import java.io.Serializable;

public class BoundingBox extends javafx.geometry.BoundingBox implements Serializable {
	public BoundingBox(double minX, double minY, double width, double height) {
		super(minX, minY, width, height);
	}
}
