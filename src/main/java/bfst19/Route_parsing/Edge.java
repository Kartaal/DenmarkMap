package bfst19.Route_parsing;

import bfst19.Line.OSMNode;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Represents a relation between two nodes in the digraph,
 * thus it holds the two nodes that are connected, a length, a name, and a speedlimit.
 * <p>
 * Very similar to algs4's DirectedEdge class, with modifications
 * to accommodate two different weights depending on if it's a fastest path
 * or not. The most notable difference that is not traversabillity and weight
 * is that one edge represents all relations between two points, and therefore
 * includes a drivabillity for each supported vehicle type.
 * This does complicate getting the tail/head of any edge, as it's dependent on
 * the direction of traversal.
 */
public class Edge implements Serializable {
	private String name;
	private float length;
	private int speedLimit;
	private Drivabillity[] drivabillity;
	private OSMNode v;
	private OSMNode w;


	public Edge(float length, int speedLimit, OSMNode v, OSMNode w,
				String name, HashMap<Vehicle, Drivabillity> vehicleTypeToDrivable) {
		if (v.getId() < 0) throw new IllegalArgumentException("vertex index must be a non-negative integer");
		if (w.getId() < 0) throw new IllegalArgumentException("vertex index must be a non-negative integer");
		if (Double.isNaN(length)) throw new IllegalArgumentException("Weight is NaN");
		if (Double.isNaN(speedLimit)) throw new IllegalArgumentException("Weight is NaN");

		this.length = length;
		this.speedLimit = speedLimit;
		this.v = v;
		this.w = w;
		this.name = name;

		drivabillity = new Drivabillity[vehicleTypeToDrivable.keySet().size()];
		drivabillity[0] = vehicleTypeToDrivable.get(Vehicle.CAR);
		drivabillity[1] = vehicleTypeToDrivable.get(Vehicle.WALKING);
		drivabillity[2] = vehicleTypeToDrivable.get(Vehicle.BIKE);
	}

	double getWeight(Vehicle type, boolean fastestPath) {

		if (fastestPath) {

			if (type.maxSpeed < speedLimit) {
				return getLength() / type.maxSpeed;
			} else {
				return getLength() / speedLimit;
			}

		} else {
			return getLength();
		}
	}

	public double getLength() {
		return length;
	}

	public String getName() {
		return name;
	}

	public OSMNode either() {
		return v;
	}

	public OSMNode other() {
		return w;
	}

	int getOtherEnd(int id) {
		if (id == w.getId()) {
			return v.getId();
		}
		return w.getId();
	}

	OSMNode getThisEndNode(int id) {
		if (id == w.getId()) {
			return w;
		} else {
			return v;
		}
	}

	public OSMNode getOtherEndNode(OSMNode node) {
		if (node.getId() == w.getId()) {
			return v;
		} else {
			return w;
		}
	}

	boolean isForwardAllowed(Vehicle type, int id) {
		Drivabillity drivable = getDrivableFromVehicleType(type);

		if (type == Vehicle.ABSTRACTVEHICLE) {
			return true;
		}

		if (drivable == Drivabillity.BOTHWAYS) {
			return true;
		}
		else if (v.getId() == id) {
			return drivable == Drivabillity.FORWARD;
		}
		else if (w.getId() == id) {
			return drivable == Drivabillity.BACKWARD;
			//this will actually never happen, due to the dataset structure
		}
		return false;
	}

	private Drivabillity getDrivableFromVehicleType(Vehicle type) {

		if (type == Vehicle.CAR) {
			return drivabillity[0];
		}
		else if (type == Vehicle.WALKING) {
			return drivabillity[1];
		}
		else if (type == Vehicle.BIKE) {
			return drivabillity[2];
		}

		return Drivabillity.NOWAY;
	}

	@Override
	public String toString() {
		return "name: " + name + " Length: " + length + "m " + " bike: " + getDrivableFromVehicleType(Vehicle.BIKE) +
				" Car: " + getDrivableFromVehicleType(Vehicle.CAR) +
				" walking: " + getDrivableFromVehicleType(Vehicle.WALKING) + " " +
				speedLimit + " Node v:" + v.toString() + " Node W:" + w.toString();
	}
}

