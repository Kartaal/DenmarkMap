package bfst19.Line;

import bfst19.KDTree.BoundingBox;
import bfst19.KDTree.BoundingBoxable;
import bfst19.KDTree.Drawable;
import bfst19.Route_parsing.ResizingArray;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class MultiPolyline implements Drawable, Serializable, BoundingBoxable {
	public ResizingArray<Polyline> lines;
	private float centerX;
	private float centerY;
	private BoundingBox bb;

	public MultiPolyline(OSMRelation rel) {
		lines = new ResizingArray<>();
		for (OSMWay way : rel){
			Polyline addingLine = new Polyline(way,false);
			add(addingLine);
		}

		//Find the limits of BoundingBox
		//Code copy pasted to KDTree
		//Arbitrary values that should exceed the coords on Denmark
		float minX = 100, maxX = 0, minY = 100, maxY = 0;
		for(int i = 0 ; i < lines.size() ; i++) {
			BoundingBox lineBB = lines.get(i).getBB();
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

		this.centerX = (minX + maxX) / 2;
		this.centerY = (minY + maxY) / 2;
		bb = new BoundingBox(minX, minY, maxX-minX, maxY-minY);
	}

	private void add(Polyline addingLine) {
		lines.add(addingLine);
	}

	@Override
	public float getCenterX() {
		return centerX;
	}

	@Override
	public float getCenterY() {
		return centerY;
	}


	public double shortestDistance(Point2D point){
		double lineDistance;
		double closestDistance = Double.POSITIVE_INFINITY;
		for(int i = 0 ; i < lines.size() ; i++) {
			lineDistance = lines.get(i).shortestDistance(x, y);
			if(lineDistance < closestDistance){
				closestDistance = lineDistance;
			}
		}
		return closestDistance;
	}

	@Override
	public BoundingBox getBB() {

		return bb;
	}

	@Override
	public void stroke(GraphicsContext gc,double singlePixelLength) {
		gc.beginPath();
		trace(gc,singlePixelLength);
		gc.stroke();
	}

	public void trace(GraphicsContext gc,double singlePixelLength) {
		for (int i = 0; i < lines.size(); i++) {
			lines.get(i).trace(gc, singlePixelLength);
		}
	}

	@Override
	public void fill(GraphicsContext gc, double singlePixelLength,double percentOfScreenArea) {
		gc.beginPath();
		double area = (bb.getMaxX()-bb.getMinX())*(bb.getMaxY()-bb.getMinY());
		if(area<percentOfScreenArea){
			return;
		}
		trace(gc,singlePixelLength);
		gc.fill();
	}

	@Override
	public OSMNode[] getNodes(){
		ArrayList<OSMNode> nodes = new ArrayList<>();

		for (int i = 0; i < lines.size(); i++) {
			ArrayList<OSMNode> tempList = new ArrayList<>(Arrays.asList(lines.get(i).getNodes()));
			nodes.addAll(tempList);
		}
		return nodes.toArray(new OSMNode[nodes.size()]);
	}

}