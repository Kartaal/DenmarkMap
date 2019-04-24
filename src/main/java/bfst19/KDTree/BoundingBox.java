package bfst19.KDTree;

import java.io.Serializable;

//This class exists because JavaJX BoundingBox is not Serializable
// and its superclass Bounds only has a 3D constructor
public class BoundingBox implements Serializable {
	private double minX;
	private double minY;
	private double maxX;
	private double maxY;

	public BoundingBox(double minX, double minY, double width, double height) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = minX+width;
		this.maxY = minY+height;

	}

	public double getMinX() {
		return minX;
	}

	public double getMinY() {
		return minY;
	}

	public double getMaxX() { return maxX; }

	public double getMaxY() {
		return maxY;
	}



	public boolean intersects(BoundingBox b) {
		//if ((b == null) || b.isEmpty()) return false;
		return intersects(b.getMinX(), b.getMinY(),
				b.getMaxX(), b.getMaxY());
	}

	public boolean intersects(double minX, double minY,
							  double maxX, double maxY) {
		//if (isEmpty() || w < 0 || h < 0) return false;
		return (maxX>= getMinX() &&
				maxY >= getMinY() &&
				minX <= getMaxX() &&
				minY <= getMaxY());
	}

	/*public boolean isEmpty() {
		return getMaxX() < getMinX() || getMaxY() < getMinY();
	}*/
}