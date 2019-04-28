package bfst19.Line;

import javafx.geometry.Point2D;

import java.io.Serializable;

//TODO make BoundingBoxable for addresses and such on map? Handled through addressParsing?
public class OSMNode implements Serializable {

	protected float lat, lon;
	protected int id;

	public float getLat() {
		return lat;
	}

	public float getLon() {
		return lon;
	}

	public OSMNode(int id, float lon, float lat) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
	}

	public void setId(int id){
		this.id = id;
	}

	public float distanceTo(Point2D point){
		float x = (float) (getLat() - point.getY());
		float y = (float) (getLon() - point.getX());

		return (float)Math.sqrt( Math.pow(x,2) + Math.pow(y,2));
	}

	public int getId() {
		return id;
	}

	public String toString(){
		return id+" "+lat+" "+lon;
	}
}