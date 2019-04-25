package bfst19;

import bfst19.Route_parsing.Edge;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.Iterator;

public class InstructionContainer extends HBox {
    private static Controller controller;


    public InstructionContainer (){
        super();
    }

    public void init(Controller controller){
        this.setStyle("-fx-min-width: 280; -fx-min-height: 400;");
        this.controller = controller;

        controller.addPathObserver(this);
    }

    public  void showInstructions(){
        addInstructions();
        /*
        if(!instructionsPane.isShowing()){

        }
        */
    }

    public void addInstructions(){
        Iterator<Edge> iterator = controller.getpathIterator();
        if(iterator.hasNext()){
           Edge e = iterator.next();
            //skal have noget cheks
            addNewInstruction(e.getLength(),"right",e.getName());
            System.out.println("FUCK "+e.getLength()+ " right "+e.getName());

        }
    }

    public void addNewInstruction(double speed, String direction, String road){
        Instruction instruction= new Instruction(speed,direction,road);
        getChildren().add(instruction);
    }



}
