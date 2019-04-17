package bfst19;

import bfst19.KDTree.KDTree;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.*;
import java.util.zip.ZipInputStream;

import static javax.xml.stream.XMLStreamConstants.*;

public class Model {
	HashMap<Long,String> pointsOfInterest = new HashMap<>();
	float lonfactor = 1.0f;
	private boolean colorBlindEnabled;
	private String datasetName;
	int count = 0;



	List<Runnable> colorObservers = new ArrayList<>();
	List<Runnable> foundMatchesObservers = new ArrayList<>();
	float minlat, minlon, maxlat, maxlon;

	String CurrentTypeColorTxt  = "data/TypeColorsNormal.txt";
	HashMap<String,ArrayList<String[]>> wayTypeCases = new HashMap<>();
	ObservableList<String[]> foundMatches = FXCollections.observableArrayList();
	ObservableList<String> typeColors = FXCollections.observableArrayList();
	Map<WayType, KDTree> kdTreeMap = new TreeMap<>();

	//for building addresses during parsing
	public static class Builder {
		private long id;
		private float lat, lon;
		private String streetName = "Unknown", houseNumber="", postcode="", city="",municipality="";
		public void reset(){
			id = 0;
			lat =0;
			lon = 0;
			streetName = "Unknown";
			houseNumber = "";
			postcode = "";
			city="";
		}
		public boolean hasFields(){
			if(!streetName.equals("Unknown")&&!houseNumber.equals("")&&!postcode.equals("")&&(!city.equals("")||!municipality.equals(""))) return true;
			return false;}
		public Address build() {
			if(streetName.contains("/")){
				streetName = streetName.replaceAll("/","");
			}
			if(city.equals("")){
				city = municipality;
			}
			return new Address(id,lat,lon,streetName, houseNumber, postcode, city);
		}
	}

	//TODO filthy disgusting typecasting
	public Iterable<Drawable> getWaysOfType(WayType type, BoundingBox bbox) {
		return kdTreeMap.get(type).rangeQuery(bbox);
	}

	public void addFoundMatchesObserver(Runnable observer) {
		foundMatchesObservers.add(observer);
	}
	public void addColorObserver(Runnable observer) { colorObservers.add(observer); }
	public void notifyFoundMatchesObservers() {
		for (Runnable observer : foundMatchesObservers) observer.run();
	}
	public void notifyColorObservers() {for (Runnable observer : colorObservers) observer.run();}

	public Model(String dataset){
		datasetName = dataset;
		//this keeps the cities and the default streets files in memory, it's about 1mb for Zealand of memory
		AddressParser.getInstance(this).setDefaults(getDefault(getDatasetName()));
		AddressParser.getInstance(this).parseCitiesAndPostCodes(getCities(getDatasetName()));
	}

