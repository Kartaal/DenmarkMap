package bfst19.Route_parsing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class EdgeWeightedGraph implements Serializable {
    //don't know what this is
    private static final String NEWLINE = System.getProperty("line.separator");

    private int V;
    private int E;
    private HashMap<Long,Integer> idToIndex;
    private ResizingArray indexToId;
    private ArrayList<ArrayList<Edge>> adj;

    public EdgeWeightedGraph(){
        this.V = 0;
        this.E = 0;
        idToIndex = new HashMap<>();
        indexToId = new ResizingArray();
        adj = new ArrayList<>();
    }

    public EdgeWeightedGraph(ArrayList<Long> V) {
        if (V.size() == 0) throw new IllegalArgumentException("Number of vertices must be nonnegative");
        this.V = V.size();
        this.E = 0;
        adj = new ArrayList<>();
    }

    public int V() {
        return V;
    }

    public int E() { return E; }

    public long getIdFromIndex(int index){
        return indexToId.get(index);
    }

    public int getIndexFromId(long id){
        return idToIndex.get(id);
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertex(int v) {
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

    private boolean isVertex(long id){
        if(idToIndex.get(id)==null){
            return false;
        }
        return true;
    }


    public void addVertex(long id){
        if(!isVertex(id)) {
            idToIndex.put(id, V);
            indexToId.add(id);
            adj.add(V, new ArrayList<>());
            V++;
        }
    }

    public void addEdge(Edge e) {
        int v = getIndexFromId(e.either());
        int w = getIndexFromId(e.other());
        validateVertex(v);
        validateVertex(w);
        adj.get(v).add(e);
        adj.get(w).add(e);
        E++;
    }

    public Iterable<Edge> adj(int v,Vehicle type) {
        validateVertex(v);
        ArrayList<Edge> adjacent = adj.get(v);
        ArrayList<Edge> temp = new ArrayList<>();
        long idOfV = indexToId.get(v);
        for(Edge edge : adjacent){
            if(edge.isForwardAllowed(type,idOfV)){
                temp.add(edge);
            }
        }
        adjacent = temp;
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
            for (Edge e : adj(v,Vehicle.BIKE)) {
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