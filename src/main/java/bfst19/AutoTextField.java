package bfst19;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.LinkedList;
import java.util.List;

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

        addAdressesToDropDown("It works");
        if(!adressDropDown.isShowing()){
            adressDropDown.show(AutoTextField.this, Side.BOTTOM,0,0);
        }
        //controller.parseSearchText(this.getText());
    }

    private void addAdressesToDropDown(String adress) {

        List<CustomMenuItem> menuItems = new LinkedList<>();

        Label labelAdress = new Label(adress);

        CustomMenuItem item = new CustomMenuItem(labelAdress, true);

        menuItems.add(item);

        adressDropDown.getItems().addAll(menuItems);
    }


    private void panAdress(){

    }

}
