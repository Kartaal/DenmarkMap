package bfst19;

import bfst19.KDTree.Drawable;
import bfst19.KDTree.KDTree;
import bfst19.Line.*;
import bfst19.Route_parsing.RouteHandler;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.*;

import static javax.xml.stream.XMLStreamConstants.*;

class OSMParser {

    static void parseOSM(InputStream osmsource,
                         RouteHandler routeHandler, Model model, TextHandler textHandler,
                         HashMap<WayType, ResizingArray<String[]>> wayTypeCases) throws XMLStreamException {

        Map<WayType, ResizingArray<Drawable>> ways = new EnumMap<>(WayType.class);
        for (WayType type : WayType.values()) {
            ways.put(type, new ResizingArray<>());
        }

        XMLStreamReader reader = XMLInputFactory
                .newInstance()
                .createXMLStreamReader(osmsource);

        LongIndex idToNodeIndex = new LongIndex();
        LongIndex idToWayIndex = new LongIndex();
        Map<WayType, KDTree> kdTreeMap = new TreeMap<>();
        ResizingArray<OSMNode> tempNodes = new ResizingArray<>();
        ResizingArray<OSMWay> tempWays = new ResizingArray<>();
        ArrayList<Address> addresses = new ArrayList<>();
        List<OSMWay> coast = new ArrayList<>();

        //variables to make OSMWay/OSMRelation
        OSMWay way = null;
        OSMRelation rel = null;
        WayType type = null;

        //used for building the nodegraph specifically edges
        int speedlimit = 0;
        String edgeName = "";

        //variables for addressParsing and OSMNode creation
        Builder b = new Builder();
        long id;
        float lat = 0;
        float lon = 0;

        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "bounds":
                            float minlat = Float.parseFloat(reader.getAttributeValue(null, "minlat"));
                            float minlon = Float.parseFloat(reader.getAttributeValue(null, "minlon"));
                            float maxlat = Float.parseFloat(reader.getAttributeValue(null, "maxlat"));
                            float maxlon = Float.parseFloat(reader.getAttributeValue(null, "maxlon"));

                            //needs to calculate a "lonfactor" so that we can project points on a sphere to points on a 2D map

                            model.setLonfactor((float) Math.cos((maxlat + minlat) / 2 * Math.PI / 180));
                            minlon *= Model.getLonfactor();
                            maxlon *= Model.getLonfactor();

                            model.setMinlat(minlat);
                            model.setMinlon(minlon);
                            model.setMaxlat(maxlat);
                            model.setMaxlon(maxlon);
                            break;
                        case "node":
                            id = Long.parseLong(reader.getAttributeValue(null, "id"));
                            lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                            lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));

                            idToNodeIndex.add(id);
                            //default id is 0 and is assigned later in the nodegraph, if it's part of a way that is in the nodegraph
                            tempNodes.add(new OSMNode(0, (float) (lon * Model.getLonfactor()), lat));
                            break;
                        case "way":
                            id = Long.parseLong(reader.getAttributeValue(null, "id"));
                            //null waytype is default
                            type = null;

                            //id is also 0 as the default for the same reason
                            way = new OSMWay(0);
                            idToWayIndex.add(id);
                            tempWays.add(way);
                            break;
                        case "nd":
                            //the reference is the same as the id given to a specific node
                            long ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            way.add(tempNodes.get(idToNodeIndex.get(ref)));
                            break;
                        case "tag":
                            String k = reader.getAttributeValue(null, "k");
                            String v = reader.getAttributeValue(null, "v");

                            if (k.equals("addr:housenumber")) {
                                b.houseNumber = v.trim();
                            }

                            if (k.equals("addr:street")) {
                                b.streetName = v.trim();
                            }

                            if (k.equals("addr:postcode")) {
                                b.postcode = v.trim();
                            }

                            if (k.equals("addr:city")) {
                                b.city = v.trim();
                            }

                            if (k.equals("addr:municipality")) {
                                b.municipality = v.trim();
                            }

                            if (k.equals("source:maxspeed")) {
                                if (v.equalsIgnoreCase("DK:urban") || v.equalsIgnoreCase("DK:city") ||
                                        v.equalsIgnoreCase("DK:zone50") || v.equalsIgnoreCase("urban")) {
                                    speedlimit = 50;
                                } else if (v.equalsIgnoreCase("DK:zone20")) {
                                    speedlimit = 20;
                                } else if (v.equalsIgnoreCase("dk:zone30") || v.equalsIgnoreCase("DK:zone30;DK:urban") ||
                                        v.equalsIgnoreCase("DK:zone:30")) {
                                    speedlimit = 30;
                                } else if (v.equalsIgnoreCase("dk:zone40") || v.equalsIgnoreCase("DK:zone:40")) {
                                    speedlimit = 40;
                                } else if (v.equalsIgnoreCase("DK:rural") || v.equalsIgnoreCase("DK:trunk")) {
                                    speedlimit = 80;
                                } else if (v.equals("DK:motorway")) {
                                    speedlimit = 130;
                                }
                            }

                            if (k.equals("name")) {
                                edgeName = v;
                            }

                            if (k.equals("maxspeed")) {
                                if (v.equalsIgnoreCase("DK:urban")) {
                                    speedlimit = 50;
                                } else if (v.equalsIgnoreCase("DK:rural")) {
                                    speedlimit = 80;
                                } else if (v.equalsIgnoreCase("DK:motorway")) {
                                    speedlimit = 130;
                                } else if (!(v.equalsIgnoreCase("default") || v.equalsIgnoreCase("implicit") ||
                                        v.equalsIgnoreCase("none") || v.equalsIgnoreCase("signals") ||
                                        v.equalsIgnoreCase("5 knots"))) {
                                    speedlimit = Math.round(Float.valueOf(v));
                                }
                            }

                            //string[0]=waytype's name, strings[1] = k for the case, strings = v for the case.
                            for (Map.Entry<WayType, ResizingArray<String[]>> wayTypeEntry : wayTypeCases.entrySet()) {
                                WayType wayType = wayTypeEntry.getKey();
                                ResizingArray<String[]> tempWay = wayTypeEntry.getValue();

                                for (int i = 0; i < tempWay.size(); i++) {
                                    String[] waycase = tempWay.get(i);

                                    if (k.equals(waycase[0]) && v.equals(waycase[1])) {
                                        type = wayType;
                                    }
                                }
                            }

                            routeHandler.checkDrivabillty(k, v);

                            switch (k) {
                                case "relation":
                                    type = null;
                                    rel = new OSMRelation();
                                    break;
                                case "member":
                                    ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                                    int index = idToWayIndex.get(ref);
                                    OSMWay member;

                                    if (!(index == -1)) {
                                        member = tempWays.get(idToWayIndex.get(ref));
                                        if (member != null) rel.add(member);
                                    }
                                    break;
                            }
                            break;
                        case "relation":
                            type = null;
                            rel = new OSMRelation();
                            break;
                        case "member":
                            ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            int index = idToWayIndex.get(ref);
                            OSMWay member;

                            if (!(index == -1)) {
                                member = tempWays.get(idToWayIndex.get(ref));
                                if (member != null) rel.add(member);
                            }
                            //can actually be null, because relations are given in their entirety despite getting cut off by the dataset
                            break;
                    }
                    break;
                case END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "way":

                            boolean isNodeGraphWay = routeHandler.isNodeGraphWay(type);

                            if (isNodeGraphWay) {
                                routeHandler.addWayToNodeGraph(way, type, edgeName, speedlimit);
                            }

                            if (type == WayType.COASTLINE) {
                                coast.add(way);
                            } else {
                                if (type != null) {
                                    ways.get(type).add(new Polyline(way, isNodeGraphWay));
                                }
                            }

                            if (b.hasFields()) {
                                createAddress(addresses, b, lat, lon);
                            }

                            //resetting variables
                            way = null;
                            b.reset();
                            speedlimit = 0;
                            edgeName = "";
                            routeHandler.resetDrivabillty();
                            break;
                        case "node":

                            if (b.hasFields()) {
                                createAddress(addresses, b, lat, lon);
                            }
                            b.reset();

                            break;
                        case "relation":
                            if (type == WayType.WATER || type == WayType.BUILDING || type == WayType.FOREST
                                    || type == WayType.FARMLAND || type == WayType.PARK || type == WayType.RECREATION
                                    || type == WayType.BOUNDARY_ADMINISTRATIVE || type == WayType.RAILWAY_PLATFORM
                                    || type == WayType.CONSTRUCTION || type == WayType.PARKING) {
                                ways.get(type).add(new MultiPolyline(rel));
                                way = null;
                            }
                            break;
                    }
                    break;
                case PROCESSING_INSTRUCTION:
                    break;
                case CHARACTERS:
                    break;
                case COMMENT:
                    break;
                case SPACE:
                    break;
                case START_DOCUMENT:
                    break;
                case END_DOCUMENT:
                    tempNodes = null;
                    tempWays = null;
                    idToNodeIndex = null;
                    idToWayIndex = null;
                    routeHandler.finishNodeGraph();

                    for (OSMWay c : merge(coast)) {
                        ways.get(WayType.COASTLINE).add(new Polyline(c, false));
                    }

                    //Make and populate KDTrees for each WayType
                    for (Map.Entry<WayType, ResizingArray<Drawable>> entry : ways.entrySet()) {
                        KDTree typeTree = new KDTree();
                        //Add entry values to KDTree
                        typeTree.insertAll(entry.getValue());
                        kdTreeMap.put(entry.getKey(), typeTree);
                    }

                    model.setKdTrees(kdTreeMap);

                    addresses.sort(Address::compareTo);
                    textHandler.makeDatabase(addresses, Model.getDirPath(), Model.getDelimeter());
                    addresses = null;

                    break;
                case ENTITY_REFERENCE:
                    break;
                case ATTRIBUTE:
                    break;
                case DTD:
                    break;
                case CDATA:
                    break;
                case NAMESPACE:
                    break;
                case NOTATION_DECLARATION:
                    break;
                case ENTITY_DECLARATION:
                    break;
            }
        }
    }

    private static void createAddress(ArrayList<Address> addresses, Builder b, float lat, float lon) {
        //0 because id not used
        b.id = 0;
        b.lat = lat;
        b.lon = lon;
        addresses.add(b.build());
    }

    private static Iterable<OSMWay> merge(List<OSMWay> coast) {
        Map<OSMNode, OSMWay> pieces = new HashMap<>();

        for (OSMWay way : coast) {
            OSMWay res = new OSMWay(0);
            OSMWay before = pieces.remove(way.getFirst());

            if (before != null) {
                pieces.remove(before.getFirst());
                for (int i = 0; i < before.size() - 1; i++) {
                    res.add(before.get(i));
                }
            }

            for (int i = 0; i < way.size(); i++) {
                res.add(way.get(i));
            }

            OSMWay after = pieces.remove(way.getLast());

            if (after != null) {
                pieces.remove(after.getLast());
                for (int i = 1; i < after.size(); i++) {
                    res.add(after.get(i));
                }
            }

            pieces.put(res.getFirst(), res);
            pieces.put(res.getLast(), res);
        }

        return pieces.values();
    }

    //for building addresses during parsing
    private static class Builder {
        private int id;
        private float lat, lon;
        private String streetName = "Unknown", houseNumber = "", postcode = "", city = "", municipality = "";

        void reset() {
            id = 0;
            lat = 0;
            lon = 0;
            streetName = "Unknown";
            houseNumber = "";
            postcode = "";
            city = "";
        }

        boolean hasFields() {
            return !streetName.equals("Unknown") && !houseNumber.equals("") && !postcode.equals("") &&
                    (!city.equals("") || !municipality.equals(""));
        }

        Address build() {
            //some streets have a forward slash in them, we need to replace them to avoid io-exceptions in writing their files
            if (streetName.contains("/")) {
                streetName = streetName.replaceAll("/", "");
            }

            if (city.equals("")) {
                city = municipality;
            }

            return new Address(id, lat, lon, streetName, houseNumber, postcode, city);
        }
    }
}