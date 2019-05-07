package bfst19.KDTree;

import bfst19.Calculator;
import bfst19.Line.OSMNode;
import bfst19.Model;
import bfst19.ResizingArray;
import bfst19.Route_parsing.RouteHandler;
import bfst19.Route_parsing.Vehicle;
import javafx.geometry.Point2D;
import java.io.Serializable;
import java.util.*;

/**
 * KDTree stores spatial data in KDNodes.
 * The constructor only initialises an empty tree which is filled with useful data
 * by using the insertAll() method.
 * The method rangeQuery() searches through the tree.
 * nodeRangeQuery() finds Drivable OSMNodes for a given vehicle type and bounding box
 * Something about nodeRangeQuery() ?????????
 * This tree also supports finding the nearest neighbor with getNearestNeighbor().
 */
public class KDTree implements Serializable {
    private KDNode root;
    private static xComparator xComp = new xComparator();
    private static yComparator yComp = new yComparator();
    private Comparator<BoundingBoxable> selectComp;
    private static final int leafSize = 500;

    public KDTree(){
        root = null;
    }

    /**
     * Inserts a ResizingArray of Drawable objects into the tree.
     * If the ResizingArray is empty, only the root of the tree is created and
     * its fields are set to arbitrary wrong values (assuming only Denmark or a subset
     * is parsed).
     * @param list	The ResizingArray of Drawable objects to be inserted into the tree.
     */
    //Method for creating a KDTree from a list of Drawable
    public void insertAll(ResizingArray<Drawable> list) {
        //If tree is currently empty, do a lot of work
        if(root == null) {
            //Set xComp as the first comparator
            selectComp = KDTree.xComp;

            //Find the middle index to find the root element
            int splitIndex = list.size() / 2;

            //Return value not used, select is only meant to partially sort the list
            select(list, splitIndex, 0, list.size()-1, selectComp);

            //Ensure that our Drawable list is not empty
            if(list.size() > 0) {
                //TODO figure out something about all these typecasts
                //Find the comparator correct value of the middle element (Root so X value)
                float splitValue = ((BoundingBoxable) list.get(splitIndex)).getCenterX();
                root = new KDNode(splitValue, true);

                //Start recursively creating the left and right subtrees
                // of indexes 0 to splitIndex for left subtree and splitIndex+1 to list.size() for the right subtree
                root.nodeL = createTree(list, root, 0, splitIndex);
                root.nodeR = createTree(list, root, splitIndex + 1, list.size()-1);
                root.growToEncompassChildren();
            } else {
                //Arbitrary values to fill root in case the list of Drawable is empty
                root = new KDNode(-1, true);
                root.setBB(0,0,0,0);
            }

        }
    }

    private KDNode createTree(ResizingArray<Drawable> list, KDNode parentNode, int lo, int hi) {
        //Added to prevent errors when lo == hi (there was a WayType with 2 elements that caused this problem)
        //TODO ensure correctness (still?)
        if (hi < lo) return null;

		//Get the index to split at
		int splitIndex = lo + (hi-lo) / 2;
		//Flip the dimension to handle 2D data
		boolean vertical = !parentNode.vertical;

        //Change comparator based on vertical
        //? is a shorthand of if-else. (expression) ? (if expression true) : (if expression false)
        selectComp = vertical ? KDTree.xComp : KDTree.yComp;

        //Return value not used, select is only meant to partially sort the list
        select(list, splitIndex, lo, hi, selectComp);

		//Figure out the splitting value based on dimension
		float splitVal;
        if(vertical) {
            splitVal = ((BoundingBoxable) list.get(splitIndex)).getCenterX();
        } else {
            splitVal = ((BoundingBoxable) list.get(splitIndex)).getCenterY();
        }

        //Create a new node to be returned
        KDNode currNode = new KDNode(splitVal, vertical);

        //If we have reached a leaf node (list has fewer than leafSize elements)
        // fill the node with data to be retrieved later
        if(leafSize >= hi-lo) {
            List<BoundingBoxable> valueList = new ArrayList<>();
            for(int i = lo ; i <= hi ; i++) {
                //Ugly typecast
                valueList.add((BoundingBoxable) list.get(i));
            }
            currNode.setValues(valueList);
            return currNode;
        }
        //Do recursion because node isn't a leaf
        //Left subtree
        currNode.nodeL = createTree(list, currNode, lo, splitIndex);
        currNode.nodeR = createTree(list, currNode, splitIndex+1, hi);
        currNode.growToEncompassChildren();

        return currNode;
    }

