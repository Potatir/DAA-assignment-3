package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adjacency-list representation of a smart city infrastructure network.
 * Supports both undirected graphs (road construction / MST) and directed graphs
 * (one-way routes, service dependencies, and shortest-path navigation).
 */
public class Graph {
    private final int vertices;
    private final boolean directed;
    private final List<List<Edge>> adjacencyList;
    private final List<Edge> edges;
    private final String[] districtNames;

    public Graph(int vertices, boolean directed) {
        if (vertices < 0) {
            throw new IllegalArgumentException("Number of vertices cannot be negative.");
        }
        this.vertices = vertices;
        this.directed = directed;
        this.adjacencyList = new ArrayList<>(vertices);
        this.edges = new ArrayList<>();
        this.districtNames = new String[vertices];
        for (int i = 0; i < vertices; i++) {
            this.adjacencyList.add(new ArrayList<>());
            this.districtNames[i] = "District-" + i;
        }
    }

    public void setDistrictName(int vertex, String name) {
        validateVertex(vertex);
        this.districtNames[vertex] = name;
    }

    public String getDistrictName(int vertex) {
        validateVertex(vertex);
        return districtNames[vertex];
    }

    public void addEdge(int source, int destination, double weight) {
        validateVertex(source);
        validateVertex(destination);
        Edge forward = new Edge(source, destination, weight);
        adjacencyList.get(source).add(forward);
        edges.add(forward);

        if (!directed && source != destination) {
            Edge backward = new Edge(destination, source, weight);
            adjacencyList.get(destination).add(backward);
        }
    }

    public int getVertices() {
        return vertices;
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Edge> getNeighbors(int vertex) {
        validateVertex(vertex);
        return Collections.unmodifiableList(adjacencyList.get(vertex));
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    private void validateVertex(int vertex) {
        if (vertex < 0 || vertex >= vertices) {
            throw new IndexOutOfBoundsException(
                    "Vertex index " + vertex + " is out of bounds for graph with " + vertices + " vertices.");
        }
    }
}
