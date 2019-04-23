package bfst19.KDTree;

import javafx.scene.canvas.GraphicsContext;

public interface Drawable {
	void stroke(GraphicsContext gc,double singlePixelLength);
	void fill(GraphicsContext gc,double singlePixelLength);
	float getCenterX();
	float getCenterY();
	double distanceTo(double x, double y);


}