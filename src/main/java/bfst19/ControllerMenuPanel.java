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

    @FXML
    RadioButton colorBlind;

    @FXML
    RadioButton defaultMode;

    @FXML
    RadioButton roadsOnly;

    static boolean roadsOnlyMode;
    static boolean colorBlindMode;

    private Controller controller;

    public void init(Controller controller) {
        this.controller = controller;
        setState();

    }

    private void setState() {

        if (colorBlindMode && !roadsOnlyMode) {
            colorBlind.setSelected(true);
        } else if (!colorBlindMode && roadsOnlyMode) {
            roadsOnly.setSelected(true);
        } else if (!colorBlindMode && !roadsOnlyMode) {
            defaultMode.setSelected(true);
        }

    }

    @FXML
    private void returnToBarPanel() {
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
    private void setToggleTheme() {
        RadioButton selectedRadioButton = (RadioButton) toggleTheme.getSelectedToggle();
        String toggleGroupValue = selectedRadioButton.getText();

        switch (toggleGroupValue) {
            case "Color Blind Mode":
                colorBlindMode = true;
                roadsOnlyMode = false;
                controller.parseTheme(colorBlindMode);
                controller.parseOnlyRoadsMode(roadsOnlyMode);
                break;
            case "Roads Only Mode":
                colorBlindMode = false;
                roadsOnlyMode = true;
                controller.parseTheme(colorBlindMode);
                controller.parseOnlyRoadsMode(roadsOnlyMode);
                break;
            case "Default":
                colorBlindMode = false;
                roadsOnlyMode = false;
                controller.parseTheme(colorBlindMode);
                controller.parseOnlyRoadsMode(roadsOnlyMode);
                break;
        }

    }
}
