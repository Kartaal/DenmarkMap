package bfst19.osmdrawing;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;

import java.util.HashMap;

public class MapCanvas extends Canvas {
    GraphicsContext gc = getGraphicsContext2D();
    Affine transform = new Affine();
    Model model;
    HashMap<WayType,Color> wayColors = new HashMap<>();
    boolean paintNonRoads = true;
    int detailLevel =1;

    public void init(Model model) {
        this.model = model;
        pan(-model.minlon, -model.maxlat);
        setTypeColors();
        zoom(800/(model.maxlon-model.minlon), 0,0);
        transform.prependScale(1,-1, 0, 0);
        model.addObserver(this::repaint);
        model.addObserver(this::repaint);
        repaint();
    }

    public void repaint() {
        gc.setTransform(new Affine());

        if (model.getWaysOfType(WayType.COASTLINE).iterator().hasNext()) {
            gc.setFill(getColor(WayType.WATER));
        } else {
            gc.setFill(Color.WHITE);
        }
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setTransform(transform);
        gc.setStroke(getColor(WayType.UNKNOWN));
        gc.setLineWidth(1/Math.sqrt(Math.abs(transform.determinant())));
        gc.setFillRule(FillRule.EVEN_ODD);
        gc.setFill(Color.WHITE);
        for (Drawable way : model.getWaysOfType(WayType.COASTLINE)) way.fill(gc);
        gc.setFill(getColor(WayType.WATER));
        for (Drawable way : model.getWaysOfType(WayType.WATER)) way.fill(gc);


        gc.setFillRule(null);
        if(paintNonRoads) {//checks for toggle for only roads
            for (WayType type : WayType.values()) {

                if (!(type.isRoadOrSimilar())&&type.levelOfDetail()<detailLevel) {
                    if(type==WayType.COASTLINE){

                    }else{
                        gc.setFill(getColor(type));
                        for (Drawable way : model.getWaysOfType(type)) way.fill(gc);
                    }
                } else if (type.isRoadOrSimilar()&&type.levelOfDetail()<detailLevel) {
                    if (type == WayType.COASTLINE) {

                    } else if (type == WayType.WATER) {

                    } else
                        gc.setStroke(getColor(type));
                        for (Drawable way : model.getWaysOfType(type)) way.stroke(gc);
                    }
            }

        }else{
            for(WayType type : WayType.values()){
                if(type.isRoadOrSimilar()&&type.levelOfDetail()<detailLevel){
                    if(type==WayType.UNKNOWN){
                        // The unknown waytype is just ways that have not bee parsed to a particular waytype, so it's better to exclude it.
                    }else{
                        gc.setStroke(getColor(type));
                        for (Drawable way : model.getWaysOfType(type)) way.stroke(gc);

                    }

                }
            }
        }


    }

    private Color getColor(WayType type) { return wayColors.get(type); }

