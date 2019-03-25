package bfst19.osmdrawing;

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.ArrayList;

//TODO: Refactor to contain arraylist, same rational
public class MultiPolyline extends ArrayList<Polyline> implements Drawable, Serializable, BoundingBoxable {
	private float centerX;
	private float centerY;

	public MultiPolyline(OSMRelation rel) {
		for (OSMWay way : rel){
			Polyline addingLine = new Polyline(way);
			add(addingLine);
			//TODO fix this? probably doesnt work as intended
			this.centerX = addingLine.getCenterX();
			this.centerY = addingLine.getCenterY();
		}

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

		//Code copy pasted to KDTree
		//Arbitrary values that should exceed the coords on Denmark
		double minX = 100, maxX = 0, minY = 100, maxY = 0;
		for(Polyline polyline : this) {
			BoundingBox lineBB = polyline.getBB();
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

	@Override
	public void stroke(GraphicsContext gc) {
		gc.beginPath();
		trace(gc);
		gc.stroke();
	}

	public void trace(GraphicsContext gc) {
		for (Polyline p : this) p.trace(gc);
	}

	@Override
	public void fill(GraphicsContext gc) {
		gc.beginPath();
		trace(gc);
		gc.fill();
	}

}