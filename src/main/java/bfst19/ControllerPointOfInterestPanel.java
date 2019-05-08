package bfst19;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class ControllerPointOfInterestPanel implements BackBtnEffect {

	private Controller controller;
	private ListChangeListener<HBox> listener;

	@FXML
	private ImageView backBtnPointOfInterest;

	@FXML
	private ScrollPane scrollPane;


	public void init(Controller controller) {
		this.controller = controller;
		showHBoxesList();

		listener = c -> showHBoxesList();

		controller.pointOfInterestList().addListener(listener);
	}

	private void removeListener() {
		controller.pointOfInterestList().removeListener(listener);
	}

	@FXML
	private void setBackBtnEffect() {
		backBtnPointOfInterest.setEffect(Controller.dropShadow);
	}

	@FXML
	private void setBackBtnEffectNone() {
		backBtnPointOfInterest.setEffect(null);
	}

	@FXML
	public void returnToBarPanel(ActionEvent actionEvent) {
		removeListener();
		controller.setUpBar();
	}

	private void showHBoxesList() {
		VBox vBox = new VBox();
		vBox.getChildren().addAll(controller.pointOfInterestList());
		scrollPane.setContent(vBox);
	}
}
