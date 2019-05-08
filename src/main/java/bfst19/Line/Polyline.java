package bfst19.Line;


import bfst19.KDTree.BoundingBox;
import bfst19.KDTree.BoundingBoxable;
import bfst19.KDTree.Drawable;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;

public class Polyline implements Drawable, Serializable, BoundingBoxable {

	private final float centerX, centerY;
	private float[] coord;
	private OSMNode[] nodes;
	private BoundingBox bb;

	public Polyline(OSMWay way, boolean isOSMWay) {

		float xMin = way.get(0).getLon();
		float xMax = way.get(0).getLon();
		float yMin = way.get(0).getLat();
		float yMax = way.get(0).getLat();

		//this may be ugly, but allows for OSMNode garbage collection
		if (isOSMWay) {
			nodes = new OSMNode[way.size()];

			for (int i = 0; i < way.size(); i++) {
				nodes[i] = way.get(i);

				xMin = Math.min(xMin, way.get(i).getLon());
				xMax = Math.max(xMax, way.get(i).getLon());
				yMin = Math.min(yMin, way.get(i).getLat());
				yMax = Math.max(yMax, way.get(i).getLat());
			}

		} else {
			coord = new float[way.size() * 2];

			for (int i = 0; i < way.size(); i++) {
				coord[2 * i] = way.get(i).getLon();
				coord[2 * i + 1] = way.get(i).getLat();

				xMin = Math.min(xMin, way.get(i).getLon());
				xMax = Math.max(xMax, way.get(i).getLon());
				yMin = Math.min(yMin, way.get(i).getLat());
				yMax = Math.max(yMax, way.get(i).getLat());
			}
		}

		this.centerX = (xMin + xMax) / 2;
		this.centerY = (yMin + yMax) / 2;

		bb = new BoundingBox((double) xMin, (double) yMin, (xMax - xMin), (double) (yMax - yMin));
	}

	public float getCenterX() {
		return centerX;
	}

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

	void trace(GraphicsContext gc, double singlePixelLength) {
		if (nodes == null) {

			gc.moveTo(coord[0], coord[1]);
			float previousX = coord[0];
			float previousY = coord[1];

			for (int i = 2; i < coord.length; i += 2) {
				if (Math.sqrt(Math.pow(Math.abs(coord[i] - previousX), 2) +
						Math.pow(Math.abs(coord[i + 1] - previousY), 2)) > singlePixelLength) {
					previousX = coord[i];
					previousY = coord[i + 1];
					gc.lineTo(coord[i], coord[i + 1]);
				}
			}

		} else {
			OSMNode firstnode = nodes[0];
			gc.moveTo(firstnode.getLon(), firstnode.getLat());

			float previousX = firstnode.getLon();
			float previousY = firstnode.getLat();

			for (int i = 1; i < nodes.length; i++) {
				OSMNode currentNode = nodes[i];
				if (Math.sqrt(Math.pow(Math.abs(currentNode.getLon() - previousX), 2) +
						Math.pow(Math.abs(currentNode.getLat() - previousY), 2)) > singlePixelLength) {
					previousX = currentNode.getLon();
					previousY = currentNode.getLat();
					gc.lineTo(previousX, previousY);
				}
			}

		}
	}

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
		return nodes;
	}

}
