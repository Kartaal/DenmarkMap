package bfst19.KDTree;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @see java.util.Comparator
 * A comparator that takes two BoundingBoxable objects and compares their center y values.
 * Returns a negative integer if a's centerY value is smaller than b's centerY value.
 * Returns 0 if a's centerY value is equal to b's centerY value.
 * Returns a positive integer if a's centerY value is larger than b's centerY value.
 */
//Innerclass comparator for Y dimension
public class yComparator implements Comparator<BoundingBoxable>, Serializable {
    //This calculation is made to convert from float to int because Comparator interface requires it
    public int compare(BoundingBoxable a, BoundingBoxable b) {
        return (int) (a.getCenterY() - b.getCenterY()) * 1000000;
    }
}
