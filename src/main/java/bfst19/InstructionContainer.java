package bfst19;

import bfst19.Line.OSMNode;
import bfst19.Route_parsing.Edge;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Iterator;

public class InstructionContainer extends VBox {
    private static Controller controller;


    public InstructionContainer() {
        super();
    }

    public void init(Controller controller) {
        this.setStyle("-fx-min-width: 280; -fx-min-height: 400;");
        this.controller = controller;

        controller.addPathObserver(this);
    }

    public void showInstructions() {
        addInstructions();
        /*
        if(!instructionsPane.isShowing()){

        }
        */
    }

    public void addInstructions() {
        Iterator<Edge> pathIterator = controller.getpathIterator();
        Edge edge = pathIterator.next();
        Edge currentEdge = pathIterator.next();

        //this section is to establish which ends of the two startingedges are the "head" and "base"
        // being which direction we are going
        OSMNode previousV = edge.getV();
        OSMNode previousW = edge.getW();
        OSMNode newV = currentEdge.getV();
        OSMNode newW = currentEdge.getW();
        if (edge != null && currentEdge != null) {
            OSMNode previousBase;
            OSMNode previousHead;
            OSMNode currentBase;
            OSMNode currentHead;
            if (previousV.getId() == newV.getId()) {
                previousHead = previousV;
                previousBase = previousW;
                currentBase = newV;
                currentHead = newW;
            } else if (previousW.getId() == newV.getId()) {
                previousHead = previousW;
                previousBase = previousV;
                currentBase = newV;
                currentHead = newW;
            } else if (previousV.getId() == newW.getId()) {
                previousHead = previousV;
                previousBase = previousW;
                currentBase = newW;
                currentHead = newV;
            } else {
                previousHead = previousW;
                previousBase = previousV;
                currentBase = newW;
                currentHead = newV;
            }

            double angle = Model.angleBetween2Lines(previousBase, previousHead, currentBase, currentHead);

            if (45 < angle && angle < 180) {
                System.out.println("turn left");
            }
            if (180 < angle && angle < 315) {
                System.out.println("turn right");
            }

            previousBase = currentBase;
            previousHead = currentHead;


            double length = currentEdge.getLength();
            String name = currentEdge.getName();
            double currentLength = 0.0;

            while (pathIterator.hasNext()) {

                currentEdge = pathIterator.next();
                currentHead = currentEdge.getOtherEndNode(previousHead);
                currentBase = currentEdge.getOtherEndNode(currentHead);
                currentLength = currentEdge.getLength();

                angle = Model.angleBetween2Lines(previousBase, previousHead, currentBase, currentHead);
                String direction = "";
                if(15 < angle && 45 > angle){
                    direction = "keep left";
                }else if (45 < angle && angle < 120) {
                    direction = "left";
                }else if(345 > angle && angle > 315){
                    direction = "keep right";
                }else if (240 < angle && angle < 315) {
                    direction = "right";
                }else if(120<angle&&angle<240){
                    direction = "u-turn";
                }
                if(currentLength>1.0) {
                    String currentName = currentEdge.getName();
                    if (direction.equals("u-turn") || direction.equals("left") || direction.equals("right") || !name.equals(currentName)) {
                        addNewInstruction(length, direction, currentName);
                        length = 0.0;
                        name = currentName;
                        direction = "";
                    }
                }

                length += currentLength;
                previousBase = currentBase;
                previousHead = currentHead;
            }
        }

    }

    public void addNewInstruction(double speed, String direction, String road){
        Instruction instruction= new Instruction(speed,direction,road);
        getChildren().add(instruction);
    }



}
