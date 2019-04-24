package bfst19;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
    private VBox pointOfInterestList;

    public ControllerPointOfInterestPanel(){
    }


    public void init(Controller controller) {
        this.controller = controller;
        showHBoxesList();
        controller.getHBoxes().addListener(new ListChangeListener<HBox>() {
            @Override
            public void onChanged(Change<? extends HBox> c) {
                showHBoxesList();
            }
        });
    }

    @FXML
    private void setBackBtnEffect() {
        DropShadow dropShadow = new DropShadow(BlurType.ONE_PASS_BOX, Color.rgb(0,0,0,0.4), 10, 0, 0, 0);
        backBtnPointOfInterest.setEffect(dropShadow);
    }

    @FXML
    private void setBackBtnEffectNone() { backBtnPointOfInterest.setEffect(null); }

    @FXML
    public void returnToBarPanel(ActionEvent actionEvent) {
        controller.setUpBar();
    }

    private void showHBoxesList() {

        pointOfInterestList.getChildren().removeAll();


        for (HBox item: controller.getHBoxes()) {
            pointOfInterestList.getChildren().add(item);
        }
        System.out.println("Called showHBoxesList method");
    }

}
