package bfst19;

import bfst19.Exceptions.nothingNearbyException;
import bfst19.Line.OSMNode;
import bfst19.Route_parsing.Edge;
import bfst19.Route_parsing.RouteHandler;
import bfst19.Route_parsing.Vehicle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.Iterator;

public class Controller {

    private Model model;
    private float x, y;
    private boolean roadNameOnHover = false;
    static final DropShadow dropShadow = new DropShadow(BlurType.ONE_PASS_BOX,
            Color.rgb(0,0,0,0.3), 10, 0, 0, 0);


    @FXML
    private MapCanvas mapCanvas;

    @FXML
    private Text scaleText;

    @FXML
    private Text closestRoadText;

    @FXML
    private BorderPane borderPane;

    public Controller() {}

    public void init(Model model) {
        this.model = model;
        mapCanvas.init(model, this);

        setScalebar();
        setUpBar();

        // update scalebar when the mxxproperty has changed. The lambda expression
        // sets the method changed from the interface (ChangeListener).
        // mxxProperty() Defines the X coordinate scaling element of the 3x4 matrix.
        mapCanvas.transform.mxxProperty().addListener((observable, oldVal, newVal) -> {
            setScalebar();
        });
    }

    public Model getModel() {
        return model;
    }

    Iterator<String[]> getFoundMatchesIterator() {
        return model.foundMatchesIterator();
    }

    Iterator<Edge> getPathIterator() {
        return model.pathIterator();
    }

    void parseSearchText(String searchText) {
        model.parseSearch(searchText);
    }

    void parseTheme(boolean colorBlindEnabled) {
        model.switchColorScheme(colorBlindEnabled);
    }

    void parseOnlyRoadsMode(boolean enabled) {
        mapCanvas.toggleNonRoads(enabled);
        mapCanvas.repaint();
    }

