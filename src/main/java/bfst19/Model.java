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
	//TODO: change from having map waytype to it's string name, simply use waytype.valueOf()
	Map<String, WayType> waytypes = new HashMap<>();
	ArrayList<String[]> wayTypeCases = new ArrayList<>();
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
		TreeMap<String,TreeMap<String,ArrayList<Address>>> addresses = new TreeMap<>();
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
							if(b.hasFields()) {
								b.id = id;
								b.lat = lat;
								b.lon = lon;
								putAddress(addresses,b);
							}
							way = null;
							break;
						case "node":
							if(b.hasFields()){
								b.id = id;
								b.lat = lat;
								b.lon = lon;
								putAddress(addresses,b);
							}
                            //todo: reset builder everytime it could be an adress.
							//b.reset();
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
						makeCityDirectories(addresses.keySet(),getCountry());
						try {
							makeStreetFiles(addresses);
						} catch (IOException e) {
							e.printStackTrace();
						}
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

	private void makeStreetFiles(TreeMap<String,TreeMap<String, ArrayList<Address>>> addresses)throws IOException{
		BufferedWriter allStreetsInCountryWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("data/"+getCountry()+"/streets.txt")),"UTF-8"));
		//an entry of a string(cityString) and the map relating to that string.
		for(Map.Entry<String,TreeMap<String,ArrayList<Address>>> city : addresses.entrySet()){
			String cityAndPostcodeString = city.getKey();
			String cityDirPath = cityAndPostcodeString.replaceAll(" QQQ "," ");
			BufferedWriter streetInCityWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("data/"+getCountry()+"/"+cityDirPath+"/streets.txt")),"UTF-8"));
			//an entry of a string(streetString) and the arraylist of the adresses relating to that string
			for(Map.Entry<String,ArrayList<Address>> street : city.getValue().entrySet()){
				String streetString = street.getKey();
				if(streetString.contains("/")){
					streetString = streetString.replaceAll("/","");
				}
				streetInCityWriter.write(streetString+"\n");
				streetInCityWriter.flush();
				allStreetsInCountryWriter.write(streetString+" ZZZ "+cityAndPostcodeString+"\n");
				allStreetsInCountryWriter.flush();
				BufferedWriter addressInStreetWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("data/"+getCountry()+"/"+cityDirPath+"/"+streetString+".txt")),"UTF-8"));
				for(Address address:street.getValue()){
					System.out.println(address.getId()+" "+address.getLat()+" "+address.getLon()+" "+address.getHouseNumber());
					addressInStreetWriter.write(address.getId()+" "+address.getLat()+" "+address.getLon()+" "+address.getHouseNumber()+ "\n");
					addressInStreetWriter.flush();
				}
				addressInStreetWriter.close();
			}
			streetInCityWriter.close();
		}
		allStreetsInCountryWriter.close();
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

	public void makeCityDirectories(Set<String> citiesAndPostcodes,String country){
		try {
			File countryDir = new File("data/"+country);
			countryDir.mkdir();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("data/"+getCountry()+"/cities.txt")),"UTF-8"));
			for(String cityAndPostcode :citiesAndPostcodes){
				writer.write(cityAndPostcode+"\n");
				//splits the city and postcode at the given delimiter, currently " QQQ " tokens is just the result of this splitting.
				String[] tokens = cityAndPostcode.split(" QQQ ");
				File cityDir = new File("data/" + country + "/" + tokens[0] + " " + tokens[1]);
				cityDir.mkdir();
				//the city is currently also making the streets.txt file in it's directory,
                // this is just to be more sure that you can find the streets.txt file even if there are no streets.
				String streetsFile = "data/" + getCountry() + "/" + tokens[0] + " " + tokens[1] + "/" + "streets" + ".txt";
				File streetsInCityFile = new File(streetsFile);
				streetsInCityFile.createNewFile();
			}
			writer.close();
		} catch (IOException e) {
		    //TODO: handle better
			e.printStackTrace();
		}


	}
	//Method checks if the index is null, if it is, it makes fills that index with a new collection, and puts the address in it's proper place.
    //it also resets the builder for future use.
	//This maps a string(city+postcode) to a map, that maps a string(streetname) to the addresses(houseNumbers) on that street
	public void putAddress(TreeMap<String, TreeMap<String, ArrayList<Address>>> addresses, Builder b){
		// QQQ is an arbitrary delimiter between city and postcode, so that we can know with more reliabillity where to split the string later.
		String cityAndPostcode = b.city+" QQQ "+b.postcode;
		if(addresses.get(cityAndPostcode)==null){
			addresses.put(cityAndPostcode,new TreeMap<>());
		}
		if(addresses.get(cityAndPostcode).get(b.streetName)==null){
			addresses.get(cityAndPostcode).put(b.streetName,new ArrayList<>());
		}
		addresses.get(cityAndPostcode).get(b.streetName).add(b.build());
		b.reset();
	}

	public void parseSearch(String proposedAddress) {
		Address address = AddressParser.getInstance().parse(proposedAddress,getCountry());
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