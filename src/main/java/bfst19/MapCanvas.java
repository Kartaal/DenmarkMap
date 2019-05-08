package bfst19;

import bfst19.Line.OSMNode;
import bfst19.Route_parsing.Edge;
import bfst19.KDTree.BoundingBox;
import bfst19.KDTree.Drawable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

import java.util.HashMap;
import java.util.Iterator;


public class MapCanvas extends Canvas {
    GraphicsContext gc = getGraphicsContext2D();
    //linear transformation object used to transform our data while preserving proportions between nodes
    public Affine transform = new Affine();
    Model model;
    Controller controller;
    HashMap<WayType,Color> wayColors = new HashMap<>();
    boolean paintNonRoads = true;
    boolean hasPath = false;
    int detailLevel =1;

    private double singlePixelLength;
    private double percentOfScreenArea;



    public void init(Model model, Controller controller) {
        this.model = model;
        this.controller = controller;
        //conventions in screen coords and map coords are not the same,
        // so we convert to screen convention by flipping x y
        pan(-model.getMinlon(), -model.getMaxlat());
        //TODO #soup type colors method
        setTypeColors();
        //sets an initial zoom level, 800 for now because it works
        transform.prependScale(1,-1, 0, 0);
        zoom(800/(model.getMaxlon() - model.getMinlon()), 0,0);

        //model.addObserver(this::repaint);
        model.addPathObserver(this::setHasPath);
        model.addColorObserver(this::setTypeColors);
        repaint();
    }

    private void setHasPath() {
        hasPath = !hasPath;
    }

    public double getDeterminant(){
        return transform.determinant();
    }

    public void repaint() {
        //to clearly communicate that the fillRect should fill the entire screen
        gc.setTransform(new Affine());
        //checks if the file contains coastlines or not, if not set background color to white
        // This assumes that the dataset contains either a fully closed coastline, or a dataset without any coastlines at all.
        // otherwise set background color to blue
        if (model.getWaysOfType(WayType.COASTLINE, new BoundingBox(model.getMinlon(), model.getMinlat(), model.getMaxlon(), model.getMaxlat())).size() >= 0) {
            gc.setFill(getColor(WayType.WATER));
        } else {
            gc.setFill(Color.WHITE);
        }
        //clears screen by painting a color on the entire screen not background
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setTransform(transform);

        //linewidth equals 1 px wide relative to the screen no matter zoom level
        gc.setLineWidth(0.1 * (1/(2000/(getDeterminant()))));


        gc.setFillRule(FillRule.EVEN_ODD);

        //color for landmasses with nothing drawn on top
        gc.setFill(Color.WHITE);
        ResizingArray<Drawable> ways = model.getWaysOfType(WayType.COASTLINE, getExtentInModel());
        for(int i = 0 ; i < ways.size() ; i++){
            Drawable way = ways.get(i);
            way.fill(gc,singlePixelLength,percentOfScreenArea);
        }

        gc.setFill(getColor(WayType.WATER));
        ways = model.getWaysOfType(WayType.WATER, getExtentInModel());
        for(int i = 0 ; i < ways.size() ; i++){
            Drawable way = ways.get(i);
            way.fill(gc,singlePixelLength,percentOfScreenArea);
        }


        gc.setFillRule(null);
        //checks for toggle for only roads
        if(paintNonRoads) {
            for (WayType type : WayType.values()) {
                if (!(type.isRoadOrSimilar()) && type.levelOfDetail() < detailLevel) {
                    if(type != WayType.COASTLINE) {

                        ways = model.getWaysOfType(type, getExtentInModel());
                        gc.setFill(getColor(type));
                        for(int i = 0 ; i < ways.size() ; i++){
                            Drawable way = ways.get(i);
                            way.fill(gc,singlePixelLength,percentOfScreenArea);
                        }
                    }
                } else if (type.isRoadOrSimilar() && type.levelOfDetail() < detailLevel) {

                    if (type != WayType.COASTLINE ) {
                        gc.setStroke(getColor(type));
                        gc.setLineWidth(0.1 * (1/(2000/(getDeterminant()))));
                        ways = model.getWaysOfType(type, getExtentInModel());
                        for(int i = 0 ; i < ways.size() ; i++){
                            Drawable way = ways.get(i);
                            way.stroke(gc,singlePixelLength);
                        }
                    }


                }
            }

        }else{
            for(WayType type : WayType.values()){
                if(type.isRoadOrSimilar() && type.levelOfDetail() < detailLevel){
                        gc.setStroke(getColor(type));
                        gc.setLineWidth(0.1 * (1/(2000/(getDeterminant()))));
                        ways = model.getWaysOfType(type, getExtentInModel());
                        for(int i = 0 ; i < ways.size() ; i++){
                            Drawable way = ways.get(i);
                            way.stroke(gc,singlePixelLength);
                        }

                }
            }
        }
        if(hasPath){
            Iterator<Edge> iterator = controller.getpathIterator();
            drawPath(iterator);
        }

        Pin cPin = Pin.currentPin;
        if(cPin != null) {
            cPin.drawPin(gc, transform);
        }

        Pin sPin = Pin.secondaryPin;
        if(sPin != null) {
            sPin.drawPin(gc, transform);
        }

    }

