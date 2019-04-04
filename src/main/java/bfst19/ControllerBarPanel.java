package bfst19;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.Iterator;


public class ControllerBarPanel {

    @FXML
    private Button menuButton;
    @FXML
    private AutoTextField autoTextField;
    @FXML
    private Button searchButton;

    Controller controller;

    public void init(Controller controller){
        this.controller = controller;
        setMenuButton();
        setSearchButton();
        autoTextField.init(controller);
    }

    public void setMenuButton(){
        menuButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                controller.setupMenuPanel();
            }
        });
    }

    @FXML
    private void openRute(ActionEvent actionEvent) {
        controller.setupRutePanel();
    }

    public void setSearchButton(){
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                autoTextField.showResults();
            }
        });
    }

}
