package bfst19.Route_parsing;

/**
 * Represents all vehicles supported by the current implementation
 * of pathfinding along with AbstractVehicle that can traverse all edges.
 * The maxspeed value represents either the legal maxspeed for the vehicle
 * or an estmation of the max speed for the given vehicle type.
 */
public enum Vehicle {
	ABSTRACTVEHICLE(0), //an abstract vehicle that can traverse any road for getNearestRoad
	CAR(130),
	WALKING(10),
	BIKE(20);

	int maxSpeed;

	Vehicle(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

}
