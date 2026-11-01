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
 * Implements Tarjan's single-pass DFS algorithm for finding all Strongly Connected
 * Components (SCCs) in a directed graph in O(V + E) time.
 */
public class TarjanSCC {

    private int index;
    private int[] indices;
    private int[] lowlink;
    private int[] componentId;
    private boolean[] onStack;
    private Deque<Integer> stack;
    private List<List<Integer>> components;

    private long edgesExamined;
    private long stackOperations;
    private long comparisons;

    public static SCCResult findSCCs(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Tarjan's SCC algorithm requires a directed graph.");
        }
        return new TarjanSCC().compute(graph);
    }

    private SCCResult compute(Graph graph) {
        int n = graph.getVertices();
        long startTime = System.nanoTime();

        index = 0;
        indices = new int[n];
        Arrays.fill(indices, -1);
        lowlink = new int[n];
        componentId = new int[n];
        Arrays.fill(componentId, -1);
        onStack = new boolean[n];
        stack = new ArrayDeque<>(n);
        components = new ArrayList<>();

        edgesExamined = 0;
        stackOperations = 0;
        comparisons = 0;

        for (int u = 0; u < n; u++) {
            comparisons++;
            if (indices[u] == -1) {
                strongConnect(graph, u);
            }
        }

        long elapsed = System.nanoTime() - startTime;
        return new SCCResult(
                "TarjanSCC",
                components,
                componentId,
                elapsed,
                edgesExamined,
                stackOperations,
                comparisons);
    }

    private void strongConnect(Graph graph, int u) {
        indices[u] = index;
        lowlink[u] = index;
        index++;
        stack.push(u);
        onStack[u] = true;
        stackOperations++;

        for (Edge edge : graph.getNeighbors(u)) {
            edgesExamined++;
            int v = edge.getDestination();
            comparisons++;
            if (indices[v] == -1) {
                strongConnect(graph, v);
                comparisons++;
                if (lowlink[v] < lowlink[u]) {
                    lowlink[u] = lowlink[v];
                }
            } else if (onStack[v]) {
                comparisons++;
                if (indices[v] < lowlink[u]) {
                    lowlink[u] = indices[v];
                }
            }
        }

        comparisons++;
        if (lowlink[u] == indices[u]) {
            List<Integer> component = new ArrayList<>();
            int compIdx = components.size();
            while (true) {
                int w = stack.pop();
                stackOperations++;
                onStack[w] = false;
                componentId[w] = compIdx;
                component.add(w);
                if (w == u) {
                    break;
                }
            }
            Collections.sort(component);
            components.add(Collections.unmodifiableList(component));
        }
    }
}
