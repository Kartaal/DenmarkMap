package bfst19.Route_parsing;

import bfst19.OSMNode;

import java.util.HashMap;

public class Edge{
    //allways set length and speedlimit as the same unit of measurement, currently km
    private double length;
    private double speedlLimit;
    //-1 = you can't drive here
    //0 node v to node w
    //1 node w to node v
    //2 both ways
    private HashMap<String, Integer> vehicleTypeToDrivable;
    private OSMNode v;
    private OSMNode w;

    public Edge(double length, double speedlLimit, OSMNode v, OSMNode w,HashMap<String, Integer> vehicleTypeToDrivable) {
        this.length = length;
        this.speedlLimit = speedlLimit;
        this.v = v;
        this.w = w;
        this.vehicleTypeToDrivable = vehicleTypeToDrivable;
    }

    public double getWeight(Vehicle type, boolean fastestPath){
        //if shortest path just return length,
        // else we two cases for calculating the time of traversing the edge
        if(fastestPath){
            //if the vehicle type's speed is less than the speedlimit of this edge,
            // we should use that instead
            if(type.maxSpeed<speedlLimit){
                return length / type.maxSpeed;
            }else {
                return length / speedlLimit;
            }
        }else{
            return length;
        }
    }

    public long either(){
        return v.getAsLong();
    }

    public long other(){
        return w.getAsLong();
    }

    public boolean isForwardAllowed(Vehicle type) {

        int drivable = getDrivableFromVehicleType(type.name());

        if (drivable == 0) {
            return true;
        } else if (drivable == 2) {
            return true;
        }
        return false;

    }

    public boolean isBackWardsAllowed(Vehicle type){

        int drivable = getDrivableFromVehicleType(type.name());

        if (drivable == 1) {
            return true;
        } else if (drivable == 2) {
            return true;
        }
        return false;
    }


    private int getDrivableFromVehicleType(String type){
        return vehicleTypeToDrivable.get(type);
    }

    public int compareTo(Edge e, Vehicle type, boolean fastestPath) {
        double firstWeight = getWeight(type,fastestPath);
        double secondWeight = e.getWeight(type,fastestPath);

        return Double.compare(firstWeight,secondWeight);
    }

    @Override
    public String toString(){
        return length+" "+" bike: "+getDrivableFromVehicleType("BIKE")+" Car: "+getDrivableFromVehicleType("CAR")+" walking: "+getDrivableFromVehicleType("WALKING")+" "+speedlLimit+" Node v:" + v.toString() + " Node W:" + w.toString();
    }
}
