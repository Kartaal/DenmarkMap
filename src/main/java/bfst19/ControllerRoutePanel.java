package bfst19;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;



public class ControllerRoutePanel {

    @FXML
    private ImageView backBtnRoutePanel;

    @FXML
    private ToggleButton car;

    @FXML
    private ToggleButton bike;
    @FXML
    ToggleGroup toggleRouteType;

    private Controller controller;

    public void init(Controller controller) {
        this.controller = controller;
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
    private void setRouteType(){
        String toggleGroupValue;
        //TODO: Find out what to do if none is selected what to do

        ToggleButton selectedToggleButton = (ToggleButton) toggleRouteType.getSelectedToggle();
        toggleGroupValue = selectedToggleButton.getId();

        if(selectedToggleButton ==null && toggleGroupValue==null) {
            toggleRouteType.selectToggle(car);
            toggleGroupValue="";
        }
        else if(toggleGroupValue.equals("car")){
            System.out.println("bike is false");
        }
        else if (toggleGroupValue.equals("bike")){
            System.out.println("car is false");
        }

    }
}
