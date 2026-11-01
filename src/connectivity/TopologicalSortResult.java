package connectivity;

import java.util.Collections;
import java.util.List;

/**
 * Result of running Topological Ordering on a directed graph.
 * Reports whether the graph is a valid DAG (no cycles), the topological ordering if valid,
 * and execution metrics.
 */
public class TopologicalSortResult {
    private final boolean dag;
    private final List<Integer> ordering;
    private final long executionTimeNanos;
    private final long edgesExamined;
    private final long queueOrStackOperations;

    public TopologicalSortResult(
            boolean dag,
            List<Integer> ordering,
            long executionTimeNanos,
            long edgesExamined,
            long queueOrStackOperations) {
        this.dag = dag;
        this.ordering = Collections.unmodifiableList(ordering);
        this.executionTimeNanos = executionTimeNanos;
        this.edgesExamined = edgesExamined;
        this.queueOrStackOperations = queueOrStackOperations;
    }

    public boolean isDAG() {
        return dag;
    }

    public boolean hasCycle() {
        return !dag;
    }

    public List<Integer> getOrdering() {
        return ordering;
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

    public long getQueueOrStackOperations() {
        return queueOrStackOperations;
    }

    public long getTotalOperations() {
        return edgesExamined + queueOrStackOperations;
    }

    @Override
    public String toString() {
        return String.format(
                "TopologicalSortResult[isDAG=%s, orderedVertices=%d, time=%.3f ms, edgesExamined=%d, queueOps=%d]",
                dag,
                ordering.size(),
                getExecutionTimeMillis(),
                edgesExamined,
                queueOrStackOperations);
    }
}
