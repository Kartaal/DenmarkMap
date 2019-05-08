package bfst19;

import bfst19.Exceptions.nothingNearbyException;
import bfst19.Line.OSMNode;
import bfst19.Route_parsing.Edge;
import bfst19.Route_parsing.Vehicle;
import javafx.geometry.Point2D;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class PathTest {
	List<String> args = new ArrayList<>();
	Model model;

	@Before
	public void setup() throws XMLStreamException, IOException, ClassNotFoundException {
		args.add("data/bornholm.zip.obj");
		model = new Model(args);
	}

	@Test
	public void simplePathCar() throws nothingNearbyException {
		Vehicle vehicle = Vehicle.CAR;
		boolean fastestPath = false;
		Point2D startlocation = new Point2D(14.6958260 * model.getLonfactor(), 55.1024570);
		OSMNode startNode = model.getNearestRoad(startlocation, vehicle);

		Point2D endlocation = new Point2D(14.6865800 * model.getLonfactor(), 55.1001850);
		OSMNode endNode = model.getNearestRoad(endlocation, vehicle);

		Iterable<Edge> path = model.findPath(startNode, endNode, vehicle, fastestPath);

		float length = 0;
		for (Edge edge : path) {
			length += edge.getLength();
		}
		//expected lengths of paths are gotten from OpenStreetMaps
		assertEquals(742, length, 10);
	}

	@Test
	public void simplePathBike() throws nothingNearbyException {
		Vehicle vehicle = Vehicle.BIKE;
		boolean fastestPath = false;
		Point2D startlocation = new Point2D(14.8500151 * model.getLonfactor(), 55.1091185);
		OSMNode startNode = model.getNearestRoad(startlocation, vehicle);

		Point2D endlocation = new Point2D(14.8544070 * model.getLonfactor(), 55.1150390);
		OSMNode endNode = model.getNearestRoad(endlocation, vehicle);

		Iterable<Edge> path = model.findPath(startNode, endNode, vehicle, fastestPath);

		float length = 0;
		for (Edge edge : path) {
			length += edge.getLength();
		}
		//expected lengths of paths are gotten from OpenStreetMaps
		assertEquals(980, length, 10);
	}

	@Test
	public void simplePathWalking() throws nothingNearbyException {
		Vehicle vehicle = Vehicle.WALKING;
		boolean fastestPath = false;
		Point2D startlocation = new Point2D(14.7109850 * model.getLonfactor(), 55.1430020);
		OSMNode startNode = model.getNearestRoad(startlocation, vehicle);

		Point2D endlocation = new Point2D(14.7042780 * model.getLonfactor(), 55.1210410);
		OSMNode endNode = model.getNearestRoad(endlocation, vehicle);

		Iterable<Edge> path = model.findPath(startNode, endNode, vehicle, fastestPath);

		float length = 0;
		for (Edge edge : path) {
			length += edge.getLength();
		}
		//expected lengths of paths are gotten from OpenStreetMaps
		assertEquals(3000, length, 10);
	}

	@Test
	public void fastestPathCar() throws nothingNearbyException {
		Vehicle vehicle = Vehicle.CAR;
		boolean fastestPath = true;
		Point2D startlocation = new Point2D(15.0793910 * model.getLonfactor(), 54.9935040);
		OSMNode startNode = model.getNearestRoad(startlocation, vehicle);

		Point2D endlocation = new Point2D(14.7725020 * model.getLonfactor(), 55.2860650);
		OSMNode endNode = model.getNearestRoad(endlocation, vehicle);

		Iterable<Edge> path = model.findPath(startNode, endNode, vehicle, fastestPath);

		float length = 0;
		for (Edge edge : path) {
			length += edge.getLength();
		}
		//expected lengths of paths are gotten from OpenStreetMaps
		assertEquals(51000, length, 100);
	}

	@Test
	public void fastestPathBike() throws nothingNearbyException {
		Vehicle vehicle = Vehicle.BIKE;
		boolean fastestPath = true;
		Point2D startlocation = new Point2D(14.7202530 * model.getLonfactor(), 55.1095860);
		// 55.1095860, 14.7202530
		OSMNode startNode = model.getNearestRoad(startlocation, vehicle);


		Point2D endlocation = new Point2D(14.7154240 * model.getLonfactor(), 55.1011610);
		//55.1010220, 14.7144240
		OSMNode endNode = model.getNearestRoad(endlocation, vehicle);

		Iterable<Edge> path = model.findPath(startNode, endNode, vehicle, fastestPath);

		float length = 0;
		for (Edge edge : path) {
			length += edge.getLength();
		}
		//expected lengths of paths are gotten from OpenStreetMaps
		//if wrong way, it would be 348
		assertEquals(1700, length, 100);
	}

	@Test
	public void correctDirectionInRoundabout() throws nothingNearbyException {
		Vehicle vehicle = Vehicle.CAR;
		boolean fastestPath = false;
		Point2D startlocation = new Point2D(14.7020980 * model.getLonfactor(), 55.0945350);
		// 55.0945350, 14.7020980
		OSMNode startNode = model.getNearestRoad(startlocation, vehicle);

		Point2D endlocation = new Point2D(14.7027070 * model.getLonfactor(), 55.0953090);
		//55.0945350
		OSMNode endNode = model.getNearestRoad(endlocation, vehicle);

		Iterable<Edge> path = model.findPath(startNode, endNode, vehicle, fastestPath);

		float length = 0;
		for (Edge edge : path) {
			length += edge.getLength();
		}
		//expected lengths of paths are gotten from OpenStreetMaps
		assertEquals(165, length, 10);
	}

     /*55.0753350, 14.8483840
             55.0773400, 14.8492920*/

	@Test
	public void nonTraversablePrimaryRoadPath() throws nothingNearbyException {
		Vehicle vehicle = Vehicle.BIKE;
		boolean fastestPath = false;

		Point2D startlocation = new Point2D(14.8483840 * model.getLonfactor(), 55.0753350);
		OSMNode startNode = model.getNearestRoad(startlocation, vehicle);

		Point2D endlocation = new Point2D(14.8492920 * model.getLonfactor(), 55.0773400);
		OSMNode endNode = model.getNearestRoad(endlocation, vehicle);

		Iterable<Edge> path = model.findPath(startNode, endNode, vehicle, fastestPath);
		assertNull(path);
	}

	@Test(expected = ExceptionInInitializerError.class)
	public void nothingNearbyTest() throws nothingNearbyException {
		Vehicle type = Vehicle.CAR;
		Point2D point = new Point2D(1000, 1000);
		model.getNearestRoad(point, type);
	}

	@Test
	public void correctDirectionInOnewayStreet() throws nothingNearbyException {
		Vehicle vehicle = Vehicle.CAR;
		boolean fastestPath = false;
		Point2D startlocation = new Point2D(14.6892942 * model.getLonfactor(), 55.0999144);
		// 55.0849650, 14.7170210 344 fra næste til den her
		OSMNode startNode = model.getNearestRoad(startlocation, vehicle);


		Point2D endlocation = new Point2D(14.6885543 * model.getLonfactor(), 55.1007478);
		//55.0845900, 14.7160720
		OSMNode endNode = model.getNearestRoad(endlocation, vehicle);

		Iterable<Edge> path = model.findPath(startNode, endNode, vehicle, fastestPath);

		float length = 0;
		for (Edge edge : path) {
			length += edge.getLength();
		}
		//expected lengths of paths are gotten from OpenStreetMaps
		//if wrong way, it would be 348
		assertEquals(344, length, 1);
	}


}
