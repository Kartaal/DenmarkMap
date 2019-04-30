package bfst19;

import bfst19.Route_parsing.Vehicle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import java.io.IOException;


public class ControllerRoutePanel {

    static Vehicle vehicleToggle;
    @FXML
    private ImageView backBtnRoutePanel;

    @FXML
    private ToggleButton car;

    @FXML
    private ToggleButton bike;
    @FXML
    ToggleGroup toggleRouteType;

    @FXML
    InstructionContainer instructions;

    private Controller controller;

    public void init(Controller controller) {
        this.controller = controller;
        instructions.init(controller);
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
    private void setRouteType(MouseEvent e){
        String toggleGroupValue;
        //TODO: Find out what to do if none is selected what to do

        ToggleButton selectedToggleButton = (ToggleButton) toggleRouteType.getSelectedToggle();
        toggleGroupValue = selectedToggleButton.getId();

        if(selectedToggleButton ==null || toggleGroupValue==null) {
            toggleRouteType.selectToggle(car);
            vehicleToggle=Vehicle.CAR;
        }
        else if(toggleGroupValue.equals("car")){
            System.out.println("car is true");
            vehicleToggle=Vehicle.CAR;
        }
        else if (toggleGroupValue.equals("bike")){
            vehicleToggle=Vehicle.BIKE;
        }
        else if (toggleGroupValue.equals("walking")){
            vehicleToggle=Vehicle.WALKING;
        }


    }

}
