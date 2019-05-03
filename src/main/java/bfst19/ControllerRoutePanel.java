package bfst19;

import bfst19.Route_parsing.Vehicle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class ControllerRoutePanel {


    static Vehicle vehicleToggle= Vehicle.CAR;

    Point2D fromPoint, toPoint;
    int fromId, toId;


    @FXML
    private ImageView backBtnRoutePanel;

    @FXML
    private ToggleButton car;

    @FXML
    private ToggleGroup toggleRouteType;

    @FXML
    private InstructionContainer instructions;

    @FXML
    private VBox vboxInstructions;

    @FXML
    private AutoTextField textFieldTo;

    @FXML
    private AutoTextField textFieldFrom;

    private Controller controller;

    public void init(Controller controller) {
        this.controller = controller;
        instructions.init(controller);
        controller.getModel().addPathObserver(this::setRouteType);

        textFieldFrom.init(controller);
        textFieldFrom.setOnResponseListener(response -> {
            fromPoint = response;
            tryToFindPath();
        });

        textFieldTo.init(controller);
        textFieldTo.setOnResponseListener(response -> {
            toPoint = response;
            tryToFindPath();
        });

        setupToggle();
        car.setSelected(true);

    }

    @FXML
    private void returnToBarPanel(ActionEvent actionEvent) {
        controller.setUpBar();
    }

    @FXML
    private void setBackBtnEffect() {
        DropShadow dropShadow = new DropShadow(BlurType.ONE_PASS_BOX, Color.rgb(0,0,0,0.4), 10, 0, 0, 0);
        backBtnRoutePanel.setEffect(dropShadow);
    }

    @FXML
    private void setBackBtnEffectNone() { backBtnRoutePanel.setEffect(null); }

    @FXML
    public void setUpInstructions(){
        instructions.addInstructions();
    }

    private void setupToggle(){
        toggleRouteType.selectedToggleProperty().addListener(((observable, oldValue, newValue) -> {
            if(newValue == null)
                oldValue.setSelected(true);
        }));
    }

    @FXML
    private void setRouteType(){
        removeInstructions();
        String toggleGroupValue;
        //TODO: Find out what to do if none is selected what to do

        ToggleButton selectedToggleButton = (ToggleButton) toggleRouteType.getSelectedToggle();
        toggleGroupValue = selectedToggleButton.getId();

        boolean changed = false;

        if(toggleGroupValue.equals("car") && vehicleToggle != Vehicle.CAR){
            vehicleToggle = Vehicle.CAR;
            changed = true;
        }
        else if (toggleGroupValue.equals("bike")  && vehicleToggle != Vehicle.BIKE){
            vehicleToggle = Vehicle.BIKE;
            changed = true;
        }
        else if (toggleGroupValue.equals("walking")  && vehicleToggle != Vehicle.WALKING){
            vehicleToggle = Vehicle.WALKING;
            changed = true;
        }

        if(changed) {
            System.out.println(vehicleToggle.toString());

            //TODO: Should be enabled when the problem is fixed
            //setUpInstructions()

        }
    }

    public void removeInstructions(){
        vboxInstructions.getChildren().remove(instructions);
    }


    private void tryToFindPath(){
        if(toPoint != fromPoint && toPoint != null && fromPoint != null) {
            System.out.println("PATHFINDING");
            System.out.println(toPoint + " " + fromPoint);
            toId = controller.getNearestRoad(toPoint).getId();
            fromId = controller.getNearestRoad(fromPoint).getId();
            //TODO Insert pathfinding code here. fromId == start vertex id, toId == end vertex id.

        }
    }
}
