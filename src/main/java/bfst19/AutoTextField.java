package bfst19;

import javafx.geometry.Side;
import javafx.scene.control.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AutoTextField extends TextField {

    Controller controller;
    Model model;

    private ContextMenu adressDropDown;

    public AutoTextField(){
        super();

        adressDropDown = new ContextMenu();
        adressDropDown.setStyle("-fx-min-width: 300; -fx-max-height: 400");

        this.setOnKeyPressed(event -> {
            switch (event.getCode())  {
                case ENTER:
                    showResults();
                    break;
            }
        });
    }

    public void init(Controller controller){
        this.setStyle("-fx-min-width: 300; -fx-min-height: 40");

        this.controller = controller;
        this.model = controller.getModel();
        model.addObserver(this::addAdressesToDropDown);

    }

    public void showResults(){
        addAdressesToDropDown();
        if(!adressDropDown.isShowing()){
            adressDropDown.show(AutoTextField.this, Side.BOTTOM,0,0);
        }
    }

    //TODO: Add ScrollPane and limit height
    private void addAdressesToDropDown() {

        List<CustomMenuItem> menuItems = new LinkedList<>();

        System.out.println(this.getText());
        controller.parseSearchText(this.getText());

        Iterator<String> iterator = controller.parsefoundMatchesIterator();
        while(iterator.hasNext()){
            Label labelAdress = new Label(iterator.next());
            CustomMenuItem item = new CustomMenuItem(labelAdress, true);

            item.setOnAction((event) -> {
                this.setText(labelAdress.getText());
                panAdress(labelAdress.getText());
            });
            menuItems.add(item);
        }

        if(menuItems.size() == 0){
            menuItems.add(new CustomMenuItem(new Label("No search result found"),true));
        }

        adressDropDown.getItems().clear();
        adressDropDown.getItems().addAll(menuItems);
        System.out.println(adressDropDown.getItems().size());
    }

    //TODO: Need Adress node
    private void panAdress(String adress){

    }
}
