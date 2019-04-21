package bfst19;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.awt.*;

public class ControllerInfoPanel implements BackBtnEffect {

    private Controller controller;

    @FXML
    private VBox vBox;

    @FXML
    private ImageView clearBtn;

    @FXML
    private ImageView addBtn;

    @FXML
    private javafx.scene.control.Label address;

    @FXML
    private javafx.scene.control.Label latlon;

    public void init(Controller controller) {
        this.controller = controller;

        String[] inputArray = AutoTextField.autoTextFieldInput.split("&");
        String adress = inputArray[0];
        String x = inputArray[1];
        String y = inputArray[2];

        setAddressCoordsLabels(adress,x,y);
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
        vBox.setVisible(false);
    }

    private void setAddressCoordsLabels(String location, String x, String y){
        address.setText(location);
        latlon.setText("Coords: " + x + ", " + y);
    }

    //TODO: Have an observable list with the point of interest the user has added, which can be removed and added from/to
    @FXML
    public void addPointOfInterest(ActionEvent actionEvent) {

    }
}
