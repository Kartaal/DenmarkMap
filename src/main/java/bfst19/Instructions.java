package bfst19;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;


public class Instructions extends Pane {

    Controller controller;
    Model model;

    private static HBox hBoxOuter = new HBox();
    private HBox hBoxInner= new HBox();

    public Instructions (){
        super();
    }

    public void init(Controller controller){

        this.setStyle("-fx-min-width: 300; -fx-min-height: 40;-fx-background-color: Olivedrab");
        this.controller = controller;
        this.model = controller.getModel();
        //Should have an observer here
    }

    public static void addNewInstruction(int speed, String direction, String road){
        Pane pane = makeNewInstruction(speed,direction,road);
        hBoxOuter.getChildren().add(pane);
    }

    public static Pane makeNewInstruction(int speed, String direction, String roadName){
        Pane pane =new Pane();
        pane.getChildren().addAll(makeSpeedText(speed), makeDirectionText(direction), makeRoadNameText(roadName) );
        return pane;
    }
    public static Label makeSpeedText(int speed){
        Label s = new Label();
        s.setText(String.valueOf(speed));
        return s;
    }

    public static Label makeDirectionText(String direction){
        Label directionText = new Label();
        directionText.setText(direction);
        return directionText;

    }
    public static Label makeRoadNameText(String roadName){
        Label roadNameText = new Label();
        roadNameText.setText(roadName);
        return roadNameText;

    }

}


