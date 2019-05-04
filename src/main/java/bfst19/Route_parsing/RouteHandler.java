package bfst19.Route_parsing;

import bfst19.Calculator;
import bfst19.Line.OSMNode;
import bfst19.Line.OSMWay;
import bfst19.Model;
import bfst19.TextHandler;
import bfst19.WayType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class RouteHandler{
    private static EdgeWeightedGraph G;
    private HashMap<WayType,HashMap<String, ResizingArray<String[]>>> drivableCases;
    private HashMap<WayType,HashMap<Vehicle, Drivabillity>> drivabillty;
    private HashMap<WayType,HashMap<Vehicle, Drivabillity>> defaultDrivabillty;
    private static Set<WayType> drivableWaytypes;
    HashMap<String,Integer> speedDefaults;

    /*public RouteHandler(Model model, EdgeWeightedGraph G){
        this.model = model;
        this.G = G;
    }*/

    public RouteHandler(EdgeWeightedGraph G) {
        this.G = G;
        speedDefaults = TextHandler.getInstance().parseSpeedDefaults("src/main/resources/config/Speed_cases.txt");
        drivableCases = TextHandler.getInstance().parseDrivableCases("src/main/resources/config/Drivable_cases.txt");
        drivabillty = new HashMap<>();

        for(WayType wayType : drivableCases.keySet()){
            drivabillty.put(wayType,new HashMap<>());
            for(String vehicleTypeAndDrivable : drivableCases.get(wayType).keySet()){

                String[] tokens = vehicleTypeAndDrivable.split(" ");
                Vehicle vehicleType = Vehicle.valueOf(tokens[0]);
                Drivabillity defaultDrivable = Drivabillity.intToDrivabillity(Integer.valueOf(tokens[1]));
                drivabillty.get(wayType).put(vehicleType,defaultDrivable);
            }
        }
        defaultDrivabillty = (HashMap<WayType, HashMap<Vehicle, Drivabillity>>)drivabillty.clone();
        drivabillty.clone();
        drivableWaytypes = drivabillty.keySet();

    }

    public static Set<WayType> getDrivableWayTypes() {
        return drivableWaytypes;
    }

    public Iterable<Edge> findPath(OSMNode startNode, OSMNode endNode,Vehicle type,boolean fastestpath){
        DijkstraSP shortpath = new DijkstraSP(G,startNode,endNode, type,fastestpath);
        Iterable<Edge> path = shortpath.pathTo(endNode.getId());
        return path;
    }

    public void checkDrivabillty(String k, String v) {
        for(WayType waytype : drivableCases.keySet()){
            for(String vehicletypeAndDrivable : drivableCases.get(waytype).keySet()){
                ResizingArray<String[]> vehicleCases = drivableCases.get(waytype).get(vehicletypeAndDrivable);
                for(int i = 0 ; i < vehicleCases.size() ; i++){
                    String[] caseTokens = vehicleCases.get(i);
                    if(k.equals(caseTokens[0])&&v.equals(caseTokens[1])){
                        Vehicle vehicletype = Vehicle.valueOf(vehicletypeAndDrivable.split(" ")[0]);
                        int drivableValue = Integer.valueOf(caseTokens[2]);
                        drivabillty.get(waytype).put(vehicletype,Drivabillity.intToDrivabillity(drivableValue));
                    }
                }
            }
        }
    }

    public boolean isNodeGraphWay(WayType type){
        boolean isNodegraphWay = false;
        for(WayType wayType : drivabillty.keySet()){
            if(type==wayType){
                isNodegraphWay = true;
            }
        }
        return isNodegraphWay;
    }

    public void addWayToNodeGraph(OSMWay way, WayType type, String name, int speedlimit) {
        HashMap<Vehicle,Drivabillity> drivabilltyForWay = drivabillty.get(type);
        OSMNode previousnode = way.get(0);

        G.addVertex(previousnode);
        for(int i = 1 ; i<way.size() ; i++){

            OSMNode currentNode = way.get(i);

            float previousNodeLat = previousnode.getLat();
            float previousNodeLon = (float) (previousnode.getLon()/Model.getLonfactor());
            float currentNodeLat = currentNode.getLat();
            float currentNodeLon = (float) (currentNode.getLon()/Model.getLonfactor());

            float length = Calculator.calculateDistanceInMeters(previousNodeLat,previousNodeLon,currentNodeLat,currentNodeLon);

            if(speedlimit==0){
                speedlimit = speedDefaults.get(type.toString());
            }

            Edge edge = new Edge(length,speedlimit,previousnode,currentNode,name,drivabilltyForWay);

            G.addVertex(currentNode);
            G.addEdge(edge);
            previousnode = currentNode;

        }
        //resets the drivabillity for the waytype by gettting the values from the default
        resetDrivabillty();
    }

    public void resetDrivabillty(){
        for(WayType waytype : drivableCases.keySet()){
            HashMap<Vehicle,Drivabillity> resetDefaults = new HashMap<>();
            for(String vehicleTypeAndDrivable : drivableCases.get(waytype).keySet()){
                String[] tokens = vehicleTypeAndDrivable.split(" ");
                Vehicle vehicleType = Vehicle.valueOf(tokens[0]);
                Drivabillity drivable = Drivabillity.intToDrivabillity(Integer.valueOf(tokens[1]));
                resetDefaults.put(vehicleType,drivable);
            }
            drivabillty.put(waytype,resetDefaults);
        }
    }

    /*private void resetDrivabillty(){
        drivabillty =(HashMap<WayType, HashMap<Vehicle, Drivabillity>>) defaultDrivabillty.clone();
    }*/

    public Iterable<Edge> getAdj(int id, Vehicle type) {
        return G.adj(id);
    }

    public static boolean isTraversableNode(OSMNode node, Vehicle type){
        Iterable<Edge> adj = G.adj(node.getId());
        for(Edge edge : adj){
            if(edge.isForwardAllowed(type,node.getId())){
                return true;
            }
        }
        return false;
    }

    public static ResizingArray<Edge> getAdjacentEdges(int id) {
        return G.getAdjacentEdges(id);
    }

    public static String getArbitraryAdjRoadName(OSMNode node){
        Iterable<Edge> adj = G.adj(node.getId());
        Iterator<Edge> iterator = adj.iterator();
        if(iterator.hasNext()){
            return iterator.next().getName();
        }else {
            return "";
        }
    }

    public void finishNodeGraph(){
        System.out.println(G.E());
        System.out.println(G.V());
        drivabillty = null;
        defaultDrivabillty = null;
        drivableCases = null;
        G.trim();
    }

    public EdgeWeightedGraph getNodeGraph() {
        return G;
    }
}

