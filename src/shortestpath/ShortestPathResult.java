package shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates shortest-path distances, predecessor trees, path reconstruction,
 * and benchmark metrics produced by Dijkstra's algorithm.
 */
public class ShortestPathResult {
    private final int source;
    private final double[] distances;
    private final int[] predecessors;
    private final long executionTimeNanos;
    private final long edgesExamined;
    private final long comparisons;
    private final long priorityQueueOps;
    private final long relaxations;

    public ShortestPathResult(
            int source,
            double[] distances,
            int[] predecessors,
            long executionTimeNanos,
            long edgesExamined,
            long comparisons,
            long priorityQueueOps,
            long relaxations) {
        this.source = source;
        this.distances = distances.clone();
        this.predecessors = predecessors.clone();
        this.executionTimeNanos = executionTimeNanos;
        this.edgesExamined = edgesExamined;
        this.comparisons = comparisons;
        this.priorityQueueOps = priorityQueueOps;
        this.relaxations = relaxations;
    }

    public int getSource() {
        return source;
    }

    public double getDistance(int target) {
        return distances[target];
    }

    public double[] getDistances() {
        return distances.clone();
    }

    public boolean isReachable(int target) {
        return !Double.isInfinite(distances[target]);
    }

    public int getReachableCount() {
        int count = 0;
        for (double d : distances) {
            if (!Double.isInfinite(d)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Reconstructs the shortest path from the source vertex to the target vertex.
     * Returns an empty list if the target is unreachable.
     */
    public List<Integer> getPathTo(int target) {
        if (!isReachable(target)) {
            return Collections.emptyList();
        }
        List<Integer> path = new ArrayList<>();
        for (int curr = target; curr != -1; curr = predecessors[curr]) {
            path.add(curr);
        }
        Collections.reverse(path);
        return Collections.unmodifiableList(path);
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

    public long getPriorityQueueOps() {
        return priorityQueueOps;
    }

    public long getRelaxations() {
        return relaxations;
    }

    public long getTotalOperations() {
        return edgesExamined + comparisons + priorityQueueOps + relaxations;
    }

    @Override
    public String toString() {
        return String.format(
                "DijkstraResult[source=%d, reachable=%d/%d, time=%.3f ms, edgesExamined=%d, relaxations=%d, pqOps=%d]",
                source,
                getReachableCount(),
                distances.length,
                getExecutionTimeMillis(),
                edgesExamined,
                relaxations,
                priorityQueueOps);
    }
}
