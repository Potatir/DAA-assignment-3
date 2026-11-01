package mst;

import dsu.DisjointSetUnion;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of Kruskal's Minimum Spanning Tree algorithm using Disjoint Set Union (DSU).
 */
public class KruskalMST {

    /**
     * Computes the Minimum Spanning Tree of an undirected weighted graph.
     * Detects whether the graph is connected and counts sorting comparisons,
     * edge examinations, and DSU operations.
     */
    public static MSTResult computeMST(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Kruskal's algorithm requires an undirected graph.");
        }

        int n = graph.getVertices();
        long startTime = System.nanoTime();

        if (n == 0) {
            long elapsed = System.nanoTime() - startTime;
            return new MSTResult("Kruskal", new ArrayList<>(), 0.0, true, elapsed, 0, 0, 0);
        }
        if (n == 1) {
            long elapsed = System.nanoTime() - startTime;
            return new MSTResult("Kruskal", new ArrayList<>(), 0.0, true, elapsed, 0, 0, 0);
        }

        List<Edge> sortedEdges = new ArrayList<>(graph.getEdges());
        long[] sortComparisons = new long[]{0};

        sortedEdges.sort((a, b) -> {
            sortComparisons[0]++;
            return a.compareTo(b);
        });

        DisjointSetUnion dsu = new DisjointSetUnion(n);
        List<Edge> mstEdges = new ArrayList<>(Math.max(0, n - 1));
        double totalWeight = 0.0;
        long edgesExamined = 0;

        for (Edge edge : sortedEdges) {
            edgesExamined++;
            int u = edge.getSource();
            int v = edge.getDestination();
            if (u == v) {
                continue;
            }
            if (dsu.union(u, v)) {
                mstEdges.add(edge);
                totalWeight += edge.getWeight();
                if (mstEdges.size() == n - 1) {
                    break;
                }
            }
        }

        boolean isConnected = (dsu.getComponentCount() == 1);
        long elapsed = System.nanoTime() - startTime;
        long comparisons = sortComparisons[0] + dsu.getRankComparisons();
        long dsuOps = dsu.getFindCalls() + dsu.getUnionCalls() + dsu.getParentAccesses();

        return new MSTResult(
                "Kruskal",
                mstEdges,
                totalWeight,
                isConnected,
                elapsed,
                edgesExamined,
                comparisons,
                dsuOps);
    }
}
