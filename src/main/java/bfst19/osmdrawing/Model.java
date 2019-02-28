package bfst19.osmdrawing;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.*;
import java.util.zip.ZipInputStream;

import static javax.xml.stream.XMLStreamConstants.*;

public class Model {
	float lonfactor = 1.0f;
	Map<WayType,List<Drawable>> ways = new EnumMap<>(WayType.class); {
		for (WayType type : WayType.values()) {
			ways.put(type, new ArrayList<>());
		}
	}
	List<Runnable> observers = new ArrayList<>();
	float minlat, minlon, maxlat, maxlon;

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

	private void parseOSM(InputStream osmsource) throws XMLStreamException {
		XMLStreamReader reader = XMLInputFactory
				.newInstance()
				.createXMLStreamReader(osmsource);
		LongIndex<OSMNode> idToNode = new LongIndex<OSMNode>();
		LongIndex<OSMWay> idToWay = new LongIndex<OSMWay>();
		List<OSMWay> coast = new ArrayList<>();
		OSMWay way = null;
		OSMRelation rel = null;
		WayType type = null;
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
							long id = Long.parseLong(reader.getAttributeValue(null, "id"));
							float lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
							float lon = lonfactor * Float.parseFloat(reader.getAttributeValue(null, "lon"));
							idToNode.add(new OSMNode(id,lon, lat));
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

							if (k.equals("building")) {
								type = WayType.BUILDING;
							}else if (k.equals("area") && v.equals("yes")) {
								type = WayType.AREA;
							}else if (k.equals("residential") && v.equals("yes")||k.equals("landuse")&&v.equals("residential")) {
								type = WayType.RESIDENTIAL;
							}else if (k.equals("pier") && v.equals("yes")) {
								type = WayType.PIER;
							}else if (k.equals("natural") && v.equals("yes")||k.equals("natural")&&v.equals("wood")) {
								type = WayType.TREE;
							}else if (k.equals("landuse")&&v.equals("grass")||k.equals("leisure")&&v.equals("common")||k.equals("landuse")&&v.equals("meadow")||k.equals("natural")&&v.equals("grassland")) {
								type = WayType.GRASS;
							}else if(k.equals("landuse")&&v.equals("forest")) {
								type = WayType.FOREST;
							}else if(k.equals("landuse")&&v.equals("commercial")) {
								type = WayType.COMMERCIAL;
							}else if(k.equals("landuse")&&v.equals("quarry")) {
								type = WayType.QUARRY;
							}else if(k.equals("natural")&&v.equals("beach")){
									type = WayType.BEACH;
							}else if (k.equals("bridge") && v.equals("yes")) {
								type = WayType.BRIDGE;
							}else if (k.equals("boundary")&&v.equals("administrative")) {
								type = WayType.BOUNDARY_ADMINISTRATIVE;
							}else if (k.equals("natural") && v.equals("scrub")) {
								type = WayType.SCRUB;
							}else if (k.equals("aeroway") && v.equals("runway")) {
								type = WayType.RUNWAY;
							}else if (k.equals("aeroway") && v.equals("taxiway")) {
								type = WayType.TAXIWAY;
							}else if (k.equals("natural")&&v.equals("water")||k.equals("water")||k.equals("landuse")&&v.equals("basin")) {
								type = WayType.WATER;
							}else if (k.equals("waterway")&&v.equals("ditch")||k.equals("waterway")&&v.equals("stream")||k.equals("waterway")&&v.equals("canal")) {
								type = WayType.DITCH;
							}else if (k.equals("landuse")&&v.equals("farmland")) {
								type = WayType.FARMLAND;
							}else if (k.equals("landuse")&&v.equals("railway")||k.equals("man_made")&&v.equals("works")||k.equals("man_made")&&v.equals("embankment")||k.equals("location")&&v.equals("underground")||k.equals("operator")&&v.equals("Energinet.dk")) {
								type = WayType.INVISIBLE;
							}else if (k.equals("natural")&&v.equals("tree_row")||k.equals("landuse")&&v.equals("greenfield")||k.equals("highway")&&v.equals("proposed")||k.equals("natural")&&v.equals("cliff")||k.equals("natural")&&v.equals("wetland")) {
								type = WayType.INVISIBLE;
							}else if (k.equals("leisure")&&v.equals("fitness_station")||k.equals("leisure")&&v.equals("horse_riding")||k.equals("tourism")&&v.equals("caravan_site")||k.equals("man_made")&&v.equals("pipeline")) {
								type = WayType.INVISIBLE;
							}else if (k.equals("landuse")&&v.equals("farmyard")) {
								type = WayType.FARMYARD;
							}else if (k.equals("landuse")&&v.equals("recreation_ground")||k.equals("leisure")&&v.equals("playground")||k.equals("leisure")&&v.equals("recreation_ground")) {
								type = WayType.RECREATION;
							}else if (k.equals("landuse")&&v.equals("military")) {
								type = WayType.MILITARY;
							}else if (v.equals("park")||k.equals("leisure")&&v.equals("park")||k.equals("leisure")&&v.equals("golf_course")||k.equals("tourism")&&v.equals("camp_site")) {
								type = WayType.PARK;
							}else if (k.equals("leisure")&&v.equals("stadium")) {
								type = WayType.STADIUM;
							}else if (v.equals("pitch")) {
								type = WayType.PITCH;
							}else if (v.equals("artwork")) {
								type = WayType.ARTWORK;
							}else if (k.equals("contruction") || k.equals("highway")&&v.equals("contruction")||k.equals("landuse")&&v.equals("construction")) {
								type = WayType.CONSTRUCTION;
							}else if (v.equals("brownfield")) {
								type = WayType.BROWNFIELD;
							}else if (k.equals("landuse")&&v.equals("industrial")||k.equals("power")&&v.equals("plant")) {
								type = WayType.INDUSTRIAL;
							}else if (v.equals("allotments")) {
								type = WayType.ALLOTMENTS;
							}else if (v.equals("cemetery")) {
								type = WayType.CEMETERY;
							}else if (v.equals("square")) {
								type = WayType.SQUARE;
							}else if (v.equals("playground")) {
								type = WayType.PLAYGROUND;
							}else if (k.equals("barrier")) {
								type = WayType.BARRIER;
							}else if (k.equals("amenity")) {
								type = WayType.AMENITY;
							}else if (v.equals("footway")||k.equals("highway")&&v.equals("pedestrian")||k.equals("highway")&&v.equals("steps")) {
								type = WayType.FOOTWAY;
							}else if(k.equals("highway")&&v.equals("motorway")||k.equals("highway")&&v.equals("motorway_link")){
								type = WayType.MOTORWAY;
							}else if(k.equals("highway")&&v.equals("raceway")){
								type = WayType.RACEWAY;
							}else if (v.equals("primary")) {
								type = WayType.PRIMARY;
							}else if (v.equals("secondary")||k.equals("highway")&&v.equals("secondary_link")||k.equals("highway")&&v.equals("trunk")) {
								type = WayType.SECONDARY;
							}else if (v.equals("tertiary")) {
								type = WayType.TERTIARY;
							}else if (v.equals("service")) {
								type = WayType.SERVICE;
							}else if (k.equals("highway") && v.equals("residential")) {
								type = WayType.ROAD_RESIDENTIAL;
							}else if (k.equals("highway") && v.equals("unclassified")) {
								type = WayType.FOOTWAY;
							}else if (k.equals("highway") && v.equals("track")||k.equals("leisure")&&v.equals("track")||(k.equals("highway") && v.equals("path"))||(k.equals("highway") && v.equals("bridleway"))||k.equals("sport")&&v.equals("running")){
								type = WayType.TRACK;
							}else if (k.equals("cycleway")||k.equals("highway")&&v.equals("cycleway")) {
								type = WayType.CYCLEWAY;
							}else if (v.equals("subway")||k.equals("railway")) {
								type = WayType.SUBWAY;
							}else if(v.equals("bridge")&&k.equals("man_made")){
								type = WayType.ROAD_BRIDGE;
							}else if (v.equals("construction") && k.equals("railway")) {
								type = WayType.RAILCONSTRUCTION;
							}else if (v.equals("disused") && k.equals("railway")) {
								type = WayType.DISUSED;
							}else if (v.equals("coastline")) {
								type = WayType.COASTLINE;
							}else if ((k.equals("boat"))||(v.equals("ferry"))||(v.equals("tour"))){
								type = WayType.BOAT;
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
							way = null;
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

	private Iterable<OSMWay> merge(List<OSMWay> coast) {
		Map<OSMNode,OSMWay> pieces = new HashMap<>();
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
}