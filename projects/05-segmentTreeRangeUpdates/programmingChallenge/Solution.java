/*  =============================================================================
 Segment Tree with Lazy Propagation - Range Updates, Range Sum Queries
 Owen Badgley
 =============================================================================

 Problem Statement:
   Given an array of integers, support two operations efficiently:
     1. Range Add Update: add a value to every element in a[left..right]
     2. Range Sum Query:  return the sum of a[left..right]
   Both operations must run in O(log n) time.

   No leaf should go over the capacity that it is given at the start of the problem. 
   A range update that has one value go over should add that maximum value to all nodes in the range.

 Input Format (stdin):
   Line 1:  N                          - number of elements
   Line 2:  a[0] a[1] ... a[N-1]       - initial array values
   Line 3:  a[0] a[1] ... a[N-1]       - array capacities
   Line 4:  Q                          - number of operations
   Next Q lines, each one of:
     U left right val                  - add val to every element in [left..right]
     Q left right                      - print the sum of a[left..right]
  All indices are 0-based, inclusive.

 Output Format (stdout):
   One integer per Q-type query, each on its own line.

 Complexity:
   Build:  O(n)
   Update: O(log n) per operation
   Query:  O(log n) per operation
   Space:  O(n)  [technically 4n nodes in the implicit tree array]
 ============================================================================
*/

import java.util.Scanner;