    public void drawPath(Iterator<Edge> iterator) {
        while(iterator.hasNext()){
            Edge edge = iterator.next();
            OSMNode first = edge.either();
            OSMNode second = edge.other();

            gc.setLineWidth(0.1 * (1/(100/(getDeterminant()))));
            gc.setStroke(Color.RED);
            gc.beginPath();
            gc.moveTo(first.getLon(),first.getLat());
            gc.lineTo(second.getLon(),second.getLat());
            gc.stroke();
        }
    }

    private BoundingBox getExtentInModel(){
        if(!Controller.KdTreeBoolean()){
            return getBounds(); }
        else{
            return getBoundsDebug();
        }
    }

    private BoundingBox getBoundsDebug() {
        Bounds localBounds = this.getBoundsInLocal();
        float minX =(float) localBounds.getMinX() + 250;
        float maxX =(float) localBounds.getMaxX() - 250;
        float minY =(float) localBounds.getMinY() + 250;
        float maxY =(float) localBounds.getMaxY() - 250;

        //Flip the boundingbox' y-coords, as the rendering is flipped, but the model isn't.
        Point2D minPoint = getModelCoords(minX, maxY);
        Point2D maxPoint = getModelCoords(maxX, minY);

        gc.setStroke(Color.RED);
        gc.beginPath();
        gc.lineTo(minPoint.getX(), minPoint.getY());
        gc.lineTo(minPoint.getX(), maxPoint.getY());
        gc.lineTo(maxPoint.getX(), maxPoint.getY());
        gc.lineTo(maxPoint.getX(), minPoint.getY());
        gc.lineTo(minPoint.getX(), minPoint.getY());
        gc.stroke();

        return new BoundingBox((float)minPoint.getX(), (float)minPoint.getY(),
                (float)(maxPoint.getX()-minPoint.getX()), (float)(maxPoint.getY()-minPoint.getY()));
    }

    public BoundingBox getBounds() {
        Bounds localBounds = this.getBoundsInLocal();
        float minX = (float)localBounds.getMinX();
        float maxX = (float)localBounds.getMaxX();
        float minY = (float)localBounds.getMinY();
        float maxY = (float)localBounds.getMaxY();

        //Flip the boundingbox' y-coords, as the rendering is flipped, but the model isn't.
        Point2D minPoint = getModelCoords(minX, maxY);
        Point2D maxPoint = getModelCoords(maxX, minY);

        return new BoundingBox((float)minPoint.getX(), (float)minPoint.getY(),
                (float)(maxPoint.getX()-minPoint.getX()), (float)(maxPoint.getY()-minPoint.getY()));
    }

    private Color getColor(WayType type) { return wayColors.get(type); }

    private void setTypeColors(){
        Iterator<String[]> iterator = model.colorIterator();
        while(iterator.hasNext()){
            String[] tokens = iterator.next();
            wayColors.put(WayType.valueOf(tokens[0]),Color.valueOf(tokens[1]));
        }
        repaint();
    }

    public void panToPoint(double x,double y){
        double centerX = getWidth()/2.0;
        double centerY = getHeight()/2.0;
        x=x*model.getLonfactor();
        Point2D point = transform.transform(x,y);
        //System.out.println("X: " + x + " Y: " + y);

        pan(centerX-point.getX(),centerY-point.getY());

    }

    public void pan(double dx, double dy) {
        transform.prependTranslation(dx, dy);
        repaint();
    }

    public void zoom(double factor, double x, double y) {
        transform.prependScale(factor, factor, x, y);
        //Detail level dependant on determinant. Divide by 5 million to achieve a "nice" integer for our detail levels.
        //TODO maybe this value is only good for bornholm
        detailLevel = (int) Math.abs(transform.determinant()/5000000);

        Point2D minXAndY = getModelCoords(0,0);
        Point2D minXPlus1px = getModelCoords(1,0);
        Point2D minYPlus1px = getModelCoords(0,-1);

        double singleXPixelLength = minXPlus1px.getX()-minXAndY.getX();
        double singleYPixelLength = minYPlus1px.getY()-minXAndY.getY();

        singlePixelLength = Math.sqrt(Math.pow(singleXPixelLength,2)+Math.pow(singleYPixelLength,2));
        percentOfScreenArea = (singleXPixelLength*25)*singleYPixelLength*25;

        repaint();
    }

    public void toggleNonRoads(boolean enabled) {
        paintNonRoads = !enabled;
    }

    //TODO Burde crashe?
    public Point2D getModelCoords(float x, float y) {
        try{
            return transform.inverseTransform(x,y);
        }catch (NonInvertibleTransformException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void toggleNonRoads() {
        paintNonRoads = !paintNonRoads;
    }
}
