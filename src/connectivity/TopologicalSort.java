package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Implements Topological Ordering for Directed Acyclic Graphs (DAGs) with cycle detection.
 * Provides both Kahn's In-Degree BFS algorithm and 3-color DFS post-order reversal.
 */
public class TopologicalSort {

    /**
     * Computes a topological ordering using Kahn's algorithm (in-degree queue).
     * Automatically detects directed cycles when fewer than V vertices can be scheduled.
     */
    public static TopologicalSortResult computeKahn(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sort requires a directed graph.");
        }

        int n = graph.getVertices();
        long startTime = System.nanoTime();

        int[] inDegree = new int[n];
        long edgesExamined = 0;
        long queueOps = 0;

        for (int u = 0; u < n; u++) {
            for (Edge edge : graph.getNeighbors(u)) {
                inDegree[edge.getDestination()]++;
                edgesExamined++;
            }
        }

        Deque<Integer> queue = new ArrayDeque<>();
        for (int u = 0; u < n; u++) {
            if (inDegree[u] == 0) {
                queue.addLast(u);
                queueOps++;
            }
        }

        List<Integer> order = new ArrayList<>(n);
        while (!queue.isEmpty()) {
            int u = queue.removeFirst();
            queueOps++;
            order.add(u);

            for (Edge edge : graph.getNeighbors(u)) {
                edgesExamined++;
                int v = edge.getDestination();
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    queue.addLast(v);
                    queueOps++;
                }
            }
        }

        boolean isDAG = (order.size() == n);
        if (!isDAG) {
            order.clear();
        }

        long elapsed = System.nanoTime() - startTime;
        return new TopologicalSortResult(isDAG, order, elapsed, edgesExamined, queueOps);
    }

    /**
     * Computes a topological ordering using 3-state Depth-First Search (DFS)
     * with explicit back-edge cycle detection.
     */
    public static TopologicalSortResult computeDFS(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sort requires a directed graph.");
        }

        int n = graph.getVertices();
        long startTime = System.nanoTime();

        // 0 = unvisited, 1 = visiting (on recursion stack), 2 = visited
        int[] state = new int[n];
        List<Integer> postOrder = new ArrayList<>(n);
        long[] counters = new long[]{0, 0}; // [0]=edgesExamined, [1]=stackOps
        boolean[] cycleDetected = new boolean[]{false};

        for (int u = 0; u < n; u++) {
            if (state[u] == 0) {
                dfsVisit(graph, u, state, postOrder, counters, cycleDetected);
                if (cycleDetected[0]) {
                    break;
                }
            }
        }

        long elapsed = System.nanoTime() - startTime;
        if (cycleDetected[0]) {
            return new TopologicalSortResult(false, Collections.emptyList(), elapsed, counters[0], counters[1]);
        }

        Collections.reverse(postOrder);
        return new TopologicalSortResult(true, postOrder, elapsed, counters[0], counters[1]);
    }

    private static void dfsVisit(
            Graph graph,
            int u,
            int[] state,
            List<Integer> postOrder,
            long[] counters,
            boolean[] cycleDetected) {
        state[u] = 1;
        counters[1]++;

        for (Edge edge : graph.getNeighbors(u)) {
            counters[0]++;
            int v = edge.getDestination();
            if (state[v] == 1) {
                cycleDetected[0] = true;
                return;
            } else if (state[v] == 0) {
                dfsVisit(graph, v, state, postOrder, counters, cycleDetected);
                if (cycleDetected[0]) {
                    return;
                }
            }
        }

        state[u] = 2;
        postOrder.add(u);
        counters[1]++;
    }

    /**
     * Verifies whether a candidate vertex sequence is a valid topological ordering of the given DAG.
     */
    public static boolean isValidTopologicalOrder(Graph graph, List<Integer> ordering) {
        int n = graph.getVertices();
        if (ordering == null || ordering.size() != n) {
            return false;
        }
        int[] position = new int[n];
        boolean[] seen = new boolean[n];
        for (int i = 0; i < n; i++) {
            int v = ordering.get(i);
            if (v < 0 || v >= n || seen[v]) {
                return false;
            }
            seen[v] = true;
            position[v] = i;
        }
        for (int u = 0; u < n; u++) {
            for (Edge edge : graph.getNeighbors(u)) {
                int v = edge.getDestination();
                if (position[u] >= position[v]) {
                    return false;
                }
            }
        }
        return true;
    }
}
