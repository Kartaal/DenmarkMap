package bfst19;

import javafx.geometry.Side;
import javafx.scene.control.*;

import java.util.ArrayList;
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
                    parseSearch();
                    break;
            }
        });
    }

    public void init(Controller controller){
        this.setStyle("-fx-min-width: 300; -fx-min-height: 40");

        this.controller = controller;
        this.model = controller.getModel();
        model.addFoundMatchesObserver(this::showResults);

    }

    public void parseSearch(){
        controller.parseSearchText(getText());
    }

    public void showResults(){
        addAddressesToDropDown();
        if(!adressDropDown.isShowing()){
            adressDropDown.show(AutoTextField.this, Side.BOTTOM,0,0);
        }
    }

    //TODO: Add ScrollPane and limit height
    private void addAddressesToDropDown() {

        List<CustomMenuItem> menuItems = new LinkedList<>();
        ArrayList<Label> addressLabels = new ArrayList<>();
        Iterator<String[]> iterator = controller.getFoundMatchesIterator();
        if(iterator.hasNext()) {
            String[] firstMatch = iterator.next();
            //this means that the match is a complete address
            if (firstMatch.length == 8) {
                panAdress(Double.valueOf(firstMatch[0]), Double.valueOf(firstMatch[1]));
                return;
                //and the rest of the address is passed of to some other part of the UI.
            } else if (firstMatch.length == 4) {
                while (iterator.hasNext()) {
                    String[] match = iterator.next();
                    Label labelAddress = new Label(match[0] + " " + match[1] + " " + match[2] + " " + match[3]);
                    addressLabels.add(labelAddress);
                }
            } else {
                while (iterator.hasNext()) {
                    String[] match = iterator.next();
                    Label labelAddress = new Label(match[0] + " " + match[1] + " " + match[2]);
                    addressLabels.add(labelAddress);
                }
            }
            for (Label addressLabel : addressLabels) {
                CustomMenuItem item = new CustomMenuItem(addressLabel, true);

                item.setOnAction((event) -> {
                    setText(addressLabel.getText());
                });
                menuItems.add(item);
            }
        }

        if(menuItems.size() == 0){
            menuItems.add(new CustomMenuItem(new Label("No search result found"),true));
        }

        adressDropDown.getItems().clear();
        adressDropDown.getItems().addAll(menuItems);
        System.out.println(adressDropDown.getItems().size());
    }

    private void panAdress(double x, double y){
        controller.panToPoint(x,y);
    }
}
