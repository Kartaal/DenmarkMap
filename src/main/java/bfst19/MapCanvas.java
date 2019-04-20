package bfst19;

import bfst19.Route_parsing.Edge;
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
    Affine transform = new Affine();
    Model model;
    HashMap<WayType,Color> wayColors = new HashMap<>();
    boolean paintNonRoads = true;
    boolean hasPath = false;
    int detailLevel =1;

    private boolean colorBlindEnabled = false;


    public void init(Model model) {
        this.model = model;
        //conventions in screen coords and map coords are not the same,
        // so we convert to screen convention by flipping x y
        pan(-model.minlon, -model.maxlat);
        //TODO #soup type colors method
        setTypeColors();
        //sets an initial zoom level, 800 for now because it works
        zoom(800/(model.maxlon-model.minlon), 0,0);
        transform.prependScale(1,-1, 0, 0);
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
        // otherwise set background color to blue
        if (model.getWaysOfType(WayType.COASTLINE, getExtentInModel()).iterator().hasNext()) {
            gc.setFill(getColor(WayType.WATER));
        } else {
            gc.setFill(Color.WHITE);
        }
        //clears screen by painting a color on the entire screen not background
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setTransform(transform);

        //linewidth equals 1 px wide relative to the screen no matter zoom level
        gc.setLineWidth(1/Math.sqrt(Math.abs(getDeterminant())));

        gc.setFillRule(FillRule.EVEN_ODD);

        //color for landmasses with nothing drawn on top
        gc.setFill(Color.WHITE);
        for (Drawable way : model.getWaysOfType(WayType.COASTLINE, getExtentInModel())) {
            way.fill(gc);
        }

        gc.setFill(getColor(WayType.WATER));
        for (Drawable way : model.getWaysOfType(WayType.WATER, getExtentInModel())) {
            way.fill(gc);
        }


        gc.setFillRule(null);
        //checks for toggle for only roads
        if(paintNonRoads) {
            for (WayType type : WayType.values()) {
                if (!(type.isRoadOrSimilar()) && type.levelOfDetail() < detailLevel) {
                    if(type != WayType.COASTLINE) {
                        gc.setFill(getColor(type));
                        for (Drawable way : model.getWaysOfType(type, getExtentInModel())) way.fill(gc);
                    }
                } else if (type.isRoadOrSimilar() && type.levelOfDetail() < detailLevel) {
                    if (type != WayType.COASTLINE && type != WayType.UNKNOWN) {
                        gc.setStroke(getColor(type));
                        for (Drawable way : model.getWaysOfType(type, getExtentInModel())) way.stroke(gc);
                    }
                }
            }

        }else{
            for(WayType type : WayType.values()){
                if(type.isRoadOrSimilar() && type.levelOfDetail() < detailLevel){
                    if(type == WayType.UNKNOWN){
                    // The unknown WayType is ways that have not been parsed to an implemented WayType,
                    // so it's better to exclude it.
                    }else{
                        gc.setStroke(getColor(type));
                        for (Drawable way : model.getWaysOfType(type, getExtentInModel())) way.stroke(gc);
                    }
                }
            }
        }
        if(hasPath){
            Iterator<Edge> iterator = model.pathIterator().next().iterator();
            while(iterator.hasNext()){
                Edge edge = iterator.next();
                OSMNode first = edge.getV();
                OSMNode second = edge.getW();

                //gc.setLineWidth(2);
                gc.setStroke(Color.RED);
                gc.beginPath();
                gc.moveTo(first.getLon(),first.getLat());
                gc.lineTo(second.getLon(),second.getLat());
                gc.stroke();
            }

        }
    }

    private BoundingBox getExtentInModel(){ return getBounds(); }

    private BoundingBox getBoundsDebug() {
        Bounds localBounds = this.getBoundsInLocal();
        double minX = localBounds.getMinX() + 100;
        double maxX = localBounds.getMaxX() - 100;
        double minY = localBounds.getMinY() + 100;
        double maxY = localBounds.getMaxY() - 100;

        //Flip the boundingbox y cordinates as the rendering is flipped as well, but the model isnt.
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

        return new BoundingBox(minPoint.getX(), minPoint.getY(),
                maxPoint.getX()-minPoint.getX(), maxPoint.getY()-minPoint.getY());
    }

    public BoundingBox getBounds() {
        Bounds localBounds = this.getBoundsInLocal();
        double minX = localBounds.getMinX();
        double maxX = localBounds.getMaxX();
        double minY = localBounds.getMinY();
        double maxY = localBounds.getMaxY();

        //Flip the boundingbox y cordinates as the rendering is flipped as well, but the model isnt.
        Point2D minPoint = getModelCoords(minX, maxY);
        Point2D maxPoint = getModelCoords(maxX, minY);

        return new BoundingBox(minPoint.getX(), minPoint.getY(),
                maxPoint.getX()-minPoint.getX(), maxPoint.getY()-minPoint.getY());
    }

    private Color getColor(WayType type) { return wayColors.get(type); }

    private void setTypeColors(){
        Iterator<String> iterator = model.colorIterator();
        while(iterator.hasNext()){
            wayColors.put(WayType.valueOf(iterator.next()),Color.valueOf(iterator.next()));
        }
        repaint();
    }

    public void panToPoint(double x,double y){
        double centerX = getWidth()/2.0;
        double centerY = getHeight()/2.0;
        x = x*model.getLonfactor();
        Point2D point = transform.transform(x,y);
        pan(centerX-point.getX(),centerY-point.getY());
    }

    public void pan(double dx, double dy) {
        transform.prependTranslation(dx, dy);
        repaint();
    }

    public void zoom(double factor, double x, double y) {
        transform.prependScale(factor, factor, x, y);
        //TODO Set level of detail dependent on determinant
        //this translates to "if zooming out decrement"
        if(factor < 1){
            detailLevel -= 1;
        //this translates to "if zooming in increment"
        }else if(factor > 1){
            detailLevel += 1;
        }
        repaint();
    }

    public void toggleNonRoads(boolean enabled) {
        paintNonRoads = !enabled;
    }


    public Point2D getModelCoords(double x, double y) {
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
