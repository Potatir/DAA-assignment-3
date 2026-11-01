package dsu;

/**
 * Disjoint Set Union (Union-Find) data structure with Path Compression
 * and Union by Rank (with subtree size tracking).
 * Tracks operation counts for experimental complexity analysis.
 */
public class DisjointSetUnion {
    private final int[] parent;
    private final int[] rank;
    private final int[] size;
    private int components;

    // Instrumentation counters
    private long findCalls;
    private long parentAccesses;
    private long unionCalls;
    private long successfulUnions;
    private long rankComparisons;

    public DisjointSetUnion(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Number of elements cannot be negative.");
        }
        this.parent = new int[n];
        this.rank = new int[n];
        this.size = new int[n];
        this.components = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
            size[i] = 1;
        }
    }

    /**
     * Finds the representative of the set containing x, applying path compression.
     */
    public int find(int x) {
        validateIndex(x);
        findCalls++;
        return findInternal(x);
    }

    private int findInternal(int x) {
        parentAccesses++;
        if (parent[x] != x) {
            parent[x] = findInternal(parent[x]);
            parentAccesses++; // Recording path compression pointer update
        }
        return parent[x];
    }

    /**
     * Unites the sets containing x and y using union by rank.
     *
     * @return true if x and y were in different sets and have now been merged,
     *         false if they were already in the same set.
     */
    public boolean union(int x, int y) {
        validateIndex(x);
        validateIndex(y);
        unionCalls++;

        int rootX = find(x);
        int rootY = find(y);

        rankComparisons++;
        if (rootX == rootY) {
            return false;
        }

        rankComparisons++;
        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
            size[rootY] += size[rootX];
        } else if (rank[rootX] > rank[rootY]) {
            rankComparisons++;
            parent[rootY] = rootX;
            size[rootX] += size[rootY];
        } else {
            rankComparisons++;
            parent[rootY] = rootX;
            size[rootX] += size[rootY];
            rank[rootX]++;
        }

        components--;
        successfulUnions++;
        return true;
    }

    public boolean connected(int x, int y) {
        return find(x) == find(y);
    }

    public int getComponentCount() {
        return components;
    }

    public int getComponentSize(int x) {
        return size[find(x)];
    }

    public long getFindCalls() {
        return findCalls;
    }

    public long getParentAccesses() {
        return parentAccesses;
    }

    public long getUnionCalls() {
        return unionCalls;
    }

    public long getSuccessfulUnions() {
        return successfulUnions;
    }

    public long getRankComparisons() {
        return rankComparisons;
    }

    public long getTotalOperations() {
        return findCalls + unionCalls + parentAccesses + rankComparisons;
    }

    private void validateIndex(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IndexOutOfBoundsException("Element index " + x + " out of bounds.");
        }
    }
}
