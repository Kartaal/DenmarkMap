package bfst19;

import bfst19.Exceptions.nothingNearbyException;
import bfst19.KDTree.BoundingBox;
import bfst19.KDTree.Drawable;
import bfst19.KDTree.KDTree;
import bfst19.Line.OSMNode;
import bfst19.Route_parsing.Edge;
import bfst19.Route_parsing.EdgeWeightedDigraph;
import bfst19.Route_parsing.RouteHandler;
import bfst19.Route_parsing.Vehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;
import java.util.zip.ZipInputStream;

public class Model {

	private static float lonfactor = 1.0f;
	private static String dirPath;
	private RouteHandler routeHandler;
	private TextHandler textHandler = TextHandler.getInstance();

	private List<Runnable> colorObservers = new ArrayList<>();
	private List<Runnable> foundMatchesObservers = new ArrayList<>();
	private List<Runnable> pathObservers = new ArrayList<>();
	private float minlat;
	private float minlon;
	private float maxlat;
	private float maxlon;

	private ObservableList<PointOfInterestItem> pointOfInterestItems = FXCollections.observableArrayList();
	private String currentTypeColorTxt = "config/TypeColorsNormal.txt";
	private ObservableList<String[]> foundMatches = FXCollections.observableArrayList();
	private ObservableList<String[]> typeColors = FXCollections.observableArrayList();
	private ObservableList<Iterable<Edge>> foundPath = FXCollections.observableArrayList();
	private Map<WayType, KDTree> kdTreeMap = new TreeMap<>();

	//used for address testing
	public Model(String dirPath) {
		Model.dirPath = dirPath;
        TextHandler.getInstance().setHasInputFile(true);
		AddressParser.getInstance().setDefaults();
		AddressParser.getInstance().setCities();
	}

	public Model(List<String> args) throws IOException, XMLStreamException, ClassNotFoundException {

		//Setup so TextHandler deals with default database in jar file
		boolean hasInputFile = !args.isEmpty();
		textHandler.setHasInputFile(hasInputFile);

		HashMap<WayType, ResizingArray<String[]>> wayTypeCases = textHandler.parseWayTypeCases();
		textHandler.parseWayColors(this);

		//Check if program is run with input argument, if not use default file (binary denmark)
		String filename;
		if (hasInputFile) {
			filename = args.get(0);
		} else {
			//TODO add denmark!!!!!!!
			filename = "amager.zip.obj";
		}

		String[] arr = filename.split("\\.");

		//When using jar, the file name might be an absolute path,
		// so replace the last \ with \data\ for the database directory
		String datasetName;
		if (hasInputFile) {                        //Matches the last \ in string
			datasetName = arr[0].replaceAll("\\\\(?!.*)$", "\\data\\") + " Database";
		} else {
			datasetName = arr[0].replace("data/", "") + " Database";
		}

		dirPath = datasetName;

		InputStream OSMSource;
		if (filename.endsWith(".obj")) {
			readObjFile(filename, hasInputFile);

		} else {

			if (filename.endsWith(".zip")) {
				ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(filename)));
				zip.getNextEntry();
				OSMSource = zip;
			} else {
				OSMSource = new BufferedInputStream(new FileInputStream(filename));
			}

			EdgeWeightedDigraph nodeGraph = new EdgeWeightedDigraph();
			routeHandler = new RouteHandler(nodeGraph);
			OSMParser.parseOSM(OSMSource, routeHandler, this, textHandler, wayTypeCases);

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

	static String getDelimeter() {
		return " QQQ ";
	}

	public static double getLonfactor() {
		return lonfactor;
	}

	void setLonfactor(float lonfactor) {
		Model.lonfactor = lonfactor;
	}

	static String getDirPath() {
		return dirPath;
	}

