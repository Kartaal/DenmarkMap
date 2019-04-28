package bfst19.KDTree;

import bfst19.Line.OSMNode;
import bfst19.Route_parsing.ResizingArray;
import javafx.geometry.Point2D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class KDTree implements Serializable {
	private KDNode root;
	private static xComparator xComp = new xComparator();
	private static yComparator yComp = new yComparator();
	private Comparator<BoundingBoxable> selectComp;
	private static final int leafSize = 500;

	public KDTree(){
		root = null;
	}



	//Method for creating a KDTree from a list of Drawable
	public void insertAll(ResizingArray<Drawable> list) {
		//If tree is currently empty, do a lot of work
		if(root == null) {
			//Set xComp as the first comparator
			selectComp = KDTree.xComp;
			//Use a modified QuickSort to ensure the lower values are in the left half
			// and the higher values are in the right half
			sort(list, selectComp);
			//Find the middle index to find the root element
			int splitIndex = list.size() / 2;

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
		//Might want an overloaded version that only sorts a sublist
		sort(list, lo, hi, selectComp);

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

		//Right subtree
		currNode.nodeR = createTree(list, currNode, splitIndex+1, hi);
		currNode.growToEncompassChildren();

		return currNode;
	}


	public OSMNode getNearestNeighbor(Point2D point) {
		//Returns node of the nearest neighbor to a point
		int count = 0;
		float distanceToQueryPoint;
		float closestDistance = Float.POSITIVE_INFINITY;
		OSMNode closestElement = null;
		float x = (float)point.getX();
		float y = (float)point.getY();
		float[] vals = {x, y, 0.0F, 0.0F};
		BoundingBox bbox = new BoundingBox(vals[0], vals[1], vals[2], vals[3]);
		ArrayList<OSMNode> queryList = (ArrayList<OSMNode>) nodeRangeQuery(bbox);


		while(queryList.isEmpty()){
			//While the queryList is empty, the rangequery box should be slightly bigger, increased the range of the rangequery
			//This is done 5000 times, which is an arbitrary value, that should cover a pretty large area, of something like 100km^2
			count++;
			if(count >= 5000){
				return null;
			}
			queryList = growBoundingBox(vals);
		}

		for(OSMNode checkNode: queryList){
			//We check distance from node to point, and then report if its closer than our previously known closest point.
			distanceToQueryPoint = checkNode.distanceTo(point);
			if(distanceToQueryPoint < closestDistance){
				closestDistance = distanceToQueryPoint;
				closestElement = checkNode;
			}
		}

		return closestElement;

	}

	private ArrayList<OSMNode> growBoundingBox(float[] vals) {
		//Take the values of the bounding box, increase them slightly
		BoundingBox bbox;
		ArrayList<OSMNode> queryList;
		//A bounding box is created from a x,y point, and with a width,height from that point.
		//When we decrease the x,y point, we have to add twice that value to width,height to insure it grows by a square
		//TODO: check if it even grows like a square. It might not currently.
		vals[0] -= 0.00001;
		vals[1] -= 0.00001;
		vals[2] += 0.00002;
		vals[3] += 0.00002;

		bbox = new BoundingBox(vals[0], vals[1], vals[2], vals[3]);
		queryList = (ArrayList<OSMNode>) nodeRangeQuery(bbox);
		return queryList;
	}


	//Method for finding elements in the KDTree that intersects a BoundingBox
	public Iterable<Drawable> rangeQuery(BoundingBox bbox) {
		List<Drawable> returnElements = new ArrayList<>();
		rangeQuery(bbox, root, returnElements);
		return returnElements;
	}

	//Recursive checks down through the KDTree
	private List<Drawable> rangeQuery(BoundingBox queryBB, KDNode node, List<Drawable> returnElements) {
		//Return null if current node is null to stop endless recursion
		if (node == null) return null;

		//Ugly casting to Drawable...
		//if we have values, check for each if its BoundingBox intersects our query BoundingBox
		// if true, report it
		if (!node.isEmpty()) {
			for (BoundingBoxable value : node.values) {
				if (queryBB.intersects(value.getBB())) {
					returnElements.add((Drawable) value);
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


	//Method for finding elements in the KDTree that intersects a BoundingBox
	public Iterable<OSMNode> nodeRangeQuery(BoundingBox bbox) {
		List<OSMNode> returnElements = new ArrayList<>();
		nodeRangeQuery(bbox, root, returnElements);
		return returnElements;
	}

	//Recursive checks down through the KDTree
	//Almost equal to rangeQuery(), however this returns a node instead, with a different structure.
	private List<OSMNode> nodeRangeQuery(BoundingBox queryBB, KDNode node, List<OSMNode> returnElements) {
		//Return null if current node is null to stop endless recursion
		if (node == null) return null;

		//Ugly casting to Drawable...
		//if we have values, check for each if its BoundingBox intersects our query BoundingBox
		// if true, report it
		if (!node.isEmpty()) {
			for (BoundingBoxable value : node.values) {
				if (queryBB.intersects(value.getBB())) {
					returnElements.addAll(Arrays.asList(value.getNodes()));
				}
			}
			return returnElements;
		}

		if (node.nodeL != null) {
			//Check whether or not to query left subtree
			if (node.nodeL.bb.intersects(queryBB)) {
				//Make temporary list to keep elements, so null returns don't cause problems
				//Check the left subtree for elements intersecting BoundingBox
				nodeRangeQuery(queryBB, node.nodeL, returnElements);

			}
		}
		if (node.nodeR != null) {
			//Check whether or not to query right subtree
			if (node.nodeR.bb.intersects(queryBB)) {
				//Check the right subtree for elements intersecting BoundingBox
				nodeRangeQuery(queryBB, node.nodeR, returnElements);
			}
		}

		return returnElements;
	}

	//For testing
	public KDNode getRoot() {
		return root;
	}


	/*
	//Not in use currently
	public BoundingBoxable select(List<Drawable> a, int k, Comparator<BoundingBoxable> comp)
	{
		shuffle(a);
		int lo = 0, hi = a.size() - 1;
		while (hi > lo)
		{
			int j = partition(a, lo, hi, comp);
			if (j == k) return (BoundingBoxable) a.get(k);
			else if (j > k) hi = j - 1;
			else if (j < k) lo = j + 1;
		}
		return (BoundingBoxable) a.get(k);
	}
	*/

	//Everything below this line is a modified version of code from Algs4

	//From Algs4 book, modified
	public void sort(ResizingArray<Drawable> a, Comparator<BoundingBoxable> comp) {
		//shuffle(a);
		sort(a, 0, a.size() - 1, comp);
	}

	// quicksort the subarray from a[lo] to a[hi]
	private void sort(ResizingArray<Drawable> a, int lo, int hi, Comparator<BoundingBoxable> comp) {
		if (hi <= lo) return;
		int j = partition(a, lo, hi, comp);
		sort(a, lo, j-1, comp);
		sort(a, j+1, hi, comp);
	}

	//From Algs4 book
	private int partition(ResizingArray<Drawable> a, int lo, int hi, Comparator<BoundingBoxable> comp)
	{ // Partition into a[lo..i-1], a[i], a[i+1..hi].
		int i = lo, j = hi+1; // left and right scan indices
		Drawable v = a.get(lo); // partitioning item
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
