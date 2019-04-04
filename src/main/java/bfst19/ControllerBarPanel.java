package bfst19;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;


public class ControllerBarPanel {

    @FXML
    private TextField searchTextField;

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
    public void giveText(javafx.scene.input.KeyEvent event){
            switch (event.getCode())  {//ev.getcode() gets the specific keycode for the pressed key
                case ENTER:
                       controller.parseSearchText(searchTextField.getText());
                    break;
            }
    }

    @FXML
    private void openRute(ActionEvent actionEvent) {
        controller.setupRutePanel();
    }
}
