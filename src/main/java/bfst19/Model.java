package bfst19;

import bfst19.Exceptions.nothingNearbyException;
import bfst19.KDTree.BoundingBox;
import bfst19.KDTree.Drawable;
import bfst19.KDTree.KDTree;
import bfst19.Route_parsing.*;
import bfst19.Line.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.*;
import java.util.zip.ZipInputStream;

import static javax.xml.stream.XMLStreamConstants.*;

public class Model{


    private RouteHandler routeHandler;
    private static float lonfactor = 1.0f;
    private String datasetName;
    private static String dirPath;
    private TextHandler textHandler = new TextHandler();

    private List<Runnable> colorObservers = new ArrayList<>();
    private List<Runnable> foundMatchesObservers = new ArrayList<>();
    private List<Runnable> pathObservers = new ArrayList<>();
    private float minlat;
    private float minlon;
    private float maxlat;
    private float maxlon;

    private ObservableList<PointOfInterestItem> pointOfInterestItems = FXCollections.observableArrayList();
    private String currentTypeColorTxt = "src/main/resources/config/TypeColorsNormal.txt";
    private HashMap<WayType,ResizingArray<String[]>> wayTypeCases = new HashMap<>();
    private ObservableList<String[]> foundMatches = FXCollections.observableArrayList();
    private ObservableList<String[]> typeColors = FXCollections.observableArrayList();
    private ObservableList<Iterable<Edge>> foundPath = FXCollections.observableArrayList();
    //todo change to other hashmaps or do something else
    private Map<WayType, KDTree> kdTreeMap = new TreeMap<>();

    //used for addresstesting
    public Model(String dirPath) {
        this.dirPath = dirPath;
        //this keeps the cities and the default streets files in memory, it's about 1mb for Zealand of memory
        AddressParser.getInstance().setDefaults();
        AddressParser.getInstance().setCities();
    }

    public Model(List<String> args) throws IOException, XMLStreamException, ClassNotFoundException {

        //Changed from field to local variable so it can be garbage collected
        Map<WayType, ResizingArray<Drawable>> ways = new EnumMap<>(WayType.class);
        for (WayType type : WayType.values()) {
            ways.put(type, new ResizingArray<>());
        }

        wayTypeCases = textHandler.parseWayTypeCases("src/main/resources/config/WayTypeCases.txt");
        textHandler.ParseWayColors(this);

        String filename = args.get(0);
        //this might not be optimal
        String[] arr = filename.split("\\.");
        datasetName = arr[0].replace("data/","") + " Database";
        dirPath = "data/"+datasetName;
        InputStream OSMSource;
        if (filename.endsWith(".obj")) {
            long time = -System.nanoTime();
            try (ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
                kdTreeMap = (Map<WayType, KDTree>) input.readObject();
                minlat = input.readFloat();
                minlon = input.readFloat();
                maxlat = input.readFloat();
                maxlon = input.readFloat();
                lonfactor = input.readFloat();
                routeHandler = new RouteHandler(this,(EdgeWeightedGraph)input.readObject());
            }
            time += System.nanoTime();
            System.out.printf("Load time: %.1fs\n", time / 1e9);
        } else {
            long time = -System.nanoTime();
            if (filename.endsWith(".zip")) {
                ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(filename)));
                zip.getNextEntry();
                OSMSource = zip;
            } else {
                OSMSource = new BufferedInputStream(new FileInputStream(filename));
            }
            EdgeWeightedGraph nodeGraph = new EdgeWeightedGraph();
            routeHandler = new RouteHandler(this,nodeGraph);
            OSMParser.parseOSM(OSMSource,routeHandler,this,textHandler,wayTypeCases);
            time += System.nanoTime();
            System.out.printf("parse time: %.1fs\n", time / 1e9);

