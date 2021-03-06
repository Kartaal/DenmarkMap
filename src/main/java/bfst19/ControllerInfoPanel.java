package bfst19;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class ControllerInfoPanel implements BackBtnEffect {

    private Controller controller;

    @FXML
    private ImageView clearBtn;

    @FXML
    private ImageView addBtn;

    @FXML
    private Label addressLabel;

    @FXML
    private Label latlon;

    private float x, y;
    private String address;

    public void init(Controller controller, String address, float x, float y) {
        this.controller = controller;
        this.address = address;
        this.x = x;
        this.y = y;
        setAddressCoordsLabels();
    }

    @FXML
    private void setBackBtnEffect() {
        clearBtn.setEffect(Controller.dropShadow);
    }

    @FXML
    private void setBackBtnEffectNone() {
        clearBtn.setEffect(null);
    }

    @FXML
    private void setAddBtnEffect() {
        addBtn.setEffect(Controller.dropShadow);
    }

    @FXML
    private void setAddBtnEffectNone() {
        addBtn.setEffect(null);
    }

    @FXML
    private void clearBtnAction() {
        controller.getBorderPane().setRight(null);
    }

    private void setAddressCoordsLabels() {
        addressLabel.setText(address);
        latlon.setText("Coords: " + x + ", " + y);
    }

    @FXML
    private void addPointOfInterest(ActionEvent actionEvent) {
        PointOfInterestItem pointOfInterestItem = new PointOfInterestItem(address, x, y);
        pointOfInterestItem.init(controller);
        controller.addPointsOfInterestItem(pointOfInterestItem);
    }
}
