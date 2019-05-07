package bfst19.KDTree;

import java.io.Serializable;
import java.util.Comparator;

//Innerclass comparator for Y dimension
public class yComparator implements Comparator<BoundingBoxable>, Serializable {
    //This calculation is made to convert from float to int because Comparator interface requires it
    // Returns a negative integer if a's centerY value is smaller than b's centerY value
    // Returns 0 if a's centerY value is equal to b's centerY value
    // Returns a positive integer if a's centerY value is larger than b's centerY value
    public int compare(BoundingBoxable a, BoundingBoxable b) {
        return (int) (a.getCenterY() - b.getCenterY())*1000000;
    }
}
