package bfst19.KDTree;

import bfst19.Calculator;
import bfst19.Line.OSMNode;
import bfst19.Model;
import bfst19.ResizingArray;
import bfst19.Route_parsing.RouteHandler;
import bfst19.Route_parsing.Vehicle;
import javafx.geometry.Point2D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * KDTree stores spatial data in KDNodes.
 * The constructor only initialises an empty tree which is filled with useful data
 * by using the insertAll() method.
 * The method rangeQuery() searches through the tree.
 * nodeRangeQuery() finds Drivable OSMNodes for a given vehicle type and bounding box.
 * This tree also supports finding the nearest neighbor with getNearestNeighbor().
 */
public class KDTree implements Serializable {
	private static final int leafSize = 500;
	private static xComparator xComp = new xComparator();
	private static yComparator yComp = new yComparator();
	private KDNode root;
	private Comparator<BoundingBoxable> selectComp;

	public KDTree() {
		root = null;
	}

	/**
	 * Inserts a ResizingArray of Drawable objects into the tree.
	 * If the ResizingArray is empty, only the root of the tree is created and
	 * its fields are set to arbitrary wrong values (assuming only Denmark or a subset
	 * is parsed).
	 *
	 * @param list The ResizingArray of Drawable objects to be inserted into the tree.
	 */
	//Method for creating a KDTree from a list of Drawable
	public void insertAll(ResizingArray<Drawable> list) {
		if (root == null) {
			selectComp = KDTree.xComp;

			int splitIndex = list.size() / 2;

			select(list, splitIndex, 0, list.size() - 1, selectComp);

			if (list.size() > 0) {

				float splitValue = ((BoundingBoxable) list.get(splitIndex)).getCenterX();
				root = new KDNode(splitValue, true);

				//Start recursively creating the left and right subtrees
				// of indexes 0 to splitIndex for left subtree and splitIndex+1 to list.size() for the right subtree
				root.nodeL = createTree(list, root, 0, splitIndex);
				root.nodeR = createTree(list, root, splitIndex + 1, list.size() - 1);
				root.growToEncompassChildren();
			} else {
				//Arbitrary values to fill root in case the list of Drawable is empty
				root = new KDNode(-1, true);
				root.setBB(0, 0, 0, 0);
			}

		}
	}

	private KDNode createTree(ResizingArray<Drawable> list, KDNode parentNode, int lo, int hi) {

		if (hi < lo) return null;

		int splitIndex = lo + (hi - lo) / 2;

		boolean vertical = !parentNode.vertical;

		//? is a shorthand of if-else. (expression) ? (if expression true) : (if expression false)
		selectComp = vertical ? KDTree.xComp : KDTree.yComp;

		select(list, splitIndex, lo, hi, selectComp);

		float splitVal;
		if (vertical) {
			splitVal = ((BoundingBoxable) list.get(splitIndex)).getCenterX();
		} else {
			splitVal = ((BoundingBoxable) list.get(splitIndex)).getCenterY();
		}

		//Create a new node to be returned
		KDNode currNode = new KDNode(splitVal, vertical);

		if (leafSize >= hi - lo) {
			List<BoundingBoxable> valueList = new ArrayList<>();
			for (int i = lo; i <= hi; i++) {
				valueList.add((BoundingBoxable) list.get(i));
			}
			currNode.setValues(valueList);
			return currNode;
		}

		//Do recursion because node isn't a leaf
		currNode.nodeL = createTree(list, currNode, lo, splitIndex);
		currNode.nodeR = createTree(list, currNode, splitIndex + 1, hi);
		currNode.growToEncompassChildren();

		return currNode;
	}

	/**
	 * Retrieves the nearest neighbor by making a tiny BoundingBox around
	 * a point and iteratively increasing its size if no elements are found.
	 * Once any amount of elements have been found, the OSMNode in a KDNode
	 * with the closest Euclidean distance is returned.
	 *
	 * @param point The point used for finding the closest element in the tree.
	 * @param type  The vehicle type to find driveable roads for.
	 * @return The OSMNode that has the shortest Euclidean distance to the point.
	 */
	public OSMNode getNearestNeighbor(Point2D point, Vehicle type) {

		int count = 0;
		OSMNode closestElement;
		float x = (float) point.getX();
		float y = (float) point.getY();
		float[] vals = {x, y, 0.0F, 0.0F};  //infinitesimal values to make a square rangequery call
		BoundingBox bbox = new BoundingBox(vals[0], vals[1], vals[2], vals[3]);
		ResizingArray<OSMNode> queryList = nodeRangeQuery(bbox, type);

		while (queryList.isEmpty()) {
			//This is done 5000 times, which is an arbitrary value, that should cover about 10x10km
			count++;
			if (count >= 5000) {
				return null;
			}
			queryList = growBoxNRQ(vals, type);
		}

		closestElement = Calculator.getClosestNode(point, queryList);

		return closestElement;
	}

