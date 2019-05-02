package bfst19;

import bfst19.Route_parsing.Vehicle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import java.io.IOException;


public class ControllerRoutePanel {


    static Vehicle vehicleToggle= Vehicle.CAR;
    @FXML
    private ImageView backBtnRoutePanel;

    @FXML
    private ToggleButton car;

    @FXML
    ToggleGroup toggleRouteType;

    @FXML
    InstructionContainer instructions;

    @FXML
    ScrollPane scrollInstructions;

    @FXML
    VBox vboxInstructions;

    @FXML
    AutoTextField textFieldTo;

    @FXML
    AutoTextField textFieldFrom;


    private Controller controller;

    public void init(Controller controller) {
        this.controller = controller;
        instructions.init(controller);
        controller.getModel().addPathObserver(this::setRouteType);
        textFieldTo.init(controller);
        textFieldFrom.init(controller);
        car.setSelected(true);

    }

    @FXML
    private void returnToBarPanel(ActionEvent actionEvent) {
        controller.setUpBar();
    }

    @FXML
    private void setBackBtnEffect() {
        DropShadow dropShadow = new DropShadow(BlurType.ONE_PASS_BOX, Color.rgb(0,0,0,0.4), 10, 0, 0, 0);
        backBtnRoutePanel.setEffect(dropShadow);
    }

    @FXML
    private void setBackBtnEffectNone() { backBtnRoutePanel.setEffect(null); }

    @FXML
    public void setUpInstructions(){
        instructions.addInstructions();
    }

    @FXML
    private void setRouteType(){
        removeInstructions();
        String toggleGroupValue;
        //TODO: Find out what to do if none is selected what to do

        ToggleButton selectedToggleButton = (ToggleButton) toggleRouteType.getSelectedToggle();
        toggleGroupValue = selectedToggleButton.getId();

        if(toggleGroupValue.equals("car")){
            vehicleToggle=Vehicle.CAR;
        }
        else if (toggleGroupValue.equals("bike")){
            vehicleToggle=Vehicle.BIKE;
        }
        else if (toggleGroupValue.equals("walking")){
            vehicleToggle=Vehicle.WALKING;
        }
        setUpInstructions();

    }

    public void removeInstructions(){
        vboxInstructions.getChildren().remove(instructions);
    }

}
