package bfst19.osmdrawing.KDTree;

import bfst19.osmdrawing.Drawable;
import com.sun.source.tree.Tree;
import javafx.geometry.Bounds;

import java.util.ArrayList;
import java.util.List;

public class KDTree {



    enum Dimension {
        X, Y
    }

     //Used for later, when making tree structure , recursion :)
     KDTree HIGH, LOW;
     private KDNode splitNode;
     private double xMin, yMin, xMax, yMax;
     private Dimension dimension;
     private int size;
     private double[] highBounds;
     private double[] lowBounds;
     private int Treesize;
     private ArrayList<KDNode> leaf;


    public KDTree(ArrayList<KDNode> nodes, double xLeft, double yBot, double xRight, double yTop){

        try {

        xMin = xLeft;
        yMin = yBot;
        xMax = xRight;
        yMax = yTop;
        this.size = nodes.size();
        Treesize++;
        int addedSize = 0;
        ArrayList<KDNode> low = new ArrayList<>();
        ArrayList<KDNode> high = new ArrayList<>();
        leaf = new ArrayList<>();
        splitNode = nodes.get(size / 2);

        if (yMax - yMin < xMax - xMin) {
            // x is greater. This depends on the program being in a widescreen format when opening. TODO: change later
            this.dimension = Dimension.X;
            for (KDNode node : nodes) {
                if(nodes.size() > 1000){
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
                } else{
                    leaf.add(node);
                }
            }
        } else {
            // y must be same or greater
			this.dimension = Dimension.Y;
            for (KDNode node : nodes) {
                if(nodes.size() > 1000){
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
                } else{
                    leaf.add(node);
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
            } else {
                throw new UnexpectedSizeException("I have no idea what happened, but sizes are not equal");
            }
        }

		this.lowBounds = new double[4];
		this.highBounds = new double[4];

        createBounds();

        //flipDimension();


		if(!low.isEmpty()) {
			LOW = new KDTree(low, lowBounds[0], lowBounds[1], lowBounds[2], lowBounds[3]);
		}
		if(!high.isEmpty()) {
			HIGH = new KDTree(high, highBounds[0], highBounds[1], highBounds[2], highBounds[3]);
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
        if (this.dimension == Dimension.X){
			lowBounds[0] = xMin;
			lowBounds[1] = yMin;
			lowBounds[2] = splitNode.getCenterX();
			lowBounds[3] = yMax;

			highBounds[0] = splitNode.getCenterX();
			highBounds[1] = yMin;
			highBounds[2] = xMax;
			highBounds[3] = yMax;
		} else { // dimension must be y
			lowBounds[0] = xMin;
			lowBounds[1] = splitNode.getCenterY();
			lowBounds[2] = xMax;
			lowBounds[3] = yMax;

			highBounds[0] = xMin;
			highBounds[1] = yMin;
	 		highBounds[2] = xMax;
			highBounds[3] = splitNode.getCenterY();
		}
    }

    public Iterable<Drawable> rangeQuery(Bounds bbox) {
        List<Drawable> returnElements = new ArrayList<>();
        rangeQuery(bbox, returnElements);
        return returnElements;
    }

    private void rangeQuery(Bounds bbox, List<Drawable> returnElements) {
        if(bbox.contains(KDNode.))

    }

    public int getTreesize() {
        return Treesize;
    }


    public boolean isEmpty(){
        return size == 0;
    }

    private void flipDimension() {
        if(this.dimension == Dimension.X) this.dimension = Dimension.Y;
        else this.dimension = Dimension.X;
    }
}
