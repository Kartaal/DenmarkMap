package bfst19.Route_parsing;

import bfst19.Line.OSMNode;
import bfst19.Line.OSMWay;
import bfst19.Model;
import bfst19.WayType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class RouteHandler{
    private Model model;
    private EdgeWeightedGraph G;
    private HashMap<Long,Integer> idToIndex;
    private HashMap<Integer,Long> indexToId;
    private HashMap<WayType,HashMap<String, ArrayList<String[]>>> drivableCases;
    private HashMap<WayType,HashMap<Vehicle, Integer>> drivabillty;
    private static Set<WayType> drivableWayTypes;
    HashMap<String,Integer> speedDefaults;

    public RouteHandler(Model model, EdgeWeightedGraph G){
        this.model = model;
        this.G = G;
    }

    public RouteHandler(Model model, EdgeWeightedGraph G, HashMap<Long,Integer> idToIndex, HashMap<Integer,Long> indexToId) {
        this.model = model;
        this.G = G;
        this.idToIndex = idToIndex;
        this.indexToId = indexToId;
        speedDefaults = parseSpeedDefaults("src/main/resources/config/Speed_cases.txt");
        drivableCases = parseDrivableCases("src/main/resources/config/Drivable_cases.txt");
        drivabillty = new HashMap<>();

        for(WayType wayType : drivableCases.keySet()){
            drivabillty.put(wayType,new HashMap<>());
            for(String vehicleTypeAndDrivable : drivableCases.get(wayType).keySet()){
                String[] tokens = vehicleTypeAndDrivable.split(" ");
                Vehicle vehicleType = Vehicle.valueOf(tokens[0]);
                Drivabillity defaultDrivable = Drivabillity.valueToDrivabillity(Integer.valueOf(tokens[1]));
                drivabillty.get(wayType).put(vehicleType,defaultDrivable);
            }
        }
        drivableWayTypes = drivabillty.keySet();

    }

    public Iterable<Edge> findPath(long startNodeId, long endNodeId,Vehicle type,boolean fastestpath){
        DijkstraSP shortpath = new DijkstraSP(G,startNodeId, type,fastestpath);
        Iterable<Edge> path = shortpath.pathTo(G.getIndexFromId(endNodeId));
        return path;
    }


    private HashMap<WayType,HashMap<String,ArrayList<String[]>>> parseDrivableCases(String filepath) {
        ArrayList<String> cases = model.getTextFile(filepath);
        HashMap<WayType,HashMap<String,ArrayList<String[]>>> drivableCases = new HashMap<>();

        WayType wayType = WayType.valueOf(cases.get(0));
        drivableCases.put(wayType,new HashMap<>());
        String[] tokens;
        String vehicleType = "";
        String vehicleDrivable = "";

        for(int i = 1 ; i<cases.size() ; i++){
            String line = cases.get(i);
            if(line.startsWith("%")){
                tokens = cases.get(i+1).split(" ");
                i++;
                vehicleType = tokens[0];
                vehicleDrivable = tokens[1];
                drivableCases.get(wayType).put(vehicleType+" "+vehicleDrivable,new ArrayList<>());
            }else if(line.startsWith("$")){
                wayType = WayType.valueOf(cases.get(i+1));
                drivableCases.put(wayType,new HashMap<>());
                i++;
            }else{
                String[] lineTokens = line.split(" ");
                drivableCases.get(wayType).get(vehicleType+" "+vehicleDrivable).add(lineTokens);
            }
        }
        return drivableCases;
    }

    private HashMap<String,Integer> parseSpeedDefaults(String filepath){
        ArrayList<String> cases = model.getTextFile(filepath);
        HashMap<String,Integer> speedDefaults = new HashMap<>();
        for(int i = 0 ; i<cases.size() ; i++){
            String line = cases.get(i);
            String[] tokens = line.split(" ");
            speedDefaults.put(tokens[0],Integer.valueOf(tokens[1]));
        }
        return speedDefaults;
    }

    public void checkDrivabillty(String k, String v) {
        for(WayType waytype : drivableCases.keySet()){
            for(String vehicletypeAndDrivable : drivableCases.get(waytype).keySet()){
                ArrayList<String[]> vehicleCases = drivableCases.get(waytype).get(vehicletypeAndDrivable);

                Vehicle vehicletype = Vehicle.valueOf(vehicletypeAndDrivable.split(" ")[0]);
                for(int i = 0 ; i<vehicleCases.size() ; i++){
                    String[] caseTokens = vehicleCases.get(i);
                    if(k.equals(caseTokens[0])&&v.equals(caseTokens[1])){
                        drivabillty.get(waytype).put(vehicletype,Drivabillity.valueToDrivabillity(Integer.valueOf(caseTokens[2])));
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

    public void addWayToNodeGraph(OSMWay way, WayType type, String name, double speedlimit) {
        HashMap<Vehicle,Drivabillity> drivabilltyForWay = drivabillty.get(type);
        OSMNode previousnode = way.get(0);

        long previousnodeID = previousnode.getAsLong();
        G.addVertex(previousnodeID);
        for(int i = 1 ; i<way.size() ; i++){

            OSMNode currentNode = way.get(i);

            double previousNodeLat = previousnode.getLat();
            double previousNodeLon = previousnode.getLon()/model.getLonfactor();
            double currentNodeLat = currentNode.getLat();
            double currentNodeLon = currentNode.getLon()/model.getLonfactor();

            double length = model.calculateDistanceInMeters(previousNodeLat,previousNodeLon,currentNodeLat,currentNodeLon);

            if(speedlimit==0){
                speedlimit = speedDefaults.get(type.toString());
            }

            Edge edge = new Edge(length,speedlimit,previousnode,currentNode,name,drivabilltyForWay);

            long currentnodeID = currentNode.getAsLong();
            G.addVertex(currentnodeID);
            G.addEdge(edge);
            previousnode = currentNode;

            //resets the drivabillity for the waytype by gettting the values from the default
            resetDrivabillty();
        }
    }

    private void resetDrivabillty(){
        for(WayType waytype : drivableCases.keySet()){
            HashMap<Vehicle,Drivabillity> resetDefaults = new HashMap<>();
            for(String vehicleTypeAndDrivable : drivableCases.get(waytype).keySet()){
                String[] tokens = vehicleTypeAndDrivable.split(" ");
                Vehicle vehicleType = Vehicle.valueOf(tokens[0]);
                Drivabillity drivable = Drivabillity.valueToDrivabillity(Integer.valueOf(tokens[1]));
                resetDefaults.put(vehicleType,drivable);
            }
            drivabillty.put(waytype,resetDefaults);
        }
    }

    public static Set<WayType> getDrivableWayTypes(){
        return drivableWayTypes;
    }

    public Iterable<Edge> getAdj(long id, Vehicle type) {
        int index = G.getIndexFromId(id);
        return G.adj(index,type);
    }

    public EdgeWeightedGraph getNodeGraph() {
        return G;
    }
}
