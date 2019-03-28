package bfst19;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;

public class Polyline implements Drawable, Serializable {
	//Serializable interface works as a pseudo tag that tells the compiler
	// it can serialize objects of this type to be saved in .obj files
	private float[] coord;

	public Polyline(OSMWay way) {
	    //Gets a pairs of x and y coords from the given way and stores them in the coord array
		// with lon coordinates stored at even indices and lat coordinates stored at odd indices
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
		//See constructor for explanation
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