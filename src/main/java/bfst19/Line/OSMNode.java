package bfst19.Line;

import javafx.geometry.Point2D;

import java.io.Serializable;

public class OSMNode implements Serializable {

	protected float lat, lon;
	protected int id;

	public OSMNode(int id, float lon, float lat) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
	}

	public float getLat() {
		return lat;
	}

	public float getLon() {
		return lon;
	}

	public double distanceTo(Point2D point) {
		double x = getLat() - point.getY();
		double y = getLon() - point.getX();

		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String toString() {
		return id + " " + lat + " " + lon;
	}
}