    /**
     * Retrieves the nearest neighbor by making a tiny BoundingBox around
     * a point and iteratively increasing its size if no elements are found.
     * Once any amount of elements have been found, the OSMNode in a KDNode
     * with the closest Euclidean distance is returned.
     * @param point	The point used for finding the closest element in the tree.
     * @param type The vehicle type to find driveable roads for.
     * @return		The OSMNode that has the shortest Euclidean distance to the point.
     */
    public OSMNode getNearestNeighbor(Point2D point, Vehicle type) {
        //Returns node of the nearest neighbor to a point
        int count = 0;
        OSMNode closestElement;
        float x = (float)point.getX();
        float y = (float)point.getY();
        float[] vals = {x, y, 0.0F, 0.0F};  //infinitesimal values to make a square rangequery call
        BoundingBox bbox = new BoundingBox(vals[0], vals[1], vals[2], vals[3]); //generate boundingbox with aforementioned values
        ResizingArray<OSMNode> queryList = nodeRangeQuery(bbox, type);

        while(queryList.isEmpty()){
            //While the queryList is empty, the rangequery box should be slightly bigger, increased the range of the rangequery
            //This is done 5000 times, which is an arbitrary value, that should cover a pretty large area, of something like 100km^2
            count++;
            if(count >= 5000){
                return null;
            }
            queryList = growBoxNRQ(vals, type);
        }

        closestElement = Calculator.getClosestNode(point, queryList);

        return closestElement;

    }

    private BoundingBox growBoundingBox(float[] vals) {
        //Take the values of the bounding box, increase them slightly
        //A bounding box is created from a x,y point, and with a width,height from that point.
        //When we decrease the x,y point, we have to add twice that value to width, height to insure it grows by a square
        vals[0] -= 0.00001 / Model.getLonfactor();
        vals[1] -= 0.00001;
        vals[2] += 0.00002 / Model.getLonfactor();
        vals[3] += 0.00002;
        return new BoundingBox(vals[0], vals[1], vals[2], vals[3]);
    }

    private ResizingArray<OSMNode> growBoxNRQ(float[] vals, Vehicle type){
        //Grows the bounding box, and performs nodeRangeQuery to it.
        ResizingArray<OSMNode> queryList;
        BoundingBox bbox = growBoundingBox(vals);
        queryList = nodeRangeQuery(bbox, type);
        return queryList;
    }


    /**
     * Searches through the tree for the stored Drawable elements that lie
     * within the given BoundingBox.
     * @param bbox	A BoundingBox that intersects all the elements returned.
     * @return		A ResizingArray of elements intersecting the bbox BoundingBox.
     */
    //Method for finding elements in the KDTree that intersects a BoundingBox
    public ResizingArray<Drawable> rangeQuery(BoundingBox bbox) {
        ResizingArray<Drawable> returnElements = new ResizingArray<>();
        rangeQuery(bbox, root, returnElements);
        return returnElements;
    }

    //Recursive checks down through the KDTree
    private ResizingArray<Drawable> rangeQuery(BoundingBox queryBB, KDNode node, ResizingArray<Drawable> returnElements) {
        //Return null if current node is null to stop endless recursion
        if (node == null) return null;

        //Ugly casting to Drawable...
        //if we have values, check for each if its BoundingBox intersects our query BoundingBox
        // if true, report it
        if (!node.isEmpty()) {
            for (BoundingBoxable value : node.values) {
                if (queryBB.intersects(value.getBB())) {
                    returnElements.add(value);
                }
            }
            return  returnElements;
        }

        if (node.nodeL != null) {
            //Check whether or not to query left subtree
            if (node.nodeL.bb.intersects(queryBB)) {
                //Make temporary list to keep elements, so null returns don't cause problems
                //Check the left subtree for elements intersecting BoundingBox
                rangeQuery(queryBB, node.nodeL, returnElements);
            }
        }
        if (node.nodeR != null) {
            //Check whether or not to query right subtree
            if (node.nodeR.bb.intersects(queryBB)) {
                //Check the right subtree for elements intersecting BoundingBox
                rangeQuery(queryBB, node.nodeR, returnElements);
            }
        }

        return returnElements;
    }