	//Reads an object file located at filename,
	// if hasInputFile is false, it needs to read the file from a different location
	private void readObjFile(String filename, boolean hasInputFile) throws IOException, ClassNotFoundException {
		if (hasInputFile) {
			try (ObjectInputStream input = new ObjectInputStream(
					new BufferedInputStream(new FileInputStream(filename)))) {
				initFieldsFromObjFile(input);
			}

		} else {
			try (ObjectInputStream input = new ObjectInputStream(
					new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(filename)))) {
				initFieldsFromObjFile(input);
			}
		}
	}

	//Helper method to prevent code duplication that merely sets up the fields.
	private void initFieldsFromObjFile(ObjectInputStream input) throws IOException, ClassNotFoundException {
		kdTreeMap = (Map<WayType, KDTree>) input.readObject();
		minlat = input.readFloat();
		minlon = input.readFloat();
		maxlat = input.readFloat();
		maxlon = input.readFloat();
		lonfactor = input.readFloat();
		routeHandler = new RouteHandler((EdgeWeightedDigraph) input.readObject());
	}

	void addFoundMatchesObserver(Runnable observer) {
		foundMatchesObservers.add(observer);
	}

	void addColorObserver(Runnable observer) {
		colorObservers.add(observer);
	}

	void addPathObserver(Runnable observer) {
		pathObservers.add(observer);
	}

	void addPath(Iterable<Edge> path) {
		foundPath.add(path);
		notifyPathObservers();
	}

	void clearPath() {
		if (!foundPath.isEmpty()) {
			foundPath.clear();
			notifyPathObservers();
		}
	}

	void addFoundMatch(String[] match) {
		foundMatches.add(match);
	}

	void clearMatches() {
		foundMatches.clear();
	}

	void clearColors() {
		if (!typeColors.isEmpty()) {
			typeColors.clear();
		}
	}

	void clearFoundMatchesObservers() {
		foundMatchesObservers = new ArrayList<>();
	}

	void notifyFoundMatchesObservers() {
		for (Runnable observer : foundMatchesObservers) {
			observer.run();
		}
	}

	void notifyColorObservers() {
		for (Runnable observer : colorObservers) {
			observer.run();
		}
	}

	private void notifyPathObservers() {
		for (Runnable observer : pathObservers) {
			observer.run();
		}
	}

	Iterator<Edge> pathIterator() {
		if (foundPath.size() > 0)
			return foundPath.iterator().next().iterator();
		else return null;
	}

	Iterator<String[]> colorIterator() {
		return typeColors.iterator();
	}

	Iterator<String[]> foundMatchesIterator() {
		return foundMatches.iterator();
	}

	ObservableList<PointOfInterestItem> pointOfInterestList() {
		return pointOfInterestItems;
	}

	void addPointOfInterestItem(PointOfInterestItem pointOfInterestItem) {

		for (PointOfInterestItem item : pointOfInterestItems) {
			if (pointOfInterestItem.equals(item)) {
				return;
			}
		}

		pointOfInterestItems.add(pointOfInterestItem);
	}

	void removePointOfInterestItem(PointOfInterestItem pointOfInterestItem) {
		pointOfInterestItems.remove(pointOfInterestItem);
	}

	private void setPointsOfInterest() {
		List<PointOfInterestItem> list = TextHandler.getInstance().getPointsOfInterest(dirPath);
		pointOfInterestItems.addAll(list);
	}

	void writePointsOfInterest() {
		TextHandler.getInstance().writePointsOfInterest(dirPath, pointOfInterestItems);
	}

	String getCurrentTypeColorTxt() {
		return currentTypeColorTxt;
	}

	void addTypeColors(String[] color) {
		typeColors.add(color);
	}

	void setKdTrees(Map<WayType, KDTree> kdTreeMap) {
		this.kdTreeMap = kdTreeMap;
	}

	float getMinlon() {
		return minlon;
	}

	void setMinlon(float minlon) {
		this.minlon = minlon;
	}

	float getMinlat() {
		return minlat;
	}

	void setMinlat(float minlat) {
		this.minlat = minlat;
	}

	float getMaxlat() {
		return maxlat;
	}

	void setMaxlat(float maxlat) {
		this.maxlat = maxlat;
	}

	float getMaxlon() {
		return maxlon;
	}

	void setMaxlon(float maxlon) {
		this.maxlon = maxlon;
	}

	ResizingArray<Drawable> getWaysOfType(WayType type, BoundingBox bbox) {
		return kdTreeMap.get(type).rangeQuery(bbox);
	}

	void switchColorScheme(boolean colorBlindEnabled) {
		if (colorBlindEnabled) {
			currentTypeColorTxt = ("config/TypeColorsColorblind.txt");
		} else {
			currentTypeColorTxt = ("config/TypeColorsNormal.txt");
		}

		textHandler.parseWayColors(this);
	}

	Iterable<Edge> findPath(OSMNode startNode, OSMNode endNode, Vehicle type, boolean fastestPath) {
		return routeHandler.findPath(startNode, endNode, type, fastestPath);
	}

	void parseSearch(String proposedAddress) {
		AddressParser.getInstance().parseSearch(proposedAddress, this);
	}

	OSMNode getNearestRoad(Point2D point, Vehicle type) throws nothingNearbyException {
		ResizingArray<OSMNode> nodeList = new ResizingArray<>();

		for (WayType wayType : RouteHandler.getDrivableWayTypes()) {
			OSMNode checkNeighbor = kdTreeMap.get(wayType).getNearestNeighbor(point, type);
			if (checkNeighbor != null) {
				nodeList.add(checkNeighbor);
			}
		}

		if (nodeList.isEmpty()) {
			throw new nothingNearbyException();
		}

		return Calculator.getClosestNode(point, nodeList);
	}

	/*OSMNode getNearestBuilding(Point2D point) {
		try {
			OSMNode closestElement = kdTreeMap.get(WayType.BUILDING).getNearestNeighbor(point, Vehicle.ABSTRACTVEHICLE);

			if (closestElement == null) {
				throw new nothingNearbyException();
			}

			return closestElement;

		} catch (nothingNearbyException e) {
			e.printStackTrace();
			return null;
		}
	}*/
}