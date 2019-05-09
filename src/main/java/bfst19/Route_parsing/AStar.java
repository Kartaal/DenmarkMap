package bfst19.Route_parsing;

import bfst19.Calculator;

/**
 * This class is for calculating the heuristic for A* based exploration of
 * the EdgeWeightedDigraph. It uses a startpoint and an endpoint to calculate
 * the distance of a straight shot between them, and either returns that distance
 * or that distance divided by the vehicle's maxspeed.
 * This heuristic is then used to guide the exploration of the graph in the Dijstra
 * algorithm to reduce the amount exploration required to find a path.
 */
class AStar {
    static float Heuristic(Vehicle type, boolean fastestPath, float startLat, float startLon, float endLat, float endLon) {
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
