package bfst19;

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
	float lonfactor = 1.0f;
	Map<WayType,List<Drawable>> ways = new EnumMap<>(WayType.class);
	private boolean colorBlindEnabled;

	{
		for (WayType type : WayType.values()) {
			ways.put(type, new ArrayList<>());
		}
	}
	List<Runnable> observers = new ArrayList<>();
	float minlat, minlon, maxlat, maxlon;

	String CurrentTypeColorTxt  = "data/TypeColorsNormal.txt";
	HashMap<String,ArrayList<String[]>> wayTypeCases = new HashMap<>();
	ObservableList<Address> searchedAddresses = FXCollections.observableArrayList();
	ObservableList<String> typeColors = FXCollections.observableArrayList();

	//for building addresses during parsing
	public static class Builder {
		private long id;
		private float lat, lon;
		private String streetName = "Unknown", houseNumber="", postcode="", city="";
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
			if(!streetName.equals("Unknown")&&!houseNumber.equals("")&&!postcode.equals("")&&!city.equals("")) return true;
			return false;}
		public Address build() {
			if(streetName.contains("/")){
				streetName = streetName.replaceAll("/","");
			}
			return new Address(id,lat,lon,streetName, houseNumber, postcode, city);
		}
	}

	public Iterable<Drawable> getWaysOfType(WayType type) {
		return ways.get(type);
	}

	public void addObserver(Runnable observer) {
		observers.add(observer);
	}

	public void notifyObservers() {
		for (Runnable observer : observers) observer.run();
	}

	public Model(List<String> args) throws IOException, XMLStreamException, ClassNotFoundException {

		//todo figure out how to do singleton but also include model in its constructor without needing to give model for every call of getinstance
		parseWayTypeCases("data/Waytype_cases.txt");
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
			AddressParser.getInstance(this).parseDefaults(getCountry());
			AddressParser.getInstance(this).parseCitiesAndPostCodes(getCountry());
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
		//TreeMap<String,TreeMap<String,ArrayList<Address>>> addresses = new TreeMap<>();
		ArrayList<Address> addresses = new ArrayList<>();
		List<OSMWay> coast = new ArrayList<>();

		OSMWay way = null;
		OSMRelation rel = null;
		WayType type = null;

		//variables for addressParsing
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
								//putAddress(addresses,b);
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
								//putAddress(addresses,b);
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
					File parseCheck = new File("data/"+getCountry());
					if(!parseCheck.isDirectory()) {
						//todo currently the addresSorting does not ignore case, perhaps change that if it ends up causing issues
						addresses.sort(Address::compareTo);
						makeDatabase(addresses,getCountry());
						//this keeps the cities and the default streets files in memory, it's about 1mb for Zealand of memory
						AddressParser.getInstance(this).parseDefaults(getCountry());
						AddressParser.getInstance(this).parseCitiesAndPostCodes(getCountry());
					}

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

	private void makeDatabase(ArrayList<Address> addresses,String country){
		File contryDir = new File("data/"+country);
		contryDir.mkdir();
		String currentCityAndPostcode = "";
		String currentStreet = "";
		try {
			//this first step looks ugly and is perhaps unnecessary
			BufferedWriter allStreetsInCountryWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("data/"+country+"/streets.txt")),"UTF-8"));
			BufferedWriter streetsInCityWriter =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("data/"+country+"/"+currentCityAndPostcode+"/streets.txt")),"UTF-8"));
			BufferedWriter citiesInCountryWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("data/"+country+"/cities.txt")),"UTF-8"));
			File streetFile = new File("data/"+country+"/"+currentCityAndPostcode+"/"+currentStreet+".txt");
			BufferedWriter adressesInStreetWriter =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(streetFile)));

		for(Address address:addresses){
			//if the city changes, flush the writers and change the writer for the streets in that city,
			// write to the file with all the cities and make the cities directory, also change the current city and postcode
			if(!(address.getCity()+getDeilimeter()+address.getPostcode()).equals(currentCityAndPostcode)){
				currentCityAndPostcode = address.getCity()+" QQQ "+address.getPostcode();
				File cityDir = new File("data/"+country+"/"+currentCityAndPostcode);
				cityDir.mkdir();
				File streetsIncityFile = new File("data/"+country+"/"+currentCityAndPostcode+"/streets.txt");
				streetsInCityWriter.flush();
				//for some reason this writer only works if i make it append the file.
				streetsInCityWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(streetsIncityFile,true),"UTF-8"));
				citiesInCountryWriter.write(currentCityAndPostcode+"\n");
			}
			//if the addresses street is different, make a new street file, write to that city's streets.txt file and change the current street.
			if(!address.getStreetName().equals(currentStreet)){
				currentStreet = address.getStreetName();
				streetFile = new File("data/"+country+"/"+currentCityAndPostcode+"/"+currentStreet+".txt");
				adressesInStreetWriter.flush();
				adressesInStreetWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(streetFile)));
				streetsInCityWriter.write(currentStreet+"\n");
				allStreetsInCountryWriter.write(currentStreet+getDeilimeter()+currentCityAndPostcode+"\n");
			}
			adressesInStreetWriter.write(address.getId()+" "+address.getLat()+" "+address.getLon()+" "+address.getHouseNumber()+"\n");
		}
		//closes all writers
			allStreetsInCountryWriter.close();
			citiesInCountryWriter.close();
			streetsInCityWriter.close();
			adressesInStreetWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getDeilimeter(){
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
		/*Address address = AddressParser.getInstance().parse(proposedAddress,getCountry());
		System.out.println(address.toString());*/
		Address a = AddressParser.getInstance(this).singleSearch(proposedAddress,getCountry());
		//a is null if the singlesearch did not find a city in the string, hence we start the autocomplete
		if(a==null){
			//this returns an arraylist of string arrays that hold a streetname city and postcode
			ArrayList<String[]> possibleMatches = AddressParser.getInstance(this).getMatchesFromDefault(proposedAddress,false);
			//if it only finds one street with that name it should jump to asking which housenumber they meant
			if(possibleMatches.size()==1){
				String[] match = possibleMatches.get(0);
				//give these to the ui somehow either through a method or an observable array or something.
				//each string array includes an id, lat, lon and a housenumber
				ArrayList<String[]> possibleAddresses = AddressParser.getInstance(this).getAddress(getCountry(),match[1],match[2],match[0],"",false);
				//insert the index of the housenumber chosen
				String[] addressMatch = possibleAddresses.get(0);
				a = new Address(Long.valueOf(addressMatch[0]),Float.valueOf(addressMatch[1]),Float.valueOf(addressMatch[2]),match[0],addressMatch[3],match[2],match[1]);

			}else{
				//give possible matches to the ui somehow
				//and receive which city it should be looking in for the housenumbers
				//that are used for the next step of the autocomplete
				//insert the index of the city+postcode chosen
				String[] match = possibleMatches.get(0);
				//getting the housenumbers with the given city, postcode and streetname
				ArrayList<String[]> possibleAddresses = AddressParser.getInstance(this).getAddress(getCountry(),match[1],match[2],match[0],"",false);
				//insert the index of the housenumber chosen
				String[] addressMatch = possibleAddresses.get(0);
				a = new Address(Long.valueOf(addressMatch[0]),Float.valueOf(addressMatch[1]),Float.valueOf(addressMatch[2]),match[0],addressMatch[3],match[2],match[1]);
			}
		}
		System.out.println(a.toString()+a.getHouseNumber());
		/*for(String[] line:a){
			System.out.println(line[0]+" "+line[1]+" "+line[2]);
		}*/
	}

	//it's only denmark right now.
	public String getCountry(){
		return "denmark";
	}

	public String[] getStreetsInCity(String country,String city){
		return getTextFile("data/"+country+"/"+city+"/streets.txt");
	}

	public String[] getCities(String country){
		return getTextFile("data/"+country+"cities.txt");
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