	public Model(List<String> args) throws IOException, XMLStreamException, ClassNotFoundException {
		//Changed from field to local variable so it can be garbage collected
		Map<WayType, List<Drawable>> ways = new EnumMap<>(WayType.class);
		for (WayType type : WayType.values()) {
			ways.put(type, new ArrayList<>());
		}
		//todo figure out how to do singleton but also include model in its constructor without needing to give model for every call of getinstance
		parseWayTypeCases("data/Waytype_cases.txt");

		ParseWayColors();

		String filename = args.get(0);
		//this might not be optimal
		String[] arr = filename.split("\\.");
		datasetName = arr[0].replace("data/","") + " Database";
		InputStream osmsource;
		if (filename.endsWith(".obj")) {
			long time = -System.nanoTime();
			try (ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
				kdTreeMap = (Map<WayType, KDTree>) input.readObject();
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
			System.out.printf("parse time: %.1fs\n", time / 1e9);
			try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename + ".obj")))) {
				output.writeObject(kdTreeMap);
				output.writeFloat(minlat);
				output.writeFloat(minlon);
				output.writeFloat(maxlat);
				output.writeFloat(maxlon);
			}
		}
		//pointsOfInterest = getPointsOfInterest(getDatasetName());
        AddressParser.getInstance(this).setDefaults(getDefault(getDatasetName()));
        AddressParser.getInstance(this).parseCitiesAndPostCodes(getCities(getDatasetName()));
	}

    public void ParseWayColors(){

		try {
			typeColors.clear();
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
		notifyColorObservers();
	}

	public void switchColorScheme(boolean colorBlindEnabled){

		System.out.println("Colorblind mode enabled: " + colorBlindEnabled);

		if (colorBlindEnabled){
			CurrentTypeColorTxt = ("data/TypeColorsColorblind.txt");
		}
		else if(!colorBlindEnabled){
			CurrentTypeColorTxt = ("data/TypeColorsNormal.txt");
		}
		ParseWayColors();
	}

	private void parseOSM(InputStream osmsource) throws XMLStreamException {
		//Changed from field to local variable so it can be garbage collected
		Map<WayType, List<Drawable>> ways = new EnumMap<>(WayType.class);
		for (WayType type : WayType.values()) {
			ways.put(type, new ArrayList<>());
		}

		XMLStreamReader reader = XMLInputFactory
				.newInstance()
				.createXMLStreamReader(osmsource);

		LongIndex<OSMNode> idToNode = new LongIndex<OSMNode>();
		LongIndex<OSMWay> idToWay = new LongIndex<OSMWay>();
		ArrayList<Address> addresses = new ArrayList<>();
		List<OSMWay> coast = new ArrayList<>();

		//variables to make OSMWay/OSMRelation
		OSMWay way = null;
		OSMRelation rel = null;
		WayType type = null;

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
							lon = lonfactor*Float.parseFloat(reader.getAttributeValue(null, "lon"));
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
								b.houseNumber = v.trim();
							}

							if(k.equals("addr:street")){
								b.streetName = v.trim();
							}

							if(k.equals("addr:postcode")){
								b.postcode = v.trim();
							}


							if(k.equals("addr:city")){
								b.city = v.trim();
							}

							if(k.equals("addr:municipality")){
								b.municipality = v.trim();
							}

							//string[0]=waytype's name, strings[1] = k for the case, strings = v for the case.

							for(Map.Entry<String,ArrayList<String[]>> wayType : wayTypeCases.entrySet()){
								String wayTypeString = wayType.getKey();
								for(String[] waycase:wayType.getValue()){
									if(k.equals(waycase[0])&&v.equals(waycase[1])){
										type = WayType.valueOf(wayTypeString);
									}
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
							if(b.hasFields()) {
								b.id = id;
								b.lat = lat;
								b.lon = lon;
								addresses.add(b.build());
							}
							way = null;
							b.reset();
							break;
						case "node":
							if(b.hasFields()){
								b.id = id;
								b.lat = lat;
								b.lon = lon;
								addresses.add(b.build());
							}
							b.reset();
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
							break;
					}
					break;
				case PROCESSING_INSTRUCTION: break;
				case CHARACTERS: break;
				case COMMENT: break;
				case SPACE: break;
				case START_DOCUMENT: break;
				case END_DOCUMENT:
					File parseCheck = new File("data/"+ getDatasetName());
					addresses.sort(Address::compareTo);
					makeDatabase(addresses, getDatasetName());

					for (OSMWay c : merge(coast)) {
						ways.get(WayType.COASTLINE).add(new Polyline(c));
					}

					//Make and populate KDTrees for each WayType
					for(Map.Entry<WayType, List<Drawable>> entry : ways.entrySet()) {
						KDTree typeTree = new KDTree();
						//Add entry values to KDTree
						typeTree.insertAll(entry.getValue());
						//Add KDTree to TreeMap
						kdTreeMap.put(entry.getKey(), typeTree);
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

	private void makeDatabase(ArrayList<Address> addresses,String datasetname){
		File contryDir = new File("data/"+datasetname);
		contryDir.mkdir();
		String currentCityAndPostcode = "";
		String currentStreet = "";
		try {
			//this first step looks ugly and is perhaps unnecessary
			BufferedWriter allStreetsInCountryWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("data/"+datasetname+"/streets.txt")),"UTF-8"));
			BufferedWriter streetsInCityWriter =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("data/"+datasetname+"/"+currentCityAndPostcode+"/streets.txt")),"UTF-8"));
			BufferedWriter citiesInCountryWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("data/"+datasetname+"/cities.txt")),"UTF-8"));
			File streetFile = new File("data/"+datasetname+"/"+currentCityAndPostcode+"/"+currentStreet+".txt");
			BufferedWriter addressesInStreetWriter =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(streetFile)));
		for(Address address:addresses) {
			//if the streetName remains the same, and the city changes we need to change the writers for streets and addresses,
			//along with writing to the appropriate files, we also change the current city and postcode, and make the directory for it
			//todo fix code dupes here
			if (address.getStreetName().equals(currentStreet) && !(address.getCity() + getDelimeter() + address.getPostcode()).equals(currentCityAndPostcode)) {
				currentCityAndPostcode = address.getCity() + " QQQ " + address.getPostcode();
				File cityDir = new File("data/" + datasetname + "/" + currentCityAndPostcode);
				cityDir.mkdir();
				File streetsIncityFile = new File("data/" + datasetname + "/" + currentCityAndPostcode + "/streets.txt");
				streetFile = new File("data/" + datasetname + "/" + currentCityAndPostcode + "/" + currentStreet + ".txt");
				streetsInCityWriter.flush();
				addressesInStreetWriter.flush();
				//because the addresses are sorted by their streetnames first, we need to accomadate changing cities many times.
				streetsInCityWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(streetsIncityFile, true), "UTF-8"));
				addressesInStreetWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(streetFile)));
				citiesInCountryWriter.write(currentCityAndPostcode + "\n");
				streetsInCityWriter.write(currentStreet + "\n");
				allStreetsInCountryWriter.write(currentStreet + getDelimeter() + currentCityAndPostcode + "\n");
			} else {
				//if the city changes, flush the writers and change the writer for the streets in that city,
				// write to the file with all the cities and make the cities directory, also change the current city and postcode
				if (!(address.getCity() + getDelimeter() + address.getPostcode()).equals(currentCityAndPostcode)) {
					currentCityAndPostcode = address.getCity() + " QQQ " + address.getPostcode();
					File cityDir = new File("data/" + datasetname + "/" + currentCityAndPostcode);
					cityDir.mkdir();
					File streetsIncityFile = new File("data/" + datasetname + "/" + currentCityAndPostcode + "/streets.txt");
					streetsInCityWriter.flush();
					//because the addresses are sorted by their streetnames first, we need to accomadate changing cities many times.
					streetsInCityWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(streetsIncityFile, true), "UTF-8"));
					citiesInCountryWriter.write(currentCityAndPostcode + "\n");
				}
				//if the addresses street is different, make a new street file, write to that city's streets.txt file and change the current street.
				if (!address.getStreetName().equals(currentStreet)) {
					currentStreet = address.getStreetName();
					streetFile = new File("data/" + datasetname + "/" + currentCityAndPostcode + "/" + currentStreet + ".txt");
					addressesInStreetWriter.flush();
					addressesInStreetWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(streetFile)));
					streetsInCityWriter.write(currentStreet + "\n");
					allStreetsInCountryWriter.write(currentStreet + getDelimeter() + currentCityAndPostcode + "\n");
				}
			}
				addressesInStreetWriter.write(address.getId() + " " + address.getLat() + " " + address.getLon() + " " + address.getHouseNumber() + "\n");
			}

		//closes all writers
			allStreetsInCountryWriter.close();
			citiesInCountryWriter.close();
			streetsInCityWriter.close();
			addressesInStreetWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getDelimeter(){
		return " QQQ ";
	}

	public void parseWayTypeCases(String pathToCasesFile){
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(pathToCasesFile),"UTF-8"));
			int n = Integer.parseInt(in.readLine().trim());
			for(int i = 0; i < n ; i++) {
				String wayType = in.readLine();
				String wayCase = in.readLine();

				while((wayCase != null) && !(wayCase.startsWith("$"))){
					String[] tokens = wayCase.split(" ");
					if(wayTypeCases.get(wayType)==null){
						wayTypeCases.put(wayType,new ArrayList<>());
					}
					wayTypeCases.get(wayType).add(new String[]{tokens[0],tokens[1]});
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
			//TODO figure out why this works and why it can't be refactored easily into OSMWay without inheritance
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

	public void parseSearch(String proposedAddress) {
        Address a = AddressParser.getInstance(this).singleSearch(proposedAddress, getDatasetName());
        //if the address does not have a city or a streetname, get the string's matches from the default file and display them
        if(a.getStreetName().equals("Unknown")||(a.getCity().equals(""))){
			ArrayList<String[]> possibleMatches = AddressParser.getInstance(this).getMatchesFromDefault(proposedAddress, false);
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
					foundMatches.add(new String[]{street,match[3],city,postcode});
				}
			}
        }else{
        	//if those 3 fields are filled, just put the address in the ui will handle the rest
			foundMatches.clear();
			foundMatches.add(new String[]{String.valueOf(a.getLon()),String.valueOf(a.getLat()),a.getStreetName(),a.getHouseNumber(),a.getFloor(),a.getSide(),a.getCity(),a.getPostcode()});
		}
        notifyFoundMatchesObservers();
	}

	public String getDatasetName(){
		return datasetName;
	}

	public ArrayList<String> getAddressesOnStreet(String country,String city,String postcode,String streetName){
	    return getTextFile("data/"+country+"/"+city+" QQQ "+postcode+"/"+streetName+".txt");
    }

    public void writePointsOfInterest(String datasetName){
		try {
			BufferedWriter pointsOfInterestWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("data/"+datasetName+"/pointsOfInterest.txt")),"UTF-8"));
			for(Map.Entry<Long,String> entry : pointsOfInterest.entrySet()){
				pointsOfInterestWriter.write(entry.getKey()+getDelimeter()+entry.getValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public HashMap<Long,String> getPointsOfInterest(String datasetName){
		HashMap<Long,String> pointsOfInterest = new HashMap<>();
		ArrayList<String> pointOfInterestFile = getTextFile("data/"+datasetName+"/pointsOfInterest.txt");
		for(String address : pointOfInterestFile){
			String[] addressFields = address.split(getDelimeter());
			long id = Long.valueOf(addressFields[0]);
			String addressString = addressFields[1]+getDelimeter()+addressFields[2]+getDelimeter()+addressFields[3]+getDelimeter()+addressFields[4]+getDelimeter()+getDelimeter()+addressFields[5];
			pointsOfInterest.put(id,addressString);
		}
		return pointsOfInterest;
	}

    public void addPointsOfInterest(long id,String pointOfInterest){
		pointsOfInterest.put(id,pointOfInterest);
	}

	public void removePointOfInterest(long id){
		pointsOfInterest.remove(id);
	}



	public ArrayList<String> getStreetsInCity(String country, String city,String postcode){
		return getTextFile("data/"+country+"/"+city+" QQQ "+postcode+"/streets.txt");
	}

	public ArrayList<String> getCities(String country){
		return getTextFile("data/"+country+"/cities.txt");
	}

    private ArrayList<String> getDefault(String country) {
		return getTextFile("data/"+country+"/streets.txt");
	}

	//generalized getCities and getStreets to getTextFile, might not be final.
	public ArrayList<String> getTextFile(String filepath){
		try {
			BufferedReader reader= new BufferedReader(new InputStreamReader(
					new FileInputStream(filepath),"UTF-8"));
			ArrayList<String> textFile = new ArrayList<>();
			String line;
			while((line = reader.readLine()) != null){
				textFile.add(line);
			}
			return textFile;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Iterator<String> colorIterator() {
		return typeColors.iterator();
	}

	public Iterator<String[]> foundMatchesIterator() { return foundMatches.iterator();}
}
