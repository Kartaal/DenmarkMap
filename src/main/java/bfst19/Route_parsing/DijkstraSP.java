package bfst19.Route_parsing;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DijkstraSP {
    private double[] distTo;          // distTo[v] = distance  of shortest s->v path
    private Edge[] edgeTo;    // edgeTo[v] = last edge on shortest s->v path
    private IndexMinPQ<Double> pq;    // priority queue of vertices    // priority queue of vertices
    private EdgeWeightedGraph G;

    public DijkstraSP(EdgeWeightedGraph G, long s, Vehicle type, boolean fastestPath) {
        /*for (Edge e : G.edges()) {
            if (e.getWeight() < 0)
                throw new IllegalArgumentException("edge " + e + " has negative weight");
        }*/

        distTo = new double[G.V()];
        edgeTo = new Edge[G.V()];
        for (int v = 0; v < G.V(); v++)
            distTo[v] = Double.POSITIVE_INFINITY;
        int indexOfS = G.getIndexFromId(s);
        distTo[indexOfS] = 0.0;
        this.G = G;
        validateVertex(indexOfS);
        // relax vertices in order of distance from s
        pq = new IndexMinPQ<>(G.V());
        pq.insert(indexOfS,distTo[indexOfS]);
        while (!pq.isEmpty()) {
            int v = pq.delMin();
            for (Edge e : G.adj(v, type))
                relax(e, v, type, fastestPath);
        }

        // check optimality conditions
        assert check(G,indexOfS,type,fastestPath);
    }

    // relax edge e and update pq if changed
    private void relax(Edge e,int vertexV ,Vehicle type, boolean fastestPath) {
        //the intent is to get both ends of the edge so we use e.getOtherEnd to do so,
        // tho we do first need to get the v's corresponding id, then get the other end on that id,
        // and then convert the id we get back to it's corresponding index
        int v = vertexV, w = G.getIndexFromId(e.getOtherEnd(G.getIdFromIndex(v)));
        double distToW = distTo[w];
        double distToV = distTo[v];
        double weight = e.getWeight(type,fastestPath);
        if (distToW > distToV + weight) {
            distTo[w] = distToV + weight;
            edgeTo[w] = e;
            if (pq.contains(w)) pq.decreaseKey(w, distToW);
            else                pq.insert(w, distToW);
        }
    }

    public double distTo(int v) {
        validateVertex(v);
        return distTo[v];
    }

    public boolean hasPathTo(int v) {
        validateVertex(v);
        return distTo[v] < Double.POSITIVE_INFINITY;
    }

    //maybe doing this is not optimal
    public Iterable<Edge> pathTo(int v) {
        validateVertex(v);
        if (!hasPathTo(v)) return null;
        int indexOfV = v;
        Stack<Edge> path = new Stack<>();
        for (Edge e = edgeTo[v]; e != null; e = edgeTo[indexOfV]) {
            indexOfV = G.getIndexFromId(e.getOtherEnd(G.getIdFromIndex(indexOfV)));
            path.push(e);
        }
        return path;
    }


    // check optimality conditions:
    // (i) for all edges e:            distTo[e.to()] <= distTo[e.from()] + e.weight()
    // (ii) for all edge e on the SPT: distTo[e.to()] == distTo[e.from()] + e.weight()
    private boolean check(EdgeWeightedGraph G, int s, Vehicle type, boolean fastestPath) {

        // check that edge weights are nonnegative
        for (Edge e : G.edges()) {
            if (e.getWeight(type,fastestPath) < 0) {
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
            for (Edge e : G.adj(v,type)) {
                int w = G.getIndexFromId(e.getOtherEnd(G.getIdFromIndex(v)));
                if (distTo[v] + e.getWeight(type,fastestPath) < distTo[w]) {
                    System.err.println("edge " + e + " not relaxed");
                    return false;
                }
            }
        }

        // check that all edges e = v->w on SPT satisfy distTo[w] == distTo[v] + e.weight()
        for (int w = 0; w < G.V(); w++) {
            if (edgeTo[w] == null) continue;
            Edge e = edgeTo[w];
            int v = G.getIndexFromId(e.getOtherEnd(G.getIdFromIndex(w)));
            if (w != e.other()) return false;
            if (distTo[v] + e.getWeight(type,fastestPath) != distTo[w]) {
                System.err.println("edge " + e + " on shortest path not tight");
                return false;
            }
        }
        return true;
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertex(int v) {
        int V = distTo.length;
        if (v < 0 || v >= V) {
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V - 1));
        }
    }

    /*public static void main(String[] args) {
        In in = new In(args[0]);
        EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);
        int s = Integer.parseInt(args[1]);

        // compute shortest paths
        DijkstraSP sp = new DijkstraSP(G, s);


        // print shortest path
        for (int t = 0; t < G.V(); t++) {
            if (sp.hasPathTo(t)) {
                StdOut.printf("%d to %d (%.2f)  ", s, t, sp.distTo(t));
                for (DirectedEdge e : sp.pathTo(t)) {
                    StdOut.print(e + "   ");
                }
                StdOut.println();
            }
            else {
                StdOut.printf("%d to %d         no path\n", s, t);
            }
        }
    }*/
    public class Stack<Item> implements Iterable<Item> {
        private Node<Item> first;     // top of stack
        private int n;                // size of the stack

        // helper linked list class
        private  class Node<Item> {
            private Item item;
            private Node<Item> next;
        }

        public Stack() {
            first = null;
            n = 0;
        }

        public boolean isEmpty() {
            return first == null;
        }

        public int size() {
            return n;
        }

        public void push(Item item) {
            Node<Item> oldfirst = first;
            first = new Node<Item>();
            first.item = item;
            first.next = oldfirst;
            n++;
        }

        public Item pop() {
            if (isEmpty()) throw new NoSuchElementException("Stack underflow");
            Item item = first.item;        // save item to return
            first = first.next;            // delete first node
            n--;
            return item;                   // return the saved item
        }

        public Item peek() {
            if (isEmpty()) throw new NoSuchElementException("Stack underflow");
            return first.item;
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
            return new ListIterator<Item>(first);
        }

        // an iterator, doesn't implement remove() since it's optional
        private class ListIterator<Item> implements Iterator<Item> {
            private Node<Item> current;

            public ListIterator(Node<Item> first) {
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
