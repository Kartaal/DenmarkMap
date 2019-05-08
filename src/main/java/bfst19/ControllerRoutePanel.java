package bfst19;

import bfst19.Line.OSMNode;
import bfst19.Route_parsing.Edge;
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
    static boolean fastestRoute = true;

    Point2D fromPoint, toPoint;
    OSMNode fromNode, toNode;


    @FXML
    private ImageView backBtnRoutePanel;

    @FXML
    private ToggleButton car;

    @FXML
    private ToggleGroup toggleRouteType;

    @FXML
    private ToggleButton fastestPathButton;

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
        //controller.getModel().addPathObserver(this::);

        textFieldFrom.init(controller, "current");
        textFieldFrom.setOnResponseListener(response -> {
            fromPoint = response;
            tryToFindPath();
        });

        textFieldTo.init(controller, "secondary");
        textFieldTo.setOnResponseListener(response -> {
            toPoint = response;
            tryToFindPath();
        });

        setupToggle();
        car.setSelected(true);
        fastestPathButton.setSelected(true);

    }

    @FXML
    public void switchText(ActionEvent actionEvent) {
        if (textFieldTo != null && textFieldFrom != null){
            removeInstructions();
            String tempText = textFieldTo.getText();
            textFieldTo.setText(textFieldFrom.getText());
            textFieldFrom.setText(tempText);
            Point2D temp = fromPoint;
            fromPoint = toPoint;
            toPoint = temp;
            tryToFindPath();
        }
    }

    @FXML
    private void returnToBarPanel(ActionEvent actionEvent) {
        Pin.secondaryPin = null;
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

        ToggleButton selectedToggleButton = (ToggleButton) toggleRouteType.getSelectedToggle();
        toggleGroupValue = selectedToggleButton.getId();

        boolean changed = false;

        //Fastest Path Button

        if (fastestPathButton.isSelected()){
            fastestRoute = true;
            changed = true;
        }
        else{
            fastestRoute = false;
            changed = true;
        }

        //ToggleGroup

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
            //Changed setUpInstructions with the below code, since the idea is to make a new path for bikes, and
            //the setup is always called by an observer when the path is found.
            tryToFindPath();
        }
    }

    public void removeInstructions(){
        instructions.removeAllChildren();
    }


    private void tryToFindPath(){
        if(toPoint != fromPoint && toPoint != null && fromPoint != null) {
            toNode = controller.getNearestRoad(toPoint, vehicleToggle);
            fromNode = controller.getNearestRoad(fromPoint, vehicleToggle);

            Iterable<Edge> path = controller.getPath(fromNode, toNode, vehicleToggle, fastestRoute);
            controller.addPath(path);

            //toPoint = null;
            //fromPoint = null;
        }
    }
}
