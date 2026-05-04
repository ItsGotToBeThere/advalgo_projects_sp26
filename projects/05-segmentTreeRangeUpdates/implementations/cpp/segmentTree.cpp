// =============================================================================
// Segment Tree with Lazy Propagation - Range Updates, Range Sum Queries
// Matthew Brown 
// =============================================================================
//
// Problem Statement:
//   Given an array of integers, support two operations efficiently:
//     1. Range Add Update: add a value to every element in a[left..right]
//     2. Range Sum Query:  return the sum of a[left..right]
//   Both operations must run in O(log n) time.
//
// Input Format (stdin):
//   Line 1:  N                          - number of elements
//   Line 2:  a[0] a[1] ... a[N-1]       - initial array values
//   Line 3:  Q                          - number of operations
//   Next Q lines, each one of:
//     U left right val                  - add val to every element in [left..right]
//     Q left right                      - print the sum of a[left..right]
//   All indices are 0-based, inclusive.
//
// Output Format (stdout):
//   One integer per Q-type query, each on its own line.
//
// Complexity:
//   Build:  O(n)
//   Update: O(log n) per operation
//   Query:  O(log n) per operation
//   Space:  O(n)  [technically 4n nodes in the implicit tree array]
// =============================================================================

#include <algorithm>
#include <iostream>
#include <vector>

// Stores range sums with lazy-propagated range-add updates.
// see https://cp-algorithms.com/data_structures/segment_tree.html#range-updates-lazy-propagation
// Uses a 1-indexed implicit binary tree in flat arrays for efficiency. 
// Node v has left child 2v and right child 2v+1.
class SegmentTree
{
public:
    explicit SegmentTree(const std::vector<long long>& initial_values)
        : array_size(static_cast<int>(initial_values.size()))
        // 4x the array size which safely covers any n, even non-powers of 2.
        , tree_sums(4 * array_size, 0LL)
        , lazy_pending(4 * array_size, 0LL)
    {
        if (array_size > 0)
            build(initial_values, /*node=*/1, /*range_left=*/0, /*range_right=*/array_size - 1);
    }

    // Returns the sum of a[query_left..query_right] (inclusive, 0-indexed).
    long long range_sum_query(const int query_left, const int query_right)
    {
        return query_sum(/*node=*/1, /*range_left=*/0, /*range_right=*/array_size - 1,
                         query_left, query_right);
    }

    // Adds addend to every element in a[update_left..update_right] (inclusive, 0-indexed).
    void range_add_update(const int update_left, const int update_right, const long long addend)
    {
        update_add(/*node=*/1, /*range_left=*/0, /*range_right=*/array_size - 1,
                   update_left, update_right, addend);
    }

    // now we implement...

private:
    const int              array_size;  // number of elements in the original array
    std::vector<long long> tree_sums;   // tree_sums[v] = sum of the segment this node covers
    std::vector<long long> lazy_pending;// lazy_pending[v] = addend not yet pushed to children

    int left_child(const int node)  const { return node * 2; }
    int right_child(const int node) const { return node * 2 + 1; }
    int midpoint(const int range_left, const int range_right) const
    {
        return (range_left + range_right) / 2;
    }

    // How many array elements this node covers - needed when applying a lazy addend,
    // since sum increases by addend * count rather than just addend.
    int segment_length(const int range_left, const int range_right) const
    {
        return range_right - range_left + 1;
    }

    // Recursively builds the tree bottom-up from the input array.
    // Leaves hold individual values; internal nodes hold sums of their children.
    void build(const std::vector<long long>& values, const int node,
               const int range_left, const int range_right)
    {
        if (range_left == range_right)
        {
            // Base case: leaf node stores the single array element.
            tree_sums[node] = values[range_left];
            return;
        }

        const int mid = midpoint(range_left, range_right);
        build(values, left_child(node),  range_left, mid);
        build(values, right_child(node), mid + 1,    range_right);

        // Internal node sum is simply the merge of its two children.
        tree_sums[node] = tree_sums[left_child(node)] + tree_sums[right_child(node)];
    }

