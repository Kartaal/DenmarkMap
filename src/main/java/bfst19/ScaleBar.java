package bfst19;

public class ScaleBar {

    // The haversine formula is a  way of computing distances between two points on the surface of a sphere, in this case Earth, using the latitude and longitude
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

    // Round Scale up and down . If math.floor is closer to the scale then choose math.floor otherwise choose ceil
    public static int round(double scale){
        if(scale-Math.floor(scale) < scale-Math.ceil(scale)){
            return (int) Math.floor(scale);
        }else {
            return (int) Math.ceil(scale);
        }
    }

    // Calculate distance. Round scale, return
    public static String getScaleText(double startLat, double startLon, double endLat, double endLon, double width){
        double scale = distance(startLat, startLon, endLat,endLon);
        // set the scale between 2 points to have the measurements in relation to the ScaleBar
        // DANSK set the scale. distance mellem 2 punket gang, gør dette for at have målene i forhold til scalebaren. 100 er scalebarens længde. width er MapCanvas.width
        scale = scale*(100/width);
        if(scale<1){
            return round(scale*1000) +" m";
        }
        return round(scale)+ " km";
    }
}
