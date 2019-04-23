package bfst19.Line;

import java.io.Serializable;
import java.util.function.LongSupplier;

//TODO make BoundingBoxable for addresses and such on map? Handled through addressParsing?
public class OSMNode implements LongSupplier, Serializable {

	protected float lat, lon;
	protected long id;

	public float getLat() {
		return lat;
	}

	public float getLon() {
		return lon;
	}

	public OSMNode(long id, float lon, float lat) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
	}

	public double distanceTo(double x, double y){
		return Math.sqrt((getLat() - y) * (getLat() - y) + (getLon() - x) * (getLon() - x));
	}

	//A functional method given by the LongSupplier interface
	public long getAsLong() {
		return id;
	}

	public String toString(){
		return id+" "+lat+" "+lon;
	}
}