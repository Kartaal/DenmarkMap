package bfst19;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;

public class ControllerMenuPanel implements BackBtnEffect {

    @FXML
    ImageView backBtnMenu;

    @FXML
    ToggleGroup toggleTheme;

    private Controller controller;

    public void init(Controller controller){
        this.controller = controller;
    }

    @FXML
    private void returnToBarPanel(){
        controller.setUpBar();
    }

    @FXML
    private void setBackBtnEffect() {
        backBtnMenu.setEffect(Controller.dropShadow);
    }

    @FXML
    private void setBackBtnEffectNone() {
        backBtnMenu.setEffect(null);
    }

    @FXML
    private void setToggleTheme(){
        RadioButton selectedRadioButton = (RadioButton) toggleTheme.getSelectedToggle();
        String toggleGroupValue = selectedRadioButton.getText();

        switch (toggleGroupValue) {
            case "Color Blind Mode":
                controller.parseTheme(true);
                controller.parseOnlyRoadsMode(false);
                break;
            case "Roads Only Mode":
                controller.parseTheme(false);
                controller.parseOnlyRoadsMode(true);
                break;
            case "Default":
                controller.parseTheme(false);
                controller.parseOnlyRoadsMode(false);
                break;
        }

    }
}
