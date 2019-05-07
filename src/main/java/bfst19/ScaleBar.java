package bfst19;

public class ScaleBar {

	public static String getScaleText(float startLat, float startLon, float endLat, float endLon, double width) {
		double scale = Calculator.otherDistance(startLat, startLon, endLat, endLon);
		// set the scale between 2 points to have the measurements in relation to the ScaleBar
		// DANSK set the scale. distance mellem 2 punket gang, gør dette for at have målene i forhold til scalebaren. 100 er scalebarens længde. width er MapCanvas.width
		scale = scale * (100 / width);
		if (scale < 1) {
			return Calculator.round(scale * 1000) + " m";
		}
		return Calculator.round(scale) + " km";
	}
}
