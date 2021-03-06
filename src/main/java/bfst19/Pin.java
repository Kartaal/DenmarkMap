package bfst19;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.transform.Affine;

class Pin {

    static Pin currentPin = null;
    static Pin secondaryPin = null;

    private double lat, lon;
    private Image image;

    Pin(double lon, double lat) {
        image = new Image("/point.png");
        this.lat = lat;
        this.lon = lon;
    }

    void drawPin(GraphicsContext gc, Affine transform) {
        double scale = Math.sqrt(Math.abs(transform.determinant()));
        double width = 0.8 * image.getWidth() / scale;
        double count = Math.max(0.00005, width);

        gc.drawImage(image, lon - width / 2, lat, count, count);
    }
}
