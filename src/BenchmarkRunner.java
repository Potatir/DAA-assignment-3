import connectivity.KosarajuSCC;
import connectivity.SCCResult;
import connectivity.TarjanSCC;
import connectivity.TopologicalSort;
import connectivity.TopologicalSortResult;
import graph.Graph;
import graph.GraphGenerator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import mst.KruskalMST;
import mst.MSTResult;
import mst.PrimMST;
import shortestpath.DijkstraShortestPath;
import shortestpath.ShortestPathResult;

/**
 * Executes reproducible benchmarks on Small (V=10, E=30), Medium (V=100, E=500),
 * and Large (V=1000, E=5000) graphs.
 * Repeats each experiment 5 times, records average execution times via System.nanoTime()
 * and exact operation counts, writes CSV tables to results/tables/, and generates plots.
 */
public class BenchmarkRunner {

    public static final long SEED = 42L;
    public static final int REPETITIONS = 5;

    public static final String[] CONFIG_NAMES = {"Small", "Medium", "Large"};
    public static final int[] VERTEX_COUNTS = {10, 100, 1000};
    public static final int[] EDGE_COUNTS = {30, 500, 5000};

    public static void runAllBenchmarks() throws IOException {
        Locale.setDefault(Locale.US);
        new File("results/tables").mkdirs();
        new File("results/plots").mkdirs();

        // Warm-up JVM JIT before timed runs
        warmUp();

        int nConfigs = CONFIG_NAMES.length;
        double[] kruskalTimeMs = new double[nConfigs];
        double[] primTimeMs = new double[nConfigs];
        double[] kruskalOps = new double[nConfigs];
        double[] primOps = new double[nConfigs];

        double[] tarjanTimeMs = new double[nConfigs];
        double[] kosarajuTimeMs = new double[nConfigs];
        double[] topoDagTimeMs = new double[nConfigs];
        double[] topoCyclicTimeMs = new double[nConfigs];

        double[] dijkstraTimeMs = new double[nConfigs];
        double[] dijkstraOps = new double[nConfigs];

        System.out.println("=================================================================");
        System.out.println("  Experimental Benchmark Results (Average of 5 Repetitions)");
        System.out.println("=================================================================");

        try (PrintWriter mstCsv = new PrintWriter(new FileWriter("results/tables/mst_comparison.csv"));
             PrintWriter connCsv = new PrintWriter(new FileWriter("results/tables/connectivity_scc_topo.csv"));
             PrintWriter dijkstraCsv = new PrintWriter(new FileWriter("results/tables/dijkstra_results.csv"))) {

            mstCsv.println("Graph,Vertices,Edges,Algorithm,AvgTimeMs,MSTWeight,Connected,EdgesExamined,Comparisons,StructOps,TotalOps");
            connCsv.println("Graph,Vertices,Edges,GraphType,Algorithm,AvgTimeMs,ResultSummary,EdgesExamined,AuxOps,TotalOps");
            dijkstraCsv.println("Graph,Vertices,Edges,Source,ReachableVertices,AvgTimeMs,EdgesExamined,Relaxations,Comparisons,PQOps,TotalOps");

            for (int i = 0; i < nConfigs; i++) {
                String label = CONFIG_NAMES[i];
                int v = VERTEX_COUNTS[i];
                int e = EDGE_COUNTS[i];

                // 1. MST Experiment (Connected Undirected Graph)
                Graph undirected = GraphGenerator.generateConnectedUndirectedGraph(v, e, SEED + i);
                long kTimeSum = 0;
                long pTimeSum = 0;
                MSTResult kLast = null;
                MSTResult pLast = null;

                for (int r = 0; r < REPETITIONS; r++) {
                    kLast = KruskalMST.computeMST(undirected);
                    kTimeSum += kLast.getExecutionTimeNanos();

                    pLast = PrimMST.computeMST(undirected);
                    pTimeSum += pLast.getExecutionTimeNanos();
                }

                kruskalTimeMs[i] = (kTimeSum / (double) REPETITIONS) / 1_000_000.0;
                primTimeMs[i] = (pTimeSum / (double) REPETITIONS) / 1_000_000.0;
                kruskalOps[i] = kLast.getTotalOperations();
                primOps[i] = pLast.getTotalOperations();

                mstCsv.printf(
                        "%s,%d,%d,Kruskal,%.4f,%.2f,%s,%d,%d,%d,%d%n",
                        label, v, e, kruskalTimeMs[i], kLast.getTotalWeight(), kLast.isConnected(),
                        kLast.getEdgesExamined(), kLast.getComparisons(),
                        kLast.getDsuOrHeapOperations(), kLast.getTotalOperations());
                mstCsv.printf(
                        "%s,%d,%d,Prim,%.4f,%.2f,%s,%d,%d,%d,%d%n",
                        label, v, e, primTimeMs[i], pLast.getTotalWeight(), pLast.isConnected(),
                        pLast.getEdgesExamined(), pLast.getComparisons(),
                        pLast.getDsuOrHeapOperations(), pLast.getTotalOperations());

                // 2. Connectivity & SCC Experiments (Directed Cyclic & Directed Acyclic Graphs)
                Graph cyclicGraph = GraphGenerator.generateDirectedCyclicGraph(v, e, SEED + 100 + i);
                Graph dagGraph = GraphGenerator.generateDAG(v, e, SEED + 200 + i);

                long tarjanSum = 0;
                long kosarajuSum = 0;
                long topoDagSum = 0;
                long topoCyclicSum = 0;
                SCCResult tarjanLast = null;
                SCCResult kosarajuLast = null;
                TopologicalSortResult topoDagLast = null;
                TopologicalSortResult topoCyclicLast = null;

                for (int r = 0; r < REPETITIONS; r++) {
                    tarjanLast = TarjanSCC.findSCCs(cyclicGraph);
                    tarjanSum += tarjanLast.getExecutionTimeNanos();

                    kosarajuLast = KosarajuSCC.findSCCs(cyclicGraph);
                    kosarajuSum += kosarajuLast.getExecutionTimeNanos();

                    topoDagLast = TopologicalSort.computeKahn(dagGraph);
                    topoDagSum += topoDagLast.getExecutionTimeNanos();

                    topoCyclicLast = TopologicalSort.computeKahn(cyclicGraph);
                    topoCyclicSum += topoCyclicLast.getExecutionTimeNanos();
                }

                tarjanTimeMs[i] = (tarjanSum / (double) REPETITIONS) / 1_000_000.0;
                kosarajuTimeMs[i] = (kosarajuSum / (double) REPETITIONS) / 1_000_000.0;
                topoDagTimeMs[i] = (topoDagSum / (double) REPETITIONS) / 1_000_000.0;
                topoCyclicTimeMs[i] = (topoCyclicSum / (double) REPETITIONS) / 1_000_000.0;

                connCsv.printf(
                        "%s,%d,%d,CyclicDirected,TarjanSCC,%.4f,SCCs=%d (max=%d),%d,%d,%d%n",
                        label, v, e, tarjanTimeMs[i], tarjanLast.getComponentCount(),
                        tarjanLast.getLargestComponentSize(), tarjanLast.getEdgesExamined(),
                        tarjanLast.getStackOperations(), tarjanLast.getTotalOperations());
                connCsv.printf(
                        "%s,%d,%d,CyclicDirected,KosarajuSCC,%.4f,SCCs=%d (max=%d),%d,%d,%d%n",
                        label, v, e, kosarajuTimeMs[i], kosarajuLast.getComponentCount(),
                        kosarajuLast.getLargestComponentSize(), kosarajuLast.getEdgesExamined(),
                        kosarajuLast.getStackOperations(), kosarajuLast.getTotalOperations());
                connCsv.printf(
                        "%s,%d,%d,DAG,TopologicalSort(Kahn),%.4f,ValidOrder(len=%d),%d,%d,%d%n",
                        label, v, e, topoDagTimeMs[i], topoDagLast.getOrdering().size(),
                        topoDagLast.getEdgesExamined(), topoDagLast.getQueueOrStackOperations(),
                        topoDagLast.getTotalOperations());
                connCsv.printf(
                        "%s,%d,%d,CyclicDirected,TopologicalSort(Kahn),%.4f,CycleDetected,%d,%d,%d%n",
                        label, v, e, topoCyclicTimeMs[i],
                        topoCyclicLast.getEdgesExamined(), topoCyclicLast.getQueueOrStackOperations(),
                        topoCyclicLast.getTotalOperations());

                // 3. Dijkstra Shortest Paths Experiment
                long dijkstraSum = 0;
                ShortestPathResult dijkstraLast = null;
                for (int r = 0; r < REPETITIONS; r++) {
                    dijkstraLast = DijkstraShortestPath.computeShortestPaths(cyclicGraph, 0);
                    dijkstraSum += dijkstraLast.getExecutionTimeNanos();
                }

                dijkstraTimeMs[i] = (dijkstraSum / (double) REPETITIONS) / 1_000_000.0;
                dijkstraOps[i] = dijkstraLast.getTotalOperations();

                dijkstraCsv.printf(
                        "%s,%d,%d,0,%d,%.4f,%d,%d,%d,%d,%d%n",
                        label, v, e, dijkstraLast.getReachableCount(), dijkstraTimeMs[i],
                        dijkstraLast.getEdgesExamined(), dijkstraLast.getRelaxations(),
                        dijkstraLast.getComparisons(), dijkstraLast.getPriorityQueueOps(),
                        dijkstraLast.getTotalOperations());

                System.out.printf(
                        "  [%s: V=%d, E=%d] Kruskal=%.4f ms | Prim=%.4f ms | TarjanSCC=%.4f ms | Topo(DAG)=%.4f ms | Dijkstra=%.4f ms%n",
                        label, v, e, kruskalTimeMs[i], primTimeMs[i], tarjanTimeMs[i], topoDagTimeMs[i], dijkstraTimeMs[i]);
            }
        }

        // Generate required plots
        String[] algoNames = {"Kruskal MST", "Prim MST", "Tarjan SCC", "Topological Sort (DAG)", "Dijkstra Shortest Path"};
        double[][] allTimes = {kruskalTimeMs, primTimeMs, tarjanTimeMs, topoDagTimeMs, dijkstraTimeMs};

        PlotGenerator.generateLinePlot(
                "results/plots/execution_time_vs_vertices.png",
                "Algorithm Execution Time vs. Number of Vertices (V)",
                "Number of Vertices (V)",
                "Average Execution Time (ms)",
                VERTEX_COUNTS,
                algoNames,
                allTimes);

        PlotGenerator.generateLinePlot(
                "results/plots/execution_time_vs_edges.png",
                "Algorithm Execution Time vs. Number of Edges (E)",
                "Number of Edges (E)",
                "Average Execution Time (ms)",
                EDGE_COUNTS,
                algoNames,
                allTimes);

        PlotGenerator.generateLinePlot(
                "results/plots/kruskal_vs_prim_operations.png",
                "Kruskal vs. Prim: Total Operation Counts Across Graph Sizes",
                "Number of Edges (E)",
                "Total Elementary Operations",
                EDGE_COUNTS,
                new String[]{"Kruskal Total Operations", "Prim Total Operations", "Dijkstra Total Operations"},
                new double[][]{kruskalOps, primOps, dijkstraOps});

        System.out.println("  Tables saved to results/tables/ and plots saved to results/plots/");
        System.out.println("=================================================================\n");
    }

    private static void warmUp() {
        Graph gUndir = GraphGenerator.generateConnectedUndirectedGraph(50, 150, 999L);
        Graph gDir = GraphGenerator.generateDirectedCyclicGraph(50, 150, 999L);
        Graph gDag = GraphGenerator.generateDAG(50, 150, 999L);
        for (int i = 0; i < 15; i++) {
            KruskalMST.computeMST(gUndir);
            PrimMST.computeMST(gUndir);
            TarjanSCC.findSCCs(gDir);
            KosarajuSCC.findSCCs(gDir);
            TopologicalSort.computeKahn(gDag);
            DijkstraShortestPath.computeShortestPaths(gDir, 0);
        }
    }
}
