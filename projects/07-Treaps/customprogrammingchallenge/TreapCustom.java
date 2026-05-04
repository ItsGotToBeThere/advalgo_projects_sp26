import java.util.Random;

public class TreapCustom {
    /*
    a treap is a binary search tree where each node also has a random priority.
    the tree is heap ordered by priority, while also maintaining the binary search tree property by key.
    insertions and deletions are done by splitting and merging the treap based on the priorities of the nodes.
    this allows for efficient insertions, deletions, and searches while keeping the tree balanced on average.
    */

    // we need a random number generator to assign priorities to the nodes
    private static final Random rand = new Random();

    // we simply define a node to represent the nodes in the treap
    static class Node {
        int key;
        int priority;
        Node left, right;
        int size = 1; // size of the subtree rooted at this node

        Node(int key) {
            this.key = key;
            this.priority = rand.nextInt();
        }
    }

    private Node root;

    // helper to safely get size
    private int size(Node n) {
        return (n == null) ? 0 : n.size;
    }

    // update the size after modifications
    private void update(Node n) {
        if (n != null) {
            n.size = 1 + size(n.left) + size(n.right);
        }
    }

    /*
    the split function takes a node and a key and will split the treap
    into two: one where all the elements are greater than the key we are at and one where
    the elements are less than or equal to the key we are at.
    the way it works is when we are at a node, if the key is less than or equal to the current nodes value,
    we split the nodes right subtree because there may be larger values that we need to split,
    if the key is greater than the current nodes value, we split the nodes left subtree because there may be smaller values that we need to split.
    so we will create two treaps both priority ordered, split by key, and we will return them as an array of nodes.
    */
    private Node[] split(Node root, int key) {
        if (root == null) {
            return new Node[]{null, null};
        }
        // less than or equal to the key, we split the right subtree
        if (root.key <= key) {
            Node[] res = split(root.right, key);
            // we want to set the root.right to be all the values in the right subtree that are less than or equal to the key we are at
            // this allows us to make sure none of the wrong values are in the wrong treap
            root.right = res[0];
            // need to update the size of the root after modifying its right
            update(root);
            // we return the root as the left treap and res[1] as the right treap
            // so when we recurse back up, we will have the correct treaps with the correct priorities and keys
            return new Node[]{root, res[1]};
        } else {
            Node[] res = split(root.left, key);
            // similar logic to the above case, just simply reversed because we are splitting the left subtree instead of the right subtree

            root.left = res[1];
            //  need to update the size of the root after modifying its left
            update(root);

            return new Node[]{res[0], root};
        }

    }

    /*
    these are custom functions for the hw
    */
    // find the k-th smallest element in the treap (1-indexed)
    // this is to find the kth smallest element in the treap, we can use the size of the left subtree to determine whether we need to go left, right, or if we found the kth smallest element
    // this is because if the size of the left subtree is k-1, then the current node is the kth smallest element, if the size of the left subtree is greater than or equal to k, then we need to go left, and if the size of the left subtree is less than k-1, then we need to go right and change k to be k - leftSize - 1 because we are skipping over the left subtree and the curr node and trying to find the kth smallest element in the right subtree
    private Node findkthsmallest(Node root, int k) {
        if (root == null || k <= 0 || k > size(root)) {
            return null; // k is out of bounds
        }
        int leftSize = size(root.left);
        if (k == leftSize + 1) {
            return root; // found the k-th smallest
        } else if (k <= leftSize) {
            return findkthsmallest(root.left, k); // search in the left subtree
        } else {
            return findkthsmallest(root.right, k - leftSize - 1); // search in the right subtree
        }

    }

    // count -> number of nodes in the treap less than or equal to a given key
    // this is simply looking for the number of nodes in the treap that are less than or equal to a given key
    // we do this by traversing the treap and if the current node is less than or equal to the key, all the nodes in the left are less but there could be nodes in the right that are not so we need to find all the nodes that are less than or equal to the key in the right subtree
    private int countLessThanOrEqual(Node root, int key) {
        if (root == null) {
            return 0; // base case: empty tree has 0 nodes
        }
        if (root.key <= key) {
            // current node is less than or equal to key, so count it and continue to the right
            return 1 + size(root.left) + countLessThanOrEqual(root.right, key);
        } else {
            // current node is greater than key, so continue to the left
            return countLessThanOrEqual(root.left, key);
        }
    }

    // find the range count of nodes in the treap between two keys (inclusive)
    // we need to use our countlessthanorequalto function to determine count(right) - count(left) to get the number of nodes in the range [low, high],

    private int countInRange(Node root, int low, int high) {
        if (low > high) return 0;

        return countLessThanOrEqual(root, high) - countLessThanOrEqual(root, low - 1);
    }
    /*
    end of custom functions
    */
    /*
    the point of the merge function is to take two treaps and merge them together
    in a manner that maintains the properties of the treap,
    so we will compare the priorities of the roots of the two treaps and merge the one with the higher priority as the new root,
    and then we will merge the other treap as a child of the new root,
    we will do this recursively until we have merged the two treaps together
    */
    private Node merge(Node left, Node right) {
        if (left == null) return right;
        if (right == null) return left;

        // the left treap has a higher priority than the right so we will call a merge on the left treaps right child
        // since that may have a higher priority than the right treap and we will return the left treap as the new root
        if (left.priority > right.priority) {
            left.right = merge(left.right, right); 
            // need to update the size of the left after modifying its right
            update(left);
            return left;
            // if the right priority is higher, choose the right root
        } else {
            right.left = merge(left, right.left);
            //  need to update the size of the right after modifying its left
            update(right);
            return right;
        }
    }

