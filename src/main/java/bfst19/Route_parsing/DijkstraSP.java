package bfst19.Route_parsing;

import bfst19.Line.OSMNode;
import edu.princeton.cs.algs4.IndexMinPQ;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DijkstraSP {
	private double[] distTo;          // distTo[v] = distance  of shortest s->v path
	private Edge[] edgeTo;    // edgeTo[v] = last edge on shortest s->v path
	private IndexMinPQ<Double> pq;    // priority queue of vertices
	private EdgeWeightedDigraph G;

	DijkstraSP(EdgeWeightedDigraph G, OSMNode startNode, OSMNode endNode, Vehicle type, boolean fastestPath) {

		int start = startNode.getId();

		distTo = new double[G.V()];
		edgeTo = new Edge[G.V()];

		for (int v = 0; v < G.V(); v++) {
			distTo[v] = Double.POSITIVE_INFINITY;
		}

		distTo[start] = 0;
		this.G = G;
		validateVertex(start);


		pq = new IndexMinPQ<>(G.V());
		pq.insert(start, distTo[start]);

		// relax vertices in order of distance from start
		while (!pq.isEmpty()) {
			int v = pq.delMin();
			for (Edge e : G.adj(v)) {
				relax(e, v, type, fastestPath, endNode);
				// if relaxed edge endpoint is the destination, then return
				if (e.getOtherEnd(v) == endNode.getId()) return;
			}
		}

		assert check(G, start, type, fastestPath);
	}

	private void relax(Edge e, int vertexV, Vehicle type, boolean fastestPath, OSMNode endNode) {

		int v = vertexV;
		int w = e.getOtherEnd(v); //the intent is to get both ends of the edge so we use e.getOtherEnd to do so

		if (distTo[w] > distTo[v] + e.getWeight(type, fastestPath) && e.isForwardAllowed(type, v)) {
			distTo[w] = distTo[v] + e.getWeight(type, fastestPath);
			edgeTo[w] = e;

			OSMNode toNode = e.getThisEndNode(w);
			double heuristic = AStar.Heuristic(type, fastestPath, toNode.getLat(), toNode.getLon(), endNode.getLat(), endNode.getLon());

			if (pq.contains(w)) {
				pq.decreaseKey(w, distTo[w] + heuristic);
			} else {
				pq.insert(w, distTo[w] + heuristic);
			}
		}
	}

	private boolean hasPathTo(int v) {
		validateVertex(v);
		return distTo[v] < Double.POSITIVE_INFINITY;
	}

	Iterable<Edge> pathTo(int v) {
		validateVertex(v);
		if (!hasPathTo(v)) {
			return null;
		}
		Stack<Edge> path = new Stack<>();
		for (Edge e = edgeTo[v]; e != null; e = edgeTo[v]) {
			v = e.getOtherEnd(v);
			path.push(e);
		}
		return path;
	}

	/* //debugging method
	public Iterable<Iterable<Edge>> paths() {
		ResizingArray<Iterable<Edge>> paths = new ResizingArray<>();
		for (int i = 0; i < G.V(); i++) {
			if (hasPathTo(i)) {
				paths.add(pathTo(i));
			}
		}
		return paths;
	}*/


	// check optimality conditions:
	// (i) for all edges e:            distTo[e.to()] <= distTo[e.from()] + e.weight()
	// (ii) for all edge e on the SPT: distTo[e.to()] == distTo[e.from()] + e.weight()
	private boolean check(EdgeWeightedDigraph G, int s, Vehicle type, boolean fastestPath) {

		// check that edge weights are nonnegative
		for (Edge e : G.edges()) {
			if (e.getWeight(type, fastestPath) < 0) {
				System.err.println("negative edge weight detected");
				return false;
			}
		}

		// check that distTo[v] and edgeTo[v] are consistent
		if (distTo[s] != 0.0 || edgeTo[s] != null) {
			System.err.println("distTo[s] and edgeTo[s] inconsistent");
			return false;
		}
		for (int v = 0; v < G.V(); v++) {
			if (v == s) continue;
			if (edgeTo[v] == null && distTo[v] != Double.POSITIVE_INFINITY) {
				System.err.println("distTo[] and edgeTo[] inconsistent");
				return false;
			}
		}

		// check that all edges e = v->w satisfy distTo[w] <= distTo[v] + e.weight()
		for (int v = 0; v < G.V(); v++) {
			for (Edge e : G.adj(v)) {
				int w = e.getOtherEnd(v);
				if (distTo[v] + e.getWeight(type, fastestPath) < distTo[w] && e.isForwardAllowed(type, v)) {
					System.err.println("edge " + e + " not relaxed");
					return false;
				}
			}
		}

		// check that all edges e = v->w on SPT satisfy distTo[w] == distTo[v] + e.weight()
		for (int w = 0; w < G.V(); w++) {
			if (edgeTo[w] == null) continue;
			Edge e = edgeTo[w];
			int v = e.getOtherEnd(w);
			if (w != e.getOtherEnd(v)) return false;
			if (distTo[v] + e.getWeight(type, fastestPath) != distTo[w]) {
				System.err.println("edge " + e + " on shortest path not tight");
				return false;
			}
		}
		return true;
	}

	private void validateVertex(int v) {
		int V = distTo.length;
		if (v < 0 || v >= V) {
			throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V - 1));
		}
	}

	public class Stack<Item> implements Iterable<Item> {
		private Node<Item> first;     // top of stack
		private int n;                // size of the stack

		Stack() {
			first = null;
			n = 0;
		}

		public int size() {
			return n;
		}

		void push(Item item) {
			Node<Item> oldfirst = first;
			first = new Node<>();
			first.item = item;
			first.next = oldfirst;
			n++;
		}

		public String toString() {
			StringBuilder s = new StringBuilder();
			for (Item item : this) {
				s.append(item);
				s.append(' ');
			}
			return s.toString();
		}

		public Iterator<Item> iterator() {
			return new ListIterator<>(first);
		}

		// helper linked list class
		private class Node<Item> {
			private Item item;
			private Node<Item> next;
		}

		// an iterator, doesn't implement remove() since it's optional
		private class ListIterator<Item> implements Iterator<Item> {
			private Node<Item> current;

			ListIterator(Node<Item> first) {
				current = first;
			}

			public boolean hasNext() {
				return current != null;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			public Item next() {
				if (!hasNext()) throw new NoSuchElementException();
				Item item = current.item;
				current = current.next;
				return item;
			}
		}


	}
}