    /**
     * Searches through the tree for the OSMNode objects inside the Drawable elements that lie
     *  within the given BoundingBox.
     * @param bbox  A BoundingBox that intersects all the elements returned.
     * @param type  A vehicle type to check traversability on each OSMNode in the Drawable elements.
     * @return	    A ResizingArray of OSMNodes intersecting the bbox BoundingBox
     */
    //Method for finding elements in the KDTree that intersects a BoundingBox
    public ResizingArray<OSMNode> nodeRangeQuery(BoundingBox bbox, Vehicle type) {
        ResizingArray<OSMNode> returnElements = new ResizingArray<>();
        nodeRangeQuery(bbox, root, returnElements, type);
        return returnElements;
    }

    //Recursive checks down through the KDTree
    //Almost equal to rangeQuery(), however this returns a node instead, with a different structure.
    private ResizingArray<OSMNode> nodeRangeQuery(BoundingBox queryBB, KDNode node, ResizingArray<OSMNode> returnElements, Vehicle type) {
        //Return null if current node is null to stop endless recursion
        if (node == null) return null;

        //Ugly casting to Drawable...
        //if we have values, check for each if its BoundingBox intersects our query BoundingBox
        // if true, report it
        if (!node.isEmpty()) {
            for (BoundingBoxable value : node.values) {
                if (queryBB.intersects(value.getBB())) {
                    for(OSMNode queryNode: value.getNodes()){
                        if(RouteHandler.isTraversableNode(queryNode,type)){
                            returnElements.add(queryNode);
                        }
                    }
                }
            }
            return returnElements;
        }

        if (node.nodeL != null) {
            //Check whether or not to query left subtree
            if (node.nodeL.bb.intersects(queryBB)) {
                //Make temporary list to keep elements, so null returns don't cause problems
                //Check the left subtree for elements intersecting BoundingBox
                nodeRangeQuery(queryBB, node.nodeL, returnElements, type);

            }
        }
        if (node.nodeR != null) {
            //Check whether or not to query right subtree
            if (node.nodeR.bb.intersects(queryBB)) {
                //Check the right subtree for elements intersecting BoundingBox
                nodeRangeQuery(queryBB, node.nodeR, returnElements, type);
            }
        }

        return returnElements;
    }

    //For testing
    public KDNode getRoot() {
        return root;
    }

    public BoundingBoxable select(ResizingArray<Drawable> a, int k, int lo, int hi, Comparator<BoundingBoxable> comp)
    {
        if(a.isEmpty()) {
            return null;
        }

        while (hi > lo)
        {
            int j = partition(a, lo, hi, comp);
            if (j == k) return (BoundingBoxable) a.get(k);
            else if (j > k) hi = j - 1;
            else if (j < k) lo = j + 1;
        }
        return (BoundingBoxable) a.get(k);
    }

    //Everything below this line is a modified version of code from Algs4
    //From Algs4 book
    private int partition(ResizingArray<Drawable> a, int lo, int hi, Comparator<BoundingBoxable> comp)
    { // Partition into a[lo..i-1], a[i], a[i+1..hi].
        int i = lo, j = hi+1; // left and right scan indices
        Random rand = new Random();
        int diff = lo < hi ? hi - lo : lo - hi;
        int pIndex = lo + rand.nextInt(diff);
        Drawable v = a.get(pIndex);
        //Drawable v = a.get(lo); // partitioning item
        while (true)
        { // Scan right, scan left, check for scan complete, and exchange.
            while (comp.compare((BoundingBoxable) a.get(++i), (BoundingBoxable) v) > 0) if (i == hi) break;
            while (comp.compare((BoundingBoxable) v, (BoundingBoxable)  a.get(--j)) < 0) if (j == lo) break;
            if (i >= j) break;
            exch(a, i, j);
        }
        exch(a, lo, j); // Put v = a[j] into position
        return j; // with a[lo..j-1] <= a[j] <= a[j+1..hi].
    }

	//From Algs4 book
	private void exch(ResizingArray<Drawable> a, int i, int j) {
		Drawable t = a.get(i);
		a.set(i, a.get(j));
		a.set(j, t);
	}
}
