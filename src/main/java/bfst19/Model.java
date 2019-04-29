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
    RouteHandler routeHandler;
    private static float lonfactor = 1.0f;
    private boolean colorBlindEnabled;
    private String datasetName;
    HashMap<Long,String> pointsOfInterest = new HashMap<>();
    TextHandler textHandler = new TextHandler();

    List<Runnable> colorObservers = new ArrayList<>();
    List<Runnable> foundMatchesObservers = new ArrayList<>();
    List<Runnable> pathObservers = new ArrayList<>();
    float minlat, minlon, maxlat, maxlon;

    String CurrentTypeColorTxt  = "src/main/resources/config/TypeColorsNormal.txt";
    HashMap<String,ArrayList<String[]>> wayTypeCases = new HashMap<>();
    ObservableList<String[]> foundMatches = FXCollections.observableArrayList();
    ObservableList<String> typeColors = FXCollections.observableArrayList();
    ObservableList<Iterable<Edge>> foundPath = FXCollections.observableArrayList();
    Map<WayType, KDTree> kdTreeMap = new TreeMap<>();

    public ArrayList<String> getTextFile(String filepath) {
        return textHandler.getTextFile(filepath);
    }

    //for building addresses during parsing
    public static class Builder {
        private int id;
        private float lat, lon;
        private String streetName = "Unknown", houseNumber="", postcode="", city="",municipality="";

        public void reset(){
            id = 0;
            lat = 0;
            lon = 0;
            streetName = "Unknown";
            houseNumber = "";
            postcode = "";
            city = "";
        }

        public boolean hasFields(){
            if(!streetName.equals("Unknown") && !houseNumber.equals("") && !postcode.equals("") &&
                    (!city.equals("") || !municipality.equals(""))) {
                return true;
            }
            return false;
        }

        public Address build() {
            if(streetName.contains("/")) {
                streetName = streetName.replaceAll("/","");
            }
            if(city.equals("")) {
                city = municipality;
            }

            return new Address(id, lat, lon, streetName, houseNumber, postcode, city);
        }
    }


    public ResizingArray<Drawable> getWaysOfType(WayType type, BoundingBox bbox) {
        return kdTreeMap.get(type).rangeQuery(bbox);
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

    public void addFoundMatchesObserver(Runnable observer) {
        foundMatchesObservers.add(observer);
    }

    public void addColorObserver(Runnable observer) {
        colorObservers.add(observer);
    }


    //TODO Might not be ideal solution if we need more then two autoTextFields...
    public void clearAddFoundMatchesObservers(){
        foundMatchesObservers = new ArrayList<>();
    }

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

    public Model(String dataset) {
        datasetName = dataset;
        //this keeps the cities and the default streets files in memory, it's about 1mb for Zealand of memory
        AddressParser.getInstance(this).setDefaults(textHandler.getDefault(getDatasetName()));
        AddressParser.getInstance(this).parseCitiesAndPostCodes(textHandler.getCities(this, getDatasetName()));
    }

    public Model(List<String> args) throws IOException, XMLStreamException, ClassNotFoundException {

        //Changed from field to local variable so it can be garbage collected
        Map<WayType, ResizingArray<Drawable>> ways = new EnumMap<>(WayType.class);
        for (WayType type : WayType.values()) {
            ways.put(type, new ResizingArray<>());
        }

        //todo figure out how to do singleton but also include model in its constructor without needing to give model for every call of getInstance
        textHandler.parseWayTypeCases("src/main/resources/config/WayTypeCases.txt", this);
        textHandler.ParseWayColors(this);

        String filename = args.get(0);
        //this might not be optimal
        String[] arr = filename.split("\\.");
        datasetName = arr[0].replace("data/","") + " Database";
        InputStream OSMSource;
        if (filename.endsWith(".obj")) {
            long time = -System.nanoTime();
            try (ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
                kdTreeMap = (Map<WayType, KDTree>) input.readObject();
                minlat = input.readFloat();
                minlon = input.readFloat();
                maxlat = input.readFloat();
                maxlon = input.readFloat();
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

            parseOSM(OSMSource);
            time += System.nanoTime();
            System.out.printf("parse time: %.1fs\n", time / 1e9);

            try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(filename + ".obj")))) {

                output.writeObject(kdTreeMap);
                output.writeFloat(minlat);
                output.writeFloat(minlon);
                output.writeFloat(maxlat);
                output.writeFloat(maxlon);
                output.writeObject(routeHandler.getNodeGraph());
            }
        }
        AddressParser.getInstance(this).setDefaults(textHandler.getDefault(getDatasetName()));
        AddressParser.getInstance(this).parseCitiesAndPostCodes(textHandler.getCities(this, getDatasetName()));
    }

    public static double getLonfactor(){
        return lonfactor;
    }

    public void switchColorScheme(boolean colorBlindEnabled) {
        //TODO Remember to remove debug println
        System.out.println("Colorblind mode enabled: " + colorBlindEnabled);

        if (colorBlindEnabled) {
            CurrentTypeColorTxt = ("src/main/resources/config/TypeColorsColorblind.txt");
        }  else {
            CurrentTypeColorTxt = ("src/main/resources/config/TypeColorsNormal.txt");
        }
        ParseWayColors();
    }


    private void parseOSM(InputStream osmsource) throws XMLStreamException {
        //Changed from field to local variable so it can be garbage collected
        Map<WayType, ResizingArray<Drawable>> ways = new EnumMap<>(WayType.class);
        for (WayType type : WayType.values()) {
            ways.put(type, new ResizingArray<>());
        }
        EdgeWeightedGraph nodeGraph = new EdgeWeightedGraph();
        //todo change to other hashmaps or do something else
        routeHandler = new RouteHandler(this,nodeGraph);
        XMLStreamReader reader = XMLInputFactory
                .newInstance()
                .createXMLStreamReader(osmsource);

        LongIndex idToNodeIndex = new LongIndex();
        LongIndex idToWayIndex = new LongIndex();

        ResizingArray<OSMNode> tempNodes = new ResizingArray<>();
        ResizingArray<OSMWay> tempWays = new ResizingArray<>();
        ArrayList<Address> addresses = new ArrayList<>();
        List<OSMWay> coast = new ArrayList<>();

        //variables to make OSMWay/OSMRelation
        OSMWay way = null;
        OSMRelation rel = null;
        WayType type = null;

        int speedlimit = 0;
        String name = "";

        //variables for addressParsing and OSMNode creation
        Builder b = new Builder();
        long id = 0;
        float lat = 0;
        float lon = 0;

        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "bounds":
                            minlat = Float.parseFloat(reader.getAttributeValue(null, "minlat"));
                            minlon = Float.parseFloat(reader.getAttributeValue(null, "minlon"));
                            maxlat = Float.parseFloat(reader.getAttributeValue(null, "maxlat"));
                            maxlon = Float.parseFloat(reader.getAttributeValue(null, "maxlon"));
                            lonfactor = (float) Math.cos((maxlat+minlat)/2*Math.PI/180);
                            minlon *= lonfactor;
                            maxlon *= lonfactor;
                            break;
                        case "node":
                            id = Long.parseLong(reader.getAttributeValue(null, "id"));
                            lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                            lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                            idToNodeIndex.add(id);
                            tempNodes.add(new OSMNode(0, lon*lonfactor, lat));
                            break;
                        case "way":
                            id = Long.parseLong(reader.getAttributeValue(null, "id"));
                            type = WayType.UNKNOWN;
                            way = new OSMWay(0);
                            idToWayIndex.add(id);
                            tempWays.add(way);
                            break;
                        case "nd":
                            long ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            way.add(tempNodes.get(idToNodeIndex.get(ref)));
                            break;
                        case "tag":
                            String k = reader.getAttributeValue(null, "k");
                            String v = reader.getAttributeValue(null, "v");

                            if(k.equals("addr:housenumber")) {
                                b.houseNumber = v.trim();
                            }

                            if(k.equals("addr:street")) {
                                b.streetName = v.trim();
                            }

                            if(k.equals("addr:postcode")) {
                                b.postcode = v.trim();
                            }


                            if(k.equals("addr:city")) {
                                b.city = v.trim();
                            }

                            if(k.equals("addr:municipality")) {
                                b.municipality = v.trim();
                            }

                            if(k.equals("source:maxspeed")){
                                if(v.equalsIgnoreCase("DK:urban")||v.equalsIgnoreCase("DK:urabn")||v.equalsIgnoreCase("DK:city")||v.equalsIgnoreCase("DK:zone50")||v.equalsIgnoreCase("dk:urban-")||v.equalsIgnoreCase("DK:uban")||v.equalsIgnoreCase("DK.urban")||v.equalsIgnoreCase("dk:urban;sign")||v.equalsIgnoreCase("urban")) {
                                    speedlimit = 50;
                                }else if (v.equalsIgnoreCase("DK:zone20")){
                                    speedlimit = 20;
                                }else if(v.equalsIgnoreCase("dk:zone30")||v.equalsIgnoreCase("DK:zone30;DK:urban")||v.equalsIgnoreCase("DK:zone:30")){
                                    speedlimit = 30;
                                }else if(v.equalsIgnoreCase("dk:zone40")||v.equalsIgnoreCase("DK:zone:40")){
                                    speedlimit = 40;
                                }else if(v.equalsIgnoreCase("DK:rural")||v.equalsIgnoreCase("dk:rutal")||v.equalsIgnoreCase("DK.rural")||v.equalsIgnoreCase("DK:trunk")){
                                    speedlimit = 80;
                                }else if(v.equals("DK:motorway")){
                                    speedlimit = 130;
                                }
                            }

                            if(k.equals("name")){
                                name = v;
                            }

                            if(k.equals("maxspeed")){
                                if(v.equalsIgnoreCase("DK:urban")){
                                    speedlimit = 50;
                                }else if(v.equalsIgnoreCase("DK:rural")){
                                    speedlimit = 80;
                                }else if(v.equalsIgnoreCase("DK:motorway")) {
                                    speedlimit = 130;
                                }else if(v.equalsIgnoreCase("default")||v.equalsIgnoreCase("implicit")||v.equalsIgnoreCase("none")||v.equalsIgnoreCase("signals")||v.equalsIgnoreCase("5 knots")){
                                }else{
                                    speedlimit = Math.round(Float.valueOf(v));
                                }
                            }

                            //string[0]=waytype's name, strings[1] = k for the case, strings = v for the case.
                            for(Map.Entry<String,ArrayList<String[]>> wayType : wayTypeCases.entrySet()){
                                String wayTypeString = wayType.getKey();
                                for(String[] waycase : wayType.getValue()) {
                                    if(k.equals(waycase[0])&&v.equals(waycase[1])) {
                                        type = WayType.valueOf(wayTypeString);
                                    }
                                }
                            }

                            routeHandler.checkDrivabillty(k,v);

                            switch (k){
                                case "relation":
                                    type = WayType.UNKNOWN;
                                    rel = new OSMRelation();
                                    break;
                                case "member":
                                    ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                                    OSMWay member = tempWays.get(idToWayIndex.get(ref));
                                    if (member != null) rel.add(member);
                                    break;
                            }
                            break;
                        case "relation":
                            type = WayType.UNKNOWN;
                            rel = new OSMRelation();
                            break;
                        case "member":
                            ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            int index = idToWayIndex.get(ref);
                            if(!(index < 0)){
                                OSMWay member = tempWays.get(idToWayIndex.get(ref));
                                if (member != null) rel.add(member);
                            }
                            break;
                    }
                    break;
                case END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "way":

                            //checks if the current waytype is one
                            // of the one's that should be in the nodegraph
                            boolean isNodeGraphWay = routeHandler.isNodeGraphWay(type);
                            if(isNodeGraphWay) {
                                routeHandler.addWayToNodeGraph(way, type,name,speedlimit);
                            }

                            if (type == WayType.COASTLINE) {
                                coast.add(way);
                            } else {
                                ways.get(type).add(new Polyline(way,isNodeGraphWay));
                            }

                            if(b.hasFields()) {
                                b.id = tempNodes.size();
                                b.lat = lat;
                                b.lon = lon;
                                addresses.add(b.build());
                            }

                            //resetting variables
                            way = null;
                            b.reset();
                            speedlimit = 0;
                            name = "";
                            break;
                        case "node":
                            if(b.hasFields()) {
                                b.id = tempNodes.size();
                                b.lat = lat;
                                b.lon = lon;
                                addresses.add(b.build());
                            }
                            b.reset();
                            break;
                        case "relation":
                            //Add rel to the list associated with its WayType in ways if type is one of following:
                            // WATER, BUILDING, FOREST, FARMLAND, PARK, RECREATION, BOUNDARY_ADMINISTRATIVE,
                            // RAILWAY_PLATFORM, CONSTRUCTION, PARKING
                            if (type == WayType.WATER || type == WayType.BUILDING || type == WayType.FOREST
                                    || type == WayType.FARMLAND || type == WayType.PARK || type == WayType.RECREATION
                                    || type == WayType.BOUNDARY_ADMINISTRATIVE || type == WayType.RAILWAY_PLATFORM
                                    || type == WayType.CONSTRUCTION || type == WayType.PARKING) {
                                ways.get(type).add(new MultiPolyline(rel));
                                way = null;
                            }else if(type == WayType.BUILDING){
                                ways.get(type).add(new MultiPolyline(rel));
                            }else if(type == WayType.FOREST){
                                ways.get(type).add(new MultiPolyline(rel));
                            }else if(type == WayType.FARMLAND){
                                ways.get(type).add(new MultiPolyline(rel));
                            }else if(type == WayType.PARK){
                                ways.get(type).add(new MultiPolyline(rel));
                            }else if(type == WayType.RECREATION){
                                ways.get(type).add(new MultiPolyline(rel));
                            }else if(type == WayType.BOUNDARY_ADMINISTRATIVE){
                                ways.get(type).add(new MultiPolyline(rel));
                            }else if(type == WayType.RAILWAY_PLATFORM){
                                ways.get(type).add(new MultiPolyline(rel));
                            }else if(type == WayType.CONSTRUCTION){
                                ways.get(type).add(new MultiPolyline(rel));
                            }else if(type == WayType.PARKING){
                                ways.get(type).add(new MultiPolyline(rel));
                            }
                            break;
                    }
                    break;
                case PROCESSING_INSTRUCTION: break;
                case CHARACTERS: break;
                case COMMENT: break;
                case SPACE: break;
                case START_DOCUMENT: break;
                case END_DOCUMENT:
                    tempNodes = null;
                    tempWays = null;
                    idToNodeIndex = null;
                    idToWayIndex = null;
                    routeHandler.finishNodeGraph();
                    for (OSMWay c : merge(coast)) {
                        ways.get(WayType.COASTLINE).add(new Polyline(c,false));
                    }

                    //Make and populate KDTrees for each WayType
                    for(Map.Entry<WayType, ResizingArray<Drawable>> entry : ways.entrySet()) {
                        KDTree typeTree = new KDTree();
                        //Add entry values to KDTree
                        typeTree.insertAll(entry.getValue());
                        //Add KDTree to TreeMap
                        kdTreeMap.put(entry.getKey(), typeTree);
                    }

                    addresses.sort(Address::compareTo);
                    textHandler.makeDatabase(this, addresses, getDatasetName());
                    addresses = null;
                    break;
                case ENTITY_REFERENCE: break;
                case ATTRIBUTE: break;
                case DTD: break;
                case CDATA: break;
                case NAMESPACE: break;
                case NOTATION_DECLARATION: break;
                case ENTITY_DECLARATION: break;
            }
        }
    }


    public float calculateDistanceInMeters(double startLat, double startLon, double endLat, double endLon){
        //Found the formula on https://www.movable-type.co.uk/scripts/latlong.html
        //Basically the same code as is shown on the site mentioned above
        final int R = 6371000; // CA. Earth radius in meters
        double φ1  = Math.toRadians(startLat);
        double φ2  = Math.toRadians(endLat);
        double Δφ  = Math.toRadians(endLat - startLat);
        double Δλ  = Math.toRadians(endLon - startLon);

        double a  = Math.sin(Δφ/2)* Math.sin(Δφ/2) + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ/2) * Math.sin(Δλ/2);
        double c  = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return (float)d;
    }

    public static float angleBetween2Lines(OSMNode A1, OSMNode A2, OSMNode B1, OSMNode B2) {
        float angle1 = (float) Math.atan2(A2.getLat() - A1.getLat(), A1.getLon() - A2.getLon());
        float angle2 = (float) Math.atan2(B2.getLat() - B1.getLat(), B1.getLon() - B2.getLon());
        float calculatedAngle = (float) Math.toDegrees(angle1 - angle2);
        if (calculatedAngle < 0) calculatedAngle += 360;
        return calculatedAngle;
    }

    public String getDelimeter() {
        return " QQQ ";
    }

    private Iterable<OSMWay> merge(List<OSMWay> coast) {
        Map<OSMNode, OSMWay> pieces = new HashMap<>();
        for (OSMWay way : coast) {
            OSMWay res = new OSMWay(0);
            OSMWay before = pieces.remove(way.getFirst());
            if (before != null) {
                pieces.remove(before.getFirst());
                for (int i = 0 ; i < before.size() - 1 ; i++) {
                    res.add(before.get(i));
                }
            }
            //TODO figure out why this works and why it can't be refactored easily into OSMWay without inheritance
            for(int i = 0 ; i < way.size() ; i++){
                res.add(way.get(i));
            }
            OSMWay after = pieces.remove(way.getLast());
            if (after != null) {
                pieces.remove(after.getLast());
                for (int i = 1 ; i < after.size() ; i++) {
                    res.add(after.get(i));
                }
            }
            pieces.put(res.getFirst(), res);
            pieces.put(res.getLast(), res);
        }
        return pieces.values();
    }

    public void ParseWayColors() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(CurrentTypeColorTxt));
            int m = Integer.parseInt(br.readLine());

            for (int i = 0 ; i < m ; i++) {
                String[] strArr = br.readLine().split(" ");
                typeColors.add(strArr[0]);
                typeColors.add(strArr[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't create a file reader from color scheme file (" + CurrentTypeColorTxt + ") or failed to read from it.");
        }

        notifyColorObservers();
    }

    public void parseSearch(String proposedAddress) {
        Address a = AddressParser.getInstance(this).singleSearch(proposedAddress, getDatasetName());
        //if the address does not have a city or a streetname, get the string's matches from the default file and display them
        if(a.getStreetName().equals("Unknown") || a.getCity().equals("")) {
            ArrayList<String[]> possibleMatches =
                    AddressParser.getInstance(this).getMatchesFromDefault(proposedAddress, false);

            if (possibleMatches != null) {
                foundMatches.clear();
                System.out.println(possibleMatches.size());
                for (String[] match : possibleMatches) {
                    foundMatches.add(new String[]{match[0],match[1],match[2]});
                }
            }
        }else if(a.getHouseNumber()==null){
            //if the housenumber is null, bet all the addresses housenumbers from the streets file and display them
            ArrayList<String[]> possibleAddresses = AddressParser.getInstance(this).getAddress(getDatasetName(),a.getCity(),a.getPostcode(),a.getStreetName(),"",false);
            if (possibleAddresses != null) {
                foundMatches.clear();
                String street = a.getStreetName();
                String city = a.getCity();
                String postcode = a.getPostcode();
                for (String[] match : possibleAddresses) {
                    foundMatches.add(new String[]{street, match[2], city, postcode});
                }
            }
        }else{
            //if those 3 fields are filled, just put the address in the ui will handle the rest
            foundMatches.clear();
            foundMatches.add(new String[]{String.valueOf(a.getLon()),
                    String.valueOf(a.getLat()), a.getStreetName(), a.getHouseNumber(),
                    a.getFloor(), a.getSide(), a.getCity(), a.getPostcode()});
        }
        notifyFoundMatchesObservers();
    }

    public String getDatasetName() {
        return datasetName;
    }

    public ArrayList<String> getAddressesOnStreet(String country,String city,String postcode,String streetName){
        return textHandler.getTextFile("data/"+country+"/"+city+" QQQ "+postcode+"/"+streetName+".txt");
    }

    public void writePointsOfInterest(String datasetName) {
        try {
            BufferedWriter pointsOfInterestWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            new File("data/" + datasetName + "/pointsOfInterest.txt"))
                            ,"UTF-8"));

            for(Map.Entry<Long, String> entry : pointsOfInterest.entrySet()) {
                pointsOfInterestWriter.write(entry.getKey() + getDelimeter() + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't create an OutputStream for points of interests or failed to write to it.");
        }
    }

    public HashMap<Long,String> getPointsOfInterest(String datasetName){
        HashMap<Long,String> pointsOfInterest = new HashMap<>();
        ArrayList<String> pointOfInterestFile = textHandler.getTextFile("data/"+datasetName+"/pointsOfInterest.txt");
        for(String address : pointOfInterestFile){
            String[] addressFields = address.split(getDelimeter());
            long id = Long.valueOf(addressFields[0]);
            String addressString = addressFields[1]+getDelimeter()+addressFields[2]+getDelimeter()+addressFields[3]+getDelimeter()+addressFields[4]+getDelimeter()+getDelimeter()+addressFields[5];
            pointsOfInterest.put(id,addressString);
        }
        return pointsOfInterest;
    }

    public void addPointsOfInterest(long id,String pointOfInterest) {
        pointsOfInterest.put(id,pointOfInterest);
    }

    public void removePointOfInterest(long id) {
        pointsOfInterest.remove(id);
    }

    //// Does this contain the in
    public Iterator<Edge> pathIterator(){
        return foundPath.iterator().next().iterator();
    }

    public Iterator<String> colorIterator() {
        return typeColors.iterator();
    }

    public Iterator<String[]> foundMatchesIterator() {
        return foundMatches.iterator();
    }

    public static OSMNode getClosestNode(Point2D point, ArrayList<OSMNode> queryList) {
        //TODO: put into a "Calculator" class.
        double closestDistance = Double.POSITIVE_INFINITY;
        double distanceToQueryPoint;
        OSMNode closestElement = null;

        for(OSMNode checkNode: queryList){
            //We check distance from node to point, and then report if its closer than our previously known closest point.
            distanceToQueryPoint = checkNode.distanceTo(point);
            if(distanceToQueryPoint < closestDistance){
                closestDistance = distanceToQueryPoint;
                closestElement = checkNode;
            }
        }
        return closestElement;
    }

    OSMNode getNearestRoad(Point2D point){
        try{
            ArrayList<OSMNode> nodeList = new ArrayList<>();

            for(WayType wayType: RouteHandler.getDrivableWayTypes()){
                OSMNode checkNeighbor = kdTreeMap.get(wayType).getNearestNeighbor(point);
                if(checkNeighbor != null) {
                    nodeList.add(checkNeighbor);
                }
            }

            if(nodeList.isEmpty()){
                throw new nothingNearbyException();
            }

            return getClosestNode(point, nodeList);

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
}