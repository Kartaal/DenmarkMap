package bfst19.osmdrawing;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import java.io.IOException;


public class ControllerBarPanel {


    @FXML
    private Button menuButton;

    Controller controller;


    public void init(Controller controller){
        this.controller = controller;
        setMenuButton();
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
}
