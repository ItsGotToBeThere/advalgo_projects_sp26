/* Persistent Segment Tree - Java implementation
 * Paul Lee kkz3uq
* Uses SUM function
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

class Node {
    int value; // The SUM value of the segment
    Node left, right; // Left and right children

    public Node(int value) {
        this.value = value;
        this.left = null;
        this.right = null;
    }

    public Node(int value, Node left, Node right) {
        this.value = value;
        this.left = left;
        this.right = right;
    }
}

public class PersistentSegmentTree {
    int size; // Size of the input array
    ArrayList<Node> versions; // Array to store root of each version of segment tree

    public PersistentSegmentTree(int[] arr) {
        if (arr.length == 0) {
            throw new IllegalArgumentException("Input array cannot be empty.");
        }
        this.size = arr.length; // Array length for boundary checks
        this.versions = new ArrayList<>();
        Node root = build(arr, 0, size - 1); // Build initial segment tree.
        versions.add(root); // Store initial version of the tree
    }

    // Build segment tree from the input array
    public Node build(int[] arr, int start, int end) {
        if (start == end) { // Leaf node
            return new Node(arr[start]);
        }
        int mid = (start + end) / 2; // Split array into two halves
        // Create left and right child and create new node with sum of the left and right child values.
        Node leftChild = build(arr, start, mid);
        Node rightChild = build(arr, mid + 1, end);
        return new Node(leftChild.value + rightChild.value, leftChild, rightChild);
    }

    // Create a new version of segment tree with updated value at specified index.
    private Node update(Node previousRoot, int start, int end, int index, int newValue) {
        if (start == end) { // Leaf node, create a new node with the updated value
            return new Node(newValue);
        }

        int mid = (start + end) / 2; // Split array into two halves
        Node leftChild, rightChild;
        // Recursively update left or right child based on index, while sharing unchanged nodes.
        if (index <= mid) {
            leftChild = update(previousRoot.left, start, mid, index, newValue);
            rightChild = previousRoot.right;
        } else {
            rightChild = update(previousRoot.right, mid + 1, end, index, newValue);
            leftChild = previousRoot.left;
        }
        // Create new node with updated value, which is sum of left and right child values.
        return new Node(leftChild.value + rightChild.value, leftChild, rightChild);
    }

    // Public method to update segment tree.
    public int update(int version, int index, int newValue) {
        if (version < 0 || version >= versions.size()) {
            throw new IllegalArgumentException("Version number out of bounds.");
        }
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException("Index out of bounds.");
        }
        Node newRoot = update(versions.get(version), 0, size - 1, index, newValue);
        versions.add(newRoot); // Store new version of tree
        return versions.size() - 1; // Return index of new version
    }

    // Query sum of a range [ql, qr]
    private int query(Node root, int start, int end, int ql, int qr) {
        if (ql > end || qr < start) { // No overlap
            return 0;
        }
        if (ql <= start && end <= qr) { // Total overlap
            return root.value;
        }
        // Partial overlap, query both children and return sum of their results.
        int mid = (start + end) / 2;
        return query(root.left, start, mid, ql, qr) + query(root.right, mid + 1, end, ql, qr);
    }

    // Public method to query a specific version of segment tree.
    public int query(int version, int ql, int qr) {
        if (version < 0 || version >= versions.size()) {
            throw new IllegalArgumentException("Version number out of bounds.");
        }
        if (ql < 0 || qr >= size || ql > qr) {
            throw new IllegalArgumentException("Invalid Query Range.");
        }
        return query(versions.get(version), 0, size - 1, ql, qr);
    }

    // Helper method to get value at a specific index for a given version.
    public int getPoint(int version, int index) {
        return query(version, index, index); // Query the range [index, index] to get the point value
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        // Read N and Q (number of operations)
        String line = br.readLine();
        if (line == null) return;
        st = new StringTokenizer(line);
        int n = Integer.parseInt(st.nextToken());
        if (st.hasMoreTokens()) st.nextToken(); 

        // Read initial array values
        int[] arr = new int[n];
        st = new StringTokenizer(br.readLine());
        for (int i = 0; i < n; i++) {
            arr[i] = Integer.parseInt(st.nextToken());
        }

        PersistentSegmentTree pst = new PersistentSegmentTree(arr);

        // Process operations
        String opLine;
        while ((opLine = br.readLine()) != null) {
            st = new StringTokenizer(opLine);
            if (!st.hasMoreTokens()) break;

            String op = st.nextToken();
            if (op.equals("Q")) {
                // Format: Q version left right
                int version = Integer.parseInt(st.nextToken());
                int left = Integer.parseInt(st.nextToken());
                int right = Integer.parseInt(st.nextToken());
                System.out.println(pst.query(version, left, right));
            } else if (op.equals("U")) {
                // Format: U version index newValue 
                int version = Integer.parseInt(st.nextToken());
                int index = Integer.parseInt(st.nextToken());
                int newValue = Integer.parseInt(st.nextToken());
                
                pst.update(version, index, newValue);
            }
        }
    }
}
