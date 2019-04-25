package bfst19;


import javafx.scene.layout.HBox;
import javafx.scene.control.Label;


public class Instruction extends HBox {


    public Instruction (double speed, String direction, String road){
        super();
        getChildren().addAll(makeSpeedText(speed), makeDirectionText(direction), makeRoadNameText(road));
    }


    public Label makeSpeedText(double speed){
        Label s = new Label();
        s.setText(String.valueOf(speed));
        return s;
    }

    public Label makeDirectionText(String direction){
        Label directionText = new Label();
        directionText.setText(direction);
        return directionText;

    }
    public Label makeRoadNameText(String roadName){
        Label roadNameText = new Label();
        roadNameText.setText(roadName);
        return roadNameText;
    }

}


