package bfst19.Route_parsing;

import bfst19.Line.OSMNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class EdgeWeightedGraph implements Serializable {
    //don't know what this is
    private static final String NEWLINE = System.getProperty("line.separator");

    private int V;
    private int E;
    private ResizingArray<ResizingArray<Edge>> adj;

    public EdgeWeightedGraph(){
        this.V = 0;
        this.E = 0;
        adj = new ResizingArray<>();
    }

    public EdgeWeightedGraph(ArrayList<Long> V) {
        if (V.size() == 0) throw new IllegalArgumentException("Number of vertices must be nonnegative");
        this.V = V.size();
        this.E = 0;
        adj = new ResizingArray<>();
    }

    public int V() {
        return V;
    }

    public int E() { return E; }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertex(int v) {
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

    private boolean isVertex(int id){
        if(0<id&&id<V){
            return true;
        }
        return false;
    }

    public void addVertex(OSMNode node){
        if(!isVertex(node.getId())) {
            node.setId(V);
            adj.add(new ResizingArray<>());
            V++;
        }
    }

    public void addEdge(Edge e) {
        int v = e.either();
        int w = e.other();
        validateVertex(v);
        validateVertex(w);
        adj.get(v).add(e);
        adj.get(w).add(e);
        E++;
    }

    public Iterable<Edge> adj(int v) {
        validateVertex(v);
        ResizingArray adjacent = adj.get(v);
        return adjacent;
    }

    public int degree(int v) {
        validateVertex(v);
        return adj.get(v).size();
    }

    public Iterable<Edge> edges() {
        ArrayList list = new ArrayList();
        for (int v = 0; v < V; v++) {
            int selfLoops = 0;
            for (Edge e : adj(v)) {
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
            ResizingArray current = adj.get(v);
            for (int i = 0 ; i<current.size() ; i++) {
                s.append(current.get(i) + "  ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }

    public void trim() {
        for( int i = 0 ; i < adj.size() ; i++){
            adj.get(i).trim();
        }
        adj.trim();
    }

    /*public static void main(String[] args) {
        In in = new In(args[0]);
        EdgeWeightedGraph G = new EdgeWeightedGraph(in);
        StdOut.println(G);
    }*/

}

