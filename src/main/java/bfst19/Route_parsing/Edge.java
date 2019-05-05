package bfst19.Route_parsing;

import bfst19.Line.OSMNode;

import java.io.Serializable;
import java.util.HashMap;

public class Edge implements Serializable {
    //allways set length and speedlimit as the same unit of measurement, currently km
    String name;
    private float length;
    private int speedlLimit;
    //-1 = you can't drive here
    //0 node v to node w
    //1 node w to node v
    //2 both ways
    private Drivabillity[] drivabillity;
    private OSMNode v;
    private OSMNode w;


    public Edge(float length, int speedlLimit, OSMNode v, OSMNode w,String name,HashMap<Vehicle, Drivabillity> vehicleTypeToDrivable) {
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
                return getLength() / type.maxSpeed;
            }else {
                return getLength() / speedlLimit;
            }
        }else{
            return getLength();
        }
    }

    //this is code dublication
    public double getLength(){return length; }

    public int getSpeedlLimit(){return speedlLimit;}

    public OSMNode getV(){
        return v;
    }

    public OSMNode getW(){
        return w;
    }


    public String getName(){return name;}

    public int either(){
        return v.getId();
    }

    public int other(){
        return w.getId();
    }

    public int getOtherEnd(int id){
        if(id==w.getId()){
            return v.getId();
        }
        return w.getId();
    }

    public OSMNode getOtherEndNode(OSMNode node) {
        if (node.getId() == w.getId()) {
            return v;
        } else {
            return w;
        }
    }

    public OSMNode getThisEndNode(int id) {
        if (id == w.getId()) {
            return w;
        } else {
            return v;
        }
    }

    public boolean isForwardAllowed(Vehicle type, int id) {
        Drivabillity drivable = getDrivableFromVehicleType(type);
        if(type == Vehicle.ABSTRACTVEHICLE){
            return true;
        }
        if(drivable==Drivabillity.BOTHWAYS){
            return true;
        }else if(v.getId()==id) {
            if(drivable==Drivabillity.FORWARD){
                return true;
            }
            //this will actually never happen, as the dataset never has data in such a way that it never happens
        }else if(w.getId()==id){
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

