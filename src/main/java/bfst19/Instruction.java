package bfst19;


import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;


public class Instruction extends HBox {


    public Instruction (double speed, String direction, String road){
        super();
        this.setStyle("-fx-min-width: 260; -fx-padding: 10; -fx-spacing: 2");
        getChildren().addAll(makeSpeedText(speed), makeDirectionText(direction), makeRoadNameText(road));
    }


    public Pane makeSpeedText(double speed){
        Pane speedPane = new Pane();
        Label speedText = new Label();
        speedText.setText(String.valueOf(speed));
        speedPane.getChildren().add(speedText);
        return speedPane;
    }

    public Pane makeDirectionText(String direction){
        Pane directionPane = new Pane();
        Label directionText = new Label();
        directionText.setText(direction);
        directionPane.getChildren().add(directionText);
        return directionPane;

    }
    public Pane makeRoadNameText(String roadName){
        Pane roadNamePane = new Pane();
        Label roadNameText = new Label();
        roadNameText.setText(roadName);
        roadNamePane.getChildren().add(roadNameText);
        return roadNamePane;
    }

}


