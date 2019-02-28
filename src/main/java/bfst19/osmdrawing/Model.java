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

							if (k.equals("waterway") && v.equals("ditch") || k.equals("waterway") && v.equals("stream") || k.equals("waterway") && v.equals("canal")) {
								type = WayType.DITCH;
							} else if (k.equals("landuse") && v.equals("railway") || k.equals("man_made") && v.equals("works") || k.equals("man_made") && v.equals("embankment") || k.equals("location") && v.equals("underground") || k.equals("operator") && v.equals("Energinet.dk")) {
								type = WayType.INVISIBLE;
							} else if (k.equals("natural") && v.equals("tree_row") || k.equals("landuse") && v.equals("greenfield") || k.equals("highway") && v.equals("proposed") || k.equals("natural") && v.equals("cliff") || k.equals("natural") && v.equals("wetland")) {
								type = WayType.INVISIBLE;
							} else if (k.equals("leisure") && v.equals("fitness_station") || k.equals("leisure") && v.equals("horse_riding") || k.equals("tourism") && v.equals("caravan_site") || k.equals("man_made") && v.equals("pipeline")) {
								type = WayType.INVISIBLE;
							} else if (v.equals("pitch")) {
								type = WayType.PITCH;
							} else if (v.equals("artwork")) {
								type = WayType.ARTWORK;
							} else if (v.equals("brownfield")) {
								type = WayType.BROWNFIELD;
							} else if (v.equals("allotments")) {
								type = WayType.ALLOTMENTS;
							} else if (v.equals("cemetery")) {
								type = WayType.CEMETERY;
							} else if (v.equals("square")) {
								type = WayType.SQUARE;
							} else if (v.equals("playground")) {
								type = WayType.PLAYGROUND;
							} else if (v.equals("service")) {
								type = WayType.SERVICE;
							} else if (v.equals("coastline")) {
								type = WayType.COASTLINE;
							}

							switch (k) {
								case "building":
									if (v.equals("yes")) {
										type = WayType.BUILDING;
									}
									break;
								case "bridge":
									if (v.equals("yes")) {
										type = WayType.BRIDGE;
									}
									break;
								case "leisure":
									switch (v) {
										case "playground":
										case "recreation_ground":
											type = WayType.RECREATION;
											break;
										case "common":
											type = WayType.GRASS;
											break;
										case "park":
										case "golf_course":
										case "camp_site":
										case "garden":
											type = WayType.PARK;
											break;
										case "pitch":
											type = WayType.PITCH;
											break;
										case "stadium":
											type = WayType.STADIUM;
											break;
										case "track":
											type = WayType.TRACK;
											break;

									}
									break;
								case "railway":
									switch (v) {
										case "rail":
											type = WayType.RAILWAY;
											break;
										case "construction":
											type = WayType.RAILCONSTRUCTION;
											break;
										case "subway":
											type = WayType.SUBWAY;
											break;
										case "disused":
											type = WayType.DISUSED;
											break;
									}
									break;
								case "landuse":
									switch (v) {
										case "recreation_ground":
											type = WayType.RECREATION;
											break;
										case "farmyard":
											type = WayType.FARMYARD;
											break;
										case "basin":
											type = WayType.WATER;
											break;
										case "farmland":
											type = WayType.FARMLAND;
											break;
										case "meadow":
										case "grass":
											type = WayType.GRASS;
											break;
										case "quarry":
											type = WayType.QUARRY;
											break;
										case "industrial":
											type = WayType.INDUSTRIAL;
											break;
										case "brownfield":
											type = WayType.BROWNFIELD;
											break;
										case "cemetery":
											type = WayType.CEMETERY;
											break;
										case "allotments":
											type = WayType.ALLOTMENTS;
											break;
										case "forest":
											type = WayType.FOREST;
											break;
										case "residential":
											type = WayType.RESIDENTIAL;
											break;
										case "commercial":
											type = WayType.COMMERCIAL;
											break;
										case "millitary":
											type = WayType.MILLITARY;
											break;
										case "construction":
											type = WayType.CONSTRUCTION;
											break;
									}
									break;
								case "place":
									if (v.equals("square")) {
										type = WayType.SQUARE;
									}
									break;
								case "residential":
									if (v.equals("yes")) {
										type = WayType.RESIDENTIAL;
									}
									break;
								case "man_made":
									if (v.equals("bridge")) {
										type = WayType.UNDERBRIDGE;
									} else if (v.equals("pier")) {
										type = WayType.PIER;
									}
									break;
								case "highway":
									switch (v) {
										case "residential":
											type = WayType.RESIDENTIAL;
											break;
										case "service":
											type = WayType.SERVICE;
											break;
										case "tertiary":
											type = WayType.TERTIARY;
											break;
										case "pedestrian":
											type = WayType.PEDESTRIAN;
											break;
										case "steps":
										case "unclassified":
										case "footway":
											type = WayType.FOOTWAY;
											break;
										case "cycleway":
											type = WayType.CYCLEWAY;
											break;
										case "construction":
											type = WayType.CONSTRUCTION;
											break;
										case "motorway":
										case "motorway_link":
											type = WayType.MOTORWAY;
											break;
										case "raceway":
											type = WayType.RACEWAY;
											break;
										case "primary":
										case "primary_link":
											type = WayType.PRIMARY;
											break;
										case "secondary":
										case "secondary_link":
										case "trunk":
											type = WayType.SECONDARY;
											break;
										case "tertiary_link":
											type = WayType.TERTIARY;
											break;
										case "track":
										case "bridleway":
										case "path":
											type = WayType.TRACK;
											break;
									}
									break;
								case "boundary":
									if (v.equals("administrative")) {
										type = WayType.BOUNDARY_ADMINISTRATIVE;
									}
									break;
								case "natural":
									switch (v) {
										case "water":
											type = WayType.WATER;
											break;
										case "grassland":
											type = WayType.GRASS;
											break;
										case "coastline":
											type = WayType.COASTLINE;
											break;
										case "beach":
											type = WayType.BEACH;
											break;
										case "tree":
										case "wood":
											type = WayType.TREE;
											break;
										case "scrub":
											type = WayType.SCRUB;
											break;
									}
									break;
								case "pier":
									if (v.equals("yes")) {
										type = WayType.PIER;
									}
									break;
								case "boat":
									if (v.equals("ferry")) {
										type = WayType.BOAT;
									}
									if (v.equals("tour")) {
										type = WayType.BOAT;
									}
									break;
								case "aeroway":
									if (v.equals("runway)")) {
										type = WayType.RUNWAY;
									}
									if (v.equals("taxiway)")) {
										type = WayType.TAXIWAY;
									}
									break;
								case "tourism":
									if (v.equals("camp_site")) {
										type = WayType.PARK;
									}
									break;
								case "construction":
									type = WayType.CONSTRUCTION;
									break;
								case "barrier":
									type = WayType.BARRIER;
									break;
								case "power":
									if (v.equals("plant")) {
										type = WayType.BARRIER;
									}
									break;
								case "amenity":
									type = WayType.AMENITY;
									break;
								case "sport":
									if (v.equals("running")) {
										type = WayType.TRACK;
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