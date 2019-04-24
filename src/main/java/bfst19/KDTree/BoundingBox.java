package bfst19.KDTree;

import java.io.Serializable;

//This class exists because JavaJX BoundingBox is not Serializable
// and its superclass Bounds only has a 3D constructor
public class BoundingBox implements Serializable {
	private double minX;
	private double minY;
	private double width;
	private double height;

	public BoundingBox(double minX, double minY, double width, double height) {
		this.minX = minX;
		this.minY = minY;
		this.width = width;
		this.height = height;
	}

	public double getMinX() {
		return minX;
	}

	public double getMinY() {
		return minY;
	}

	public double getMaxX() {
		return minX + width;
	}

	public double getMaxY() {
		return minY + height;
	}


	//Code from javafx.geometry.BoundingBox.java
	public boolean intersects(BoundingBox b) {
		if ((b == null) || b.isEmpty()) return false;
		return intersects(b.getMinX(), b.getMinY(),
				b.getMaxX()-b.getMinX(), b.getMaxY()-b.getMinY());
	}

	//Code from javafx.geometry.BoundingBox.java
	public boolean intersects(double x, double y,
							  double w, double h) {
		if (isEmpty() || w < 0 || h < 0) return false;
		return (x + w >= getMinX() &&
				y + h >= getMinY() &&
				x <= getMaxX() &&
				y <= getMaxY());
	}

	//Code from javafx.geometry.BoundingBox.java
	public boolean isEmpty() {

		return getMaxX() < getMinX() || getMaxY() < getMinY();
	}

}