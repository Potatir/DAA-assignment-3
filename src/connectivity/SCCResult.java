package connectivity;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the Strongly Connected Components (SCCs) of a directed graph,
 * component assignment per vertex, and algorithm benchmark metrics.
 */
public class SCCResult {
    private final String algorithmName;
    private final List<List<Integer>> components;
    private final int[] componentId;
    private final long executionTimeNanos;
    private final long edgesExamined;
    private final long stackOperations;
    private final long comparisons;

    public SCCResult(
            String algorithmName,
            List<List<Integer>> components,
            int[] componentId,
            long executionTimeNanos,
            long edgesExamined,
            long stackOperations,
            long comparisons) {
        this.algorithmName = algorithmName;
        this.components = Collections.unmodifiableList(components);
        this.componentId = componentId.clone();
        this.executionTimeNanos = executionTimeNanos;
        this.edgesExamined = edgesExamined;
        this.stackOperations = stackOperations;
        this.comparisons = comparisons;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public List<List<Integer>> getComponents() {
        return components;
    }

    public int getComponentCount() {
        return components.size();
    }

    public int getComponentId(int vertex) {
        return componentId[vertex];
    }

    public int getLargestComponentSize() {
        int max = 0;
        for (List<Integer> comp : components) {
            if (comp.size() > max) {
                max = comp.size();
            }
        }
        return max;
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

    public long getStackOperations() {
        return stackOperations;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getTotalOperations() {
        return edgesExamined + stackOperations + comparisons;
    }

    @Override
    public String toString() {
        return String.format(
                "%s[sccCount=%d, largestSCC=%d, time=%.3f ms, edgesExamined=%d, stackOps=%d]",
                algorithmName,
                components.size(),
                getLargestComponentSize(),
                getExecutionTimeMillis(),
                edgesExamined,
                stackOperations);
    }
}
