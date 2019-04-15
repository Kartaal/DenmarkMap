package bfst19;

import javafx.scene.canvas.GraphicsContext;

public interface Drawable {
	void stroke(GraphicsContext gc,double singlePixelLength);
	void fill(GraphicsContext gc,double singlePixelLength);


}