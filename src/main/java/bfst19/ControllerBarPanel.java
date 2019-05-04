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

    //ControllerBarPanels initialize method, which initialize controller field and runs the AutoTextFields init.
    public void init(Controller controller){
        this.controller = controller;
        setMenuButton();
        setSearchButton();
        autoTextField.init(controller, "current");
    }

    //Sets up MenuPanel when MenuButton is pressed
    private void setMenuButton(){
        menuButton.setOnAction(e -> controller.setupMenuPanel());
    }

    //Sets up RoutePanel when RouteBtn is pressed
    @FXML
    private void openRoute(ActionEvent actionEvent) {
        controller.setupRoutePanel();
    }

    //When SearchButton is pressed it runs AutoTextField's showResults method
    private void setSearchButton(){
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                autoTextField.showResults();
            }
        });
    }

    @FXML
    private void openPointOfInterest(ActionEvent actionEvent) {
        controller.setUpPointOfInterestPanel();
    }
}
