package bfst19.osmdrawing;

import bfst19.osmdrawing.KDTree.KDTree;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.*;
import java.util.zip.ZipInputStream;

import static javax.xml.stream.XMLStreamConstants.*;

public class Model {
	float lonfactor = 1.0f;
	Map<WayType, List<Drawable>> ways = new EnumMap<>(WayType.class);
	private boolean colorBlindEnabled;

	List<Runnable> observers = new ArrayList<>();
	float minlat, minlon, maxlat, maxlon;

	String CurrentTypeColorTxt  = "data/TypeColorsNormal.txt";
	Map<String, WayType> waytypes = new HashMap<>();
	ArrayList<String[]> wayTypeCases = new ArrayList<>();
	ObservableList<Address> searchAddresses = FXCollections.observableArrayList();
	ObservableList<String> typeColors = FXCollections.observableArrayList();
	Map<WayType, KDTree> kdTreeMap;
	KDTree tree;

	//TODO filthy disgusting typecasting
	public Iterable<Drawable> getWaysOfType(WayType type, Bounds bbox) {
		return kdTreeMap.get(type).rangeQuery((BoundingBox) bbox);
	}

	public void addObserver(Runnable observer) {
		observers.add(observer);
	}

	public void notifyObservers() {
		for (Runnable observer : observers) observer.run();
	}

