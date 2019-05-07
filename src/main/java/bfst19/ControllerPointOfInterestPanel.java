package bfst19;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;


public class ControllerPointOfInterestPanel implements BackBtnEffect {

	@FXML
	private ImageView backBtnPointOfInterest;

	private Controller controller;

	@FXML
	private ScrollPane scrollPane;

	private ListChangeListener<HBox> listener;

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
		DropShadow dropShadow = new DropShadow(BlurType.ONE_PASS_BOX, Color.rgb(0, 0, 0, 0.4), 10, 0, 0, 0);
		backBtnPointOfInterest.setEffect(dropShadow);
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
