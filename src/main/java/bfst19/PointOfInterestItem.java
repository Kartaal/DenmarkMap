package bfst19;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.EventListener;

public class PointOfInterestItem extends HBox {


    private Controller controller;

    private Button removeBtn;

    private Pane pane;

    private ImageView imageView;

    private String adress;
    private double x, y;

    public PointOfInterestItem(String adress, double x, double y) {
        this.adress = adress;
        this.x = x;
        this.y = y;
        this.setPrefHeight(50);
        this.setPrefWidth(240);
        this.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 0);");
        this.setPadding(new Insets(5));
        makeLabel(adress);
        makeRemoveBtn();

    }

    public void init(Controller controller) {
        this.controller = controller;
        onActionLabel();

        removeBtn.setOnAction(event -> controller.getHBoxes().remove(this));
    }


    private void makeLabel(String adress){
        pane = new Pane();
        pane.setPrefHeight(50);
        pane.setMinWidth(240);
        pane.getChildren().add(new Label(adress));
        pane.getChildren().get(0).setLayoutY(15);
        pane.getChildren().get(0).setLayoutX(5);
        this.getChildren().add(pane);

    }

    private void onActionLabel(){
        pane.setOnMouseClicked(event -> {
            controller.panToPoint(x, y);
            controller.setUpInfoPanel(adress, x, y);
        });
    }

    private void makeRemoveBtn(){
        removeBtn = new Button();
        removeBtn.setStyle("-fx-background-color: grey;");
        imageView = new ImageView("/clear.png");
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        removeBtn.setGraphic(imageView);
        this.getChildren().add(removeBtn);
    }
}
