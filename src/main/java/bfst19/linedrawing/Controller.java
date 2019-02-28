package bfst19.linedrawing;

import javafx.geometry.Point2D;
import javafx.stage.Stage;

import javax.naming.ldap.Control;

public class Controller {
	Model model;
	View view;
	double x, y;
	Line dragged;

	public Controller(Model model, View view) {
		this.model = model;
		this.view = view;
		view.scene.setOnKeyPressed(e -> {
			switch (e.getCode()) {
				case V:
					new Controller(model, new View(model, new Stage()));
					break;
				case F:
					new Controller(model, new FancyView(model, new Stage()));
					break;
				case S:
					model.save();
					break;
			}
		});
		view.scene.setOnMousePressed(e -> {
			x = e.getX();
			y = e.getY();
			if (e.isSecondaryButtonDown()) {
				Point2D modelCoords = view.modelCoords(x, y);
				double mx = modelCoords.getX();
				double my = modelCoords.getY();
				dragged = new Line(mx,my,mx,my);
				model.add(dragged);
			}
		});
		view.scene.setOnMouseDragged(e -> {
			if (e.isPrimaryButtonDown()) view.pan(e.getX() - x, e.getY() - y);
			x = e.getX();
			y = e.getY();
			if (e.isSecondaryButtonDown()) {
				Point2D modelCoords = view.modelCoords(x, y);
				dragged.x1 = modelCoords.getX();
				dragged.y1 = modelCoords.getY();
				model.notifyObservers();
			}
		});
		view.scene.setOnScroll(e -> {
			view.zoom(e.getDeltaY(), e.getX(), e.getY());
		});
	}
}
