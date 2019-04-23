package bfst19;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;


public class ControllerPointOfInterestPanel implements BackBtnEffect {


    public ControllerPointOfInterestPanel(){

    }

    @FXML
    private ImageView backBtnPointOfInterest;

    private Controller controller;

    public static ObservableList<HBox> hBoxes = FXCollections.observableArrayList();

    public void init(Controller controller) {
        this.controller = controller;
    }

    @FXML
    private void setBackBtnEffect() {
        DropShadow dropShadow = new DropShadow(BlurType.ONE_PASS_BOX, Color.rgb(0,0,0,0.4), 10, 0, 0, 0);
        backBtnPointOfInterest.setEffect(dropShadow);
    }

    @FXML
    private void setBackBtnEffectNone() { backBtnPointOfInterest.setEffect(null); }


}
