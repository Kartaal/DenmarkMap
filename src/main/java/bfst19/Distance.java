package bfst19;

public class Distance {

    public Distance(){

    }

    public static double distance (double startLat, double startLon, double endLat, double endLon){
        final int EARTH_RADIUS = 6371; // CA. Earth radius in KM
        System.out.println(endLon+" "+startLon);
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

}