    // Pushes any pending lazy addend from node down to its two children.
    // Must be called before descending into children during update or query,
    // so that children reflect all previously applied range updates.
    void push_lazy_down(const int node, const int range_left, const int range_right)
    {
        if (lazy_pending[node] == 0)
            return; // nothing to do ...

        const int      mid    = midpoint(range_left, range_right);
        const long long addend = lazy_pending[node];

        // Apply the deferred addend to each child's sum.
        // Multiply by segment length because every element in the child's range gets +addend.
        tree_sums[left_child(node)]    += addend * segment_length(range_left, mid);
        lazy_pending[left_child(node)] += addend;

        tree_sums[right_child(node)]    += addend * segment_length(mid + 1, range_right);
        lazy_pending[right_child(node)] += addend;

        // This node has fully passed its obligation down so clear it.
        lazy_pending[node] = 0;
    }

    // Adds addend to all elements in [update_left..update_right].
    // When the node's range is fully inside the update range, apply directly
    // and mark lazy bc no need to touch children yet.
    // Otherwise push laziness down before splitting into two recursive calls.
    void update_add(const int node, const int range_left, const int range_right,
                    const int update_left, const int update_right, const long long addend)
    {
        if (update_left > update_right)
            return; // Empty or inverted range ... nothing to do.

        if (update_left == range_left && update_right == range_right)
        {
            // This node's entire range is covered so apply directly and defer children.
            tree_sums[node]    += addend * segment_length(range_left, range_right);
            lazy_pending[node] += addend;
            return;
        }

        // Partial overlap: flush pending work before we split, so children are current.
        push_lazy_down(node, range_left, range_right);

        const int mid = midpoint(range_left, range_right);
        update_add(left_child(node),  range_left, mid, update_left, std::min(update_right, mid), addend);
        update_add(right_child(node), mid + 1, range_right, std::max(update_left, mid + 1), update_right, addend);

        // Recompute this node's sum from its now-updated children.
        tree_sums[node] = tree_sums[left_child(node)] + tree_sums[right_child(node)];
    }

    // Returns the sum of elements in [query_left..query_right].
    // Returns 0 for empty/inverted ranges (additive identity)
    long long query_sum(const int node, const int range_left, const int range_right,
                        const int query_left, const int query_right)
    {
        // One of the two recursive calls below may produce an inverted range if
        // the query falls entirely on one side. Return 0 so it contributes nothing to the sum.
        if (query_left > query_right)
            return 0LL; 

        if (query_left == range_left && query_right == range_right)
        {
            // precomputed sum already accounts for all lazy addends above.
            return tree_sums[node];
        }

        // children may not reflect recent updates.
        push_lazy_down(node, range_left, range_right);

        const int mid = midpoint(range_left, range_right);
        return query_sum(left_child(node),  range_left, mid, query_left, std::min(query_right, mid))
             + query_sum(right_child(node), mid + 1, range_right, std::max(query_left, mid + 1), query_right);
    }
};

// read input and display results 
int main()
{
    std::ios::sync_with_stdio(false);
    std::cin.tie(nullptr);

    int array_size = 0;
    std::cin >> array_size;

    std::vector<long long> initial_values(array_size);
    for (int index = 0; index < array_size; ++index)
        std::cin >> initial_values[index];

    SegmentTree seg(initial_values);

    int operation_count = 0;
    std::cin >> operation_count;

    // Process each operation: U = range add update, Q = range sum query.
    for (int op = 0; op < operation_count; ++op)
    {
        char op_type = ' ';
        std::cin >> op_type;

        if (op_type == 'U')
        {
            int left = 0, right = 0;
            long long addend = 0;
            std::cin >> left >> right >> addend;
            seg.range_add_update(left, right, addend);
        }
        else /* if (op_type == 'Q') */
        {
            int left = 0, right = 0;
            std::cin >> left >> right;
            std::cout << seg.range_sum_query(left, right) << '\n';
        }
    }

    return 0;
}