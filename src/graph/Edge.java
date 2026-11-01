package graph;

import java.util.Objects;

/**
 * Represents a weighted connection (road or infrastructure link) between two city districts.
 */
public class Edge implements Comparable<Edge> {
    private final int source;
    private final int destination;
    private final double weight;

    public Edge(int source, int destination, double weight) {
        if (source < 0 || destination < 0) {
            throw new IllegalArgumentException("Vertex indices must be non-negative.");
        }
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }

    public double getWeight() {
        return weight;
    }

    /**
     * Returns the opposite endpoint of this edge given one endpoint.
     */
    public int getOther(int vertex) {
        if (vertex == source) {
            return destination;
        } else if (vertex == destination) {
            return source;
        }
        throw new IllegalArgumentException("Vertex " + vertex + " is not an endpoint of this edge.");
    }

    @Override
    public int compareTo(Edge other) {
        int cmp = Double.compare(this.weight, other.weight);
        if (cmp != 0) {
            return cmp;
        }
        cmp = Integer.compare(this.source, other.source);
        if (cmp != 0) {
            return cmp;
        }
        return Integer.compare(this.destination, other.destination);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Edge)) return false;
        Edge other = (Edge) obj;
        return source == other.source
                && destination == other.destination
                && Double.compare(weight, other.weight) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination, weight);
    }

    @Override
    public String toString() {
        return String.format("(%d -> %d, w=%.2f)", source, destination, weight);
    }
}
