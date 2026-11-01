package graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Generates reproducible undirected and directed graphs using a fixed random seed
 * for experimental benchmarking and verification.
 */
public class GraphGenerator {
    private static final double MIN_WEIGHT = 1.0;
    private static final double MAX_WEIGHT = 100.0;

    /**
     * Generates a connected undirected weighted graph with V vertices and E edges.
     * First builds a random spanning tree (V - 1 edges) using a randomized Prufer-like
     * attachment to guarantee connectivity, then adds (E - (V - 1)) unique random edges.
     */
    public static Graph generateConnectedUndirectedGraph(int vertices, int edges, long seed) {
        if (vertices <= 0) {
            throw new IllegalArgumentException("Vertices must be positive.");
        }
        int maxPossibleEdges = vertices * (vertices - 1) / 2;
        if (edges < vertices - 1 || edges > maxPossibleEdges) {
            throw new IllegalArgumentException(
                    String.format("Invalid edge count %d for connected undirected graph with %d vertices.", edges, vertices));
        }

        Random rng = new Random(seed);
        Graph graph = new Graph(vertices, false);
        Set<Long> existingEdges = new HashSet<>();

        // Step 1: Ensure connectivity by forming a random spanning tree (V - 1 edges)
        int[] perm = new int[vertices];
        for (int i = 0; i < vertices; i++) {
            perm[i] = i;
        }
        for (int i = vertices - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = perm[i];
            perm[i] = perm[j];
            perm[j] = tmp;
        }

        for (int i = 1; i < vertices; i++) {
            int u = perm[i];
            int v = perm[rng.nextInt(i)];
            double weight = roundWeight(MIN_WEIGHT + rng.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT));
            graph.addEdge(u, v, weight);
            existingEdges.add(encodeUndirectedPair(u, v));
        }

        // Step 2: Add remaining unique edges randomly until reaching E edges
        while (graph.getEdgeCount() < edges) {
            int u = rng.nextInt(vertices);
            int v = rng.nextInt(vertices);
            if (u == v) {
                continue;
            }
            long key = encodeUndirectedPair(u, v);
            if (!existingEdges.contains(key)) {
                double weight = roundWeight(MIN_WEIGHT + rng.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT));
                graph.addEdge(u, v, weight);
                existingEdges.add(key);
            }
        }

        return graph;
    }

    /**
     * Generates a Directed Acyclic Graph (DAG) with V vertices and E edges.
     * Edges are strictly oriented from lower topological rank to higher rank
     * under a seeded random vertex permutation, guaranteeing zero cycles.
     */
    public static Graph generateDAG(int vertices, int edges, long seed) {
        int maxPossibleEdges = vertices * (vertices - 1) / 2;
        if (edges < 0 || edges > maxPossibleEdges) {
            throw new IllegalArgumentException("Invalid edge count for DAG.");
        }

        Random rng = new Random(seed);
        Graph graph = new Graph(vertices, true);
        Set<Long> existingEdges = new HashSet<>();

        int[] rankOrder = new int[vertices];
        for (int i = 0; i < vertices; i++) {
            rankOrder[i] = i;
        }
        for (int i = vertices - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = rankOrder[i];
            rankOrder[i] = rankOrder[j];
            rankOrder[j] = tmp;
        }

        // Guarantee a directed backbone path where possible so the DAG isn't trivially sparse
        int backboneEdges = Math.min(edges, vertices - 1);
        for (int i = 0; i < backboneEdges; i++) {
            int u = rankOrder[i];
            int v = rankOrder[i + 1];
            double weight = roundWeight(MIN_WEIGHT + rng.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT));
            graph.addEdge(u, v, weight);
            existingEdges.add(encodeDirectedPair(u, v));
        }

        while (graph.getEdgeCount() < edges) {
            int idxA = rng.nextInt(vertices);
            int idxB = rng.nextInt(vertices);
            if (idxA == idxB) {
                continue;
            }
            int fromIdx = Math.min(idxA, idxB);
            int toIdx = Math.max(idxA, idxB);
            int u = rankOrder[fromIdx];
            int v = rankOrder[toIdx];
            long key = encodeDirectedPair(u, v);
            if (!existingEdges.contains(key)) {
                double weight = roundWeight(MIN_WEIGHT + rng.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT));
                graph.addEdge(u, v, weight);
                existingEdges.add(key);
            }
        }

        return graph;
    }

    /**
     * Generates a general directed weighted graph with V vertices and E edges that contains
     * non-trivial Strongly Connected Components (cycles) and non-negative weights.
     * Used for SCC analysis, cyclic topological sort detection, and Dijkstra benchmarks.
     */
    public static Graph generateDirectedCyclicGraph(int vertices, int edges, long seed) {
        int maxPossibleEdges = vertices * (vertices - 1);
        if (edges < 0 || edges > maxPossibleEdges) {
            throw new IllegalArgumentException("Invalid edge count for directed graph.");
        }

        Random rng = new Random(seed);
        Graph graph = new Graph(vertices, true);
        Set<Long> existingEdges = new HashSet<>();

        // Plant at least one explicit cycle among the first min(vertices, 4) districts
        // so that cyclic detection is deterministic even for small edge counts
        int cycleLen = Math.min(vertices, Math.min(4, Math.max(2, edges)));
        if (cycleLen >= 2 && edges >= cycleLen) {
            for (int i = 0; i < cycleLen; i++) {
                int u = i;
                int v = (i + 1) % cycleLen;
                double weight = roundWeight(MIN_WEIGHT + rng.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT));
                graph.addEdge(u, v, weight);
                existingEdges.add(encodeDirectedPair(u, v));
            }
        }

        // Ensure high reachability from source vertex 0 by adding a spanning tree out of 0
        for (int v = 1; v < vertices && graph.getEdgeCount() < edges; v++) {
            int u = rng.nextInt(v);
            long key = encodeDirectedPair(u, v);
            if (!existingEdges.contains(key)) {
                double weight = roundWeight(MIN_WEIGHT + rng.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT));
                graph.addEdge(u, v, weight);
                existingEdges.add(key);
            }
        }

        // Fill remaining edges uniformly across all ordered pairs (u, v)
        while (graph.getEdgeCount() < edges) {
            int u = rng.nextInt(vertices);
            int v = rng.nextInt(vertices);
            if (u == v) {
                continue;
            }
            long key = encodeDirectedPair(u, v);
            if (!existingEdges.contains(key)) {
                double weight = roundWeight(MIN_WEIGHT + rng.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT));
                graph.addEdge(u, v, weight);
                existingEdges.add(key);
            }
        }

        return graph;
    }

    private static long encodeUndirectedPair(int u, int v) {
        int a = Math.min(u, v);
        int b = Math.max(u, v);
        return (((long) a) << 32) | (b & 0xFFFFFFFFL);
    }

    private static long encodeDirectedPair(int u, int v) {
        return (((long) u) << 32) | (v & 0xFFFFFFFFL);
    }

    private static double roundWeight(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
