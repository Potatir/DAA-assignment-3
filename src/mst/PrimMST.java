package mst;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Implementation of Prim's Minimum Spanning Tree algorithm using a binary heap PriorityQueue.
 */
public class PrimMST {

    /**
     * Computes the Minimum Spanning Tree starting from vertex 0.
     * Detects whether all vertices are reachable (connected graph) and records
     * edge examinations, priority queue comparisons, and heap operations.
     */
    public static MSTResult computeMST(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Prim's algorithm requires an undirected graph.");
        }

        int n = graph.getVertices();
        long startTime = System.nanoTime();

        if (n == 0) {
            long elapsed = System.nanoTime() - startTime;
            return new MSTResult("Prim", new ArrayList<>(), 0.0, true, elapsed, 0, 0, 0);
        }
        if (n == 1) {
            long elapsed = System.nanoTime() - startTime;
            return new MSTResult("Prim", new ArrayList<>(), 0.0, true, elapsed, 0, 0, 0);
        }

        boolean[] visited = new boolean[n];
        double[] minEdgeWeight = new double[n];
        for (int i = 0; i < n; i++) {
            minEdgeWeight[i] = Double.POSITIVE_INFINITY;
        }

        long[] pqComparisons = new long[]{0};
        PriorityQueue<Edge> pq = new PriorityQueue<>((a, b) -> {
            pqComparisons[0]++;
            return a.compareTo(b);
        });

        List<Edge> mstEdges = new ArrayList<>(n - 1);
        double totalWeight = 0.0;
        long edgesExamined = 0;
        long weightComparisons = 0;
        long heapOps = 0;

        // Start from vertex 0
        visited[0] = true;
        minEdgeWeight[0] = 0.0;
        int visitedCount = 1;

        for (Edge edge : graph.getNeighbors(0)) {
            edgesExamined++;
            int next = edge.getDestination();
            weightComparisons++;
            if (!visited[next] && edge.getWeight() < minEdgeWeight[next]) {
                minEdgeWeight[next] = edge.getWeight();
                pq.offer(edge);
                heapOps++;
            }
        }

        while (!pq.isEmpty() && mstEdges.size() < n - 1) {
            Edge current = pq.poll();
            heapOps++;
            int target = current.getDestination();

            if (visited[target]) {
                continue;
            }

            visited[target] = true;
            visitedCount++;
            mstEdges.add(current);
            totalWeight += current.getWeight();

            for (Edge neighborEdge : graph.getNeighbors(target)) {
                edgesExamined++;
                int next = neighborEdge.getDestination();
                weightComparisons++;
                if (!visited[next] && neighborEdge.getWeight() < minEdgeWeight[next]) {
                    minEdgeWeight[next] = neighborEdge.getWeight();
                    pq.offer(neighborEdge);
                    heapOps++;
                }
            }
        }

        boolean isConnected = (visitedCount == n);
        long elapsed = System.nanoTime() - startTime;
        long totalComparisons = pqComparisons[0] + weightComparisons;

        return new MSTResult(
                "Prim",
                mstEdges,
                totalWeight,
                isConnected,
                elapsed,
                edgesExamined,
                totalComparisons,
                heapOps);
    }
}
