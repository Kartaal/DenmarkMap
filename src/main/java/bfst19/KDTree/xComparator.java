package bfst19.KDTree;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @see java.util.Comparator
 * A comparator that takes two BoundingBoxable objects and compares their center x values.
 * Returns a negative integer if a's centerX value is smaller than b's centerX value.
 * Returns 0 if a's centerX value is equal to b's centerX value.
 * Returns a positive integer if a's centerX value is larger than b's centerX value.
 */
//Innerclass comparator for X dimension
public class xComparator implements Comparator<BoundingBoxable>, Serializable {
	//This calculation is made to convert from float to int because Comparator interface requires it
	public int compare(BoundingBoxable a, BoundingBoxable b) {
		return (int) (a.getCenterX() - b.getCenterX()) * 1000000;
	}
}
