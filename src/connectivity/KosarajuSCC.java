package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Implements Kosaraju's two-pass DFS algorithm for finding Strongly Connected
 * Components (SCCs) in a directed graph in O(V + E) time.
 */
public class KosarajuSCC {

    public static SCCResult findSCCs(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Kosaraju's SCC algorithm requires a directed graph.");
        }

        int n = graph.getVertices();
        long startTime = System.nanoTime();

        boolean[] visited = new boolean[n];
        Deque<Integer> finishStack = new ArrayDeque<>(n);
        long[] metrics = new long[]{0, 0, 0}; // [0]=edgesExamined, [1]=stackOps, [2]=comparisons

        // Pass 1: Record finish order on original graph
        for (int u = 0; u < n; u++) {
            metrics[2]++;
            if (!visited[u]) {
                dfsPass1(graph, u, visited, finishStack, metrics);
            }
        }

        // Build transposed graph
        List<List<Integer>> transpose = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            transpose.add(new ArrayList<>());
        }
        for (int u = 0; u < n; u++) {
            for (Edge edge : graph.getNeighbors(u)) {
                metrics[0]++;
                transpose.get(edge.getDestination()).add(u);
            }
        }

        // Pass 2: Traverse transposed graph in reverse finish order
        Arrays.fill(visited, false);
        List<List<Integer>> components = new ArrayList<>();
        int[] componentId = new int[n];
        Arrays.fill(componentId, -1);

        while (!finishStack.isEmpty()) {
            int u = finishStack.pop();
            metrics[1]++;
            metrics[2]++;
            if (!visited[u]) {
                List<Integer> component = new ArrayList<>();
                int compIdx = components.size();
                dfsPass2(transpose, u, visited, component, componentId, compIdx, metrics);
                Collections.sort(component);
                components.add(Collections.unmodifiableList(component));
            }
        }

        long elapsed = System.nanoTime() - startTime;
        return new SCCResult(
                "KosarajuSCC",
                components,
                componentId,
                elapsed,
                metrics[0],
                metrics[1],
                metrics[2]);
    }

    private static void dfsPass1(
            Graph graph,
            int u,
            boolean[] visited,
            Deque<Integer> finishStack,
            long[] metrics) {
        visited[u] = true;
        for (Edge edge : graph.getNeighbors(u)) {
            metrics[0]++;
            int v = edge.getDestination();
            metrics[2]++;
            if (!visited[v]) {
                dfsPass1(graph, v, visited, finishStack, metrics);
            }
        }
        finishStack.push(u);
        metrics[1]++;
    }

    private static void dfsPass2(
            List<List<Integer>> transpose,
            int u,
            boolean[] visited,
            List<Integer> component,
            int[] componentId,
            int compIdx,
            long[] metrics) {
        visited[u] = true;
        componentId[u] = compIdx;
        component.add(u);
        for (int v : transpose.get(u)) {
            metrics[0]++;
            metrics[2]++;
            if (!visited[v]) {
                dfsPass2(transpose, v, visited, component, componentId, compIdx, metrics);
            }
        }
    }
}
