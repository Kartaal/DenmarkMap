package bfst19.KDTree;

import java.io.Serializable;

/**
 * This class implements an abstract bounding box based on minimum x and y values as well as width and height.
 * The minimum x and y values are stored in fields of type double, and width and height are used to
 * calculate the maximum x and y values before storing these. This construction is used because JavaFX Bounds
 * operate with minimum values and relatives while this program mostly deals in absolute values.
 * It is Serializable and can be saved to .obj files as opposed to the JavaFX BoundingBox class.
 * A point or another BoundingBox can be checked for intersecting using the intersects() methods.
 */
//This class exists because JavaJX BoundingBox is not Serializable
// and its superclass Bounds only has a 3D constructor
public class BoundingBox implements Serializable {
	private double minX;
	private double minY;
	private double maxX;
	private double maxY;

	/**
	 * Constructor for BoundingBox.
	 * The maximum x and y values are calculated and stored but width and height are discarded afterwards.
	 * @param minX		The minimum y-axis bounding line of the BoundingBox.
	 * @param minY		The minimum x-axis bounding line of the BoundingBox.
	 * @param width		The width of the BoundingBox, starting from minX.
	 * @param height	The height of the BoundingBox, starting from minY.
	 */
	//todo add isEmpty check in constructor
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

	/**
	 * Checks whether this intersects the BoundingBox b using the other intersects method.
	 * @param b	The BoundingBox to check for intersection with this object.
	 * @return	A boolean true if the current maximum x value is equal to or larger than BoundingBox b's
	 * 			minimum x value, current maximum y is equal to or larger than b's minimum y value,
	 * 			current minimum x is less or equal to b's maximum x and current minimum y is equal to or
	 * 			less than b's maximum y.
	 */
	//Code from javafx.geometry.BoundingBox.java
	public boolean intersects(BoundingBox b) {
		//if ((b == null) || b.isEmpty()) return false;
		return intersects(b.getMinX(), b.getMinY(),
				b.getMaxX(), b.getMaxY());
	}

	/**
	 * Checks whether this intersects a box from two x and two y values.
	 * @param minX	The minimum y-axis bounding line, intersects if it is equal to or larger than current maximum x.
	 * @param minY	The minimum x-axis bounding line, intersects if it is equal to or larger than current maximum y.
	 * @param maxX	The maximum y-axis bounding line, intersects if it is equal to or smaller than current minimum x.
	 * @param maxY	The maximum x-axis bounding line, intersects if it is equal to or smaller than current y.
	 * @return 		A boolean true if the current maximum x value is equal to or larger than minX,
	 * 				current maximum y is equal to or larger than minY,
	 * 				current minimum x is less or equal to maxX and
	 * 				current minimum y is equal to or less than maxY.
	 */
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

