import connectivity.KosarajuSCC;
import connectivity.SCCResult;
import connectivity.TarjanSCC;
import connectivity.TopologicalSort;
import connectivity.TopologicalSortResult;
import graph.Graph;
import graph.GraphGenerator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mst.KruskalMST;
import mst.MSTResult;
import mst.PrimMST;
import shortestpath.DijkstraShortestPath;
import shortestpath.ShortestPathResult;

/**
 * Automated verification suite testing all edge cases and correctness invariants
 * required in Section 6 of the assignment specification.
 */
public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void runAllTests() {
        passed = 0;
        failed = 0;
        System.out.println("=================================================================");
        System.out.println("  Running Smart City Graph Algorithms Verification Suite");
        System.out.println("=================================================================");

        testSingleVertexGraph();
        testSmallConnectedGraph();
        testDisconnectedGraph();
        testCyclicAndAcyclicGraphs();
        testMultipleValidMSTsAndDuplicateWeights();
        testUnreachableVerticesInDijkstra();
        testNegativeWeightRejectionInDijkstra();
        testSCCPartitionCompleteness();
        testConsistencyAcrossRandomSeededGraphs();

        System.out.println("-----------------------------------------------------------------");
        System.out.printf("  Test Summary: %d PASSED, %d FAILED%n", passed, failed);
        System.out.println("=================================================================\n");

        if (failed > 0) {
            throw new AssertionError("Verification failed with " + failed + " failing test(s).");
        }
    }

    private static void testSingleVertexGraph() {
        Graph undirected = new Graph(1, false);
        MSTResult kruskal = KruskalMST.computeMST(undirected);
        MSTResult prim = PrimMST.computeMST(undirected);

        check("Single-vertex MST connected (Kruskal)", kruskal.isConnected());
        check("Single-vertex MST connected (Prim)", prim.isConnected());
        check("Single-vertex MST zero weight", Math.abs(kruskal.getTotalWeight()) < 1e-9 && Math.abs(prim.getTotalWeight()) < 1e-9);
        check("Single-vertex MST empty edge list", kruskal.getMstEdges().isEmpty() && prim.getMstEdges().isEmpty());

        Graph directed = new Graph(1, true);
        TopologicalSortResult topo = TopologicalSort.computeKahn(directed);
        SCCResult scc = TarjanSCC.findSCCs(directed);
        ShortestPathResult sp = DijkstraShortestPath.computeShortestPaths(directed, 0);

        check("Single-vertex DAG topological order valid", topo.isDAG() && topo.getOrdering().size() == 1);
        check("Single-vertex SCC count == 1", scc.getComponentCount() == 1);
        check("Single-vertex Dijkstra distance to self == 0", Math.abs(sp.getDistance(0)) < 1e-9);
    }

    private static void testSmallConnectedGraph() {
        Graph city = new Graph(6, false);
        city.addEdge(0, 1, 4.0);
        city.addEdge(0, 2, 3.0);
        city.addEdge(1, 2, 1.0);
        city.addEdge(1, 3, 2.0);
        city.addEdge(2, 3, 4.0);
        city.addEdge(3, 4, 2.0);
        city.addEdge(4, 5, 6.0);
        city.addEdge(3, 5, 3.0);

        MSTResult kruskal = KruskalMST.computeMST(city);
        MSTResult prim = PrimMST.computeMST(city);

        // Expected MST edges: (1-2:1), (1-3:2), (3-4:2), (0-2:3), (3-5:3) => total = 11.0
        check("Small graph connected", kruskal.isConnected() && prim.isConnected());
        check("Small graph Kruskal == Prim MST weight (11.0)",
                Math.abs(kruskal.getTotalWeight() - 11.0) < 1e-6
                        && Math.abs(prim.getTotalWeight() - 11.0) < 1e-6);
        check("Small graph MST has V - 1 edges", kruskal.getMstEdges().size() == 5 && prim.getMstEdges().size() == 5);
    }

    private static void testDisconnectedGraph() {
        Graph disconnected = new Graph(6, false);
        // Component A: {0, 1, 2}
        disconnected.addEdge(0, 1, 5.0);
        disconnected.addEdge(1, 2, 7.0);
        // Component B: {3, 4, 5}
        disconnected.addEdge(3, 4, 2.0);
        disconnected.addEdge(4, 5, 3.0);

        MSTResult kruskal = KruskalMST.computeMST(disconnected);
        MSTResult prim = PrimMST.computeMST(disconnected);

        check("Disconnected graph detected by Kruskal", !kruskal.isConnected());
        check("Disconnected graph detected by Prim", !prim.isConnected());
    }

    private static void testCyclicAndAcyclicGraphs() {
        // 1. Valid DAG representing city infrastructure dependencies
        Graph dag = new Graph(6, true);
        dag.addEdge(0, 1, 1.0);
        dag.addEdge(0, 2, 1.0);
        dag.addEdge(1, 3, 1.0);
        dag.addEdge(2, 3, 1.0);
        dag.addEdge(3, 4, 1.0);
        dag.addEdge(4, 5, 1.0);

        TopologicalSortResult kahnDag = TopologicalSort.computeKahn(dag);
        TopologicalSortResult dfsDag = TopologicalSort.computeDFS(dag);

        check("DAG recognized by Kahn's algorithm", kahnDag.isDAG());
        check("DAG recognized by DFS algorithm", dfsDag.isDAG());
        check("Kahn topological ordering satisfies all directed edges",
                TopologicalSort.isValidTopologicalOrder(dag, kahnDag.getOrdering()));
        check("DFS topological ordering satisfies all directed edges",
                TopologicalSort.isValidTopologicalOrder(dag, dfsDag.getOrdering()));

        // 2. Cyclic directed graph (0 -> 1 -> 2 -> 0) + (3 -> 4)
        Graph cyclic = new Graph(5, true);
        cyclic.addEdge(0, 1, 1.0);
        cyclic.addEdge(1, 2, 1.0);
        cyclic.addEdge(2, 0, 1.0);
        cyclic.addEdge(2, 3, 1.0);
        cyclic.addEdge(3, 4, 1.0);

        TopologicalSortResult kahnCyclic = TopologicalSort.computeKahn(cyclic);
        TopologicalSortResult dfsCyclic = TopologicalSort.computeDFS(cyclic);

        check("Cycle correctly detected by Kahn's algorithm", kahnCyclic.hasCycle() && kahnCyclic.getOrdering().isEmpty());
        check("Cycle correctly detected by DFS topological sort", dfsCyclic.hasCycle() && dfsCyclic.getOrdering().isEmpty());
    }

    private static void testMultipleValidMSTsAndDuplicateWeights() {
        // Square graph where all 4 cycle edges have weight 5.0 and diagonal has weight 5.0
        // Multiple distinct spanning trees have identical minimum weight = 15.0
        Graph duplicateWeights = new Graph(4, false);
        duplicateWeights.addEdge(0, 1, 5.0);
        duplicateWeights.addEdge(1, 2, 5.0);
        duplicateWeights.addEdge(2, 3, 5.0);
        duplicateWeights.addEdge(3, 0, 5.0);
        duplicateWeights.addEdge(0, 2, 5.0);
        duplicateWeights.addEdge(1, 3, 5.0);

        MSTResult kruskal = KruskalMST.computeMST(duplicateWeights);
        MSTResult prim = PrimMST.computeMST(duplicateWeights);

        check("Duplicate-weight graph with multiple MSTs connected", kruskal.isConnected() && prim.isConnected());
        check("Duplicate-weight MST weight matches between Kruskal and Prim (15.0)",
                Math.abs(kruskal.getTotalWeight() - 15.0) < 1e-6
                        && Math.abs(prim.getTotalWeight() - 15.0) < 1e-6);
        check("Duplicate-weight MST selects exactly V - 1 = 3 edges",
                kruskal.getMstEdges().size() == 3 && prim.getMstEdges().size() == 3);
    }

    private static void testUnreachableVerticesInDijkstra() {
        Graph routes = new Graph(6, true);
        routes.addEdge(0, 1, 2.5);
        routes.addEdge(1, 2, 3.5);
        routes.addEdge(0, 2, 10.0);
        routes.addEdge(2, 3, 1.0);
        // Districts 4 and 5 form an isolated directed island
        routes.addEdge(4, 5, 2.0);

        ShortestPathResult sp = DijkstraShortestPath.computeShortestPaths(routes, 0);

        check("Shortest distance 0 -> 2 is 6.0 via vertex 1", Math.abs(sp.getDistance(2) - 6.0) < 1e-6);
        check("Shortest distance 0 -> 3 is 7.0", Math.abs(sp.getDistance(3) - 7.0) < 1e-6);
        check("Shortest path 0 -> 3 is [0, 1, 2, 3]", sp.getPathTo(3).equals(List.of(0, 1, 2, 3)));
        check("Unreachable vertex 4 has +Infinity distance and empty path",
                !sp.isReachable(4) && Double.isInfinite(sp.getDistance(4)) && sp.getPathTo(4).isEmpty());
        check("Unreachable vertex 5 has +Infinity distance and empty path",
                !sp.isReachable(5) && Double.isInfinite(sp.getDistance(5)) && sp.getPathTo(5).isEmpty());
    }

    private static void testNegativeWeightRejectionInDijkstra() {
        Graph negativeGraph = new Graph(3, true);
        negativeGraph.addEdge(0, 1, 4.0);
        negativeGraph.addEdge(1, 2, -2.0);

        boolean threwException = false;
        try {
            DijkstraShortestPath.computeShortestPaths(negativeGraph, 0);
        } catch (IllegalArgumentException ex) {
            threwException = true;
        }
        check("Dijkstra rejects negative edge weights with IllegalArgumentException", threwException);
    }

    private static void testSCCPartitionCompleteness() {
        Graph directed = new Graph(8, true);
        // SCC 1: {0, 1, 2}
        directed.addEdge(0, 1, 1.0);
        directed.addEdge(1, 2, 1.0);
        directed.addEdge(2, 0, 1.0);
        // SCC 2: {3, 4}
        directed.addEdge(2, 3, 1.0);
        directed.addEdge(3, 4, 1.0);
        directed.addEdge(4, 3, 1.0);
        // SCC 3: {5, 6}
        directed.addEdge(4, 5, 1.0);
        directed.addEdge(5, 6, 1.0);
        directed.addEdge(6, 5, 1.0);
        // SCC 4: {7} (singleton sink)
        directed.addEdge(6, 7, 1.0);

        SCCResult tarjan = TarjanSCC.findSCCs(directed);
        SCCResult kosaraju = KosarajuSCC.findSCCs(directed);

        check("Tarjan and Kosaraju find identical SCC count (4)",
                tarjan.getComponentCount() == 4 && kosaraju.getComponentCount() == 4);
        check("Every vertex belongs to exactly one SCC (Tarjan)",
                verifyExactVertexPartition(tarjan.getComponents(), 8));
        check("Every vertex belongs to exactly one SCC (Kosaraju)",
                verifyExactVertexPartition(kosaraju.getComponents(), 8));
    }

    private static void testConsistencyAcrossRandomSeededGraphs() {
        int[][] configs = {{10, 30}, {100, 500}, {1000, 5000}};
        for (int[] cfg : configs) {
            int v = cfg[0];
            int e = cfg[1];
            Graph undirected = GraphGenerator.generateConnectedUndirectedGraph(v, e, 42L);
            MSTResult kruskal = KruskalMST.computeMST(undirected);
            MSTResult prim = PrimMST.computeMST(undirected);

            check(String.format("Kruskal == Prim MST weight on (V=%d, E=%d)", v, e),
                    kruskal.isConnected()
                            && prim.isConnected()
                            && Math.abs(kruskal.getTotalWeight() - prim.getTotalWeight()) < 1e-4);

            Graph dag = GraphGenerator.generateDAG(v, e, 42L);
            TopologicalSortResult topo = TopologicalSort.computeKahn(dag);
            check(String.format("Valid topological order on generated DAG (V=%d, E=%d)", v, e),
                    topo.isDAG() && TopologicalSort.isValidTopologicalOrder(dag, topo.getOrdering()));

            Graph cyclic = GraphGenerator.generateDirectedCyclicGraph(v, e, 42L);
            SCCResult scc = TarjanSCC.findSCCs(cyclic);
            check(String.format("Every vertex in exactly one SCC on (V=%d, E=%d)", v, e),
                    verifyExactVertexPartition(scc.getComponents(), v));
        }
    }

    private static boolean verifyExactVertexPartition(List<List<Integer>> components, int totalVertices) {
        Set<Integer> seen = new HashSet<>();
        int count = 0;
        for (List<Integer> comp : components) {
            if (comp.isEmpty()) {
                return false;
            }
            for (int v : comp) {
                if (v < 0 || v >= totalVertices || !seen.add(v)) {
                    return false;
                }
                count++;
            }
        }
        return count == totalVertices && seen.size() == totalVertices;
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.printf("  [PASS] %s%n", description);
        } else {
            failed++;
            System.out.printf("  [FAIL] %s%n", description);
        }
    }
}
