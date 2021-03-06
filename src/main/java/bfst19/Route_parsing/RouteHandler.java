package bfst19.Route_parsing;

import bfst19.*;
import bfst19.Line.OSMNode;
import bfst19.Line.OSMWay;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Routehandler is a helper class for model that is responsible for creating
 * the nodegraph that is then used by Routehandler to do pathfinding.
 * To accomplish these goals it has an EdgeWeightedDigraph that it builds concurrently
 * during parsing of the dataset.
 * <p>
 * To build the nodegraph the routehandler uses 4 helper methods, one for checking if a
 * waytype should be in the nodegraph, one for checking/setting drivabillity from
 * a list of cases/exceptions for drivabillity, another method for putting a way
 * into the nodegraph by using the nodes in the way, and the map of waytype to drivabillity
 * to create edges for that way. The final method is for avoiding loitering.
 * <p>
 * To do pathfinding the routehandler makes an instance of DijstraSp with the nodegraph
 * and a start and end point, and returns the path(if one is found).
 */
public class RouteHandler {
    private static EdgeWeightedDigraph G;
    private static Set<WayType> drivableWaytypes;
    private HashMap<WayType, HashMap<String, ResizingArray<String[]>>> drivableCases;
    private HashMap<WayType, HashMap<Vehicle, Drivabillity>> drivabillty;
    private HashMap<String, Integer> speedDefaults;

    public RouteHandler(EdgeWeightedDigraph G) {
        RouteHandler.G = G;
        speedDefaults = TextHandler.getInstance().parseSpeedDefaults();
        drivableCases = TextHandler.getInstance().parseDrivableCases();
        drivabillty = new HashMap<>();

        for (WayType wayType : drivableCases.keySet()) {
            drivabillty.put(wayType, new HashMap<>());

            for (String vehicleTypeAndDrivable : drivableCases.get(wayType).keySet()) {

                String[] tokens = vehicleTypeAndDrivable.split(" ");
                Vehicle vehicleType = Vehicle.valueOf(tokens[0]);

                Drivabillity defaultDrivable = Drivabillity.intToDrivabillity(Integer.valueOf(tokens[1]));
                drivabillty.get(wayType).put(vehicleType, defaultDrivable);
            }
        }

        drivableWaytypes = drivabillty.keySet();
    }

    public static Set<WayType> getDrivableWayTypes() {
        return drivableWaytypes;
    }

    public static boolean isTraversableNode(OSMNode node, Vehicle type) {
        Iterable<Edge> adj = G.adj(node.getId());
        for (Edge edge : adj) {
            if (edge.isForwardAllowed(type, node.getId())) {
                return true;
            }
        }
        return false;
    }

    public static String getArbitraryAdjRoadName(OSMNode node) {
        if (node == null) {
            return "No Name Found";
        }
        Iterable<Edge> adj = G.adj(node.getId());
        Iterator<Edge> iterator = adj.iterator();
        while (iterator.hasNext()) {
            Edge edge = iterator.next();
            if (!edge.getName().equals("")) {
                return edge.getName();
            }
        }
        return "No Name Found";
    }

    public Iterable<Edge> findPath(OSMNode startNode, OSMNode endNode, Vehicle type, boolean fastestpath) {
        DijkstraSP shortpath = new DijkstraSP(G, startNode, endNode, type, fastestpath);
        return shortpath.pathTo(endNode.getId());
    }

    /**
     * checks the key k and the value v if they
     * are an exception to the default drivabillity
     * for any of the waytypes, and if so it puts that
     * drivabillity in the map of current drivabillty for that waytype.
     *
     * @param k the key.
     * @param v the value.
     */
    public void checkDrivabillty(String k, String v) {

        for (WayType waytype : drivableCases.keySet()) {

            for (String vehicletypeAndDrivable : drivableCases.get(waytype).keySet()) {
                ResizingArray<String[]> vehicleCases = drivableCases.get(waytype).get(vehicletypeAndDrivable);

                for (int i = 0; i < vehicleCases.size(); i++) {
                    String[] caseTokens = vehicleCases.get(i);

                    if (k.equals(caseTokens[0]) && v.equals(caseTokens[1])) {
                        setCurrentDrivability(waytype, vehicletypeAndDrivable, caseTokens[2]);
                    }
                }
            }
        }
    }

    private void setCurrentDrivability(WayType waytype, String vehicletypeAndDrivable, String caseToken) {
        Vehicle vehicletype = Vehicle.valueOf(vehicletypeAndDrivable.split(" ")[0]);

        int drivableValue = Integer.valueOf(caseToken);
        drivabillty.get(waytype).put(vehicletype, Drivabillity.intToDrivabillity(drivableValue));
    }

    public boolean isNodeGraphWay(WayType type) {
        boolean isNodeGraphWay = false;

        for (WayType wayType : drivabillty.keySet()) {
            if (type == wayType) {
                isNodeGraphWay = true;
            }
        }

        return isNodeGraphWay;
    }


    /**
     * addWayToNodeGraph is the method used to create edges from
     * a given way, and put those edges in the nodegraph.
     * For each node in the way, a edge is created between it,
     * and the previous node. The variables type, name and speedlimit
     * are used to make the edges with the name, speedlimit and the current
     * drivabillity for the given waytype, along with the distance between the
     * two points as the length of the edge.
     * After all the edges have been made, it resets drivabillity.
     *
     * @param way        a way to add
     * @param type       the WayType
     * @param name       the name of the way
     * @param speedlimit the speed limit of the way
     */
    public void addWayToNodeGraph(OSMWay way, WayType type, String name, int speedlimit) {
        HashMap<Vehicle, Drivabillity> drivabilltyForWay = drivabillty.get(type);
        OSMNode previousnode = way.get(0);

        G.addVertex(previousnode);
        for (int i = 1; i < way.size(); i++) {

            OSMNode currentNode = way.get(i);

            float previousNodeLat = previousnode.getLat();
            float previousNodeLon = (float) (previousnode.getLon() / Model.getLonfactor());
            float currentNodeLat = currentNode.getLat();
            float currentNodeLon = (float) (currentNode.getLon() / Model.getLonfactor());

            float length = Calculator.calculateDistanceInMeters(previousNodeLat, previousNodeLon, currentNodeLat, currentNodeLon);

            if (speedlimit == 0) {
                speedlimit = speedDefaults.get(type.toString());
            }

            Edge edge = new Edge(length, speedlimit, previousnode, currentNode, name, drivabilltyForWay);

            G.addVertex(currentNode);
            G.addEdge(edge);
            previousnode = currentNode;

        }
        resetDrivabillty();
    }

    public void resetDrivabillty() {

        for (WayType waytype : drivableCases.keySet()) {
            HashMap<Vehicle, Drivabillity> resetDefaults = new HashMap<>();

            for (String vehicleTypeAndDrivable : drivableCases.get(waytype).keySet()) {
                String[] tokens = vehicleTypeAndDrivable.split(" ");
                Vehicle vehicleType = Vehicle.valueOf(tokens[0]);

                Drivabillity drivable = Drivabillity.intToDrivabillity(Integer.valueOf(tokens[1]));
                resetDefaults.put(vehicleType, drivable);
            }
            drivabillty.put(waytype, resetDefaults);
        }
    }

    //to avoid loitering
    public void finishNodeGraph() {
        drivabillty = null;
        drivableCases = null;
        G.trim();
    }

    public EdgeWeightedDigraph getNodeGraph() {
        return G;
    }

}
