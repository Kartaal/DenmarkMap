package bfst19.KDTree;

import bfst19.BoundingBox;
import bfst19.BoundingBoxable;
import bfst19.Drawable;

import java.io.Serializable;
import java.util.*;

public class KDTree implements Serializable {
	KDNode root;
	private static xComparator xComp = new xComparator();
	private static yComparator yComp = new yComparator();
	private Comparator<BoundingBoxable> selectComp;
	//TODO static?
	private final int listSize = 500;

	//From StdRandom
	private Random random;    // pseudo-random number generator
	private long seed;        // pseudo-random number generator seed

	public KDTree(){
		root = null;

		//From StdRandom
		// this is how the seed was set in Java 1.4
		seed = System.currentTimeMillis();
		random = new Random(seed);
	}

	//Method for creating a KDTree from a list of Drawable
	public void insertAll(List<Drawable> list) {
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
				root = new KDNode(null, splitValue, false);

				//Start recursively creating the left and right subtrees
				// of indexes 0 to splitIndex for left subtree and splitIndex+1 to list.size() for the right subtree
				root.nodeL = createTree(list, root, 0, splitIndex);
				root.nodeR = createTree(list, root, splitIndex + 1, list.size()-1);
			} else {
				//Arbitrary values to fill root in case the list of Drawable is empty
				root = new KDNode(null, -1, false);
			}
		}
	}

	private KDNode createTree(List<Drawable> list, KDNode parentNode, int lo, int hi) {
		//Added to prevent errors when lo == hi (there was a WayType with 2 elements that caused this problem)
		//TODO ensure correctness (still?)
		if (hi <= lo) return null;

		//Change comparator
		//? is a shorthand of if-else. (expression) ? (if expression true) : (if expression false)
		selectComp = selectComp == KDTree.xComp ? KDTree.yComp : KDTree.xComp;
		//Might want an overloaded version that only sorts a sublist
		sort(list, lo, hi, selectComp);

		int splitIndex = lo + (hi-lo) / 2;
		boolean horizontal = !parentNode.horizontal;

		float splitVal;
		if(!horizontal) {
			splitVal = ((BoundingBoxable) list.get(splitIndex)).getCenterX();
		} else {
			splitVal = ((BoundingBoxable) list.get(splitIndex)).getCenterY();
		}

		KDNode currNode = new KDNode(null, splitVal, horizontal);

		//If we have reached a leaf node (list has fewer than listSize elements)
		if(listSize >= hi-lo) {
			List<BoundingBoxable> valueList = new ArrayList<>();
			for(int i = lo ; i < hi ; i++) {
				//Ugly typecast
				valueList.add((BoundingBoxable) list.get(i));
			}
			currNode.setValues(valueList);
			return currNode;
		} else {
			//Left subtree
			parentNode.nodeL = createTree(list, currNode, lo, splitIndex);

			//Right subtree
			parentNode.nodeR = createTree(list, currNode, splitIndex+1, hi);
		}

		return currNode;
	}

	/*
	//Currently never used
	public void insert(BoundingBoxable value){
		if(root == null) {
			List<BoundingBoxable> list = new ArrayList<>();
			list.add(value);
			root = new KDNode(list, value.getCenterX(), false);
		} else {
			//recursive insert, third parameter is for splitting dimension
			// 0 means split is on x-axis, 1 means split is on y-axis
			root = insert(root, value, false);
		}
	}

	private KDNode insert(KDNode x, BoundingBoxable value, boolean horizontal) {
		//If an empty leaf has been reached, create a new KDNode and return
		if(x == null) {
			//Ensure the new node has the correct axis split value
			float splitValue;
			if(horizontal) {
				splitValue = value.getCenterY();
			} else {
				splitValue = value.getCenterX();
			}
			List<BoundingBoxable> list = new ArrayList<>();
			list.add(value);

			return new KDNode(list, splitValue, horizontal);
		}

		//maybe to-do improve on this, KDNode.getSplit gets the correct dimensional split value
		//Split on x
		if(!horizontal) {
			//if current BoundingBoxable has a centerX less than current KDNode
			// recursive insert the BoundingBoxable to left child
			//Otherwise, insert BoundingBoxable to right child
			if(value.getCenterX() <= x.getSplit()) {
				x.nodeL = insert(x.nodeL, value, false);
			} else {
				x.nodeR = insert(x.nodeR, value, false);
			}
		} else {
			if(value.getCenterY() <= x.getSplit()) {
				x.nodeL = insert(x.nodeL, value, true);
			} else {
				x.nodeR = insert(x.nodeR, value, true);
			}
		}

		return x;
	}
	*/

	//Method for finding elements in the KDTree that intersects a BoundingBox
	public Iterable<Drawable> rangeQuery(BoundingBox bbox) {
		Set<Drawable> returnElements = new HashSet<>();
		rangeQuery(bbox, root, returnElements);
		return returnElements;
	}

	//Recursive checks down through the KDTree
	private Set<Drawable> rangeQuery(BoundingBox queryBB, KDNode node, Set<Drawable> returnElements) {
		//Return null if current node is null to stop endless recursion
		if(node == null) return null;

		//Ugly casting to Drawable...
		//if we have values, check for each if its BoundingBox intersects our query BoundingBox
		// if true, report it
		if(node.values != null) {
			for (BoundingBoxable value : node.values) {
				if (queryBB.intersects(value.getBB())) {
					returnElements.add((Drawable) value);
				}
			}
		}

		//Make temporary list to keep elements, so null returns don't cause problems
		//Check the left subtree for elements intersecting BoundingBox
		Set<Drawable> tempList = rangeQuery(queryBB, node.nodeL, returnElements);
		if(tempList != null) {
			returnElements.addAll(tempList);
		}

		//Check the right subtree for elements intersecting BoundingBox
		tempList = rangeQuery(queryBB, node.nodeR, returnElements);
		if(tempList != null) {
			returnElements.addAll(tempList);
		}

		return returnElements;
	}


	public class KDNode implements Serializable{
		public List<BoundingBoxable> values = new ArrayList<>();
		public float split;
		public boolean horizontal; //if false, splits on x
		public KDNode nodeL; //child
		public KDNode nodeR; //child
		public BoundingBox bb;

		public KDNode(List<BoundingBoxable> value, float split, boolean horizontal) {
			if(value != null) {
				values.addAll(value);
			}
			this.split = split;
			this.horizontal = horizontal;
			nodeL = nodeR = null;

			//Create BoundingBox for the KDNode
			makeNodeBB();
		}

		//Sets up some arbitrary values that should be beyond the coords of Denmark
		// Runs through the values in the node to find the encompassing bounding box
		// Creates and sets the bounding box based on the values
		private void makeNodeBB() {
			double minX = 100, maxX = 0, minY = 100, maxY = 0;
			for(BoundingBoxable valueBB : values) {
				BoundingBox lineBB = valueBB.getBB();
				if (lineBB.getMinX() < minX) {
					minX = lineBB.getMinX();
				}
				if (lineBB.getMaxX() > maxX) {
					maxX = lineBB.getMaxX();
				}
				if (lineBB.getMinY() < minY) {
					minY = lineBB.getMinY();
				}
				if (lineBB.getMaxY() > maxY) {
					maxY = lineBB.getMaxY();
				}
			}

			bb = new BoundingBox(minX, minY, maxX-minX, maxY-minY);
		}

		public float getSplit() {
			return split;
		}

		//Returns a BoundingBox object representing the bounding box of all the elements in the node
		public BoundingBox getBB() {
			return bb;
		}

		public void setValues(List<BoundingBoxable> valueList) {
			values = valueList;

			//Create BoundingBox for the KDNode
			makeNodeBB();
		}
	}

	//Innerclass comparator for X dimension
	public static class xComparator implements Comparator<BoundingBoxable>, Serializable {
		//This calculation is made to convert from float to int because Comparator interface requires it
		// Returns a negative integer if a's centerX value is smaller than b's centerX value
		// Returns 0 if a's centerX value is equal to b's centerX value
		// Returns a positive integer if a's centerX value is larger than b's centerX value
		public int compare(BoundingBoxable a, BoundingBoxable b) {
			return (int) (a.getCenterX() - b.getCenterX())*1000000;
		}
	}

	//Innerclass comparator for Y dimension
	public static class yComparator implements Comparator<BoundingBoxable>, Serializable {
		//This calculation is made to convert from float to int because Comparator interface requires it
		// Returns a negative integer if a's centerY value is smaller than b's centerY value
		// Returns 0 if a's centerY value is equal to b's centerY value
		// Returns a positive integer if a's centerY value is larger than b's centerY value
		public int compare(BoundingBoxable a, BoundingBoxable b) {
			return (int) (a.getCenterY() - b.getCenterY())*1000000;
		}
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

	//From Algs4 book, modified
	public void sort(List<Drawable> a, Comparator<BoundingBoxable> comp) {
		shuffle(a);
		sort(a, 0, a.size() - 1, comp);
	}

	// quicksort the subarray from a[lo] to a[hi]
	private void sort(List<Drawable> a, int lo, int hi, Comparator<BoundingBoxable> comp) {
		if (hi <= lo) return;
		int j = partition(a, lo, hi, comp);
		sort(a, lo, j-1, comp);
		sort(a, j+1, hi, comp);
	}

	//From Algs4 book
	private int partition(List<Drawable> a, int lo, int hi, Comparator<BoundingBoxable> comp)
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
	private void exch(List<Drawable> a, int i, int j) {
		Drawable t = a.get(i);
		a.set(i, a.get(j));
		a.set(j, t);
	}

	//From StdRandom
	public void shuffle(List<Drawable> a) {
		int n = a.size();
		for (int i = 0; i < n; i++) {
			int r = i + uniform(n - i);     // between i and n-1
			Drawable temp = a.get(i);
			a.set(i, a.get(r));
			a.set(r, temp);
		}
	}

	//From StdRandom
	public int uniform(int n) {
		if (n <= 0) throw new IllegalArgumentException("argument must be positive: " + n);
		return random.nextInt(n);
	}
}
