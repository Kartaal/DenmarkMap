package bfst19;

import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AutoTextField extends TextField {

    Controller controller;
    Model model;
    private OnResponseListener<Point2D> listener;
    private ContextMenu addressDropDown;
    private String tag;

    public AutoTextField() {
        super();

        addressDropDown = new ContextMenu();
        addressDropDown.setStyle("-fx-max-height: 400");

        setOnAction(event -> parseSearch());
    }

    void setOnResponseListener(OnResponseListener<Point2D> listener) {
        this.listener = listener;
    }

    public void init(Controller controller, String tag) {
        this.setStyle("-fx-min-width: 300; -fx-min-height: 40");

        this.controller = controller;
        this.model = controller.getModel();
        this.tag = tag;
    }

    void parseSearch() {
        if (getText() != null && !getText().equals("")) {
            model.addFoundMatchesObserver(this::showResults);
            controller.parseSearchText(getText());
        }
    }

    void showResults() {
        model.clearFoundMatchesObservers();
        if (addAddressesToDropDown()) {
            addressDropDown.show(this, Side.BOTTOM, 0, 0);
        }
    }

    private boolean addAddressesToDropDown() {

        List<CustomMenuItem> menuItems = new LinkedList<>();
        ResizingArray<Label> addressLabels = new ResizingArray<>();
        Iterator<String[]> iterator = controller.getFoundMatchesIterator();

        if (iterator.hasNext()) {
            String[] firstMatch = iterator.next();

            //this means that the match is a complete address
            if (firstMatch.length == 8) {
                panAddress(Float.valueOf(firstMatch[0]), Float.valueOf(firstMatch[1]));
                addressDropDown.getItems().clear();
                menuItems.add(new CustomMenuItem(new Label(this.getText()), true));

            } else if (firstMatch.length == 4) {
                addressLabels.add(new Label(firstMatch[0] + " " + firstMatch[1] + " " + firstMatch[2] + " " + firstMatch[3]));

                while (iterator.hasNext()) {
                    String[] match = iterator.next();
                    Label labelAddress = new Label(match[0] + " " + match[1] + " " + match[2] + " " + match[3]);
                    addressLabels.add(labelAddress);
                }

            } else {
                addressLabels.add(new Label(firstMatch[0] + " " + firstMatch[1] + " " + firstMatch[2]));

                while (iterator.hasNext()) {
                    String[] match = iterator.next();
                    Label labelAddress = new Label(match[0] + " " + match[1] + " " + match[2]);
                    addressLabels.add(labelAddress);
                }

            }

            for (Label addressLabel : addressLabels) {

                CustomMenuItem item = new CustomMenuItem(addressLabel, true);
                menuItems.add(item);

                item.setOnAction((event) -> setText(addressLabel.getText()));
            }
        }

        if (menuItems.size() == 0) {
            menuItems.add(new CustomMenuItem(new Label("No search result found"), true));
        }

        addressDropDown.getItems().clear();
        addressDropDown.getItems().addAll(menuItems);

        return true;
    }

    public void clear() {
        addressDropDown.getItems().clear();
    }

    private void panAddress(float x, float y) {

        if (tag.equals("current")) {
            Pin.currentPin = new Pin(x * Model.getLonfactor(), y);

        } else {
            Pin.secondaryPin = new Pin(x * Model.getLonfactor(), y);
        }

        controller.panToPoint(x, y);
        controller.setUpInfoPanel(this.getText(), x, y);

        if (listener != null) {
            listener.getResponse(new Point2D(x * Model.getLonfactor(), y));
        }
    }
}
