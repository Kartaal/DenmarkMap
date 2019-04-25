package bfst19;

import bfst19.Route_parsing.Edge;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;

import java.util.Iterator;


public class Instructions extends Pane {

    Controller controller;
    Model model;

    private static HBox hBoxOuter = new HBox();
    private static Pane instructionsPane= new Pane();

    public Instructions (){
        super();
    }

    public void init(Controller controller){

        this.setStyle("-fx-min-width: 300; -fx-min-height: 40;-fx-background-color: Olivedrab");
        this.controller = controller;
        this.model = controller.getModel();

        model.addPathObserver(this::showInstructions);
    }

    public  void showInstructions(){
        addInstructions();
        if(!instructionsPane.isShowing()){

        }

    }

    public  void addInstructions(){
        Iterator<Iterable<Edge>> iterator = controller.getpathIterator();
        if(iterator.hasNext()){

            Iterable<Edge> iteratorEdge= iterator.next();
            for (Edge e : iteratorEdge){
                addNewInstruction(e.getLength(),"right",e.getName() );
                System.out.println("FUCK "+e.getLength()+ " right "+e.getName());
            }
        }
    }

    public static void addNewInstruction(double speed, String direction, String road){
        Pane pane = makeNewInstruction(speed,direction,road);
        hBoxOuter.getChildren().add(pane);
    }

    public static Pane makeNewInstruction(double speed, String direction, String roadName){
        instructionsPane.getChildren().addAll(makeSpeedText(speed), makeDirectionText(direction), makeRoadNameText(roadName) );
        return instructionsPane;
    }
    public static Label makeSpeedText(double speed){
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


