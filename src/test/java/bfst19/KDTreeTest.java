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
	Set<Drawable> midSet = new HashSet<>();
	Set<Drawable> fullSet = new HashSet<>();

	Affine transform = new Affine();

	@Before
	public void initialize() {
		Map<WayType, List<Drawable>> ways = new EnumMap<>(WayType.class);
		LongIndex<OSMNode> idToNode = new LongIndex<>();
		//LongIndex<OSMWay> idToWay = new LongIndex<OSMWay>();			for relations
		OSMWay way;
		WayType type;
		//Fill map with empty arraylists as setup
		for (WayType wtype : WayType.values()) {
			ways.put(wtype, new ArrayList<>());
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
		Polyline line = new Polyline(way);
		ways.get(type).add(line);

		midSet.add(line);
		fullSet.add(line);


		way = new OSMWay(619269637);
		type = WayType.SERVICE;
		way.add(idToNode.get(5852189410L));
		way.add(idToNode.get(5852189412L));
		line = new Polyline(way);
		ways.get(type).add(line);

		midSet.add(line);
		fullSet.add(line);

		way = new OSMWay(619270946);
		type = WayType.SERVICE;
		way.add(idToNode.get(5852195004L));
		way.add(idToNode.get(5852195005L));
		way.add(idToNode.get(5852195007L));
		line = new Polyline(way);
		ways.get(type).add(line);

		fullSet.add(line);

		way = new OSMWay(619274513);
		type = WayType.SERVICE;
		way.add(idToNode.get(5852227208L));
		way.add(idToNode.get(5852227209L));
		line = new Polyline(way);
		ways.get(type).add(line);

		fullSet.add(line);

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
	public void testServiceKDTreeExists(){
		KDTree.KDNode l = kdTreeMap.get(WayType.SERVICE).getRoot().getNodeL();
		KDTree.KDNode r = kdTreeMap.get(WayType.SERVICE).getRoot().getNodeR();

		//Tests that root's left and right child aren't null
		assertTrue(kdTreeMap.get(WayType.SERVICE).getRoot().getNodeL() != null && kdTreeMap.get(WayType.SERVICE).getRoot().getNodeR() != null);
	}


	@Test
	public void testDitchKDTreeNotExists(){

		//Tests that root's left and right child are null
		assertTrue(kdTreeMap.get(WayType.DITCH).getRoot().getNodeL() == null && kdTreeMap.get(WayType.DITCH).getRoot().getNodeR() == null);
	}


	@Test
	public void testGetAllServiceLines(){
		//Tests that KDTree for SERVICE WayType has as many elements

		//Actual min and max coords
		//min X 14.7818229		min Y 55.1828097
		//max X 14.8497670		max Y 55.2675671
		Point2D minPoint = getModelCoords(14.6818229f, -55.0828097f);
		Point2D maxPoint = getModelCoords(14.9497670f, -55.3675671f);
		//BoundingBox with a BB bigger than the coords to catch everything
		BoundingBox bb = new BoundingBox(minPoint.getX(), minPoint.getY(),
				maxPoint.getX()-minPoint.getX(), maxPoint.getY()-minPoint.getY());

		Set<Drawable> set = ((Set<Drawable>) kdTreeMap.get(WayType.SERVICE).rangeQuery(bb));
		assertEquals(fullSet, set);
	}


	@Test
	public void testGetMiddleHalfServiceLines(){
		//Tests rangeQuery gets the correct x-middle elements of the KDTree for SERVICE WayType

		//Actual min and max coords for 2 middle elements
		//min X 14.799789428710938		min Y 55.267234802246094
		//max X 14.800257682800293		max Y 55.2675666809082

		Point2D minPoint = getModelCoords(14.799689428710938f, -55.267134802246094f);
		Point2D maxPoint = getModelCoords(14.80047117f, -55.2676666809f);
		BoundingBox bb = new BoundingBox(minPoint.getX(), minPoint.getY(),
				maxPoint.getX()-minPoint.getX(), maxPoint.getY()-minPoint.getY());


		Set<Drawable> set = ((Set<Drawable>) kdTreeMap.get(WayType.SERVICE).rangeQuery(bb));

		assertEquals(midSet, set);
	}


	//TODO This fails, root.NodeL has 3 elements, root.NodeR is null and it is supposed to have 4 elements total
	@Test
	public void testCheckEmptyQueryBox(){
		//Tests that KDTree for SERVICE WayType has as many elements

		//Actual min and max coords
		//min X 14.7818229		min Y 55.1828097
		//max X 14.8497670		max Y 55.2675671
		Point2D minPoint = getModelCoords(10f, -20f);
		Point2D maxPoint = getModelCoords(11f, -23f);
		//BoundingBox with a BB bigger than the coords to catch everything
		BoundingBox bb = new BoundingBox(minPoint.getX(), minPoint.getY(),
				maxPoint.getX()-minPoint.getX(), maxPoint.getY()-minPoint.getY());

		Set<Drawable> emptySet = new HashSet<>();

		assertEquals(emptySet, kdTreeMap.get(WayType.SERVICE).rangeQuery(bb));
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
