package bfst19.osmdrawing;

import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class Controller {
	private Model model;
	double x, y;

	//This only means that .fxml can use this field despite visibility
    @FXML
	private MapCanvas mapCanvas;

	public void init(Model model) {
		//TODO: figure out init methods
	    this.model = model;
		mapCanvas.init(model);
	}

	@FXML
	private void onKeyPressed(KeyEvent e) {
		switch (e.getCode()) {//e.getcode() gets the specific keycode for the pressed key
		    case T: //toggle so that the canvas only draws roads or similar draws everything by default
				mapCanvas.toggleNonRoads();
				mapCanvas.repaint();
				break;
		}
	}

	@FXML
	private void onScroll(ScrollEvent e) {
        //because scrollwheels/touchpads scroll by moving up and down the zoomfactor as calculated based on the distance moved by the "scroll"
        //The pow part is just about trial and error to find a good amount of zoom per "scroll"
        double factor = Math.pow(1.01, e.getDeltaY());
		mapCanvas.zoom(factor, e.getX(), e.getY());
	}

	@FXML
	private void onMouseDragged(MouseEvent e) {
	    //pans based on difference between mousePressed event and current mouse coords
		if (e.isPrimaryButtonDown()) mapCanvas.pan(e.getX() - x, e.getY() - y);
		x = e.getX();
		y = e.getY();
	}

	@FXML
	private void onMousePressed(MouseEvent e) {
		x = e.getX();
		y = e.getY();
	}
}
