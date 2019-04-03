package bfst19;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;

public class AutoTextField extends TextField {

    Controller controller;

    private ContextMenu adressDropDown;


    public AutoTextField(){
        super();
        this.setStyle("-fx-min-width: 300; -fx-min-height: 40");
        adressDropDown = new ContextMenu();
        adressDropDown.setStyle("-fx-min-width: 300");

        this.setOnKeyPressed(event -> {
            switch (event.getCode())  {
                case ENTER:
                    showResults();
                        break;

            }
        });
    }

    private void showResults(){
        controller.parseSearchText(this.getText());
    }


    private void panAdress(){

    }

}
