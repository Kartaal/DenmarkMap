package bfst19.Route_parsing;

import bfst19.Line.OSMNode;

import java.io.Serializable;
import java.util.HashMap;

public class Edge implements Serializable {
    //allways set length and speedlimit as the same unit of measurement, currently km
    String name;
    private double length;
    private double speedlLimit;
    //-1 = you can't drive here
    //0 node v to node w
    //1 node w to node v
    //2 both ways
    private Drivabillity[] drivabillity;
    private OSMNode v;
    private OSMNode w;

    public Edge(double length, double speedlLimit, OSMNode v, OSMNode w,String name,HashMap<Vehicle, Drivabillity> vehicleTypeToDrivable) {
        this.length = length;
        this.speedlLimit = speedlLimit;
        this.v = v;
        this.w = w;
        this.name = name;
        drivabillity = new Drivabillity[vehicleTypeToDrivable.keySet().size()];
        drivabillity[0] = vehicleTypeToDrivable.get(Vehicle.CAR);
        drivabillity[1] = vehicleTypeToDrivable.get(Vehicle.WALKING);
        drivabillity[2] = vehicleTypeToDrivable.get(Vehicle.BIKE);
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
    //this is code dublication
    public double getLength(){return length / speedlLimit; }
    public OSMNode getV(){
        return v;
    }

    public OSMNode getW(){
        return w;
    }

    public String getName(){return name;}

    //todo make these dependt on a call with a specific node
    public long either(){
        return v.getAsLong();
    }

    public long other(){
        return w.getAsLong();
    }

    public long getOtherEnd(long id){
        if(id==w.getAsLong()){
            return v.getAsLong();
        }
        return w.getAsLong();
    }

    public OSMNode getOtherEndNode(OSMNode node){
        if(node.getAsLong()==w.getAsLong()){
            return v;
        }
        return w;
    }

    public boolean isForwardAllowed(Vehicle type, long id) {
        Drivabillity drivable = getDrivableFromVehicleType(type);
        if(drivable==Drivabillity.BOTHWAYS){
            return true;
        }else if(v.getAsLong()==id) {
            if(drivable==Drivabillity.FORWARD){
                return true;
            }
        //this will actually never happen, as the dataset never has data in such a way that it never happens
        }else if(w.getAsLong()==id){
            if(drivable==Drivabillity.BACKWARD){
                return true;
            }
        }
        return false;
    }

    private Drivabillity getDrivableFromVehicleType(Vehicle type){
        if(type==Vehicle.CAR){
            return drivabillity[0];
        }else if(type==Vehicle.WALKING){
            return drivabillity[1];
        }else if(type==Vehicle.BIKE){
            return drivabillity[2];
        }
        return Drivabillity.NOWAY;
    }

    public int compareTo(Edge e, Vehicle type, boolean fastestPath) {
        double firstWeight = getWeight(type,fastestPath);
        double secondWeight = e.getWeight(type,fastestPath);

        return Double.compare(firstWeight,secondWeight);
    }

    @Override
    public String toString(){
        return "name: "+name +" Length: "+length+"m "+" bike: "+getDrivableFromVehicleType(Vehicle.BIKE)+" Car: "+getDrivableFromVehicleType(Vehicle.CAR)+" walking: "+getDrivableFromVehicleType(Vehicle.WALKING)+" "+speedlLimit+" Node v:" + v.toString() + " Node W:" + w.toString();
    }
}
