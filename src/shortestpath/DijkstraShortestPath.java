package shortestpath;

import graph.Edge;
import graph.Graph;
import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Implementation of Dijkstra's single-source shortest-path algorithm using a binary heap PriorityQueue.
 * Enforces non-negative edge weights and tracks operation counts across graph sizes.
 */
public class DijkstraShortestPath {

    private static final class NodeDistance implements Comparable<NodeDistance> {
        final int vertex;
        final double distance;

        NodeDistance(int vertex, double distance) {
            this.vertex = vertex;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeDistance other) {
            int cmp = Double.compare(this.distance, other.distance);
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(this.vertex, other.vertex);
        }
    }

    /**
     * Computes shortest paths from the specified source district to all reachable districts.
     *
     * @throws IllegalArgumentException if any edge has a negative weight or source is invalid.
     */
    public static ShortestPathResult computeShortestPaths(Graph graph, int source) {
        int n = graph.getVertices();
        if (source < 0 || source >= n) {
            throw new IllegalArgumentException("Invalid source vertex: " + source);
        }

        long startTime = System.nanoTime();

        double[] dist = new double[n];
        int[] prev = new int[n];
        boolean[] settled = new boolean[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);

        long[] pqComparisons = new long[]{0};
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>((a, b) -> {
            pqComparisons[0]++;
            return a.compareTo(b);
        });

        long edgesExamined = 0;
        long relaxComparisons = 0;
        long pqOps = 0;
        long relaxations = 0;

        dist[source] = 0.0;
        pq.offer(new NodeDistance(source, 0.0));
        pqOps++;

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            pqOps++;
            int u = current.vertex;

            relaxComparisons++;
            if (settled[u]) {
                continue;
            }
            settled[u] = true;

            for (Edge edge : graph.getNeighbors(u)) {
                edgesExamined++;
                double weight = edge.getWeight();
                if (weight < 0.0) {
                    throw new IllegalArgumentException(
                            "Dijkstra's algorithm does not support negative edge weights: " + edge);
                }

                int v = edge.getDestination();
                relaxComparisons++;
                if (settled[v]) {
                    continue;
                }

                double candidateDist = dist[u] + weight;
                relaxComparisons++;
                if (candidateDist < dist[v]) {
                    dist[v] = candidateDist;
                    prev[v] = u;
                    relaxations++;
                    pq.offer(new NodeDistance(v, candidateDist));
                    pqOps++;
                }
            }
        }

        long elapsed = System.nanoTime() - startTime;
        long totalComparisons = pqComparisons[0] + relaxComparisons;

        return new ShortestPathResult(
                source,
                dist,
                prev,
                elapsed,
                edgesExamined,
                totalComparisons,
                pqOps,
                relaxations);
    }
}
