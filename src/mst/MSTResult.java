package mst;

import graph.Edge;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the result of running a Minimum Spanning Tree algorithm
 * (Kruskal's or Prim's) along with detailed performance and operation metrics.
 */
public class MSTResult {
    private final String algorithmName;
    private final List<Edge> mstEdges;
    private final double totalWeight;
    private final boolean connected;
    private final long executionTimeNanos;
    private final long edgesExamined;
    private final long comparisons;
    private final long dsuOrHeapOperations;

    public MSTResult(
            String algorithmName,
            List<Edge> mstEdges,
            double totalWeight,
            boolean connected,
            long executionTimeNanos,
            long edgesExamined,
            long comparisons,
            long dsuOrHeapOperations) {
        this.algorithmName = algorithmName;
        this.mstEdges = Collections.unmodifiableList(mstEdges);
        this.totalWeight = Math.round(totalWeight * 1000.0) / 1000.0;
        this.connected = connected;
        this.executionTimeNanos = executionTimeNanos;
        this.edgesExamined = edgesExamined;
        this.comparisons = comparisons;
        this.dsuOrHeapOperations = dsuOrHeapOperations;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public List<Edge> getMstEdges() {
        return mstEdges;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public boolean isConnected() {
        return connected;
    }

    public long getExecutionTimeNanos() {
        return executionTimeNanos;
    }

    public double getExecutionTimeMillis() {
        return executionTimeNanos / 1_000_000.0;
    }

    public long getEdgesExamined() {
        return edgesExamined;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getDsuOrHeapOperations() {
        return dsuOrHeapOperations;
    }

    public long getTotalOperations() {
        return edgesExamined + comparisons + dsuOrHeapOperations;
    }

    @Override
    public String toString() {
        return String.format(
                "%s[connected=%s, edges=%d, totalWeight=%.2f, time=%.3f ms, edgesExamined=%d, comparisons=%d, structOps=%d]",
                algorithmName,
                connected,
                mstEdges.size(),
                totalWeight,
                getExecutionTimeMillis(),
                edgesExamined,
                comparisons,
                dsuOrHeapOperations);
    }
}
