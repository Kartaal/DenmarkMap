package bfst19;

class ScaleBar {

    static String getScaleText(float startLat, float startLon, float endLat, float endLon, double width) {
        double scale = Calculator.calculateDistanceInMeters(startLat, startLon, endLat, endLon);

        // set the scale between 2 points to have the measurements in relation to the ScaleBar, the scalebar is 100px wide
        scale = scale * (100 / width);

        if (scale < 1000) {
            return Calculator.round(scale) + " m";
        }

        return Calculator.round(scale / 1000) + " km";
    }
}
