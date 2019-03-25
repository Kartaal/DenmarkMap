package bfst19.osmdrawing;

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