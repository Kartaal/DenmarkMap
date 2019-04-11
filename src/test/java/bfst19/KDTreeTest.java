package bfst19;

import bfst19.KDTree.KDTree;
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

	@Before
	public void initialize() {
		//Fill map with empty arraylists as setup
		for (WayType type : WayType.values()) {
			ways.put(type, new ArrayList<>());
		}

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


		way = new OSMWay(619269637); //2 nodes?
		type = WayType.SERVICE;
		way.add(idToNode.get(5852189410L));
		way.add(idToNode.get(5852189412L));
		ways.get(type).add(new Polyline(way));


		way = new OSMWay(619270946);
		type = WayType.SERVICE;
		way.add(idToNode.get(5852195004L));
		way.add(idToNode.get(5852195005L));
		way.add(idToNode.get(5852195007L));
		ways.get(type).add(new Polyline(way));


		way = new OSMWay(619274513);
		type = WayType.SERVICE;
		way.add(idToNode.get(5852227208L));
		way.add(idToNode.get(5852227209L));
		ways.get(type).add(new Polyline(way));

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
}
