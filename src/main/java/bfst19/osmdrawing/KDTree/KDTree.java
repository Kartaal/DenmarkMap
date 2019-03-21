package bfst19.osmdrawing.KDTree;

import java.util.ArrayList;

public class KDTree {
    enum Dimension {
        X, Y
    }

     //Used for later, when making tree structure , recursion :)
     KDTree HIGH, LOW;
     private KDNode splitNode;
     private double xMin, yMin, xMax, yMax;
     private Dimension dimension;

	 public boolean isEmpty(){
		 return size == null;
	 }

     public KDTree(ArrayList<KDNode> nodes, double xLeft, double yBot, double xRight, double yTop, Dimension dimension){

        try {

        xMin = xLeft;
        yMin = yBot;
        xMax = xRight;
        yMax = yTop;
        this.dimension = dimension;
        int size = nodes.size();
        int addedSize = 0;

        ArrayList<KDNode> low = new ArrayList<>();
        ArrayList<KDNode> high = new ArrayList<>();
        splitNode = nodes.get(size / 2);

        if (yMax - yMin < xMax - xMin) {
            // x is greater
            dimension = Dimension.X;
            for (KDNode node : nodes) {
                // if node centerX is lower than splitNode centerX then add to low, else high.
                // If they are same, then its split node
                if (node == splitNode) {
                    // Go to next node
                    continue;
                }
                if (node.getCenterX() < splitNode.getCenterX()) {
                    low.add(node);
                    addedSize++;
                } else {
                    high.add(node);
                    addedSize++;
                }
            }
        } else {
            // y must be same or greater
			dimension = Dimension.Y;
            for (KDNode node : nodes) {
                // if node centerY is lower than splitNode centerY then add to low, else high.
                // If they are same, then its split node
                if (node == splitNode) {
                    // Go to next node
                    continue;
                }
                if (node.getCenterY() < splitNode.getCenterY()) {
                    low.add(node);
                    addedSize++;
                } else {
                    high.add(node);
                    addedSize++;
                }
            }
        }
        if (size != addedSize + 1){
            //makes sure if size is equals to addedSize + 1. The +1 is from the split Node
            if (size < addedSize + 1){
                throw new UnexpectedSizeException("Size larger than expected");
            }
            if (size > addedSize + 1){
                throw new UnexpectedSizeException("Size smaller than expected");
            }   else{
                throw new UnexpectedSizeException("I have no idea what happened, but sizes are not equal");
            }
        }

		double[] lowBounds = new double[4];
		double[] highBounds = new double[4];

        createBoundsArrays();

        flipDimension();

		if(!low.isEmpty) {
			LOW = new KDTree(low, lowbounds[0], lowBounds[1], lowBounds[2], lowBounds[3], dimension);
		}
		if(!high.isEmpty) {
			HIGH = new KDTree(high, highBounds[0], highBounds[1], highBounds[2], highBounds[3], dimension);
		}

        } catch (UnexpectedSizeException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Something in general went wrong");
        }
    }

    private void createBounds() {
        if (dimension == Dimension.X){
			lowbounds[0] = xMin;
			lowbounds[1] = yMin;
			lowbounds[2] = splitNode.getCenterX();
			lowbounds[3] = yMax;

			highbounds[0] = splitNode.getCenterX();
			highbounds[1] = yMin;
			highbounds[2] = xMax;
			highbounds[3] = yMax;
		} else { // dimension must be y
			lowbounds[0] = xMin;
			lowbounds[1] = splitNode.getCenterY();
			lowbounds[2] = xMax;
			lowbounds[3] = yMax;

			highbounds[0] = xMin;
			highbounds[1] = yMin;
	 		highbounds[2] = xMax;
			highbounds[3] = splitNode.getCenterY();
		}
    }

    private void flipDimension() {
        if(dimension == Dimension.X) dimension = Dimension.Y;
        else dimension = Dimension.X;
    }
}
