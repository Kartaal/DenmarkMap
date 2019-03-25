package bfst19.osmdrawing.KDTree;

import bfst19.osmdrawing.BoundingBoxable;
import bfst19.osmdrawing.Drawable;
import javafx.geometry.BoundingBox;

import java.util.*;

public class KDTree {
	KDNode root;

	public KDTree(){
		root = null;
	}

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

	private KDNode insert(KDNode x, BoundingBoxable value, boolean vertical) {
		//If an empty leaf has been reached, create a new KDNode and return
		if(x == null) {
			//Ensure the new node has the correct axis split value
			float splitValue;
			if(vertical) {
				splitValue = value.getCenterY();
			} else {
				splitValue = value.getCenterX();
			}
			List<BoundingBoxable> list = new ArrayList<>();
			list.add(value);

			return new KDNode(list, splitValue, vertical);
		}

		//TODO improve on this, KDNode.getSplit gets the correct dimensional split value
		//Split on x
		if(!vertical) {
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

	public Iterable<Drawable> rangeQuery(BoundingBox bbox) {
		List<Drawable> returnElements = new ArrayList<>();
		rangeQuery(bbox, root, returnElements);
		return returnElements;
	}

	private List<Drawable> rangeQuery(BoundingBox bb, KDNode node, List<Drawable> returnElements) {
		/*if(bbox.contains(KDNode.))*/
		//Ugly casting to Drawable...
		if(bb.contains(root.getBB())) {
			returnElements.add((Drawable) root);
		}
		ArrayList<Drawable> subElements = new ArrayList<>();
		subElements.addAll(rangeQuery(bb, node.nodeL, returnElements));
		subElements.addAll(rangeQuery(bb, node.nodeR, returnElements));

		return subElements;
	}


	public class KDNode {
		private List<BoundingBoxable> values = new ArrayList<>();
		public float split;
		public boolean vertical;
		public KDNode nodeL; //child
		public KDNode nodeR; //child
		private final int listSize = 100;

		public KDNode(List<BoundingBoxable> value, float split, boolean vertical) {
			values.addAll(value);
			this.split = split;
			this.vertical = vertical;
			nodeL = nodeR = null;
		}

		public float getSplit() {
			return split;
		}


		//Returns a bounds object representing the bounding box of all the elements in the node
		public BoundingBox getBB() {

			//Duplicate code from MultiPolyline
			//Arbitrary values that should exceed the coords on Denmark
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

			return new BoundingBox(minX, minY, maxX-minX, maxY-minY);
		}
	}
}
