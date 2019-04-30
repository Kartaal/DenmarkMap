package bfst19;

import bfst19.Line.OSMNode;
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

    public static float calculateDistanceInMeters(double startLat, double startLon, double endLat, double endLon){
        //Found the formula on https://www.movable-type.co.uk/scripts/latlong.html
        //Basically the same code as is shown on the site mentioned above
        final int R = 6371000; // CA. Earth radius in meters
        double φ1  = Math.toRadians(startLat);
        double φ2  = Math.toRadians(endLat);
        double Δφ  = Math.toRadians(endLat - startLat);
        double Δλ  = Math.toRadians(endLon - startLon);

        double a  = Math.sin(Δφ/2)* Math.sin(Δφ/2) + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ/2) * Math.sin(Δλ/2);
        double c  = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return (float)d;
    }

    public static OSMNode getClosestNode(Point2D point, ArrayList<OSMNode> queryList) {
        //TODO: put into a "Calculator" class.
        double closestDistance = Double.POSITIVE_INFINITY;
        double distanceToQueryPoint;
        OSMNode closestElement = null;

        for(OSMNode checkNode: queryList){
            //We check distance from node to point, and then report if its closer than our previously known closest point.
            distanceToQueryPoint = checkNode.distanceTo(point);
            if(distanceToQueryPoint < closestDistance){
                closestDistance = distanceToQueryPoint;
                closestElement = checkNode;
            }
        }
        return closestElement;
    }
}