    public void setTypeColors(){//this really shouldn't be here, it should be in WayType is one of it's fields
        wayColors.put(WayType.INVISIBLE,Color.TRANSPARENT);
        wayColors.put(WayType.AREA,Color.LAVENDER);
        wayColors.put(WayType.UNKNOWN,Color.BLACK);
        wayColors.put(WayType.BUILDING,Color.DARKGRAY);
        wayColors.put(WayType.RESIDENTIAL,Color.LAVENDER);
        wayColors.put(WayType.PIER,Color.WHITESMOKE);
        wayColors.put(WayType.TREE,Color.DARKGREEN);
        wayColors.put(WayType.GRASS,Color.LAWNGREEN);
        wayColors.put(WayType.FOREST,Color.DARKGREEN);
        wayColors.put(WayType.BRIDGE,Color.DARKKHAKI);
        wayColors.put(WayType.WATER,Color.LIGHTSKYBLUE);
        wayColors.put(WayType.PARK,Color.PALEGREEN);
        wayColors.put(WayType.PITCH,Color.MEDIUMAQUAMARINE);
        wayColors.put(WayType.ARTWORK,Color.MEDIUMAQUAMARINE);
        wayColors.put(WayType.CONSTRUCTION,Color.DARKSEAGREEN);
        wayColors.put(WayType.BROWNFIELD,Color.TAN);
        wayColors.put(WayType.INDUSTRIAL,Color.THISTLE);
        wayColors.put(WayType.ALLOTMENTS,Color.PALEGREEN);
        wayColors.put(WayType.CEMETERY,Color.OLIVEDRAB);
        wayColors.put(WayType.SQUARE,Color.AZURE);
        wayColors.put(WayType.PLAYGROUND,Color.MEDIUMAQUAMARINE);
        wayColors.put(WayType.BARRIER,Color.BROWN);
        wayColors.put(WayType.AMENITY,Color.LIGHTSALMON);
        wayColors.put(WayType.FOOTWAY,Color.LIGHTCORAL);
        wayColors.put(WayType.PRIMARY,Color.LIGHTGRAY);
        wayColors.put(WayType.SECONDARY,Color.LIGHTGRAY);
        wayColors.put(WayType.TERTIARY,Color.LIGHTGRAY);
        wayColors.put(WayType.SERVICE,Color.LIGHTGRAY);
        wayColors.put(WayType.ROAD_RESIDENTIAL,Color.DIMGRAY);
        wayColors.put(WayType.CYCLEWAY,Color.LIGHTSLATEGRAY);
        wayColors.put(WayType.SUBWAY,Color.ORANGE);
        wayColors.put(WayType.RAILCONSTRUCTION,Color.GRAY);
        wayColors.put(WayType.DISUSED,Color.DARKGRAY);
        wayColors.put(WayType.ROAD_BRIDGE,Color.YELLOW);
        wayColors.put(WayType.COASTLINE,Color.MEDIUMPURPLE);
        wayColors.put(WayType.BOAT,Color.MEDIUMSLATEBLUE);
        wayColors.put(WayType.RECREATION,Color.LIGHTGREEN);
        wayColors.put(WayType.FARMLAND,Color.LIGHTYELLOW);
        wayColors.put(WayType.FARMYARD,Color.ROSYBROWN);
        wayColors.put(WayType.SCRUB ,Color.DARKOLIVEGREEN);
        wayColors.put(WayType.TAXIWAY,Color.SLATEGRAY);
        wayColors.put(WayType.RUNWAY ,Color.SLATEGRAY);
        wayColors.put(WayType.RACEWAY ,Color.LIGHTPINK);
        wayColors.put(WayType.QUARRY ,Color.DARKSLATEBLUE);
        wayColors.put(WayType.MILITARY ,Color.DARKSALMON);
        wayColors.put(WayType.STADIUM ,Color.LIGHTGRAY);
        wayColors.put(WayType.TRACK ,Color.PALEVIOLETRED);
        wayColors.put(WayType.DITCH ,Color.LIGHTSKYBLUE);
        wayColors.put(WayType.MOTORWAY ,Color.LIGHTGRAY);
        wayColors.put(WayType.BOUNDARY_ADMINISTRATIVE,Color.TRANSPARENT);
        wayColors.put(WayType.COMMERCIAL,Color.PALEVIOLETRED);
    }

    public void pan(double dx, double dy) {
        transform.prependTranslation(dx, dy);
        repaint();
    }

    public void zoom(double factor, double x, double y) {
        transform.prependScale(factor, factor, x, y);
        if(factor<1){//this translates to "if zooming out decrement"
            detailLevel -= 1;
        }else if(factor> 1){ //this translates to "if zooming in increment"
            detailLevel += 1;
        }
        repaint();
    }

    public Point2D modelCoords(double x, double y) {
        try {
            return transform.inverseTransform(x, y);
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void toggleNonRoads() {
        paintNonRoads = !paintNonRoads;
    }
}