    /*
    this insert is straightforward, so if we hit the case in our recursion that the node were on's
    priority is less than the inserted node, we will split the current node by the inserted node's key and make the inserted node the new root of the subtree,
    and then we will set the left and right children of the new root to be the two treaps that we got from the split function,
    this allows us to insert our node while maintaining treap property
    */
    // we call insert separately because we need to create a new node

    public void insert(int key) {
        root = insert(root, new Node(key));
    }

    private Node insert(Node root, Node node) {
        if (root == null) return node;
        // check if node priority is > root priority,
        // if it is we call our split function and place node at current root
        if (node.priority > root.priority) {
            Node[] halves = split(root, node.key);

            node.left = halves[0];
            node.right = halves[1];
            // need to update the size of the node after setting its left and right
            update(node);

            return node;
        }
        // traverse the tree to maintain binary search tree order
        if (node.key < root.key) {
            root.left = insert(root.left, node);
        } else if (node.key > root.key) {
            root.right = insert(root.right, node);
        }
        // need to update the size of the root after modifying its left or right
        update(root);
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
        // need to update the size of the root after modifying its left or right
        update(root);
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
    private static String inorderString(TreapCustom t) {
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

    /*

    */
    public static void main(String[] args) {
        int passed = 0;
        int total = 7;

        // basic insert and search
        TreapCustom treaps = new TreapCustom();
        treaps.insert(5);
        treaps.insert(3);
        treaps.insert(7);
        treaps.insert(2);
        treaps.insert(4);
        boolean testinsert = treaps.search(5) && treaps.search(2) && !treaps.search(6);
        System.out.println("Test 1: insert/search is a" + (testinsert ? "PASS" : "FAIL"));
        if (testinsert) {
            passed++;
        }

        // make sure inorder is right
        // test inorder should be 2 3 4 5 7 because we inserted those values
        String expected = "2 3 4 5 7";
        String actual = inorderString(treaps);
        boolean testinorder = expected.equals(actual);
        System.out.println("Test 2: inorder is a " + (testinorder ? "PASS" : "FAIL"));
        if (testinorder) {
            passed++;
        }

        // delete
        // test that we went from 2 3 4 5 7 to 2 4 5 7 and that search for the deleted node returns false
        treaps.delete(3);
        String expecteddel = "2 4 5 7";
        String actualdel = inorderString(treaps);
        boolean testdel = expecteddel.equals(actualdel) && !treaps.search(3);
        System.out.println("Test 3: delete is a " + (testdel ? "PASS" : "FAIL"));
        if (testdel) {
            passed++;
        }

        // Test Case 4: if we try to delete something that doesnt exist it wont change
        treaps.delete(42);
        String expected4 = "2 4 5 7";
        String actual4 = inorderString(treaps);
        boolean test4 = expected4.equals(actual4);
        System.out.println("Test 4: delete something that doesnt exist  " + (test4 ? "PASS" : "FAIL") + " (got: " + actual4 + ")");
        if (test4) {
            passed++;
        }

        // Test Case 5: KTH smallest (after delete 3)
        boolean test5 = treaps.findkthsmallest(treaps.root, 1).key == 2
                && treaps.findkthsmallest(treaps.root, 2).key == 4
                && treaps.findkthsmallest(treaps.root, 3).key == 5
                && treaps.findkthsmallest(treaps.root, 4).key == 7
                && treaps.findkthsmallest(treaps.root, 5) == null;
        System.out.println("Test 5: kth smallest  " + (test5 ? "PASS" : "FAIL"));
        if (test5) {
            passed++;
        }

        // Test Case 6: COUNT <= x
        boolean test6 = treaps.countLessThanOrEqual(treaps.root, 1) == 0
                && treaps.countLessThanOrEqual(treaps.root, 2) == 1
                && treaps.countLessThanOrEqual(treaps.root, 4) == 2
                && treaps.countLessThanOrEqual(treaps.root, 5) == 3
                && treaps.countLessThanOrEqual(treaps.root, 6) == 3
                && treaps.countLessThanOrEqual(treaps.root, 7) == 4;
        System.out.println("Test 6: count <= x  " + (test6 ? "PASS" : "FAIL"));
        if (test6) {
            passed++;
        }

        // Test Case 7: RANGE count
        boolean test7 = treaps.countInRange(treaps.root, 0, 1) == 0
                && treaps.countInRange(treaps.root, 2, 2) == 1
                && treaps.countInRange(treaps.root, 2, 4) == 2
                && treaps.countInRange(treaps.root, 3, 6) == 2
                && treaps.countInRange(treaps.root, 4, 7) == 3;
        System.out.println("Test 7: range count  " + (test7 ? "PASS" : "FAIL"));
        if (test7) {
            passed++;
        }

        // test that all of our cases passed and return to user!
        System.out.println("\nwhen running my treaps you got: " + passed + " / " + total + " tests passed.");
        if (passed != total) {
            System.exit(1);
        }
    }
}
