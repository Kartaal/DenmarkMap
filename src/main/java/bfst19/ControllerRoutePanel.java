package bfst19;

import bfst19.Line.OSMNode;
import bfst19.Route_parsing.Edge;
import bfst19.Route_parsing.Vehicle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;

public class ControllerRoutePanel {

	private static Vehicle vehicleToggle = Vehicle.CAR;
	private static boolean fastestRoute = true;

	private Point2D fromPoint, toPoint;


	@FXML
	private ImageView backBtnRoutePanel;

	@FXML
	private ToggleButton car;

	@FXML
	private ToggleGroup toggleRouteType;

	@FXML
	private ToggleButton fastestPathButton;

	@FXML
	private InstructionContainer instructions;

	@FXML
	private AutoTextField textFieldTo;

	@FXML
	private AutoTextField textFieldFrom;

	private Controller controller;

	public void init(Controller controller) {
		this.controller = controller;
		instructions.init(controller);

		textFieldFrom.init(controller, "current");
		textFieldFrom.setOnResponseListener(response -> {
			fromPoint = response;
			tryToFindPath();
		});

		textFieldTo.init(controller, "secondary");
		textFieldTo.setOnResponseListener(response -> {
			toPoint = response;
			tryToFindPath();
		});

		setupToggle();
		car.setSelected(true);
		fastestPathButton.setSelected(true);
	}

	@FXML
	public void switchText(ActionEvent actionEvent) {
		if (textFieldTo != null && textFieldFrom != null) {
			removeInstructions();
			String tempText = textFieldTo.getText();

			textFieldTo.setText(textFieldFrom.getText());
			textFieldFrom.setText(tempText);

			Point2D temp = fromPoint;
			fromPoint = toPoint;
			toPoint = temp;

			tryToFindPath();
		}
	}

	@FXML
	private void returnToBarPanel(ActionEvent actionEvent) {
		Pin.secondaryPin = null;
		controller.setUpBar();
	}

	@FXML
	private void setBackBtnEffect() {
		backBtnRoutePanel.setEffect(Controller.dropShadow);
	}

	@FXML
	private void setBackBtnEffectNone() {
		backBtnRoutePanel.setEffect(null);
	}

	private void setupToggle() {
		toggleRouteType.selectedToggleProperty().addListener(((observable, oldValue, newValue) -> {
			if (newValue == null)
				oldValue.setSelected(true);
		}));
	}

	@FXML
	private void setRouteType() {

		String toggleGroupValue;

		ToggleButton selectedToggleButton = (ToggleButton) toggleRouteType.getSelectedToggle();
		toggleGroupValue = selectedToggleButton.getId();

		boolean changed = false;

		//Fastest Path Button

		if (fastestPathButton.isSelected() && !fastestRoute) {
			fastestRoute = true;
			changed = true;
		} else if (!fastestPathButton.isSelected() && fastestRoute) {
			fastestRoute = false;
			changed = true;
		}

		//ToggleGroup

		if (toggleGroupValue.equals("car") && vehicleToggle != Vehicle.CAR) {
			vehicleToggle = Vehicle.CAR;
			changed = true;
		} else if (toggleGroupValue.equals("bike") && vehicleToggle != Vehicle.BIKE) {
			vehicleToggle = Vehicle.BIKE;
			changed = true;
		} else if (toggleGroupValue.equals("walking") && vehicleToggle != Vehicle.WALKING) {
			vehicleToggle = Vehicle.WALKING;
			changed = true;
		}

		if (changed) {
			removeInstructions();
			tryToFindPath();
		}
	}

	private void removeInstructions() {
		instructions.removeAllChildren();
	}

	private void tryToFindPath() {
		if (toPoint != fromPoint && toPoint != null && fromPoint != null) {
			OSMNode toNode = controller.getNearestRoad(toPoint, vehicleToggle);
			OSMNode fromNode = controller.getNearestRoad(fromPoint, vehicleToggle);

			Iterable<Edge> path = controller.getPath(fromNode, toNode, vehicleToggle, fastestRoute);
			controller.addPath(path);
		}
	}
}
