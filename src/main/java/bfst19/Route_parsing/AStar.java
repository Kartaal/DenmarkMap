package bfst19.Route_parsing;

import bfst19.Calculator;

public class AStar {
	public static float Heuristic(Vehicle type, boolean fastestPath, float startLat, float startLon, float endLat, float endLon) {
		float weight;
		if (fastestPath) {
			float distance = Calculator.calculateDistanceInMeters(startLat, startLon, endLat, endLon);
			float maxSpeed = type.maxSpeed;
			weight = distance / maxSpeed;
		} else {
			weight = Calculator.calculateDistanceInMeters(startLat, startLon, endLat, endLon);
		}
		return weight;
	}
}
