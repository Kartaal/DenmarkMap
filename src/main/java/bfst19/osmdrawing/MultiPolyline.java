package bfst19.osmdrawing;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.ArrayList;
//TODO: Refactor to contain arraylist, same rational
public class MultiPolyline extends ArrayList<Polyline> implements Drawable, Serializable {
	public MultiPolyline(OSMRelation rel) {
		for (OSMWay way : rel) add(new Polyline(way));
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