package bfst19;
import bfst19.Line.OSMNode;
import bfst19.Route_parsing.Edge;
import bfst19.Route_parsing.RouteHandler;
import bfst19.Route_parsing.Vehicle;
import javafx.collections.ObservableList;
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
    float x, y;
    private double factor, oldDeterminant;
    private boolean fastestBoolean = false;
    private static boolean kdTreeBoolean = false;
    private long time;
    private OSMNode[] nodeIDs = new OSMNode[2];
    private OSMNode closestNode;
    private String closestRoad;



    //This only means that .fxml can use this field despite visibility
    @FXML
    private MapCanvas mapCanvas;

    @FXML
    private Text scaleText;

    @FXML
    private Text closestRoadText;

    @FXML
    private BorderPane borderPane;

    @FXML
    private StackPane stackPane;

    public void init(Model model) {
        //TODO: figure out init methods
        this.model = model;
        mapCanvas.init(model,this);


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

    /*
    public Double getDistanceFromModel(double startLat, double startLon, double endLat, double endLon){
        return model.calculateDistanceInMeters(startLat,startLon,endLat,endLon);
    }
    */

    public Iterator<String[]> getFoundMatchesIterator(){
        return model.foundMatchesIterator();
    }
    public Iterator<Edge> getpathIterator(){
        return model.pathIterator();
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
    public void setUpPointOfInterestPanel() {
        VBox vBox = null;

        if(borderPane.getLeft() != null){
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

    //Initialize InfoPanel
    public void setUpInfoPanel(String adress, float x, float y){
            VBox vBox = null;

            if(borderPane.getRight() != null){
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
            controllerInfoPanel.init(this, adress, x, y);
    }

    //Initialize BarPanel
    public void setUpBar(){
        if(borderPane.getLeft() != null){
            borderPane.setLeft(null);
        }

        //TODO Should maybe add the clear observer method here, so that it doesnt clear on each init() but instead when the bar is created

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
        float minX = (float)mapCanvas.getModelCoords(0, 0).getY();
        float maxX = (float)mapCanvas.getModelCoords(0, (float)mapCanvas.getHeight()).getY();
        float y = (float) (mapCanvas.getModelCoords(0, 0).getX()/model.getLonfactor());
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
            case H:
                fastestBoolean = !fastestBoolean;
                break;
            case K:
                kdTreeBoolean = !kdTreeBoolean;
                break;
            case C:

                model.clearPath();
                mapCanvas.repaint();
                nodeIDs[0] = null;
                nodeIDs[1] = null;
                break;
            case S:

                break;
            case E:

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
        x = (float) e.getX();
        y = (float) e.getY();
    }

    @FXML
    private void onMousePressed(MouseEvent e) {
        x = (float) e.getX();
        y = (float) e.getY();


        long prevtime = time;
        time = System.nanoTime();

        if(e.isPrimaryButtonDown()){
            //System.out.println(Math.abs((-time + prevtime) / 1e8));
            //Todo: delete this part in the final commit. This is here for debugging purposes
            if (Math.abs((-time + prevtime) / 1e8) <= 2){
                closestNode = model.getNearestRoad(mapCanvas.getModelCoords(x,y), Vehicle.CAR);
                if(nodeIDs[0] == null){
                    if(closestNode != null) {
                        nodeIDs[0] = closestNode;
                        System.out.println(closestNode.getId());
                        nodeIDs[1] = null;
                    }

                } else if(nodeIDs[1] == null){
                    nodeIDs[1] = closestNode;
                    System.out.println(closestNode.getId());
                    Iterable<Edge> path = model.findPath(nodeIDs[0],nodeIDs[1], Vehicle.CAR, fastestBoolean);
                    if(path != null){
                        model.clearPath();
                        model.addPath(path);
                    }
                    mapCanvas.repaint();

                    nodeIDs[0] = null;
                    nodeIDs[1] = null;
                }
            }
        }

        if(e.isSecondaryButtonDown()){
            setClosestRoadText(x,y);
        }
    }

    @FXML
    public void onMouseMoved(MouseEvent e){
        /*float contX = (float) e.getX();
        float contY = (float) e.getY();
        System.out.println(setClosestRoad(contX, contY));*/

    }

    public static boolean KdTreeBoolean() {
        return kdTreeBoolean;
    }

    public void addPathObserver(InstructionContainer instructionContainer) {
        model.addPathObserver(instructionContainer::showInstructions);
    }

    public BorderPane getBorderPane(){
        return borderPane;
    }

    public ObservableList<PointOfInterestItem> pointOfInterestList(){
        return model.pointOfInterestList();
    }

    public void addPointsOfInterestItem(PointOfInterestItem pointOfInterestItem) { model.addPointOfInterestItem(pointOfInterestItem);}

    public void removePointOfInterestItem(PointOfInterestItem pointOfInterestItem){ model.removePointOfInterestItem(pointOfInterestItem);}

    private void setClosestRoadText(float contx, float conty){
        OSMNode tempClosest = model.getNearestRoad(mapCanvas.getModelCoords(contx,conty), Vehicle.ABSTRACTVEHICLE);
        closestRoad = RouteHandler.getArbitraryAdjRoadName(tempClosest);
        closestRoadText.setText(closestRoad);
    }

}
