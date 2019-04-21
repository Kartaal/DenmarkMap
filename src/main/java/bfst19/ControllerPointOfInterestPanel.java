package bfst19;

import javafx.fxml.FXML;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.awt.*;

public class ControllerPointOfInterestPanel implements BackBtnEffect {

    @FXML
    private Button btnPointOfInterest;

    @FXML
    private VBox vBox;

    private Controller controller;

    @FXML
    private ImageView clearBtn;

    @FXML
    private ImageView addBtn;

    @FXML
    private javafx.scene.control.Label address;

    @FXML
    private javafx.scene.control.Label latlon;


    public void init(Controller controller) { this.controller = controller; setAddressCoordsLabels("En adresse", 6.66, 6.66 ); }

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

    private void setAddressCoordsLabels(String location, double x, double y){
        address.setText(location);
        latlon.setText(x + ", " + y);
    }


}
