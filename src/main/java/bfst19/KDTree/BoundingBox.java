package bfst19.KDTree;

import java.io.Serializable;

//This class exists because JavaJX BoundingBox is not Serializable
// and its superclass Bounds only has a 3D constructor
public class BoundingBox implements Serializable {
	private float minX;
	private float minY;
	private float maxX;
	private float maxY;

	public BoundingBox(float minX, float minY, float width, float height) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = minX+width;
		this.maxY = minY+height;

	}

	public float getMinX() {
		return minX;
	}

	public float getMinY() {
		return minY;
	}

	public float getMaxX() { return maxX; }

	public float getMaxY() {
		return maxY;
	}


	//Code from javafx.geometry.BoundingBox.java
	public boolean intersects(BoundingBox b) {
		//if ((b == null) || b.isEmpty()) return false;
		return intersects(b.getMinX(), b.getMinY(),
				b.getMaxX(), b.getMaxY());
	}

	public boolean intersects(float minX, float minY,
							  float maxX, float maxY) {
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
