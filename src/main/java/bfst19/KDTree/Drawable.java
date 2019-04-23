package bfst19.KDTree;

import javafx.scene.canvas.GraphicsContext;

public interface Drawable {
	void stroke(GraphicsContext gc,double singlePixelLength);
	void fill(GraphicsContext gc,double singlePixelLength);
	double shortestDistance(double x, double y);


}