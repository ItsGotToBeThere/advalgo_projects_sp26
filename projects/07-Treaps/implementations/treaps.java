import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class treaps {
    /*
    a treap is a binary search tree where each node also has a random priority.
    the tree is heap ordered by priority, while also maintaining the binary search tree property by key.
    insertions and deletions are done by splitting and merging the treap based on the priorities and keys of the nodes.
    this allows for efficient insertions, deletions, and searches while keeping the tree balanced on average.
    */

    // we need a random number generator to assign priorities to the nodes
    private static final Random rand = new Random();

    // we simply define a node to represent the nodes in the treap
    static class Node {
        int key;
        int priority;
        Node left, right;

        Node(int key) {
            this.key = key;
            this.priority = rand.nextInt();
        }
    }

    private Node root;

    /*
    Splits the treap into two treaps by key:
    - left contains all keys <= key
    - right contains all keys > key

    If root.key <= key, root belongs in the left treap, but some nodes in root.right may still be <= key,
    so we split root.right. Otherwise root belongs in the right treap, and we split root.left.
    */
    private Node[] split(Node root, int key) {
        if (root == null) {
            return new Node[]{null, null};
        }
        // root less than or equal to the key, we split the right subtree
        if (root.key <= key) {
            Node[] res = split(root.right, key);
            // we want to set the root.right to be all the values in the right subtree that are less than or equal to the key we are at
            // this allows us to make sure none of the wrong values are in the wrong treap
            root.right = res[0];
            // we return the root as the left treap and res[1] as the right treap
            // so when we recurse back up, we will have the correct treaps with the correct priorities and keys
            return new Node[]{root, res[1]};
        } else {
            Node[] res = split(root.left, key);
            // similar logic to the above case, just simply reversed because we are splitting the left subtree instead of the right subtree

            root.left = res[1];

            return new Node[]{res[0], root};
        }
    }

    /*
    Assumes all keys in left are <= all keys in right
    the point of the merge function is to take two treaps and merge them together
    in a manner that maintains the properties of the treap,
    so we will compare the priorities of the roots of the two treaps and merge the one with the higher priority as the new root,
    and then we will merge the other treap as a child of the new root,
    we will do this recursively until we have merged the two treaps together
    */
    private Node merge(Node left, Node right) {
        if (left == null) return right;
        if (right == null) return left;

        // we call merge recursively on the child of the root with the higher priority, and then return the new root of the merged treap
        // if the left priority is higher, choose the left root 
        // we will merge the right treap as the right child of the left root, and then return the left root as the new root of the merged treap
        if (left.priority > right.priority) {
            left.right = merge(left.right, right);
            return left;
        // if the right priority is higher, choose the right root
        } else {
            right.left = merge(left, right.left);
            return right;
        }
    }

    /*
    this insert is straightforward, so if we hit the case in our recursion that the node were on's
    priority is less than the inserted node, we will split the current node by the inserted node's key and make the inserted node the new root of the subtree,
    and then we will set the left and right children of the new root to be the two treaps that we got from the split function 
    this allows us to insert our node while maintaining treap property
    */
    // we call insert separately because we need to create a new node

    public void insert(int key) {
        root = insert(root, new Node(key));
    }

    private Node insert(Node root, Node node) {
        if (root == null) return node;
        // check if node priority is > root priority,
        // if it is we call our split 
        // the point of calling split is to maintain the treap properties, we will set the left and right children of the new root to be the two treaps that we got from the split function, 
        if (node.priority > root.priority) {
            Node[] halves = split(root, node.key);

            node.left = halves[0];
            node.right = halves[1];

            return node;
        }
        // traverse the tree to maintain binary search tree order
        if (node.key < root.key) {
            root.left = insert(root.left, node);
        } else if (node.key > root.key) {
            root.right = insert(root.right, node);
        }
        return root;
    }

    /*
    the delete function is easy because all we do is merge
    the left and right subtrees of the node we want to delete,
    this merge will maintain the treap properties and we will return the new root of the merged treap,
    skipping over the node we want to delete, and then return all the way back up with the new treap
    */

    // again call it separately, same reason as insert
    public void delete(int key) {
        root = delete(root, key);
    }

    private Node delete(Node root, int key) {
        if (root == null) return null;

        // if found, remove node by merging its children
        if (root.key == key) {
            return merge(root.left, root.right);
        }

        // traverse to locate node, then refresh pointer
        if (key < root.key) {
            root.left = delete(root.left, key);
        } else {
            root.right = delete(root.right, key);
        }

        return root;
    }

    // search for key in treap (BST traversal)
    public boolean search(int key) {
        return search(root, key);
    }

    private boolean search(Node root, int key) {
        // normal search
        if (root == null) return false;
        if (root.key == key) return true;
        if (key < root.key) {
            return search(root.left, key);
        } else {
            return search(root.right, key);
        }
    }

    // inorder traversal for sorted output (by key)
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

    // this function is just to help us test because 
    // our current inorder function only prints the values but we need a comparison
    private static String inorderString(treaps t) {
        return inorderString(t.root).trim();
    }

    private static String inorderString(Node root) {
        if (root == null) return "";
        String left = inorderString(root.left);
        String node = Integer.toString(root.key);
        String right = inorderString(root.right);

        String result = "";
        if (!left.isEmpty()) {
            result += left + " ";
        }
        result += node;
        if (!right.isEmpty()) result += " " + right;
        return result;
    }

    private static boolean runTest(String inputFile, String outputFile) {
        treaps treap = new treaps();
        List<String> actualOutput = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 0 || parts[0].isEmpty()) continue;

                String cmd = parts[0];
                if (cmd.equals("insert")) {
                    treap.insert(Integer.parseInt(parts[1]));
                } else if (cmd.equals("erase")) {
                    treap.delete(Integer.parseInt(parts[1]));
                } else if (cmd.equals("search")) {
                    boolean result = treap.search(Integer.parseInt(parts[1]));
                    actualOutput.add(Boolean.toString(result).toLowerCase());
                } else if (cmd.equals("inorder")) {
                    actualOutput.add(inorderString(treap));
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read input file: " + e.getMessage());
            return false;
        }

        List<String> expectedOutput = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(outputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    expectedOutput.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read output file: " + e.getMessage());
            return false;
        }

        boolean passed = actualOutput.equals(expectedOutput);
        System.out.println(inputFile + ": " + (passed ? "PASS" : "FAIL"));
        if (!passed) {
            System.out.println("Expected:");
            expectedOutput.forEach(System.out::println);
            System.out.println("Got:");
            actualOutput.forEach(System.out::println);
        }

        return passed;
    }

    public static void main(String[] args) {
        Path baseDir = Paths.get(System.getProperty("user.dir"));
        Path ioDir = baseDir.resolve("io");

        int total = 3;
        int passed = 0;

        for (int i = 1; i <= total; i++) {
            String inFile = ioDir.resolve("sample.in." + i).toString();
            String outFile = ioDir.resolve("sample.out." + i).toString();
            if (runTest(inFile, outFile)) {
                passed++;
            }
        }

        System.out.println();
        System.out.println("Final: " + passed + " / " + total + " tests passed.");
        if (passed != total) {
            System.exit(1);
        }
    }
}
