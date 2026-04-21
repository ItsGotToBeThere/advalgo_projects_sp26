import java.io.*;
import java.util.*;

/*
 * Persistent Segment Tree with XOR-Sum andPolynomial Hash
 *
 * Each node stores two fingerprints over its leaf range [l, r]:
 *   xorSum: XOR of all values in [l, r]
 *   hashVal: sum of A[i] * 131^i mod (10^9+7) for i in [l, r]
 */
public class Solution {
    static final long MOD  = 1_000_000_007L; // large prime mod for the polynomial hash
    static final long BASE = 131L; // hash base

    // pow131[i] = 131^i mod MOD. Precomputed so each leaf hash is O(1)
    static long[] pow131;

    // One segment tree node covering some range [l, r]
    static class Node {
        long xorSum;   // XOR of all values in this node's range
        long hashVal;  // polynomial hash of all values in this node's range
        Node left, right;

        // Internal node. XOR computed with ^. Polynomial hash with + because the two children cover disjoint index ranges and their position
        Node(Node left, Node right) {
            this.left    = left;
            this.right   = right;
            this.xorSum  = left.xorSum ^ right.xorSum;
            this.hashVal = (left.hashVal + right.hashVal) % MOD;
        }

        // Leaf node at position pos with value val. hashVal = val * 131^pos mod MOD
        Node(long val, int pos) {
            this.xorSum  = val;
            this.hashVal = (val % MOD) * pow131[pos] % MOD;
            this.left    = null;
            this.right   = null;
        }
    }

    // Precompute powers of BASE so leaf construction is O(1)
    static void precomputePowers(int n) {
        pow131 = new long[n + 1];
        pow131[0] = 1L;
        for (int i = 1; i <= n; i++)
            pow131[i] = (pow131[i - 1] * BASE) % MOD;
    }

    // Build initial segment tree over A[l..r] in O(N)
    static Node build(long[] A, int l, int r) {
        // Base case: single element so make leaf
        if (l == r) return new Node(A[l], l);

        int mid = (l + r) / 2;
        // Build left and right subtrees, then merge
        return new Node(build(A, l, mid), build(A, mid + 1, r));
    }

    // Create a new version by updating position pos to val, O(log N)
    // Traverse from root to the target leaf, allocating new node at every level. All siblings off the update path are reused from
    //  the old version unchanged, so only O(log N) new nodes are created.
    static Node update(Node oldNode, int l, int r, int pos, long val) {
        // Base case: reached the target leaf so replace it with new leaf
        if (l == r) return new Node(val, l);

        int mid = (l + r) / 2;
        if (pos <= mid) {
            // Target is in the left half, recurse left, reuse old right child
            return new Node(update(oldNode.left, l, mid, pos, val), oldNode.right);
        } else {
            // Target is in the right half, reuse old left child, recurse right
            return new Node(oldNode.left, update(oldNode.right, mid + 1, r, pos, val));
        }
    }

    // Find the smallest index in [l, r] where versions n1 and n2 differ, O(log N)
    // If n1 and n2 are the same object or the fingerprints agree, the entire subtree is identical, skip it.
    // Two independent hash functions make a false positive astronomically unlikely: probability ~1/MOD per node.
    // Otherwise, recurse left first so we always return the smallest index.
    static int diverge(Node n1, Node n2, int l, int r) {
        // Same object reference means this subtree is fully shared
        if (n1 == n2) return -1;

        // Both fingerprints agree, treat as identical
        if (n1.xorSum == n2.xorSum && n1.hashVal == n2.hashVal) return -1;

        // Leaf with differing fingerprints, divergence point
        if (l == r) return l;

        int mid = (l + r) / 2;

        // Check the left half first to return the smallest index
        int res = diverge(n1.left, n2.left, l, mid);
        if (res != -1) return res;

        // No difference on the left, first difference must be on the right
        return diverge(n1.right, n2.right, mid + 1, r);
    }


    // XOR of all values in version `node` over query range [ql, qr], O(log N)
    // Standard segment tree range query: skip ranges that don't overlap,
    // return the stored xorSum for ranges fully contained in [ql, qr],
    // otherwise split and combine children with XOR.

    static long rangeXOR(Node node, int l, int r, int ql, int qr) {
        if (ql > r || qr < l)       return 0L;           // no overlap, contributes nothing to XOR
        if (ql <= l && r <= qr)     return node.xorSum;  // fully covered, use stored value
        int mid = (l + r) / 2;
        return rangeXOR(node.left, l, mid, ql, qr)
             ^ rangeXOR(node.right, mid + 1, r, ql, qr);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        int N = Integer.parseInt(st.nextToken()); // array length
        int Q = Integer.parseInt(st.nextToken()); // number of queries

        // Read the initial array (version 0)
        long[] A = new long[N];
        st = new StringTokenizer(br.readLine());
        for (int i = 0; i < N; i++) A[i] = Long.parseLong(st.nextToken());

        precomputePowers(N);

        // roots.get(v) is the root node of version v
        // Version 0 is built from the initial array
        List<Node> roots = new ArrayList<>();
        roots.add(build(A, 0, N - 1));

        StringBuilder sb = new StringBuilder();
        for (int q = 0; q < Q; q++) {
            st = new StringTokenizer(br.readLine());
            String op = st.nextToken();

            if (op.equals("UPDATE")) {
                int  v = Integer.parseInt(st.nextToken());
                int  i = Integer.parseInt(st.nextToken());
                long x = Long.parseLong(st.nextToken());
                // Path-copy update on version v produces the next version
                roots.add(update(roots.get(v), 0, N - 1, i, x));
                sb.append("Version ").append(roots.size() - 1).append(" created\n");

            } else if (op.equals("DIVERGE")) {
                int v1 = Integer.parseInt(st.nextToken());
                int v2 = Integer.parseInt(st.nextToken());
                sb.append(diverge(roots.get(v1), roots.get(v2), 0, N - 1)).append('\n');

            } else { // rangeXOR
                int  v = Integer.parseInt(st.nextToken());
                int  L = Integer.parseInt(st.nextToken());
                int  R = Integer.parseInt(st.nextToken());
                sb.append(rangeXOR(roots.get(v), 0, N - 1, L, R)).append('\n');
            }
        }
        System.out.print(sb);
    }
}