package bfst19.Route_parsing;

import bfst19.Line.OSMNode;
import bfst19.ResizingArray;

import java.io.Serializable;
import java.util.ArrayList;

public class EdgeWeightedDigraph implements Serializable {

	private static final String NEWLINE = System.getProperty("line.separator");

	private int V;
	private int E;
	private ResizingArray<ResizingArray<Edge>> adj;

	public EdgeWeightedDigraph() {
		this.V = 0;
		this.E = 0;
		adj = new ResizingArray<>();
	}

	int V() {
		return V;
	}

	//used for testing
	public int E() {
		return E;
	}

	// throw an IllegalArgumentException unless {@code 0 <= v < V}
	private void validateVertex(int v) {
		if (v < 0 || v >= V)
			throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V - 1));
	}

	private boolean isVertex(int id) {
		return 0 < id && id < V;
	}

	void addVertex(OSMNode node) {
		if (!isVertex(node.getId())) {
			node.setId(V);
			adj.add(new ResizingArray<>());
			V++;
		}
	}

	void addEdge(Edge e) {
		int v = e.either().getId();
		int w = e.other().getId();

		validateVertex(v);
		validateVertex(w);

		adj.get(v).add(e);
		adj.get(w).add(e);
		E++;
	}

	Iterable<Edge> adj(int v) {
		validateVertex(v);
		ResizingArray adjacent = adj.get(v);
		return (Iterable<Edge>) adjacent;
	}

	Iterable<Edge> edges() {
		ArrayList list = new ArrayList();
		for (int v = 0; v < V; v++) {
			int selfLoops = 0;

			for (Edge e : adj(v)) {
				if (e.other().getId() > v) {
					list.add(e);
				}
				// add only one copy of each self loop (self loops will be consecutive)
				else if (e.other().getId() == v) {
					if (selfLoops % 2 == 0) list.add(e);
					selfLoops++;
				}
			}
		}
		return list;
	}

	void trim() {
		for (int i = 0; i < adj.size(); i++) {
			adj.get(i).trim();
		}
		adj.trim();
	}

}
