package bfst19.osmdrawing;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;



public class ControllerRutePanel {


    private Controller controller;

    public void init(Controller controller) {
        this.controller = controller;
    }

    @FXML
    private void returnToBarPanel(ActionEvent actionEvent) {
        controller.setUpBar();
    }
}
