package bfst19.osmdrawing;

public class Scalebar {

    public static double distance (double startLat, double startLon, double endLat, double endLon){
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

    public static int round(double scale){
        if(scale-Math.floor(scale) < scale-Math.ceil(scale)){
            return (int) Math.floor(scale);
        }else {
            return (int) Math.ceil(scale);
        }
    }

    public static String getScaleText(double startLat, double startLon, double endLat, double endLon, double width){
        double scale = distance(startLat, startLon, endLat,endLon);
        // set the scale. distance mellem 2 punket gang, gør dette for at have målene i forhold til scalebaren
        scale = scale*(100/width);
        if(scale<1){
            return round(scale*1000) +" m";
        }
        return round(scale)+ " km";

    }

}
