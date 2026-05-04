# Persistent Segment Tree
# Joshua Markle (cze9bj)
#
# This is really the same as a normal segment tree but stores multiple
# versions of the array as well. Each update creates a new version
#
# This segment tree uses the SUM function for simplicity
#
# The main optimization is structural sharing:
# when one index is updated, we only rebuild the nodes along the path
# from the root to the changed leaf, everything else is reused


# A node in the segment tree
class Node:
    # value: the aggregate value (SUM)
    # left:  pointer to the left child
    # right: pointer to the right child
    def __init__(self, value, left=None, right=None):
        self.value = value
        self.left = left
        self.right = right


# The class implementation for the persistent segment tree
class PersistentSegmentTree:
    # n:        array length
    # versions: list of root nodes; each update appends to this list

    def __init__(self, arr):
        # No empty inputs
        if not arr:
            raise ValueError("Input array must not be empty!")

        # Store array length for bounds checking
        self.n = len(arr)

        # Build the first version of the tree from the initial array
        # versions[0] is the root of the initial segment tree
        self.versions = [self._build(arr, 0, self.n - 1)]

    # Build a normal segment tree
    def _build(self, arr, lo, hi):
        # lo and hi is the segment covered by the current node
        # Base case: if lo == hi, the range is singular and its a leaf node
        if lo == hi:
            return Node(arr[lo])

        # Split the segment into left and right halves
        mid = (lo + hi) // 2

        # Create child subtrees
        left = self._build(arr, lo, mid)
        right = self._build(arr, mid + 1, hi)

        # Merge: the current node stores the sum of the two children
        return Node(left.value + right.value, left, right)

    # Create a NEW version of the tree
    def _update(self, node, lo, hi, idx, new_value):
        # node:      root of the subtree from the old version
        # lo, hi:    segment represented by this subtree
        # idx:       array index to update
        # new_value: new value that should be written at arr[idx]

        # If at the leaf for idx, return a new leaf node
        if lo == hi:
            return Node(new_value)

        # Find midpoint to see if updated index is in the left or right segment
        mid = (lo + hi) // 2

        # Rebuild the child that contains idx; reuse other child
        if idx <= mid:
            new_left = self._update(node.left, lo, mid, idx, new_value)
            new_right = node.right
        else:
            new_left = node.left
            new_right = self._update(node.right, mid + 1, hi, idx, new_value)

        # Merge: create a new parent whose value is the sum of the new children
        return Node(new_left.value + new_right.value, new_left, new_right)

    # Public method to create a new version
    def update(self, version, idx, new_value):
        # version:   which existing version we want to update from
        # idx:       which array index to change
        # new_value: value to place at that index

        # Check that the version exists
        if version < 0 or version >= len(self.versions):
            raise IndexError("Bad version index!")

        # Check that the array index is valid
        if idx < 0 or idx >= self.n:
            raise IndexError("Index out of bounds!")

        # Make a new root by copying from the chosen version
        new_root = self._update(self.versions[version], 0, self.n - 1, idx, new_value)

        # Save as another version
        self.versions.append(new_root)

        # Return the index of the new version
        return len(self.versions) - 1

    # Get the range sum for [ql, qr]
    def _query(self, node, lo, hi, ql, qr):
        # Three cases:
        # 1. Total overlap: current segment is completely inside query range
        # 2. No overlap: current segment is completely outside query range
        # 3. Partial overlap: split and recurse

        # Total overlap
        if ql <= lo and hi <= qr:
            return node.value

        # No overlap
        if hi < ql or qr < lo:
            return 0

        # Partial overlap: query both children and add results
        mid = (lo + hi) // 2
        total = 0

        if node.left is not None:
            total += self._query(node.left, lo, mid, ql, qr)

        if node.right is not None:
            total += self._query(node.right, mid + 1, hi, ql, qr)

        return total

    # Public method for range sum queries
    def query(self, version, left, right):
        # version:     which historical version to query
        # left, right: inclusive query bounds

        # Validate version
        if version < 0 or version >= len(self.versions):
            raise IndexError("Bad version index!")

        # Validate range
        if left < 0 or right >= self.n or left > right:
            raise IndexError("Bad query range!")

        return self._query(self.versions[version], 0, self.n - 1, left, right)

    # Helper to get one value from one version
    def point_get(self, version, idx):
        return self.query(version, idx, idx)


# --- Take Input --- # 


# Input:
# n q
# array of n ints
# then q ops:
# U v i x (update -> new version)
# Q v l r (range sum)
# 0-based indexing, print result for each Q
def main():
    # Read n and q
    n, q = map(int, input().split())

    # Read initial array
    arr = list(map(int, input().split()))

    # Build tree
    pst = PersistentSegmentTree(arr)

    # Process commands
    outputs = []

    for _ in range(q):
        parts = input().split()
        cmd = parts[0]

        if cmd == "U":
            # U version index new_value
            version = int(parts[1])
            idx = int(parts[2])
            new_value = int(parts[3])
            pst.update(version, idx, new_value)

        elif cmd == "Q":
            # Q version left right
            version = int(parts[1])
            left = int(parts[2])
            right = int(parts[3])
            result = pst.query(version, left, right)
            outputs.append(str(result))

        else:
            raise ValueError("Invalid command")

    # Print results
    print("\n".join(outputs))


if __name__ == "__main__":
    main()
