package bfst19.Route_parsing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class EdgeWeightedGraph implements Serializable {
    private static final String NEWLINE = System.getProperty("line.separator");

    private final long V;
    private int E;
    private HashMap<Long,ArrayList<Edge>> adj;

    public EdgeWeightedGraph(long V) {
        if (V < 0) throw new IllegalArgumentException("Number of vertices must be nonnegative");
        this.V = V;
        this.E = 0;
        adj = new HashMap<>();
        for (int v = 0; v < V; v++) {
            adj.put(Long.valueOf(v), new ArrayList<>());
        }
    }

    public long V() {
        return V;
    }

    public int E() {
        return E;
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertex(long v) {
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

    public void addEdge(Edge e) {
        long v = e.either();
        long w = e.other();
        validateVertex(v);
        validateVertex(w);
        adj.get(v).add(e);
        adj.get(w).add(e);
        E++;
    }

    public Iterable<Edge> adj(long v) {
        validateVertex(v);
        return adj.get(v);
    }

    public int degree(long v) {
        validateVertex(v);
        return adj.get(v).size();
    }

    public Set<Long> getIDs(){
        return adj.keySet();
    }

    public Iterable<Edge> edges() {
        ArrayList list = new ArrayList();
        for (int v = 0; v < V; v++) {
            int selfLoops = 0;
            for (Edge e : adj.get(v)) {
                if (e.other() > v) {
                    list.add(e);
                }
                // add only one copy of each self loop (self loops will be consecutive)
                else if (e.other() == v) {
                    if (selfLoops % 2 == 0) list.add(e);
                    selfLoops++;
                }
            }
        }
        return list;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(V + " " + E + NEWLINE);
        for (int v = 0; v < V; v++) {
            s.append(v + ": ");
            for (Edge e : adj.get(v)) {
                s.append(e + "  ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }

    /*public static void main(String[] args) {
        In in = new In(args[0]);
        EdgeWeightedGraph G = new EdgeWeightedGraph(in);
        StdOut.println(G);
    }*/

}

