package bfst19.Line;

import bfst19.KDTree.BoundingBox;
import bfst19.KDTree.BoundingBoxable;
import bfst19.KDTree.Drawable;
import bfst19.ResizingArray;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class MultiPolyline implements Drawable, Serializable, BoundingBoxable {
	private ResizingArray<Polyline> lines;
	private float centerX;
	private float centerY;
	private BoundingBox bb;

	public MultiPolyline(OSMRelation rel) {
		lines = new ResizingArray<>();
		for (OSMWay way : rel) {
			Polyline addingLine = new Polyline(way, false);
			add(addingLine);
		}

		//Arbitrary values that should exceed the coords on Denmark
		double minX = 100, maxX = 0, minY = 100, maxY = 0;
		for (Polyline polyline : lines) {
			BoundingBox lineBB = polyline.getBB();

			minX = Double.min(lineBB.getMinX(), minX);
			minY = Double.min(lineBB.getMinY(), minY);
			maxX = Double.max(lineBB.getMaxX(), maxX);
			maxY = Double.max(lineBB.getMaxY(), maxY);
		}

		this.centerX = (float) (minX + maxX) / 2;
		this.centerY = (float) (minY + maxY) / 2;
		bb = new BoundingBox(minX, minY, maxX - minX, maxY - minY);
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

	@Override
	public BoundingBox getBB() {
		return bb;
	}

	@Override
	public void stroke(GraphicsContext gc, double singlePixelLength) {
		gc.beginPath();
		trace(gc, singlePixelLength);
		gc.stroke();
	}

	private void trace(GraphicsContext gc, double singlePixelLength) {
		for (Polyline p : lines) p.trace(gc, singlePixelLength);
	}

	@Override
	public void fill(GraphicsContext gc, double singlePixelLength, double percentOfScreenArea) {
		gc.beginPath();
		double area = (bb.getMaxX() - bb.getMinX()) * (bb.getMaxY() - bb.getMinY());
		if (area < percentOfScreenArea) {
			return;
		}
		trace(gc, singlePixelLength);
		gc.fill();
	}

	@Override
	public OSMNode[] getNodes() {
		ArrayList<OSMNode> nodes = new ArrayList<>();

		for (Polyline line : lines) {
			ArrayList<OSMNode> tempList = new ArrayList<>(Arrays.asList(line.getNodes()));
			nodes.addAll(tempList);
		}

		return nodes.toArray(new OSMNode[nodes.size()]);
	}

}