	public Model(List<String> args) throws IOException, XMLStreamException, ClassNotFoundException {

		for (WayType type : WayType.values()) {
			ways.put(type, new ArrayList<>());
		}

		for(WayType type: WayType.values()){
			waytypes.put(type.name(),type);
		}

		parseCases("data/Waytype_cases.txt");
		ParseWayColors();


		String filename = args.get(0);
		InputStream osmsource;
		if (filename.endsWith(".obj")) {
			long time = -System.nanoTime();
			try (ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
				ways = (Map<WayType, List<Drawable>>) input.readObject();
				minlat = input.readFloat();
				minlon = input.readFloat();
				maxlat = input.readFloat();
				maxlon = input.readFloat();
			}
			time += System.nanoTime();
			System.out.printf("Load time: %.1fs\n", time / 1e9);
		} else {
			long time = -System.nanoTime();
			if (filename.endsWith(".zip")) {
				ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(filename)));
				zip.getNextEntry();
				osmsource = zip;
			} else {
				osmsource = new BufferedInputStream(new FileInputStream(filename));
			}
			parseOSM(osmsource);
			time += System.nanoTime();
			System.out.printf("Parse time: %.1fs\n", time / 1e9);
			try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename + ".obj")))) {
				output.writeObject(ways);
				output.writeFloat(minlat);
				output.writeFloat(minlon);
				output.writeFloat(maxlat);
				output.writeFloat(maxlon);
			}
		}
	}

	public void ParseWayColors(){
		try {
			BufferedReader br = new BufferedReader(new FileReader(CurrentTypeColorTxt));
			int m = Integer.parseInt(br.readLine());

			for (int i = 0; i < m; i++) {
				String[] strArr = br.readLine().split(" ");
				typeColors.add(strArr[0]);
				typeColors.add(strArr[1]);
			}
		}
		catch(Exception e){
			e.printStackTrace();
			//TODO: fix this, uncle bob wont like this one hehe;)
			System.out.println("something went wrong");
		}
		notifyObservers();
	}

	public void switchColorScheme(){
		colorBlindEnabled = !colorBlindEnabled;
		System.out.println("Colorblind mode enabled: " + colorBlindEnabled);

			if (colorBlindEnabled){
				CurrentTypeColorTxt = ("data/TypeColorsColorblind.txt");
			}
			else{
				CurrentTypeColorTxt = ("data/TypeColorsNormal.txt");
			}
			ParseWayColors();
		}

	private void parseOSM(InputStream osmsource) throws XMLStreamException {
		XMLStreamReader reader = XMLInputFactory
				.newInstance()
				.createXMLStreamReader(osmsource);
		LongIndex<OSMNode> idToNode = new LongIndex<OSMNode>();
		LongIndex<OSMWay> idToWay = new LongIndex<OSMWay>();
		List<OSMWay> coast = new ArrayList<>();
		ArrayList<String[]> cityBoundaries = new ArrayList<>();
		TreeMap<String, ArrayList<String[]>> addressNodes = new TreeMap<>();

		OSMWay way = null;
		OSMRelation rel = null;
		WayType type = null;

		//variables for addressParsing
		String houseNumber = "";
		String streetName = "";
		String name = "";
		boolean isAddress = false;
		boolean isCity = false;

		//might be better solutions
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
							lon = lonfactor * Float.parseFloat(reader.getAttributeValue(null, "lon"));
							idToNode.add(new OSMNode(id, lon, lat));
							break;
						case "way":
							id = Long.parseLong(reader.getAttributeValue(null, "id"));
							type = WayType.UNKNOWN;
							way = new OSMWay(id);
							idToWay.add(way);
							break;
						case "nd":
							long ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
							way.add(idToNode.get(ref));
							break;
						case "tag":
							String k = reader.getAttributeValue(null, "k");
							String v = reader.getAttributeValue(null, "v");

							if(k.equals("addr:housenumber")){
								houseNumber = v;
								isAddress = true;
							}
							if(k.equals("addr:street")){
								streetName = v;
								isAddress = true;
							}

							if(k.equals("name")){
								name = v;
							}

							//This is perhaps not general enough. It is flagged as defacto on the OSM wiki
							//but it seems that admin level 7 is the consensus for danish city boundaries
							if(k.equals("admin_level") && v.equals("7")){
								isCity = true;
							}


							for(String[] strings : wayTypeCases){
								if(k.equals(strings[1]) && v.equals(strings[2])){
									type = waytypes.get(strings[0]);
									break;
								}
							}
							switch (k){
								case "relation":
									type = WayType.UNKNOWN;
									rel = new OSMRelation();
									break;
								case "member":
									ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
									OSMWay member = idToWay.get(ref);
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
							OSMWay member = idToWay.get(ref);
							if (member != null) rel.add(member);
							break;
					}
					break;
				case END_ELEMENT:
					switch (reader.getLocalName()) {
						case "way":
							if (type == WayType.COASTLINE) {
								coast.add(way);
							} else {
								ways.get(type).add(new Polyline(way));
							}

							if(isAddress){
								//TODO Make address class to prevent awkward array index contrivances
								String[] address = new String[4];
								OSMNode node = way.get(0);
								address[0] = String.valueOf(node.getAsLong());
								address[1] = String.valueOf(node.getLat());
								address[2] = String.valueOf(node.getLon());
								address[3] = houseNumber;
								putAddressNodes(addressNodes, streetName, address);
								isAddress = false;
							}
							way = null;
							break;
						case "node":
							if(isAddress){
								//TODO Make address class to prevent awkward array index contrivances
								String[] address = new String[4];
								address[0] = String.valueOf(id);
								address[1] = String.valueOf(lat);
								address[2] = String.valueOf(lon);
								address[3] = houseNumber;
								putAddressNodes(addressNodes, streetName, address);
								isAddress = false;
							}
							break;
						case "relation":
							if (type == WayType.WATER) {
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

							if(isCity){
								//TODO Maybe make city class to prevent awkward array index contrivances
								String[] city = new String[5];
								city[0] = name;
								String[] boundingBox = findRelBoundingBox(rel);
								city[1] = boundingBox[0];
								city[2] = boundingBox[1];
								city[3] = boundingBox[2];
								city[4] = boundingBox[3];
								cityBoundaries.add(city);
								isCity = false;
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
					File parseCheck = new File("data/" + getCountry());
					if(!parseCheck.isDirectory()) {
						makeCityDirectories(cityBoundaries);
						makeStreetFiles(addressNodes, cityBoundaries);
					}
					addressNodes = null;
					cityBoundaries = null;
					for (OSMWay c : merge(coast)) {
						ways.get(WayType.COASTLINE).add(new Polyline(c));
					}
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

	private void makeStreetFiles(TreeMap<String,ArrayList<String[]>> addressNodes, ArrayList<String[]> cityBoundaries){
			for (Map.Entry<String, ArrayList<String[]>> entry : addressNodes.entrySet()) {
				ArrayList<String[]> street = entry.getValue();
				String[] firstAdress = street.get(0);
				float streetLat = Float.valueOf(firstAdress[1]);
				float streetLon = Float.valueOf(firstAdress[2]);
				try {
					for (String[] city : cityBoundaries) {
						String cityName = city[0];
						float cityMinLat = Float.valueOf(city[1]);
						float cityMaxLat = Float.valueOf(city[2]);
						float cityMinLon = Float.valueOf(city[3]);
						float cityMaxLon = Float.valueOf(city[4]);
						if ((cityMinLat <= streetLat) && (streetLat <= cityMaxLat)) {
							if ((cityMinLon <= streetLon) && (streetLon <= cityMaxLon)) {
								String streetsFile = "data/"+getCountry()+"/" + cityName + "/" + "streets" + ".txt";
								File streetsInCity = new File(streetsFile);

								BufferedWriter streetsInCityWriter;

								if (streetsInCity.isFile()) {
									streetsInCityWriter = new BufferedWriter(new FileWriter(streetsFile, true));
								} else {
									streetsInCityWriter = new BufferedWriter(new FileWriter(streetsFile));
								}

								streetsInCityWriter.write(entry.getKey() + "\n");
								for (String[] address : street) {
									String addressString = address[0] + " " + address[1] + " " + address[2] + " " + address[3] + "\n";
									streetsInCityWriter.write(addressString);
								}
								streetsInCityWriter.write("$\n");
								streetsInCityWriter.close();
							}
						}
					}
				} catch (IOException e) {
					//TODO Handle exception better
					e.printStackTrace();
					System.out.println("IOException when making BufferedWriter for streets");
					System.out.println("likely a mistake relating to / or  \\ in streetnames");
				}
			}
		}



	private String[] findRelBoundingBox(OSMRelation rel) {
		float minLat = maxlat;
		float maxLat = minlat;
		float minLon = maxlon;
		float maxLon = minlon;
		for(OSMWay way :rel){
			for(OSMNode node:way){
				float nodeLat = node.getLat();
				float nodeLon = node.getLon();
				if(nodeLat < minLat) minLat = nodeLat;
				if(nodeLat > maxLat) maxLat = nodeLat;
				if(nodeLon < minLon) minLon = nodeLon;
				if(nodeLon > maxLon) maxLon = nodeLon;
			}
		}
		return new String[]{ String.valueOf(minLat),
							  String.valueOf(maxLat),
							  String.valueOf(minLon),
							  String.valueOf(maxLon)
							};
	}

	public void parseCases(String pathToCasesFile){
		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(
								new FileInputStream(pathToCasesFile),"UTF-8"));
			int n = Integer.parseInt(in.readLine().trim());
			for(int i = 0; i < n ; i++) {
				String wayType = in.readLine();
				String wayCase = in.readLine();

				while((wayCase != null) && !(wayCase.startsWith("$"))){
					String[] tokens = wayCase.split(" ");
					wayTypeCases.add(new String[]{wayType,tokens[0],tokens[1]});
					wayCase = in.readLine();
				}
			}
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

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
			res.addAll(way);
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

	public void makeCityDirectories( ArrayList<String[]> cityBoundaries){
		try {
			File countryDir = new File("data/"+getCountry());
			countryDir.mkdir();
			BufferedWriter writer = new BufferedWriter(new FileWriter("data/"+getCountry()+"/cities.txt"));
			writer.write(cityBoundaries.size() + "\n");
			for(String[] city :cityBoundaries){
				writer.write(city[0] + "\n" + city[1] + "\n"+city[2] + "\n" + city[3] + "\n" + city[4] + "\n$\n");
				File cityDir = new File("data/"+getCountry()+"/" + city[0]);
				cityDir.mkdir();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public void putAddressNodes(TreeMap<String, ArrayList<String[]>> addressNodes, String streetName, String[] address){
		if(addressNodes.get(streetName) == null){
			ArrayList<String[]> streetAddresses = new ArrayList<>();
			streetAddresses.add(address);
			addressNodes.put(streetName, streetAddresses);
		}else{
			addressNodes.get(streetName).add(address);
		}
	}

	public void parseSearch(String proposedAddress) {
		searchAddresses.add(Address.parse(proposedAddress));
	}


	//it's only denmark right now.
	public String getCountry(){
		return "denmark";
	}

	//generalized getCities and getStreets to getTextFile, might not be final.
	public String[] getTextFile(String filepath){
		try {
			BufferedReader reader= new BufferedReader(new InputStreamReader(
					new FileInputStream(filepath),"UTF-8"));
			String[] textFile = new String[Integer.valueOf(reader.readLine())];
			String line;
			for(int i = 0 ; (line = reader.readLine()) != null ; i++){
				textFile[i] = line;
				return textFile;
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


		return null;
	}

	public Iterator<String> colorIterator() {
		return typeColors.iterator();
	}
}
