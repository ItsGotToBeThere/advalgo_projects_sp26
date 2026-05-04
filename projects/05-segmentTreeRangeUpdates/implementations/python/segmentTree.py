# =============================================================================
# Segment Tree with Lazy Propagation - Range Updates, Range Sum Queries
# Bryan Katuari
# =============================================================================
#
# Problem Statement:
#   Given an array of integers, support two operations efficiently:
#     1. Range Add Update: add a value to every element in a[left..right]
#     2. Range Sum Query:  return the sum of a[left..right]
#   Both operations must run in O(log n) time.
#
# Input Format (stdin):
#   Line 1:  N                          - number of elements
#   Line 2:  a[0] a[1] ... a[N-1]       - initial array values
#   Line 3:  Q                          - number of operations
#   Next Q lines, each one of:
#     U left right val                  - add val to every element in [left..right]
#     Q left right                      - print the sum of a[left..right]
#   All indices are 0-based, inclusive.
#
# Output Format (stdout):
#   One integer per Q-type query, each on its own line.
#
# Complexity:
#   Build:  O(n)
#   Update: O(log n) per operation
#   Query:  O(log n) per operation
#   Space:  O(n)  [technically 4n nodes in the implicit tree array]
# ============================================================================


class SegmentTree:
    def __init__(self, initial_values):
        self.array_size = len(initial_values)
        # 4*n ensures enough space for the full segment tree
        self.tree_sums = [0] * (4 * self.array_size if self.array_size > 0 else 1)
        # stores deferred (lazy) updates
        self.lazy_pending = [0] * (4 * self.array_size if self.array_size > 0 else 1)

        if self.array_size > 0:
            self._build(initial_values, 1, 0, self.array_size - 1)

    def range_sum_query(self, query_left, query_right):
        if self.array_size == 0:
            return 0
        return self._query_sum(1, 0, self.array_size - 1, query_left, query_right)

    def range_add_update(self, update_left, update_right, addend):
        if self.array_size == 0:
            return
        return self._update_add(
            1, 0, self.array_size - 1, update_left, update_right, addend
        )

    def _left_child(self, node):
        return node * 2  # implicit binary tree indexing

    def _right_child(self, node):
        return node * 2 + 1

    def _midpoint(self, range_left, range_right):
        return (range_left + range_right) // 2

    def _segment_length(self, range_left, range_right):
        # number of elements covered by this segment
        return range_right - range_left + 1

    # Builds the segment tree from the input array.
    # Each node stores the sum of the segment it represents.
    # Leaves store single array values, and internal nodes store the sum of their two children.
    def _build(self, values, node, range_left, range_right):
        if range_left == range_right:
            self.tree_sums[node] = values[range_left]  # leaf node
            return

        mid = self._midpoint(range_left, range_right)
        self._build(values, self._left_child(node), range_left, mid)
        self._build(values, self._right_child(node), mid + 1, range_right)

        # parent node stores sum of children
        self.tree_sums[node] = (
            self.tree_sums[self._left_child(node)]
            + self.tree_sums[self._right_child(node)]
        )

    # Lazy propagation puts off range updates so don't need to visit every element immediately. Instead, store a pending addend at the current node and only push it to children when actually need to go deeper into the tree.
    # This keeps updates and queries at O(log n) instead of O(n).
    def _push_lazy_down(self, node, range_left, range_right):
        if self.lazy_pending[node] == 0:
            return

        addend = self.lazy_pending[node]
        mid = self._midpoint(range_left, range_right)

        left = self._left_child(node)
        right = self._right_child(node)

        # propagate lazy update to children
        self.tree_sums[left] += addend * self._segment_length(range_left, mid)
        self.lazy_pending[left] += addend

        self.tree_sums[right] += addend * self._segment_length(mid + 1, range_right)
        self.lazy_pending[right] += addend

        self.lazy_pending[node] = 0  # clear after pushing

    # Range update adds a value to every element in the target interval.
    # If the current node is fully covered by the update range, update its stored sum directly and save the addend in lazy_pending.
    # If only part of the node's range is covered, first push pending work down, recurse on children, then redo the current node from its children.
    def _update_add(
        self, node, range_left, range_right, update_left, update_right, addend
    ):
        if update_left > update_right:
            return  # empty range

        # full overlap
        if update_left == range_left and update_right == range_right:
            self.tree_sums[node] += addend * self._segment_length(
                range_left, range_right
            )
            self.lazy_pending[node] += addend
            return

        # push lazy updates before recursion
        self._push_lazy_down(node, range_left, range_right)

        mid = self._midpoint(range_left, range_right)

        # recurse left child
        self._update_add(
            self._left_child(node),
            range_left,
            mid,
            update_left,
            min(update_right, mid),
            addend,
        )
        # recurse right child
        self._update_add(
            self._right_child(node),
            mid + 1,
            range_right,
            max(update_left, mid + 1),
            update_right,
            addend,
        )

        # recompute parent after children updated
        self.tree_sums[node] = (
            self.tree_sums[self._left_child(node)]
            + self.tree_sums[self._right_child(node)]
        )

    # Range query returns the sum over the requested interval.
    # If the current segment exactly matches the query, return the stored sum immediately.
    # Otherwise, push any pending lazy updates downward so the children are up to date, split the query across the left and right child, and combine the results.
    def _query_sum(self, node, range_left, range_right, query_left, query_right):
        if query_left > query_right:
            return 0  # empty query

        if query_left == range_left and query_right == range_right:
            return self.tree_sums[node]

        # ensure children reflect pending updates
        self._push_lazy_down(node, range_left, range_right)

        mid = self._midpoint(range_left, range_right)

        # split query across children
        return self._query_sum(
            self._left_child(node), range_left, mid, query_left, min(query_right, mid)
        ) + self._query_sum(
            self._right_child(node),
            mid + 1,
            range_right,
            max(query_left, mid + 1),
            query_right,
        )


def main():
    import sys

    input = sys.stdin.readline

    array_size = int(input().strip())
    initial_values = list(map(int, input().split()))

    seg = SegmentTree(initial_values)

    operation_count = int(input().strip())

    output = []
    for _ in range(operation_count):
        parts = input().split()
        op_type = parts[0]

        if op_type == "U":
            left = int(parts[1])
            right = int(parts[2])
            addend = int(parts[3])
            seg.range_add_update(left, right, addend)
        else:
            left = int(parts[1])
            right = int(parts[2])
            output.append(str(seg.range_sum_query(left, right)))

    sys.stdout.write("\n".join(output))


if __name__ == "__main__":
    main()
