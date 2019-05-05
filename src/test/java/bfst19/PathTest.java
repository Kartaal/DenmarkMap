package bfst19;

import bfst19.Line.OSMNode;
import bfst19.Route_parsing.Edge;
import bfst19.Route_parsing.Vehicle;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import javafx.geometry.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class PathTest {
    List<String> args = new ArrayList<>();
    Model model;

    @Before
    public void setup() throws XMLStreamException, IOException, ClassNotFoundException {
        args.add("data/bornholm.zip.obj");
        model = new Model(args);
    }

    @Test
    public void simplePathCar(){
        Vehicle vehicle = Vehicle.CAR;
        boolean fastestPath = false;
        Point2D startlocation =  new Point2D(14.6958260*model.getLonfactor(),  55.1024570);
        OSMNode startNode = model.getNearestRoad(startlocation,vehicle);

        Point2D endlocation =  new Point2D(14.6865800*model.getLonfactor(),55.1001850);
        OSMNode endNode = model.getNearestRoad(endlocation,vehicle);

        Iterable<Edge> path =  model.findPath(startNode,endNode,vehicle,fastestPath);

        float length = 0;
        for(Edge edge : path){
            length += edge.getLength();
        }
        //expected lengths of paths are gotten from OpenStreetMaps
        assertEquals(742,length,10);
    }

    @Test
    public void simplePathBike(){
        Vehicle vehicle = Vehicle.BIKE;
        boolean fastestPath = false;
        Point2D startlocation =  new Point2D(14.8500151*model.getLonfactor(),  55.1091185);
        OSMNode startNode = model.getNearestRoad(startlocation,vehicle);

        Point2D endlocation =  new Point2D( 14.8544070*model.getLonfactor(), 55.1150390);
        OSMNode endNode = model.getNearestRoad(endlocation,vehicle);

        Iterable<Edge> path =  model.findPath(startNode,endNode,vehicle,fastestPath);

        float length = 0;
        for(Edge edge : path){
            length += edge.getLength();
        }
        //expected lengths of paths are gotten from OpenStreetMaps
        assertEquals(980,length,10);
    }

    @Test
    public void simplePathWalking(){
        Vehicle vehicle = Vehicle.WALKING;
        boolean fastestPath = false;
        Point2D startlocation =  new Point2D( 14.7109850*model.getLonfactor(),   55.1430020);
        OSMNode startNode = model.getNearestRoad(startlocation,vehicle);

        Point2D endlocation =  new Point2D( 14.7042780*model.getLonfactor(),  55.1210410);
        OSMNode endNode = model.getNearestRoad(endlocation,vehicle);

        Iterable<Edge> path =  model.findPath(startNode,endNode,vehicle,fastestPath);

        float length = 0;
        for(Edge edge : path){
            length += edge.getLength();
        }
        //expected lengths of paths are gotten from OpenStreetMaps
        assertEquals(3000,length,10);
    }

    @Test
    public void fastestPathCar(){
        Vehicle vehicle = Vehicle.CAR;
        boolean fastestPath = true;
        Point2D startlocation =  new Point2D(15.0793910*model.getLonfactor(),54.9935040);
        OSMNode startNode = model.getNearestRoad(startlocation,vehicle);

        Point2D endlocation =  new Point2D(14.7725020*model.getLonfactor(), 55.2860650);
        OSMNode endNode = model.getNearestRoad(endlocation,vehicle);

        Iterable<Edge> path =  model.findPath(startNode,endNode,vehicle,fastestPath);

        float length = 0;
        for(Edge edge : path){
            length += edge.getLength();
        }
        //expected lengths of paths are gotten from OpenStreetMaps
        assertEquals(51000,length,100);
    }


}
