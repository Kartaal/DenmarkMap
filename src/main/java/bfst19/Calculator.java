package bfst19;

import bfst19.Line.OSMNode;
import bfst19.Route_parsing.ResizingArray;
import javafx.geometry.Point2D;

import java.util.ArrayList;

public class Calculator {
    public static float angleBetween2Lines(OSMNode A1, OSMNode A2, OSMNode B1, OSMNode B2) {
        float angle1 = (float) Math.atan2(A2.getLat() - A1.getLat(), A1.getLon() - A2.getLon());
        float angle2 = (float) Math.atan2(B2.getLat() - B1.getLat(), B1.getLon() - B2.getLon());
        float calculatedAngle = (float) Math.toDegrees(angle1 - angle2);
        if (calculatedAngle < 0) calculatedAngle += 360;
        return calculatedAngle;
    }

    public static float calculateDistanceInMeters(float startLat, float startLon, float endLat, float endLon){
        //Found the formula on https://www.movable-type.co.uk/scripts/latlong.html
        //Basically the same code as is shown on the site mentioned above
        final int R = 6371000; // CA. Earth radius in meters
        double φ1  = Math.toRadians(startLat);
        double φ2  = Math.toRadians(endLat);
        double Δφ  = Math.toRadians(endLat - startLat);
        double Δλ  = Math.toRadians(endLon - startLon);

        float a  = (float) (Math.sin(Δφ/2)* Math.sin(Δφ/2) + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ/2) * Math.sin(Δλ/2));
        float c  = (float) (2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a)));
        float d = R * c;
        return d;
    }

    //todo figure out the difference between this and the other one, and why they give different results, should

    // The haversine formula is a  way of computing distances between two points on the surface of a sphere, in this case Earth, using the latitude and longitude
    public static double otherDistance (double startLat, double startLon, double endLat, double endLon){
        final int EARTH_RADIUS = 6371; // CA. Earth radius in KM
        double deltaLat  = Math.toRadians((endLat - startLat));
        double deltaLon = Math.toRadians((endLon - startLon));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        double a = haversin(deltaLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(deltaLon);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }


    private static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    public static OSMNode getClosestNode(Point2D point, ResizingArray<OSMNode> queryList) {
        double closestDistance = Double.POSITIVE_INFINITY;
        double distanceToQueryPoint;
        OSMNode closestElement = null;

        for(int i = 0; i < queryList.size(); i++){
            //We check distance from node to point, and then report if its closer than our previously known closest point.
            distanceToQueryPoint = queryList.get(i).distanceTo(point);
            if(distanceToQueryPoint < closestDistance){
                closestDistance = distanceToQueryPoint;
                closestElement = queryList.get(i);
            }
        }
        return closestElement;
    }

    public static int round(double length){
        if(length-Math.floor(length) < length-Math.ceil(length)){
            return (int) Math.floor(length);
        }else {
            return (int) Math.ceil(length);
        }
    }
}
