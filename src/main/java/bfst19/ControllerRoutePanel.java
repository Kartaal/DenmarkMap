package bfst19;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;



public class ControllerRoutePanel {

    @FXML
    private ImageView backBtnRoutePanel;

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
}
