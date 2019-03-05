package bfst19.osmdrawing;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;

public class Polyline implements Drawable, Serializable {
	//serializable interface works as a pseudo tag that tells the compiler that it can store this objekt as an .obj file
	private float[] coord;

	public Polyline(OSMWay way) {
	    //this gets a pairs of x and y coords from the given way and stores them
		coord = new float[way.size() * 2];
		for (int i = 0 ; i < way.size() ; i++) {
			coord[2*i] = way.get(i).getLon();
			coord[2*i+1] = way.get(i).getLat();
		}
	}

	public void stroke(GraphicsContext gc) {
		gc.beginPath();
		trace(gc);
		gc.stroke();
	}

	public void trace(GraphicsContext gc) {
		//see constructor
	    gc.moveTo(coord[0], coord[1]);
		for (int i = 2 ; i < coord.length ; i+=2) {
			gc.lineTo(coord[i], coord[i+1]);
		}
	}

	public void fill(GraphicsContext gc) {
		gc.beginPath();
		trace(gc);
		gc.fill();
	}


}