	private BoundingBox growBoundingBox(float[] vals) {
		//When we decrease the x,y point, we have to add twice that value to width, height to insure it grows by a square
		vals[0] -= 0.00001 / Model.getLonfactor();
		vals[1] -= 0.00001;
		vals[2] += 0.00002 / Model.getLonfactor();
		vals[3] += 0.00002;
		return new BoundingBox(vals[0], vals[1], vals[2], vals[3]);
	}

	private ResizingArray<OSMNode> growBoxNRQ(float[] vals, Vehicle type) {

		ResizingArray<OSMNode> queryList;
		BoundingBox bbox = growBoundingBox(vals);

		queryList = nodeRangeQuery(bbox, type);
		return queryList;
	}


	/**
	 * Searches through the tree for the stored Drawable elements that lie
	 * within the given BoundingBox.
	 *
	 * @param bbox A BoundingBox that intersects all the elements returned.
	 * @return A ResizingArray of elements intersecting the bbox BoundingBox.
	 */
	public ResizingArray<Drawable> rangeQuery(BoundingBox bbox) {
		ResizingArray<Drawable> returnElements = new ResizingArray<>();
		rangeQuery(bbox, root, returnElements);
		return returnElements;
	}


	private void rangeQuery(BoundingBox queryBB, KDNode node, ResizingArray<Drawable> returnElements) {

		if (node == null) return;

		if (node.isEmpty()) {
			for (BoundingBoxable value : node.values) {
				if (queryBB.intersects(value.getBB())) {
					returnElements.add(value);
				}
			}
			return;
		}

		if (node.nodeL != null) {
			if (node.nodeL.bb.intersects(queryBB)) {
				rangeQuery(queryBB, node.nodeL, returnElements);
			}
		}
		if (node.nodeR != null) {
			if (node.nodeR.bb.intersects(queryBB)) {
				rangeQuery(queryBB, node.nodeR, returnElements);
			}
		}
	}

	/**
	 * Searches through the tree for the OSMNode objects inside the Drawable elements that lie
	 * within the given BoundingBox.
	 *
	 * @param bbox A BoundingBox that intersects all the elements returned.
	 * @param type A vehicle type to check traversability on each OSMNode in the Drawable elements.
	 * @return A ResizingArray of OSMNodes intersecting the bbox BoundingBox
	 */
	private ResizingArray<OSMNode> nodeRangeQuery(BoundingBox bbox, Vehicle type) {
		ResizingArray<OSMNode> returnElements = new ResizingArray<>();
		nodeRangeQuery(bbox, root, returnElements, type);
		return returnElements;
	}

	//Almost equal to rangeQuery(), however this returns a node instead, with a different structure.
	private void nodeRangeQuery(BoundingBox queryBB, KDNode node, ResizingArray<OSMNode> returnElements, Vehicle type) {

		if (node == null) return;

		if (node.isEmpty()) {
			for (BoundingBoxable value : node.values) {
				if (queryBB.intersects(value.getBB())) {
					for (OSMNode queryNode : value.getNodes()) {
						if (RouteHandler.isTraversableNode(queryNode, type)) {
							returnElements.add(queryNode);
						}
					}
				}
			}
			return;
		}

		if (node.nodeL != null) {
			if (node.nodeL.bb.intersects(queryBB)) {
				nodeRangeQuery(queryBB, node.nodeL, returnElements, type);

			}
		}
		if (node.nodeR != null) {
			if (node.nodeR.bb.intersects(queryBB)) {
				nodeRangeQuery(queryBB, node.nodeR, returnElements, type);
			}
		}
	}

	//For testing
	public KDNode getRoot() {
		return root;
	}

	//used for partial sorting
	private void select(ResizingArray<Drawable> a, int k, int lo, int hi, Comparator<BoundingBoxable> comp) {
		if (a.isEmpty()) {
			return;
		}

		while (hi > lo) {
			int j = partition(a, lo, hi, comp);
			if (j == k) {
				a.get(k);
				return;
			} else if (j > k) hi = j - 1;
			else lo = j + 1;
		}
		a.get(k);
	}

	//Everything below this line is a modified version of code from Algs4
	private int partition(ResizingArray<Drawable> a, int lo, int hi, Comparator<BoundingBoxable> comp) { // Partition into a[lo..i-1], a[i], a[i+1..hi].
		int i = lo, j = hi + 1; // left and right scan indices
		Random rand = new Random();
		int diff = lo < hi ? hi - lo : lo - hi;
		int pIndex = lo + rand.nextInt(diff);
		Drawable v = a.get(pIndex);
		while (true) { // Scan right, scan left, check for scan complete, and exchange.
			while (comp.compare((BoundingBoxable) a.get(++i), (BoundingBoxable) v) > 0) if (i == hi) break;
			while (comp.compare((BoundingBoxable) v, (BoundingBoxable) a.get(--j)) < 0) if (j == lo) break;
			if (i >= j) break;
			exch(a, i, j);
		}
		exch(a, lo, j); // Put v = a[j] into position
		return j; // with a[lo..j-1] <= a[j] <= a[j+1..hi].
	}

	private void exch(ResizingArray<Drawable> a, int i, int j) {
		Drawable t = a.get(i);
		a.set(i, a.get(j));
		a.set(j, t);
	}
}
