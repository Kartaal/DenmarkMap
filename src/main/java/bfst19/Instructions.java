package bfst19;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.event.EventTarget;

public class Instructions extends Pane {

    Controller controller;
    Model model;

    private static HBox hBoxOuter;
    private HBox hBoxInner;

    public Instructions (){
        super();
    }

    public void init(Controller controller){

        this.setStyle("-fx-min-width: 300; -fx-min-height: 40;-fx-background-color: Black");
        this.controller = controller;
        this.model = controller.getModel();
        //Should have an observer here
    }

    public static void addNewInstruction(int speed, String direction, String road){
        hBoxOuter.getChildren().add(makeNewInstruction(speed,direction,road));
    }

    public static Pane makeNewInstruction(int speed, String direction, String roadName){
        Pane pane =new Pane();
        pane.getChildren().addAll(makeSpeedPanal(speed),makeDirectionPanal(direction),makeRoadNamePanal(roadName) );
        return pane;
    }
    public static Pane makeSpeedPanal(int speed){
        return new Pane();
    }
    public static Pane makeDirectionPanal(String direction){
        return new Pane();
    }
    public static Pane makeRoadNamePanal(String roadName){
        return new Pane();
    }

}


