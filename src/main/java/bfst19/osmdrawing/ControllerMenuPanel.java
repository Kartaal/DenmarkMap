package bfst19.osmdrawing;

import javafx.fxml.FXML;

public class ControllerMenuPanel {

    Controller controller;


    public void init(Controller controller){
        this.controller = controller;
    }


    @FXML
    private void returnToBarPanel(){
        controller.setUpBar();
    }

}
