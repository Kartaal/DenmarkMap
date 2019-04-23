package bfst19;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class PointOfInterestItem extends HBox {


    private Controller controller;

    public PointOfInterestItem(String adress, double x, double y) {
        makeLabel(adress);
        removeBtn();

    }

    public void init(Controller controller) {
        this.controller = controller;
        //setAddressCoordsLabels(AutoTextField.autoTextFieldAdress,AutoTextField.x,AutoTextField.y);
    }


    private void makeLabel(String adress){

        Label label = new Label(adress);

        this.getChildren().add(label);

        label.setOnMouseClicked(event -> {

        });

    }

    private void removeBtn(){

        Button removeBtn = new Button();

        removeBtn.setGraphic(new ImageView("/clear.png"));

        this.getChildren().add(removeBtn);

        removeBtn.setOnAction(event -> {
            ControllerPointOfInterestPanel.hBoxes.remove(this);
        });
    }
}
