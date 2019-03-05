package bfst19.osmdrawing;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

import java.util.HashMap;

public class MapCanvas extends Canvas {
    GraphicsContext gc = getGraphicsContext2D();
    //linear transformation object used to transform our data while preserving proportions between nodes
    Affine transform = new Affine();
    Model model;
    HashMap<WayType,Color> wayColors = new HashMap<>();
    boolean paintNonRoads = true;
    int detailLevel =1;

    public void init(Model model) {
        this.model = model;
        //conventions in screen coords and map coords are not the same, so we convert to screen convention by flipping x y
        pan(-model.minlon, -model.maxlat);
        setTypeColors();//#soup
        //sets an initial zoom level, 800 for now because it works
        zoom(800/(model.maxlon-model.minlon), 0,0);
        transform.prependScale(1,-1, 0, 0);
        model.addObserver(this::repaint);
        repaint();
    }

    public void repaint() {
        //to clearly communicate that the fillRect should fill the entire screen
        gc.setTransform(new Affine());
        //checks if the file contains coastlines or not, if not set bacground color to white otherwise blue
        if (model.getWaysOfType(WayType.COASTLINE).iterator().hasNext()) {
            gc.setFill(getColor(WayType.WATER));
        } else {
            gc.setFill(Color.WHITE);
        }
        //clears screen by painting a color on the entire screen not background
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setTransform(transform);
        //linewidth equals 1 px wide relative to the screen no matter zoom level
        gc.setLineWidth(1/Math.sqrt(Math.abs(transform.determinant())));
        //
        gc.setFillRule(FillRule.EVEN_ODD);

        //color for landmasses with nothing drawn on top
        gc.setFill(Color.WHITE);
        for (Drawable way : model.getWaysOfType(WayType.COASTLINE)) way.fill(gc);
        gc.setFill(getColor(WayType.WATER));
        for (Drawable way : model.getWaysOfType(WayType.WATER)) way.fill(gc);


        gc.setFillRule(null);
        if(paintNonRoads) {//checks for toggle for only roads
            for (WayType type : WayType.values()) {

                if (!(type.isRoadOrSimilar())&&type.levelOfDetail()<detailLevel) {
                    if(type==WayType.COASTLINE) {
                    }else if(type==WayType.UNKNOWN) {
                    }else{
                        gc.setFill(getColor(type));
                        for (Drawable way : model.getWaysOfType(type)) way.fill(gc);

                    }
                } else if (type.isRoadOrSimilar()&&type.levelOfDetail()<detailLevel) {
                    if (type == WayType.COASTLINE) {
                    }else if(type==WayType.UNKNOWN) {

                    } else {
                        gc.setStroke(getColor(type));
                        for (Drawable way : model.getWaysOfType(type)) way.stroke(gc);

                    }
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
    //TODO:setTypeColors should read from a file
    public void setTypeColors(){//this really shouldn't be here, it should be in WayType is one of it's fields
        wayColors.put(WayType.INVISIBLE,Color.TRANSPARENT);
        wayColors.put(WayType.AREA,Color.LAVENDER);
        wayColors.put(WayType.UNKNOWN,Color.BLACK);
        wayColors.put(WayType.BUILDING,Color.DARKGRAY);
        wayColors.put(WayType.RESIDENTIAL,Color.LAVENDER);
        wayColors.put(WayType.PIER,Color.WHITESMOKE);
        wayColors.put(WayType.TREE,Color.DARKSEAGREEN);
        wayColors.put(WayType.GRASS,Color.LIGHTGREEN);
        wayColors.put(WayType.FOREST,Color.DARKSEAGREEN);
        wayColors.put(WayType.BRIDGE,Color.DARKKHAKI);
        wayColors.put(WayType.WATER,Color.LIGHTSKYBLUE);
        wayColors.put(WayType.PARK,Color.PALEGREEN);
        wayColors.put(WayType.PITCH,Color.MEDIUMAQUAMARINE);
        wayColors.put(WayType.ARTWORK,Color.MEDIUMAQUAMARINE);
        wayColors.put(WayType.CONSTRUCTION,Color.DARKSEAGREEN);
        wayColors.put(WayType.BROWNFIELD,Color.TAN);
        wayColors.put(WayType.INDUSTRIAL,Color.THISTLE);
        wayColors.put(WayType.ALLOTMENTS,Color.PALEGREEN);
        wayColors.put(WayType.CEMETERY,Color.DARKKHAKI);
        wayColors.put(WayType.SQUARE,Color.AZURE);
        wayColors.put(WayType.PLAYGROUND,Color.MEDIUMAQUAMARINE);
        wayColors.put(WayType.BARRIER,Color.BROWN);
        wayColors.put(WayType.BEACH,Color.LEMONCHIFFON);
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
        wayColors.put(WayType.FARMYARD,Color.SANDYBROWN);
        wayColors.put(WayType.SCRUB ,Color.DARKSEAGREEN);
        wayColors.put(WayType.AIRPORT_APRON, Color.LIGHTSTEELBLUE);
        wayColors.put(WayType.AIRPORT_TAXIWAY,Color.RED);
        wayColors.put(WayType.AIRPORT_RUNWAY,Color.BLUE);
        wayColors.put(WayType.RACEWAY ,Color.LIGHTPINK);
        wayColors.put(WayType.QUARRY ,Color.DARKGRAY);
        wayColors.put(WayType.MILITARY ,Color.DARKSALMON);
        wayColors.put(WayType.STADIUM ,Color.LIGHTGRAY);
        wayColors.put(WayType.TRACK ,Color.PALEVIOLETRED);
        wayColors.put(WayType.DITCH ,Color.LIGHTSKYBLUE);
        wayColors.put(WayType.MOTORWAY ,Color.LIGHTGRAY);
        wayColors.put(WayType.BOUNDARY_ADMINISTRATIVE,Color.TRANSPARENT);
        wayColors.put(WayType.COMMERCIAL,Color.PALEVIOLETRED);
        wayColors.put(WayType.RAILWAY,Color.ORANGE);
        wayColors.put(WayType.MILLITARY,Color.BLACK);
        wayColors.put(WayType.UNDERBRIDGE,Color.DARKGRAY);
        wayColors.put(WayType.PEDESTRIAN,Color.BLACK);
        wayColors.put(WayType.RAILWAY_PLATFORM,Color.DARKGRAY);
        wayColors.put(WayType.HELIPAD,Color.LAVENDER);
        wayColors.put(WayType.BREAKWATER,Color.SLATEGREY);
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



    public void toggleNonRoads() {
        paintNonRoads = !paintNonRoads;
    }
}