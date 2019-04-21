package bfst19;
import bfst19.KDTree.Drawable;
import bfst19.Route_parsing.Edge;
import bfst19.Route_parsing.EdgeWeightedGraph;
import bfst19.Route_parsing.Vehicle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import java.io.IOException;
import java.util.Iterator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class Controller {
    private Model model;
    double x, y;
    private double factor, oldDeterminant, zoomLevel;

    //This only means that .fxml can use this field despite visibility
    @FXML
    private MapCanvas mapCanvas;

    @FXML
    private Text scaleText;

    @FXML
    private BorderPane borderPane;

    @FXML
    private StackPane stackPane;

    public void init(Model model) {
        //TODO: figure out init methods
        this.model = model;
        mapCanvas.init(model);

        oldDeterminant = mapCanvas.getDeterminant();
        setScalebar();
        setUpBar();

        // update scalebar when the mxxproperty has changed. The lambda expression
        // sets the method changed from the interface (ChangeListener).
        // mxxProperty() Defines the X coordinate scaling element of the 3x4 matrix.
        mapCanvas.transform.mxxProperty().addListener((observable, oldVal, newVal) -> {
            // sets the after mx is changed scale
            setScalebar();
        });

    }

    //Methods from model which are used by the class AutoTextField

    public Model getModel(){
        return model;
    }

    public Double getDistanceFromModel(double startLat, double startLon, double endLat, double endLon){
        return model.calculateDistanceInMeters(startLat,startLon,endLat,endLon);
    }

    public Iterator<String[]> getFoundMatchesIterator(){
        return model.foundMatchesIterator();
    }

    public void parseSearchText(String searchText){
        model.parseSearch(searchText);
    }

    public void parseTheme(boolean colorBlindEnabled){ model.switchColorScheme(colorBlindEnabled);}

    public void parseOnlyRoadsMode(boolean enabled){
        mapCanvas.toggleNonRoads(enabled);
        mapCanvas.repaint();
    }

    //Initialize PointOfInterestPanel
    public void setUpPointOfInterestPanel(double x, double y){
            VBox vBox = null;

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PointOfInterestPanel.fxml"));
            try {
                vBox = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            vBox.setLayoutX(x);
            vBox.setLayoutX(y);
            stackPane.getChildren().add(vBox);

            ControllerPointOfInterestPanel controllerPointOfInterestPanel = fxmlLoader.getController();
            controllerPointOfInterestPanel.init(this);
    }

    //Initialize BarPanel
    public void setUpBar(){
        if(borderPane.getLeft() != null){
            borderPane.setLeft(null);
        }

        HBox hBox = null;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ViewBarPanel.fxml"));

        try {
            hBox = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
			System.out.println("Failed to load from FXMLLoader associated with ViewBarPanel.fxml");
        }

        borderPane.setLeft(hBox);

        ControllerBarPanel controllerBarPanel = fxmlLoader.getController();
        controllerBarPanel.init(this);
    }

    //Initialize MenuPanel
    public void setupMenuPanel(){
        if(borderPane.getLeft() != null){
            borderPane.setLeft(null);
        }

        VBox VBox = null;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ViewMenuPanel.fxml"));

        try {
            VBox = fxmlLoader.load();
        } catch (IOException event) {
            event.printStackTrace();
			System.out.println("Failed to load from FXMLLoader associated with ViewMenuPanel.fxml");
        }

        borderPane.setLeft(VBox);

        ControllerMenuPanel controllerMenuPanel = fxmlLoader.getController();
        controllerMenuPanel.init(this);
    }


    //Initialize RoutePanel
    public void setupRoutePanel() {
        if(borderPane.getLeft() != null){
            borderPane.setLeft(null);
        }

        VBox VBox = null;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ViewRoutePanel.fxml"));

        try {
            VBox = fxmlLoader.load();
        } catch (IOException event) {
            event.printStackTrace();
			System.out.println("Failed to load from FXMLLoader associated with ViewRoutePanel.fxml");
        }

        borderPane.setLeft(VBox);

        ControllerRoutePanel controllerRoutePanel = fxmlLoader.getController();
        controllerRoutePanel.init(this);
    }

    public void setScalebar() {
        // TODO findout and resolve getY so it can be getX, since it the te x-coor we want
        //todo fix using model to calculate distance
        double minX = mapCanvas.getModelCoords(0, 0).getY();
        double maxX = mapCanvas.getModelCoords(0, mapCanvas.getHeight()).getY();
        double y = mapCanvas.getModelCoords(0, 0).getX()/model.getLonfactor();
        scaleText.setText(ScaleBar.getScaleText(minX, y, maxX, y, mapCanvas.getWidth()));
    }

    public void panToPoint(double x, double y){
        mapCanvas.panToPoint(x,y);
    }

    @FXML
    private void onKeyPressed(KeyEvent e) {
        switch (e.getCode()) {//e.getcode() gets the specific keycode for the pressed key
            case T: //toggle so that the canvas only draws roads or similar draws everything by default
                mapCanvas.toggleNonRoads();
                mapCanvas.repaint();
                break;
            case P:
                mapCanvas.panToPoint(14.8429560,55.0967440);
                break;
            case C:
                Iterable<Edge> path = model.routeHandler.findPath(4048894613L,489365650L, Vehicle.CAR,false);
                Iterable<Edge> adj = model.routeHandler.getAdj(2091635039L,Vehicle.CAR);
                for(Edge edge : adj){
                    System.out.print(edge.toString());
                }
                model.foundPath.add(path);
                model.notifyPathObservers();
                mapCanvas.repaint();
                break;
        }
    }

    @FXML
    private void onScroll(ScrollEvent e) {
        //because scrollwheels/touchpads scroll by moving up and down
        // the zoomfactor as calculated based on the distance moved by the "scroll"
        //The pow part is just about trial and error to find a good amount of zoom per "scroll"
        factor = Math.pow(1.01, e.getDeltaY());
        mapCanvas.zoom(factor, e.getX(), e.getY());
    }

    @FXML
    private void onMouseDragged(MouseEvent e) {
        //pans based on difference between mousePressed event and current mouse coords
        if (e.isPrimaryButtonDown()) mapCanvas.pan(e.getX() - x, e.getY() - y);
        x = e.getX();
        y = e.getY();
    }

    @FXML
    private void onMousePressed(MouseEvent e) {
        x = e.getX();
        y = e.getY();
    }
}



