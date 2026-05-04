import random
import os

"""
Treap (tree + heap) - a randomized binary search tree with heap properties
a treap combines BST ordering with random priorities to maintain balance
"""

# a simple node in a treap structure
class TreapNode:
    def __init__(self, key):
        self.key = key

        # random priority for every node in order to maintain the heap property
        self.priority = random.random()  
        self.left = None
        self.right = None

# the actual treap data structure implementation
class Treap:
    
    def __init__(self):
        self.root = None

    """
    splitting the treap into two parts based on the key using simple recursion
    the left treap will contain all keys <= key and the right treap will contain all keys > key
    this is a helper function for insertion and deletion to maintain the treap properties
    """
    def split(self, root, key):
        if root is None:
            return (None, None)
        """
        if the root's key is less than or equal to the key, then the split must be in the right subtree and the resulting left treap 
        will include the original left subtree as well as a portion of the original right subtree.

        if the root's key is greater than the key, then the split must be in the left subtree and the resulting right treap will include
        the original right subtree as well as a portion of the original left subtree.
        """
        if root.key <= key:
            root.right, right = self.split(root.right, key)
            return (root, right)
        else:
            left, root.left = self.split(root.left, key)
            return (left, root)
    
    """
    merge two treaps and return the resulting treap. 
    merge operation is based on the priorities of the root nodes of the two treaps since
    the treap with the higher priority root becomes the new root of the merged treap, and the other treap is merged as a subtree of the new root. 
    """
    def merge(self, left, right):
        if left is None:
            return right
        if right is None:
            return left
        
        # here we recursively merge the treaps based on their priorities
        if left.priority > right.priority:
            left.right = self.merge(left.right, right)
            return left
        else:
            right.left = self.merge(left, right.left)
            return right

    """
    inserting a key into the treap is done by first splitting the treap into two parts based on the key, then creating a new node with the key and merging it with the two parts.
    this ensures that the new node is placed in the correct position according to the BST property, and the merge operation maintains the heap property of the treap.
    this is indeed a different approach and implementation of insert, yet it achieves the insertion in a less BST-like manner
    """
    def insert(self, key):
        if self.search(key):
            return

        new_node = TreapNode(key)
        left, right = self.split(self.root, key)
        temp = self.merge(left, new_node)
        self.root = self.merge(temp, right)

    """
    removes a key by finding the node and replacing it with a merge of its children.
    instead of manually restructuring, we let merge decide the new root based on priorities.
    """
    def erase(self, key):
        self.root = self._erase_helper(self.root, key)
    
    """
    walks down the tree like a normal BST search until the target node is found.
    once found, the node is removed by merging its left and right subtrees together,
    effectively "skipping over" the node while keeping the tree connected.
    """
    def _erase_helper(self, node, key):
        if node is None:
            return None
        
        # move left or right depending on where the key should be
        if key < node.key:
            node.left = self._erase_helper(node.left, key)
        elif key > node.key:
            node.right = self._erase_helper(node.right, key)
        else:
            # found the node: replace it by merging its two children
            return self.merge(node.left, node.right)
        
        return node 

    """
    combines two treaps into one by recursively choosing which root should come first.
    the structure is rebuilt by always keeping the higher-priority node above.
    """
    def unite(self, other):
        self.root = self._unite_helper(self.root, other.root)

    """
    compares the roots of both trees and picks the one that should be higher.
    then recursively attaches the remaining part of the other tree into the correct subtree.
    """
    def _unite_helper(self, node1, node2):
        if node1 is None:
            return node2
        if node2 is None:
            return node1

        if node1.priority < node2.priority:
            node1, node2 = node2, node1

        left2, right2 = self.split(node2, node1.key)
        node1.left = self._unite_helper(node1.left, left2)
        node1.right = self._unite_helper(node1.right, right2)
        return node1
    
    """
    checks whether a key exists by following the same path a BST would take.
    priorities are irrelevant here since they only affect structure, not lookup direction.
    """
    def search(self, key):
        return self._search_helper(self.root, key)
    
    """
    at each step, decide whether to go left or right based on the key.
    stop early if the value is found, or return false if a leaf is reached.
    """
    def _search_helper(self, node, key):
        if node is None:
            return False
        
        if key == node.key:
            return True
        elif key < node.key:
            return self._search_helper(node.left, key)
        else:
            return self._search_helper(node.right, key)

    """
    returns all keys in sorted order by performing an inorder traversal.
    this visits nodes in increasing order of their keys, and this is useful for debugging
    """
    def inorder(self):
        result = []
        self._inorder_helper(self.root, result)
        return result
    
    """
    standard inorder traversal: left subtree, current node, right subtree.
    values are appended as we visit each node.
    """
    def _inorder_helper(self, node, result):
        if node is not None:
            self._inorder_helper(node.left, result)
            result.append(node.key)
            self._inorder_helper(node.right, result)


def inorder_string(treap):
        return " ".join(str(x) for x in treap.inorder())

# testing framework to run any input file
def run_test(input_file, output_file):
    treap = Treap()
    actual_output = []

    with open(input_file, "r") as f:
        for line in f:
            parts = line.strip().split()
            if not parts:
                continue

            cmd = parts[0]

            if cmd == "insert":
                treap.insert(int(parts[1]))

            elif cmd == "erase":
                treap.erase(int(parts[1]))

            elif cmd == "search":
                result = treap.search(int(parts[1]))
                actual_output.append(str(result).lower())

            elif cmd == "inorder":
                actual_output.append(inorder_string(treap))

    # read expected output
    with open(output_file, "r") as f:
        expected_output = [line.strip() for line in f if line.strip()]

    # compare
    passed = actual_output == expected_output

    print(f"{input_file}: {'PASS' if passed else 'FAIL'}")

    if not passed:
        print("Expected:")
        print("\n".join(expected_output))
        print("Got:")
        print("\n".join(actual_output))

    return passed

# currently set to only handle 3 tests
if __name__ == "__main__":
    base_dir = os.path.dirname(__file__)  
    io_dir = os.path.join(base_dir, "io")

    total = 3
    passed = 0

    for i in range(1, total + 1):
        in_file = os.path.join(io_dir, f"sample.in.{i}")
        out_file = os.path.join(io_dir, f"sample.out.{i}")

        if run_test(in_file, out_file):
            passed += 1

    print(f"\nFinal: {passed} / {total} tests passed.")
