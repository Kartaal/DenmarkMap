package bfst19.Route_parsing;

import bfst19.LongIndex;
import edu.princeton.cs.algs4.Stack;

import java.util.HashMap;

public class DijkstraSP {
    private HashMap<Long,Double> distTo;          // distTo[v] = distance  of shortest s->v path
    private HashMap<Long,Edge> edgeTo;    // edgeTo[v] = last edge on shortest s->v path
    private IndexMinPQ<Double> pq;    // priority queue of vertices

    public DijkstraSP(EdgeWeightedGraph G, long s, Vehicle type, boolean fastestPath) {
        /*for (Edge e : G.edges()) {
            if (e.getWeight() < 0)
                throw new IllegalArgumentException("edge " + e + " has negative weight");
        }*/

        distTo = new HashMap<>();
        edgeTo = new HashMap<>();

        validateVertex(s);

        for(Long id:G.getIDs()){
            distTo.put(id,Double.POSITIVE_INFINITY);
        }
        distTo.put(s, 0.0);

        // relax vertices in order of distance from s
        pq = new IndexMinPQ<>((int)G.V());
        pq.insert(s,distTo.get(s));
        while (!pq.isEmpty()) {
            long v = pq.delMin();
            for (Edge e : G.adj(v))
                relax(e,type,fastestPath);
        }

        // check optimality conditions
        assert check(G,s,type,fastestPath);
    }

    // relax edge e and update pq if changed
    private void relax(Edge e, Vehicle type, boolean fastestPath) {
        long v = e.either(), w = e.other();
        if (distTo.get(w) > distTo.get(v) + e.getWeight(type,fastestPath)) {
            distTo.put(w,distTo.get(v) + e.getWeight(type,fastestPath));
            edgeTo.put(w, e);
            if (pq.contains(w)) pq.decreaseKey(w, distTo.get(w));
            else                pq.insert(w, distTo.get(w));
        }
    }

    public double distTo(long v) {
        validateVertex(v);
        return distTo.get(v);
    }

    public boolean hasPathTo(int v) {
        validateVertex(v);
        return distTo.get(v) < Double.POSITIVE_INFINITY;
    }

    public Iterable<Edge> pathTo(int v) {
        validateVertex(v);
        if (!hasPathTo(v)) return null;
        Stack<Edge> path = new Stack<Edge>();
        for (Edge e = edgeTo.get(v); e != null; e = edgeTo.get(e.either())) {
            path.push(e);
        }
        return path;
    }


    // check optimality conditions:
    // (i) for all edges e:            distTo[e.to()] <= distTo[e.from()] + e.weight()
    // (ii) for all edge e on the SPT: distTo[e.to()] == distTo[e.from()] + e.weight()
    private boolean check(EdgeWeightedGraph G, long s, Vehicle type, boolean fastestPath) {

        // check that edge weights are nonnegative
        for (Edge e : G.edges()) {
            if (e.getWeight(type,fastestPath) < 0) {
                System.err.println("negative edge weight detected");
                return false;
            }
        }

        // check that distTo[v] and edgeTo[v] are consistent
        if (distTo.get(s) != 0.0 || edgeTo.get(s) != null) {
            System.err.println("distTo[s] and edgeTo[s] inconsistent");
            return false;
        }
        for (int v = 0; v < G.V(); v++) {
            if (v == s) continue;
            if (edgeTo.get(v) == null && distTo.get(v) != Double.POSITIVE_INFINITY) {
                System.err.println("distTo[] and edgeTo[] inconsistent");
                return false;
            }
        }

        // check that all edges e = v->w satisfy distTo[w] <= distTo[v] + e.weight()
        for (int v = 0; v < G.V(); v++) {
            for (Edge e : G.adj(v)) {
                long w = e.other();
                if (distTo.get(v) + e.getWeight(type,fastestPath) < distTo.get(w)) {
                    System.err.println("edge " + e + " not relaxed");
                    return false;
                }
            }
        }

        // check that all edges e = v->w on SPT satisfy distTo[w] == distTo[v] + e.weight()
        for (int w = 0; w < G.V(); w++) {
            if (edgeTo.get(w) == null) continue;
            Edge e = edgeTo.get(w);
            long v = e.either();
            if (w != e.other()) return false;
            if (distTo.get(v) + e.getWeight(type,fastestPath) != distTo.get(w)) {
                System.err.println("edge " + e + " on shortest path not tight");
                return false;
            }
        }
        return true;
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertex(long v) {

        if (distTo.get(v)==null)
            throw new IllegalArgumentException("vertex " + v + " is not one of the recognized ID's");
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

}
