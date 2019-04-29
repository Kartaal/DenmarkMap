package bfst19;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

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

    private double x, y;
    private String address;

    public void init(Controller controller, String address, double x, double y) {
        this.controller = controller;
        this.address = address;
        this.x = x;
        this.y = y;
        setAddressCoordsLabels();
    }

    @FXML
    private void setBackBtnEffect(){
        DropShadow dropShadow = new DropShadow(BlurType.ONE_PASS_BOX, Color.rgb(0,0,0,0.3), 10, 0, 0, 0);
        clearBtn.setEffect(dropShadow);
    }

    @FXML
    private void setBackBtnEffectNone(){ clearBtn.setEffect(null);}

    @FXML
    private void setAddBtnEffect(){
        DropShadow dropShadow = new DropShadow(BlurType.ONE_PASS_BOX, Color.rgb(0,0,0,0.3), 10, 0, 0, 0);
        addBtn.setEffect(dropShadow);
    }

    @FXML
    private void setAddBtnEffectNone(){ addBtn.setEffect(null);}

    @FXML
    private void clearBtnAction(){
        controller.getBorderPane().setRight(null);
    }


    private void setAddressCoordsLabels(){
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