    void setUpPointOfInterestPanel() {
        VBox vBox = null;

        if (borderPane.getLeft() != null) {
            borderPane.setLeft(null);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PointOfInterestPanel.fxml"));

        try {
            vBox = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        borderPane.setLeft(vBox);

        ControllerPointOfInterestPanel controllerPointOfInterestPanel = fxmlLoader.getController();
        controllerPointOfInterestPanel.init(this);
    }

    void setUpInfoPanel(String address, float x, float y) {
        VBox vBox = null;

        if (borderPane.getRight() != null) {
            borderPane.setRight(null);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("InfoPanel.fxml"));

        try {
            vBox = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        borderPane.setRight(vBox);

        ControllerInfoPanel controllerInfoPanel = fxmlLoader.getController();
        controllerInfoPanel.init(this, address, x, y);
    }

    void setUpBar() {

        if (borderPane.getLeft() != null) {
            borderPane.setLeft(null);
        }

        HBox hBox = null;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ViewBarPanel.fxml"));

        try {
            hBox = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load from FXMLLoader associated with ViewBarPanel.fxml");
        }

        borderPane.setLeft(hBox);
        ControllerBarPanel controllerBarPanel = fxmlLoader.getController();
        controllerBarPanel.init(this);
    }

    void setupMenuPanel() {
        if (borderPane.getLeft() != null) {
            borderPane.setLeft(null);
        }

        VBox VBox = null;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ViewMenuPanel.fxml"));

        try {
            VBox = fxmlLoader.load();
        } catch (IOException event) {
            event.printStackTrace();
            System.err.println("Failed to load from FXMLLoader associated with ViewMenuPanel.fxml");
        }

        borderPane.setLeft(VBox);
        ControllerMenuPanel controllerMenuPanel = fxmlLoader.getController();
        controllerMenuPanel.init(this);
    }

    void setupRoutePanel() {
        if (borderPane.getLeft() != null) {
            borderPane.setLeft(null);
        }

        VBox VBox = null;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ViewRoutePanel.fxml"));

        try {
            VBox = fxmlLoader.load();
        } catch (IOException event) {
            event.printStackTrace();
            System.err.println("Failed to load from FXMLLoader associated with ViewRoutePanel.fxml");
        }

        borderPane.setLeft(VBox);
        ControllerRoutePanel controllerRoutePanel = fxmlLoader.getController();
        controllerRoutePanel.init(this);
    }

    private void setScalebar() {
        //flipped weird, have to getY when needing x and vice versa
        float minX = (float) mapCanvas.getModelCoords(0, 0).getY();
        float maxX = (float) mapCanvas.getModelCoords(0, (float) mapCanvas.getHeight()).getY();
        float y = (float) (mapCanvas.getModelCoords(0, 0).getX() / Model.getLonfactor());
        scaleText.setText(ScaleBar.getScaleText(minX, y, maxX, y, mapCanvas.getWidth()));
    }

    void panToPoint(double x, double y) {
        mapCanvas.panToPoint(x, y);
    }

    @FXML
    private void onKeyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case F10:
                // do not enable on larger dataset than bornholm or so, makes it very laggy!!!
                roadNameOnHover = !roadNameOnHover;
                break;
        }
    }

    @FXML
    private void onScroll(ScrollEvent e) {
        // the zoomfactor as calculated based on the distance moved by the "scroll"
        //The pow part is just about trial and error to find a good amount of zoom per "scroll"
        double factor = Math.pow(1.01, e.getDeltaY());
        mapCanvas.zoom(factor, e.getX(), e.getY());
    }

    @FXML
    private void onMouseDragged(MouseEvent e) {
        if (e.isPrimaryButtonDown()) mapCanvas.pan(e.getX() - x, e.getY() - y);
        x = (float) e.getX();
        y = (float) e.getY();
    }

    @FXML
    private void onMousePressed(MouseEvent e) {
        x = (float) e.getX();
        y = (float) e.getY();

        if(e.isSecondaryButtonDown()){
            setClosestRoadText(x,y);
        }
    }

    @FXML
    public void onMouseMoved(MouseEvent e) {
        //If your dataset is small, this runs fine, however on larger dataset (denmark) never enable this
        if (roadNameOnHover) {
            float continuousX = (float) e.getX();
            float continuousY = (float) e.getY();

            setClosestRoadText(continuousX, continuousY);
        }
    }

    void addPath(Iterable<Edge> path) {
        if (path != null) {
            model.clearPath();
            model.addPath(path);
        }
        mapCanvas.repaint();
    }

    Iterable<Edge> getPath(OSMNode startNode, OSMNode endNode, Vehicle type, boolean b) {
        return model.findPath(startNode, endNode, type, b);
    }

    void addPathObserver(InstructionContainer instructionContainer) {
        model.addPathObserver(instructionContainer::showInstructions);
    }

    BorderPane getBorderPane() {
        return borderPane;
    }

    ObservableList<PointOfInterestItem> pointOfInterestList() {
        return model.pointOfInterestList();
    }

    void addPointsOfInterestItem(PointOfInterestItem pointOfInterestItem) {
        model.addPointOfInterestItem(pointOfInterestItem);
    }

    void removePointOfInterestItem(PointOfInterestItem pointOfInterestItem) {
        model.removePointOfInterestItem(pointOfInterestItem);
    }

    private void setClosestRoadText(float continuousX, float continuousY) {
        OSMNode tempClosest = null;

        try {
            tempClosest = model.getNearestRoad(mapCanvas.getModelCoords(continuousX, continuousY), Vehicle.ABSTRACTVEHICLE);
        } catch (nothingNearbyException e) {
            e.printStackTrace();
        }

        String closestRoad = RouteHandler.getArbitraryAdjRoadName(tempClosest);
        closestRoadText.setText(closestRoad);
    }

    OSMNode getNearestRoad(Point2D point2D, Vehicle type) {
        try {
            return model.getNearestRoad(point2D, type);
        } catch (nothingNearbyException e) {
            e.printStackTrace();
            return null;
        }
    }

}
