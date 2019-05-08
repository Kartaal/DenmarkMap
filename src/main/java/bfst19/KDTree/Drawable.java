package bfst19.KDTree;

import javafx.scene.canvas.GraphicsContext;

/**
 * @see javafx.scene.canvas.GraphicsContext
 * Represents elements that can be drawn in a GraphicsContext.
 */
public interface Drawable {
	/**
	 * Uses the given GraphicsContext to paint the element with stroke.
	 * It is intended to only paint lines with a total length greater than 1 pixel.
	 *
	 * @param gc                The GraphicsContext in which to paint the element using the stroke.
	 *                          method of the GraphicsContext.
	 * @param singlePixelLength The value that equates to a single pixel length.
	 */
	void stroke(GraphicsContext gc, double singlePixelLength);

	/**
	 * Uses the given GraphicsContext to paint the element with fill.
	 * It is intended to only paint elements with a total length greater than 1 pixel and
	 * total area no less than percentOfScreenArea.
	 *
	 * @param gc                  The GraphicsContext in which to paint the element using the fill
	 *                            method of the GraphicsContext.
	 * @param singlePixelLength   The value that equates to a single pixel length.
	 * @param percentOfScreenArea The value that should be a minimum bound for total area before element is painted.
	 */
	void fill(GraphicsContext gc, double singlePixelLength, double percentOfScreenArea);
}
