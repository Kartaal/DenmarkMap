package bfst19.osmdrawing;

import java.io.Serializable;

//This class exists because JavaJX BoundingBox is not Serializable
// and its superclass Bounds only has a 3D constructor
public class BoundingBox extends javafx.geometry.BoundingBox implements Serializable {
	public BoundingBox(double minX, double minY, double width, double height) {
		super(minX, minY, width, height);
	}
}
