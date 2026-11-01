import connectivity.SCCResult;
import connectivity.TarjanSCC;
import connectivity.TopologicalSort;
import connectivity.TopologicalSortResult;
import graph.Graph;
import java.io.IOException;
import mst.KruskalMST;
import mst.MSTResult;
import mst.PrimMST;
import shortestpath.DijkstraShortestPath;
import shortestpath.ShortestPathResult;

/**
 * Main entry point for Assignment 3: Smart City Infrastructure and Route Planning.
 * Demonstrates the Smart City scenario on an example district network, runs the
 * automated verification test suite, and executes the experimental benchmarks.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        runSmartCityScenarioDemo();
        TestRunner.runAllTests();
        BenchmarkRunner.runAllBenchmarks();
    }

    private static void runSmartCityScenarioDemo() {
        System.out.println("=================================================================");
        System.out.println("  Smart City Infrastructure & Route Planning — Scenario Demo");
        System.out.println("=================================================================");

        String[] districts = {
            "0: Central Hub",
            "1: Tech Park",
            "2: University Quarter",
            "3: Medical Center",
            "4: Industrial Zone",
            "5: Green Residential",
            "6: Airport Terminal"
        };

        // Task 1: Minimum-cost fiber & road backbone connecting all districts
        Graph roadBackbone = new Graph(districts.length, false);
        for (int i = 0; i < districts.length; i++) {
            roadBackbone.setDistrictName(i, districts[i]);
        }
        roadBackbone.addEdge(0, 1, 4.5);
        roadBackbone.addEdge(0, 2, 3.2);
        roadBackbone.addEdge(1, 2, 2.1);
        roadBackbone.addEdge(1, 3, 5.0);
        roadBackbone.addEdge(2, 3, 3.8);
        roadBackbone.addEdge(2, 5, 6.4);
        roadBackbone.addEdge(3, 4, 2.7);
        roadBackbone.addEdge(4, 5, 4.0);
        roadBackbone.addEdge(4, 6, 7.5);
        roadBackbone.addEdge(5, 6, 5.2);

        MSTResult kruskal = KruskalMST.computeMST(roadBackbone);
        MSTResult prim = PrimMST.computeMST(roadBackbone);

        System.out.println("[Task 1 — Minimum Spanning Tree (Infrastructure Backbone)]");
        System.out.printf("  Graph Connected: %s%n", kruskal.isConnected());
        System.out.printf("  Kruskal MST Total Cost: %.2f (Edges: %s)%n", kruskal.getTotalWeight(), kruskal.getMstEdges());
        System.out.printf("  Prim MST Total Cost:    %.2f (Edges: %s)%n%n", prim.getTotalWeight(), prim.getMstEdges());

        // Task 2: Service dependency DAG & One-way road network SCC analysis
        Graph serviceDependencies = new Graph(6, true);
        serviceDependencies.addEdge(0, 1, 1.0); // Power Grid -> Water Pumping
        serviceDependencies.addEdge(0, 2, 1.0); // Power Grid -> Telecom Core
        serviceDependencies.addEdge(1, 3, 1.0); // Water Pumping -> Hospital Complex
        serviceDependencies.addEdge(2, 3, 1.0); // Telecom Core -> Hospital Complex
        serviceDependencies.addEdge(2, 4, 1.0); // Telecom Core -> Traffic Control
        serviceDependencies.addEdge(4, 5, 1.0); // Traffic Control -> Autonomous Transit

        TopologicalSortResult topo = TopologicalSort.computeKahn(serviceDependencies);
        System.out.println("[Task 2A — Service Dependency Topological Ordering (DAG)]");
        System.out.printf("  Is Acyclic (Valid DAG): %s%n", topo.isDAG());
        System.out.printf("  Deployment Order:       %s%n%n", topo.getOrdering());

        Graph oneWayTraffic = new Graph(districts.length, true);
        oneWayTraffic.addEdge(0, 1, 4.5);
        oneWayTraffic.addEdge(1, 2, 2.1);
        oneWayTraffic.addEdge(2, 0, 3.2); // Cycle 1: {0, 1, 2}
        oneWayTraffic.addEdge(2, 3, 3.8);
        oneWayTraffic.addEdge(3, 4, 2.7);
        oneWayTraffic.addEdge(4, 3, 2.9); // Cycle 2: {3, 4}
        oneWayTraffic.addEdge(4, 5, 4.0);
        oneWayTraffic.addEdge(5, 6, 5.2);

        SCCResult scc = TarjanSCC.findSCCs(oneWayTraffic);
        System.out.println("[Task 2B — Strongly Connected Traffic Zones (Tarjan SCC)]");
        System.out.printf("  Total Strongly Connected Components: %d%n", scc.getComponentCount());
        System.out.printf("  Components: %s%n%n", scc.getComponents());

        // Task 3: Emergency response shortest paths from Central Hub (0)
        ShortestPathResult sp = DijkstraShortestPath.computeShortestPaths(oneWayTraffic, 0);
        System.out.println("[Task 3 — Emergency Route Planning (Dijkstra from District 0)]");
        for (int v = 0; v < oneWayTraffic.getVertices(); v++) {
            System.out.printf(
                    "  To %-24s : distance = %6.2f | path = %s%n",
                    districts[v], sp.getDistance(v), sp.getPathTo(v));
        }
        System.out.println("=================================================================\n");
    }
}
