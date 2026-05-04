package solutions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class TreapCustom {
    /*
    A treap is a binary search tree where each node also has a random priority.
    The tree is heap ordered by priority, while also maintaining the binary search tree property by key.
    This allows for efficient insertions, deletions, and searches while keeping the tree balanced on average.

    For the new version of the assignment, we augment every node with:
    - size of the subtree
    - sum of all keys in the subtree

    This supports:
    - KTH(k)
    - COUNT(x)
    - RANGE(l, r)
    - SUM(l, r)

    We also change main() so that instead of hardcoding a few tests,
    it reads all *_in.txt files from a folder called inputoutputtests,
    runs each one, and compares the result to the matching *_out.txt file.
    */

    // we need a random number generator to assign priorities to the nodes
    private static final Random rand = new Random();

    // define a node for the treap
    static class Node {
        int key;
        int priority;
        Node left, right;

        int size;          // number of nodes in this subtree
        long subtreeSum;   // sum of all keys in this subtree

        Node(int key) {
            this.key = key;
            this.priority = rand.nextInt();
            this.size = 1;
            this.subtreeSum = key;
        }
    }

    private Node root;

    // helper to safely get size
    private int size(Node n) {
        return (n == null) ? 0 : n.size;
    }

    // helper to safely get subtree sum
    private long sum(Node n) {
        return (n == null) ? 0L : n.subtreeSum;
    }

    /*
    update all augmented metadata after changes

    Before, we only needed to update subtree size.
    Now, for the SUM query, we also need to maintain subtreeSum.

    Any time we change left/right child pointers, we should call update().
    */
    private void update(Node n) {
        if (n != null) {
            n.size = 1 + size(n.left) + size(n.right);
            n.subtreeSum = (long) n.key + sum(n.left) + sum(n.right);
        }
    }

    /*
    Splits the treap into two treaps by key:
    - left contains all keys <= key
    - right contains all keys > key

    If root.key <= key, root belongs in the left treap,
    but some nodes in root.right may still be <= key, so we split root.right.

    Otherwise root belongs in the right treap, and we split root.left.
    */
    private Node[] split(Node root, int key) {
        if (root == null) {
            return new Node[]{null, null};
        }

        // root belongs in the left treap, so split the right subtree
        if (root.key <= key) {
            Node[] res = split(root.right, key);

            // res[0] should stay with root as its new right child
            root.right = res[0];

            // refresh metadata after changing root.right
            update(root);

            // root is part of the left result, res[1] is the right result
            return new Node[]{root, res[1]};
        } else {
            // root belongs in the right treap, so split the left subtree
            Node[] res = split(root.left, key);

            // res[1] should stay with root as its new left child
            root.left = res[1];

            // refresh metadata after changing root.left
            update(root);

            // res[0] is the left result, root is the right result
            return new Node[]{res[0], root};
        }
    }

    /*
    Assumes all keys in left are <= all keys in right.

    The point of merge is to take two treaps and combine them
    while maintaining both:
    - BST order by key
    - heap order by priority

    We choose the root with the higher priority, then recursively merge the remaining side.
    */
    private Node merge(Node left, Node right) {
        if (left == null) return right;
        if (right == null) return left;

        // if left root has higher priority, it becomes the new root
        if (left.priority > right.priority) {
            left.right = merge(left.right, right);

            // update after changing child pointer
            update(left);
            return left;
        } else {
            // otherwise right root becomes the new root
            right.left = merge(left, right.left);

            // update after changing child pointer
            update(right);
            return right;
        }
    }

    /*
    insert is done by either:
    - making the new node the root of a subtree if its priority is higher, using split
    - or recursively descending like a BST if its priority is lower

    Since the assignment says inserted values are unique, duplicate keys are ignored.
    */
    public void insert(int key) {
        root = insert(root, new Node(key));
    }

    private Node insert(Node root, Node node) {
        if (root == null) return node;

        // if the new node has higher priority, it becomes root of this subtree
        if (node.priority > root.priority) {
            Node[] halves = split(root, node.key);

            node.left = halves[0];
            node.right = halves[1];

            // update after attaching children
            update(node);
            return node;
        }

        // otherwise, descend by BST order
        if (node.key < root.key) {
            root.left = insert(root.left, node);
        } else if (node.key > root.key) {
            root.right = insert(root.right, node);
        }
        // if equal, do nothing since duplicates are ignored

        // refresh metadata after modifying a child
        update(root);
        return root;
    }

    /*
    delete works by finding the node to remove, then merging its left and right children.
    This skips over the deleted node while preserving treap properties.
    */
    public void delete(int key) {
        root = delete(root, key);
    }

    private Node delete(Node root, int key) {
        if (root == null) return null;

        // if found, remove it by merging its two children
        if (root.key == key) {
            return merge(root.left, root.right);
        }

        // otherwise recurse to locate it
        if (key < root.key) {
            root.left = delete(root.left, key);
        } else {
            root.right = delete(root.right, key);
        }

        // refresh metadata after modifying a child
        update(root);
        return root;
    }

    // search is just normal BST traversal
    public boolean search(int key) {
        return search(root, key);
    }

    private boolean search(Node root, int key) {
        if (root == null) return false;
        if (root.key == key) return true;

        if (key < root.key) {
            return search(root.left, key);
        } else {
            return search(root.right, key);
        }
    }

    /*
    these are the custom query functions for the assignment
    */

    // KTH(k): find the k-th smallest element in the treap, 1-indexed
    public Integer kth(int k) {
        Node ans = findKthSmallest(root, k);
        return (ans == null) ? null : ans.key;
    }

    /*
    We use subtree sizes to find the k-th smallest.
    Let leftSize = size(root.left).

    - if k == leftSize + 1, current root is the answer
    - if k <= leftSize, answer is in the left subtree
    - otherwise answer is in the right subtree, but we skip leftSize + 1 nodes
    */
    private Node findKthSmallest(Node root, int k) {
        if (root == null || k <= 0 || k > size(root)) {
            return null;
        }

        int leftSize = size(root.left);

        if (k == leftSize + 1) {
            return root;
        } else if (k <= leftSize) {
            return findKthSmallest(root.left, k);
        } else {
            return findKthSmallest(root.right, k - leftSize - 1);
        }
    }

    // COUNT(x): number of keys <= x
    public int countLessThanOrEqual(int key) {
        return countLessThanOrEqual(root, key);
    }

    /*
    If root.key <= key, then:
    - every node in root.left is also <= key
    - current root counts too
    - we still need to search root.right

    If root.key > key, we must search only the left subtree.
    */
    private int countLessThanOrEqual(Node root, int key) {
        if (root == null) {
            return 0;
        }

        if (root.key <= key) {
            return 1 + size(root.left) + countLessThanOrEqual(root.right, key);
        } else {
            return countLessThanOrEqual(root.left, key);
        }
    }

    // RANGE(l, r): number of keys in [l, r]
    public int countInRange(int low, int high) {
        if (low > high) return 0;

        // count <= high minus count <= low-1
        return countLessThanOrEqual(root, high) - countLessThanOrEqual(root, low - 1);
    }

    /*
    SUM queries are the new extension.

    We use subtreeSum similarly to how COUNT uses subtree size.

    sumLessThanOrEqual(x) gives the sum of all keys <= x.
    Then:
        SUM(l, r) = sum<=r - sum<=(l-1)
    */

    // SUM of all keys <= key
    private long sumLessThanOrEqual(Node root, int key) {
        if (root == null) {
            return 0L;
        }

        if (root.key <= key) {
            // everything in the left subtree is <= key, so we can take its whole sum
            // also include the current root, then continue to the right
            return sum(root.left) + root.key + sumLessThanOrEqual(root.right, key);
        } else {
            // current root is too large, so only search left
            return sumLessThanOrEqual(root.left, key);
        }
    }

    // SUM(l, r): sum of all keys in [l, r]
    public long sumInRange(int low, int high) {
        if (low > high) return 0L;

        return sumLessThanOrEqual(root, high) - sumLessThanOrEqual(root, low - 1);
    }

    /*
    inorder traversal is still useful for debugging because it prints keys in sorted order
    */
    public void inorder() {
        inorder(root);
        System.out.println();
    }

    private void inorder(Node root) {
        if (root == null) return;
        inorder(root.left);
        System.out.print(root.key + " ");
        inorder(root.right);
    }

    /*
    This helper runs one test case file.

    Each input file is expected to look like:
    Q
    OP ...
    OP ...
    ...

    We create a fresh treap for each file, execute all operations,
    and collect the outputs from query operations only.
    */
    private static String runOneInputFile(Path inputFile) throws IOException {
        List<String> lines = Files.readAllLines(inputFile, StandardCharsets.UTF_8);

        TreapCustom treap = new TreapCustom();
        StringBuilder out = new StringBuilder();

        // skip any leading blank lines just in case
        int lineIndex = 0;
        while (lineIndex < lines.size() && lines.get(lineIndex).trim().isEmpty()) {
            lineIndex++;
        }

        // empty file means empty output
        if (lineIndex >= lines.size()) {
            return "";
        }

        int q = Integer.parseInt(lines.get(lineIndex).trim());
        lineIndex++;

        // process exactly q operations, skipping blank lines if they appear
        for (int i = 0; i < q && lineIndex < lines.size(); i++, lineIndex++) {
            String line = lines.get(lineIndex).trim();

            if (line.isEmpty()) {
                i--;
                continue;
            }

            String[] parts = line.split("\\s+");
            String op = parts[0];

            switch (op) {
                case "INSERT": {
                    int x = Integer.parseInt(parts[1]);
                    treap.insert(x);
                    break;
                }

                case "DELETE": {
                    int x = Integer.parseInt(parts[1]);
                    treap.delete(x);
                    break;
                }

                case "SEARCH": {
                    int x = Integer.parseInt(parts[1]);
                    out.append(treap.search(x) ? "true" : "false").append('\n');
                    break;
                }

                case "KTH": {
                    int k = Integer.parseInt(parts[1]);
                    Integer ans = treap.kth(k);

                    // if a test asks for invalid kth, we print null
                    out.append(ans == null ? "null" : ans).append('\n');
                    break;
                }

                case "COUNT": {
                    int x = Integer.parseInt(parts[1]);
                    out.append(treap.countLessThanOrEqual(x)).append('\n');
                    break;
                }

                case "RANGE": {
                    int l = Integer.parseInt(parts[1]);
                    int r = Integer.parseInt(parts[2]);
                    out.append(treap.countInRange(l, r)).append('\n');
                    break;
                }

                case "SUM": {
                    int l = Integer.parseInt(parts[1]);
                    int r = Integer.parseInt(parts[2]);
                    out.append(treap.sumInRange(l, r)).append('\n');
                    break;
                }

                default:
                    throw new IllegalArgumentException(
                            "Unknown operation in " + inputFile.getFileName() + ": " + op
                    );
            }
        }

        return normalizeOutput(out.toString());
    }

    /*
    normalize output so that tiny formatting differences do not break tests

    We:
    - remove carriage returns
    - trim each line
    - drop blank lines
    - join everything back with \n
    */
    private static String normalizeOutput(String s) {
        return Arrays.stream(s.replace("\r", "").split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.joining("\n"));
    }

    // compare two outputs after normalization
    private static boolean compareOutputs(String actual, String expected) {
        return normalizeOutput(actual).equals(normalizeOutput(expected));
    }

    /*
    This is the folder-based test runner.

    It:
    - looks inside inputoutputtests
    - finds every file ending in _in.txt
    - matches it to the corresponding _out.txt
    - runs the input file
    - compares actual vs expected
    - prints PASS/FAIL
    */
    private static void runAllFolderTests(String folderName) throws IOException {
        Path folder = Paths.get(folderName);

        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            System.out.println("Test folder not found: " + folder.toAbsolutePath());
            System.out.println("Make sure the folder '" + folderName + "' exists where you run the program.");
            return;
        }

        List<Path> inputFiles;
        try (var stream = Files.list(folder)) {
            inputFiles = stream
                    .filter(p -> Files.isRegularFile(p))
                    .filter(p -> p.getFileName().toString().endsWith("_in.txt"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .collect(Collectors.toList());
        }

        if (inputFiles.isEmpty()) {
            System.out.println("No *_in.txt files found in: " + folder.toAbsolutePath());
            return;
        }

        int passed = 0;
        int total = 0;

        for (Path inputFile : inputFiles) {
            total++;

            String inputName = inputFile.getFileName().toString();
            String outputName = inputName.replaceFirst("_in\\.txt$", "_out.txt");
            Path outputFile = folder.resolve(outputName);

            // if matching output file is missing, that test automatically fails
            if (!Files.exists(outputFile)) {
                System.out.println("[FAIL] " + inputName + " -> missing expected file " + outputName);
                continue;
            }

            String actual = runOneInputFile(inputFile);
            String expected = normalizeOutput(Files.readString(outputFile, StandardCharsets.UTF_8));

            boolean ok = compareOutputs(actual, expected);

            if (ok) {
                passed++;
                System.out.println("[PASS] " + inputName);
            } else {
                System.out.println("[FAIL] " + inputName);

                System.out.println("  Expected:");
                if (expected.isEmpty()) {
                    System.out.println("  <empty>");
                } else {
                    for (String line : expected.split("\n")) {
                        System.out.println("  " + line);
                    }
                }

                System.out.println("  Actual:");
                if (actual.isEmpty()) {
                    System.out.println("  <empty>");
                } else {
                    for (String line : actual.split("\n")) {
                        System.out.println("  " + line);
                    }
                }
            }
        }

        // final summary
        System.out.println();
        System.out.println("Passed " + passed + " / " + total + " test files.");
    }

    public static void main(String[] args) {
        String folderName = "../io";

        // allow user to optionally pass a folder path on the command line
        if (args.length >= 1) {
            folderName = args[0];
        }

        try {
            runAllFolderTests(folderName);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}