public class Solution {
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);

        int size = scan.nextInt();
        int[] valArr = new int[size];

        for (int i = 0; i < size; i++){
            valArr[i] = scan.nextInt();
        }

        int[] capArr = new int[size];

        for (int i = 0; i < size; i++){
            capArr[i] = scan.nextInt();
        }

        SegTree tree = new SegTree(valArr, capArr, size);

        int queries = scan.nextInt();

        StringBuilder results = new StringBuilder();
        
        for (int i = 0; i < queries; i++){
            //tree.printTree();
            char type = scan.next().charAt(0);
            if (type == 'U'){
                if (type == 'U'){
                int l = scan.nextInt();
                int r = scan.nextInt();
                int val = scan.nextInt();
                tree.update(l, r, val);
            }
            }
            else if (type == 'Q'){
                results.append(tree.sum(scan.nextInt(), scan.nextInt()));
                results.append('\n');
            }
        }
        System.out.print(results.toString());
        scan.close();
    }

    static class SegTreeNode{
        
        // store all the stuff a node needs - left and right index, sum, and left and right children, 
        int left, right;
        int val;

        // NEW FOR PC IMPLEMENTATION: capacity, minimum open capacity at a lower node
        int cap;
        int minRemaining;

        // NEW THING HERE!! each node has to keep this value that indicates how much to add to every node below it. This makes it so that range updates can be O(log(n)), because it gives them functionally the same implementation as the sum. Each node just has 2 sums now.
        int buffer;

        SegTreeNode leftChild;
        SegTreeNode rightChild;

        // create a node
        public SegTreeNode(int left, int right) {
            this.left = left;
            this.right = right;
            this.val = 0;
            this.cap = 0;
            this.minRemaining = 0;
            this.leftChild = null;
            this.rightChild = null;
            this.buffer = 0;
        }
    }

    static class SegTree{

        SegTreeNode root;

        // given a non-leaf node, set its variables based on the children
        private void merge(SegTreeNode curNode){
            if (curNode.rightChild == null || curNode.leftChild == null){
                return;
            }
            curNode.val = curNode.leftChild.val + curNode.rightChild.val;
            curNode.minRemaining = Math.min(curNode.leftChild.minRemaining, curNode.rightChild.minRemaining);
        }

        public int sum(int left, int right){
            // ignore out of bounds stuff for now
            if (left < 0){
                left = 0;
            }
            if (right > root.right){
                right = root.right;
            }

            // call helper on root node to start recursion
            return sumHelper(left, right, root);
        }
        
        // Recursive helper function for sum
        private int sumHelper(int left, int right, SegTreeNode curNode){

            pushDown(curNode);

            int mid = curNode.left + ((curNode.right - curNode.left) / 2);

            // check if the range is the same as the size of the node we are at
            if (left == curNode.left && right == curNode.right){
                return curNode.val;
            }
            // if not, check if the query is entirely on the left side of the node
            else if (right <= mid){
                return sumHelper(left, right, curNode.leftChild);
            }
            // or maybe the right side?
            else if (left > mid){
                return sumHelper(left, right, curNode.rightChild);
            }
            // if it splits the middle, call the recursive call on both halves of the query
            else{
                return sumHelper(left, mid, curNode.leftChild) + sumHelper(mid + 1, right, curNode.rightChild);
            }
        }

        public int min(int left, int right){
            if (left < 0){
                left = 0;
            }
            if (right > root.right){
                right = root.right;
            }

            // call helper on root node to start recursion
            return minHelper(left, right, root);
        }

        private int minHelper(int left, int right, SegTreeNode curNode){

            pushDown(curNode);

            int mid = curNode.left + ((curNode.right - curNode.left) / 2);

            // check if the range is the same as the size of the node we are at
            if (left == curNode.left && right == curNode.right){
                return curNode.minRemaining;
            }
            // if not, check if the query is entirely on the left side of the node
            else if (right <= mid){
                return minHelper(left, right, curNode.leftChild);
            }
            // or maybe the right side?
            else if (left > mid){
                return minHelper(left, right, curNode.rightChild);
            }
            // if it splits the middle, call the recursive call on both halves of the query
            else{
                return Math.min(
                    minHelper(left, mid, curNode.leftChild),
                    minHelper(mid + 1, right, curNode.rightChild)
                );
            }
        }

        private void pushDown(SegTreeNode node) {
            // if there is a buffer in one of your nodes, you have to push it down to keep your sums up to date (only doing it when the node is already being used means we can keep our juicy O(log(n)) for everything)
            if (node.buffer != 0){
                // add the value of all of the children getting the buffer
                int len = node.right - node.left + 1;
                node.val += node.buffer * len;
                node.minRemaining -= node.buffer;
                // push the buffer down to the children, but don't recurse- would lose time
                if (node.left != node.right) {
                    node.leftChild.buffer += node.buffer;
                    node.rightChild.buffer += node.buffer;
                }
                // this line is self explanatory
                node.buffer = 0;
            }
        }

        // range update implementation -- very very similar to sum
        public void update(int left, int right, int value){
            // ignore out of bounds stuff for now
            if (left < 0){
                left = 0;
            }
            if (right > root.right){
                right = root.right;
            }
            int minRem = min(left, right);
            int actual = Math.min(value, minRem);
            if (actual > 0) {
                updateHelper(left, right, actual, root);
            }
        }

        public void updateHelper(int left, int right, int value, SegTreeNode curNode){

            pushDown(curNode);
            // no overlap
            if (curNode.right < left || curNode.left > right) return;
            // otherwise, if the range matches, just update the buffer value for the current node and you're done
            if (left <= curNode.left && curNode.right <= right){
                curNode.buffer += value;
                pushDown(curNode);
                return;
            }
            // if it splits the middle, call the recursive call on both halves of the query
            int mid = (curNode.left + curNode.right) / 2;
            // curNode.val += (1 + right - left) * value;
            updateHelper(left, Math.min(right, mid), value, curNode.leftChild);
            updateHelper(Math.max(left, mid + 1), right, value, curNode.rightChild);

            merge(curNode);
        }

        public void printTree() {
            printNode(root, 0);
        }

        private void printNode(SegTreeNode node, int depth) {
            if (node == null) return;

            // indentation for tree structure
            String indent = "  ".repeat(depth);

            // print current node state
            System.out.println(indent +
                "[" + node.left + "," + node.right + "] " +
                "val=" + node.val + 
                " buffer=" + node.buffer + 
                " minRem=" + node.minRemaining
            );

            // force lazy push visualization (optional but VERY useful)
            if (node.leftChild != null || node.rightChild != null) {
                printNode(node.leftChild, depth + 1);
                printNode(node.rightChild, depth + 1);
            }
        }
                
        // start recursive construction of the tree
        public SegTree(int[] valArr, int[] capArr, int size){
            root = new SegTreeNode(0, size - 1);
            SegTreeRecurse(valArr, capArr, 0, size - 1, root);
        }
        // create each node of the tree recursively
        private void SegTreeRecurse(int[] valArr, int[] capArr, int left, int right, SegTreeNode curNode){

            // if left and right are the same, reached base case
            if (left == right){
                curNode.val = valArr[left];
                curNode.cap = capArr[left];
                curNode.minRemaining = curNode.cap - curNode.val;
            }
            // otherwise, recursively create the children and give them the info they need, then merge them to get the value for this node
            else{
                int midpoint = left + ((right - left) / 2);
                curNode.leftChild = new SegTreeNode(left, midpoint);
                curNode.rightChild = new SegTreeNode(midpoint + 1, right);
                SegTreeRecurse(valArr, capArr, left, midpoint, curNode.leftChild);
                SegTreeRecurse(valArr, capArr, midpoint + 1, right, curNode.rightChild);
                merge(curNode);
            }

        }
    }
}