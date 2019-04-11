package bfst19;

import bfst19.KDTree.KDTree;
import javafx.geometry.Point2D;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import org.junit.Before;
import org.junit.Test;
import java.util.*;

import static org.junit.Assert.*;

public class KDTreeTest {
	Map<WayType, KDTree> kdTreeMap = new TreeMap<>();
	OSMWay way;
	WayType type;
	LongIndex<OSMNode> idToNode = new LongIndex<>();
	//LongIndex<OSMWay> idToWay = new LongIndex<OSMWay>();			for relations
	Map<WayType, List<Drawable>> ways = new EnumMap<>(WayType.class);

	Affine transform = new Affine();

	int servicePolylines;

	@Before
	public void initialize() {
		//Fill map with empty arraylists as setup
		for (WayType type : WayType.values()) {
			ways.put(type, new ArrayList<>());
		}

		//Setup transform
		transform.prependScale(1,-1, 0, 0);

		//Set up a bunch of nodes for putting in ways
		OSMNode node = new OSMNode(5852189410L, 14.7999881f, 55.2672338f);
		idToNode.add(node);
		node = new OSMNode(5852189412L, 14.8003713f, 55.2673134f);
		idToNode.add(node);
		node = new OSMNode(5852189411L, 14.7997896f, 55.2672766f);
		idToNode.add(node);
		node = new OSMNode(5852189413L, 14.8001919f, 55.2675585f);
		idToNode.add(node);
		node = new OSMNode(5852189414L, 14.8002576f, 55.2675671f);
		idToNode.add(node);
		node = new OSMNode(5852195004L, 14.8493998f, 55.2433121f);
		idToNode.add(node);
		node = new OSMNode(5852195005L, 14.8496784f, 55.2434415f);
		idToNode.add(node);
		node = new OSMNode(5852195007L, 14.8497670f, 55.2434826f);
		idToNode.add(node);
		node = new OSMNode(5852227208L, 14.7818229f, 55.1828097f);
		idToNode.add(node);
		node = new OSMNode(5852227209L, 14.7821364f, 55.1828254f);
		idToNode.add(node);



		//Add a whole bunch of ways
		way = new OSMWay(619269638);
		type = WayType.SERVICE;
		way.add(idToNode.get(5852189411L));
		way.add(idToNode.get(5852189413L));
		way.add(idToNode.get(5852189414L));
		ways.get(type).add(new Polyline(way));

		servicePolylines++;


		way = new OSMWay(619269637);
		type = WayType.SERVICE;
		way.add(idToNode.get(5852189410L));
		way.add(idToNode.get(5852189412L));
		ways.get(type).add(new Polyline(way));

		servicePolylines++;

		way = new OSMWay(619270946);
		type = WayType.SERVICE;
		way.add(idToNode.get(5852195004L));
		way.add(idToNode.get(5852195005L));
		way.add(idToNode.get(5852195007L));
		ways.get(type).add(new Polyline(way));

		servicePolylines++;

		way = new OSMWay(619274513);
		type = WayType.SERVICE;
		way.add(idToNode.get(5852227208L));
		way.add(idToNode.get(5852227209L));
		ways.get(type).add(new Polyline(way));

		servicePolylines++;

		//Stolen from Model
		//Make and populate KDTrees for each WayType
		for(Map.Entry<WayType, List<Drawable>> entry : ways.entrySet()) {
			KDTree typeTree = new KDTree();
			//Add entry values to KDTree
			typeTree.insertAll(entry.getValue());
			//Add KDTree to TreeMap
			kdTreeMap.put(entry.getKey(), typeTree);
		}
	}


	@Test
	public void ServiceKDTreeExists(){

		//Tests that root's left and right child aren't null
		assertTrue(kdTreeMap.get(WayType.SERVICE).getRoot().getNodeL() != null && kdTreeMap.get(WayType.SERVICE).getRoot().getNodeR() != null);
	}


	@Test
	public void DitchKDTreeNotExists(){

		//Tests that root's left and right child are null
		assertTrue(kdTreeMap.get(WayType.DITCH).getRoot().getNodeL() == null && kdTreeMap.get(WayType.DITCH).getRoot().getNodeR() == null);
	}


	//TODO This fails, root.NodeL has 3 elements, root.NodeR is null and it is supposed to have 4 elements total
	@Test
	public void GetAllServiceLines(){
		//Tests that KDTree for SERVICE WayType has as many elements

		//Actual min and max coords
		//min X 14.7818229		min Y 55.1828097
		//max X 14.8497670		max Y 55.2675671
		Point2D minPoint = getModelCoords(14.6818229f, -55.0828097f);
		Point2D maxPoint = getModelCoords(14.9497670f, -55.3675671f);
		//BoundingBox with a BB bigger than the coords to catch everything
		BoundingBox bb = new BoundingBox(minPoint.getX(), minPoint.getY(),
				maxPoint.getX()-minPoint.getX(), maxPoint.getY()-minPoint.getY());

		assertEquals(servicePolylines, ((Set<Drawable>) kdTreeMap.get(WayType.SERVICE).rangeQuery(bb)).size());
	}


	@Test
	public void GetMiddleHalfServiceLines(){
		//Tests rangeQuery gets the correct middle elements of the KDTree for SERVICE WayType


		/*Center  X			Y
		14.800080			55.267468
		14.800179			55.267275
		14.849615			55.243413
		14.781979			55.182817

		//Actual min and max coords for 2 middle elements
		//min X 14.7997896		min Y 55.2433121
		//max X 14.8493998		max Y 55.2673134
		*/
		Point2D minPoint = getModelCoords(14.800050f, -55.243313f);
		Point2D maxPoint = getModelCoords(14.810179f, -55.267375f);
		//BoundingBox with a BB bigger than the coords to catch everything
		BoundingBox bb = new BoundingBox(minPoint.getX(), minPoint.getY(),
				maxPoint.getX()-minPoint.getX(), maxPoint.getY()-minPoint.getY());

		int i = ((Set<Drawable>) kdTreeMap.get(WayType.SERVICE).rangeQuery(bb)).size();

		//TODO Ensure the found elements are the right ones instead of just matching a certain number
		assertEquals(servicePolylines/2, i);
	}


	//TODO This fails, root.NodeL has 3 elements, root.NodeR is null and it is supposed to have 4 elements total
	@Test
	public void CheckEmptyQueryBox(){
		//Tests that KDTree for SERVICE WayType has as many elements

		//Actual min and max coords
		//min X 14.7818229		min Y 55.1828097
		//max X 14.8497670		max Y 55.2675671
		Point2D minPoint = getModelCoords(10f, -20f);
		Point2D maxPoint = getModelCoords(11f, -23f);
		//BoundingBox with a BB bigger than the coords to catch everything
		BoundingBox bb = new BoundingBox(minPoint.getX(), minPoint.getY(),
				maxPoint.getX()-minPoint.getX(), maxPoint.getY()-minPoint.getY());

		assertEquals(0, ((Set<Drawable>) kdTreeMap.get(WayType.SERVICE).rangeQuery(bb)).size());
	}


	//Test helper method
	Point2D getModelCoords(double x, double y) {
		try{
			return transform.inverseTransform(x,y);
		}catch (NonInvertibleTransformException e) {
			e.printStackTrace();
			return null;
		}
	}
}
