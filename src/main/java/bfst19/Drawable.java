package bfst19;

import javafx.scene.canvas.GraphicsContext;

public interface Drawable {
	public void stroke(GraphicsContext gc);
	public void fill(GraphicsContext gc);
}