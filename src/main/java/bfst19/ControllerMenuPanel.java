package bfst19;

import javafx.fxml.FXML;
import javafx.scene.effect.*;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public class ControllerMenuPanel implements BackBtnEffect {

    Controller controller;

    @FXML
    ImageView backBtnMenu;

    @FXML
    ToggleGroup toggleTheme;

    public void init(Controller controller){
        this.controller = controller;
    }

    @FXML
    private void returnToBarPanel(){
        controller.setUpBar();
    }

    //Image changes color when it is entered
    @FXML
    private void setBackBtnEffect() {
        DropShadow dropShadow = new DropShadow(BlurType.ONE_PASS_BOX, Color.rgb(0,0,0,0.4), 10, 0, 0, 0);
        backBtnMenu.setEffect(dropShadow);
    }

    @FXML
    private void setBackBtnEffectNone() {
        backBtnMenu.setEffect(null);
    }

    @FXML
    private void setToggleTheme(){
        boolean colorBlindEnabled=false;
        RadioButton selectedRadioButton = (RadioButton) toggleTheme.getSelectedToggle();
        String toogleGroupValue = selectedRadioButton.getText();
        if(toogleGroupValue.equals("ColorBlind Mode")){
            colorBlindEnabled=true;
            controller.parseTheme(colorBlindEnabled);
        }
        if(!toogleGroupValue.equals("ColorBlind Mode")){
            colorBlindEnabled=false;
            controller.parseTheme(colorBlindEnabled);
        }
    }
}
