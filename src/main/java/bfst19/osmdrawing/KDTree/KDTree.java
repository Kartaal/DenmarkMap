package bfst19.osmdrawing.KDTree;

import java.util.LinkedList;

public class KDTree {
    enum Dimension {
        x, y
    }

    KDTree HIGH, LOW; //Used for later, when making tree structure
    private Edge splitEdge;
    private double xMin, yMin, xMax, yMax;
    private Dimension dimension;

    public KDTree(LinkedList<Edge> edges, double xLeft, double yBot, double xRight, double yTop){
        try {

        xMin = xLeft;
        yMin = yBot;
        xMax = xRight;
        yMax = yTop;
        int size = edges.size();
        int addedSize = 0;

        LinkedList<Edge> low = new LinkedList<>();
        LinkedList<Edge> high = new LinkedList<>();
        splitEdge = edges.get(size / 2);

        if (yMax - yMin < xMax - xMin) {
            // x is greater
            dimension = Dimension.x;
            for (Edge edge : edges) {
                // if edge centerX is lower than splitEdge centerX then add to low, else high. If they are same, then its split edge
                if (edge == splitEdge) {
                    continue; // Go to next edge
                }
                if (edge.getCenterX() < splitEdge.getCenterX()) {
                    low.add(edge);
                    addedSize++;
                } else {
                    high.add(edge);
                    addedSize++;
                }

            }
        } else {
            // y must be same or greater
            dimension = Dimension.y;
            for (Edge edge : edges) {
                // if edge centerY is lower than splitEdge centerY then add to low, else high. If they are same, then its split edge
                if (edge == splitEdge) {
                    continue; // Go to next edge
                }
                if (edge.getCenterY() < splitEdge.getCenterY()) {
                    low.add(edge);
                    addedSize++;
                } else {
                    high.add(edge);
                    addedSize++;
                }

            }
        }
        if (size != ++addedSize){
            //makes sure if size is equals to addedSize + 1. The +1 is from the split Edge
            if (size < ++addedSize){
                throw new UnexpectedSizeException("Size larger than expected");
            }
            if (size > ++addedSize){
                throw new UnexpectedSizeException("Size smaller than expected");
            }   else{
                throw new UnexpectedSizeException("I have no idea what happened, but sizes are not equal");
            }
        }



        //TODO: set boundaries(make boxes;) ) (low bound = max value, high bound min val)
        //TODO: if tree not empty, create new high/low tree with boundaries (equal to LOW / HIGH above)




        } catch (UnexpectedSizeException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Something in general went wrong");
        }
    }



}