            try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(filename + ".obj")))) {

                output.writeObject(kdTreeMap);
                output.writeFloat(minlat);
                output.writeFloat(minlon);
                output.writeFloat(maxlat);
                output.writeFloat(maxlon);
                output.writeFloat(lonfactor);
                output.writeObject(routeHandler.getNodeGraph());
            }
        }
        setPointsOfInterest();
        AddressParser.getInstance().setDefaults();
        AddressParser.getInstance().setCities();
    }



    public void addFoundMatchesObserver(Runnable observer) {
        foundMatchesObservers.add(observer);
    }

    public void addColorObserver(Runnable observer) {
        colorObservers.add(observer);
    }

    public void addPathObserver(Runnable observer){pathObservers.add(observer);}

    public void addPath(Iterable<Edge> path){
        foundPath.add(path);
        notifyPathObservers();
    }

    public void clearPath(){
        if(!foundPath.isEmpty()){
            foundPath.clear();
            notifyPathObservers();
        }
    }

    public void addFoundMatch(String[] match){
        foundMatches.add(match);
    }

    public void clearMatches(){
        foundMatches.clear();
    }

    public void clearColors(){
        if(!typeColors.isEmpty()){
            typeColors.clear();
        }
    }

    public void clearFoundMatchesObservers(){
        foundMatchesObservers = new ArrayList<>();
    }

    //notify observer methods run through each array of observers and calls their run() method which they themselves have defined
    public void notifyFoundMatchesObservers() {
        for (Runnable observer : foundMatchesObservers) {
            observer.run();
        }
    }

    public void notifyColorObservers() {
        for (Runnable observer : colorObservers) {
            observer.run();
        }
    }

    public void notifyPathObservers(){
        for(Runnable observer : pathObservers) {
            observer.run();
        }
    }

    // Does this contain the in
    public Iterator<Edge> pathIterator(){
        return foundPath.iterator().next().iterator();
    }

    public Iterator<String[]> colorIterator() {
        return typeColors.iterator();
    }

    public Iterator<String[]> foundMatchesIterator() {
        return foundMatches.iterator();
    }

    public ObservableList<PointOfInterestItem> pointOfInterestList(){ return pointOfInterestItems; }

    public void addPointOfInterestItem(PointOfInterestItem pointOfInterestItem){
        for(PointOfInterestItem item : pointOfInterestItems){
            if(pointOfInterestItem.equals(item)){
                return;
            }
        }
        pointOfInterestItems.add(pointOfInterestItem);
    }

    public void removePointOfInterestItem(PointOfInterestItem pointOfInterestItem) { pointOfInterestItems.remove(pointOfInterestItem);}

    private void setPointsOfInterest(){
        List<PointOfInterestItem> list = TextHandler.getInstance().getPointsOfInterest(dirPath);
        for(PointOfInterestItem item : list){
            pointOfInterestItems.add(item);
        }
    }

    public void writePointsOfInterest(){
        TextHandler.getInstance().writePointsOfInterest(dirPath,pointOfInterestItems);
    }

    public String getCurrentTypeColorTxt(){
        return currentTypeColorTxt;
    }

    public void addTypeColors(String[] color){
        typeColors.add(color);
    }

    public void setMinlon(float minlon){
        this.minlon = minlon;
    }

    public void setMaxlon(float maxlon){
        this.maxlon = maxlon;
    }

    public void setMinlat(float minlat){ this.minlat = minlat;  }

    public void setMaxlat(float maxlat){
        this.maxlat= maxlat;
    }

    public void setLonfactor(float lonfactor){ this.lonfactor = lonfactor;}

    public void setKdTrees(Map<WayType, KDTree> kdTreeMap) {
        this.kdTreeMap = kdTreeMap;
    }

    public float getMinlon(){
        return minlon;
    }

    public float getMinlat() {
        return minlat;
    }

    public float getMaxlat() {
        return maxlat;
    }

    public float getMaxlon() {
        return maxlon;
    }


    public static String getDelimeter() {
        return " QQQ ";
    }

    public String getDatasetName() {
        return datasetName;
    }


    //returns the ways of the given type within the given boundingbox
    public ResizingArray<Drawable> getWaysOfType(WayType type, BoundingBox bbox) {
        return kdTreeMap.get(type).rangeQuery(bbox);
    }

    public static double getLonfactor(){
        return lonfactor;
    }

    public static String getDirPath(){
        return dirPath;
    }

    public void switchColorScheme(boolean colorBlindEnabled) {
        //TODO Remember to remove debug println
        System.out.println("Colorblind mode enabled: " + colorBlindEnabled);

        if (colorBlindEnabled) {
            currentTypeColorTxt = ("src/main/resources/config/TypeColorsColorblind.txt");
        }  else {
            currentTypeColorTxt = ("src/main/resources/config/TypeColorsNormal.txt");
        }
        textHandler.ParseWayColors(this);
    }

    public Iterable<Edge> findPath(int startId , int endId , Vehicle type , boolean fastestPath){
        return routeHandler.findPath(startId,endId,type,fastestPath);
    }

    public void parseSearch(String proposedAddress){
        AddressParser.getInstance().parseSearch(proposedAddress,this);
    }


    OSMNode getNearestRoad(Point2D point){
        try{
            ResizingArray<OSMNode> nodeList = new ResizingArray<>();

            for(WayType wayType: RouteHandler.getDrivableWayTypes()){
                OSMNode checkNeighbor = kdTreeMap.get(wayType).getNearestNeighbor(point);
                if(checkNeighbor != null) {
                    nodeList.add(checkNeighbor);
                }
            }

            if(nodeList.isEmpty()){
                throw new nothingNearbyException();
            }

            return Calculator.getClosestNode(point, nodeList);

        }catch (nothingNearbyException e){
            e.printStackTrace();
            return null;
        }
    }

    OSMNode getNearestBuilding(Point2D point){
        try {
            OSMNode closestElement = kdTreeMap.get(WayType.BUILDING).getNearestNeighbor(point);

            if(closestElement == null){
                throw new nothingNearbyException();
            }

            return closestElement;

        }catch(nothingNearbyException e){
            e.printStackTrace();
            return null;
        }


    }

    public HashMap<String, Integer> parseSpeedDefaults(String s) {
        return textHandler.parseSpeedDefaults(s);
    }

    public HashMap<WayType, HashMap<String, ResizingArray<String[]>>> parseDrivableCases(String s) {
        return textHandler.parseDrivableCases(s);
    }

    public RouteHandler getRouteHandler() {
        return routeHandler;
    }
}
