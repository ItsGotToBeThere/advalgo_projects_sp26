/*
Persistent segment tree, C++ implementation
Shubhrangshu Debsarkar (fvc9ch)
We use the sum function for these segment trees:
*/

#include <algorithm>
#include <iostream>
#include <vector>
#include <string>

using namespace std;

struct PersistentSegTree {
    /*
    - Our nodes are the same as for regular segment trees
    - They include information on the sum of the segment and the left and right endpoints
    */
    struct Node {
        long long sum;
        int left, right;  // We use an array here because it's probably faster than doing pointer stuff all the time
        };

    vector<Node> nodes;   // node pool (all versions share this)
    /*
    - The roots vector stores the root node indices for each version of the segment tree
    - This is how we ensure persistence: each update creates a new root, and we can access any version by its root index
    */
    vector<int> roots;
    int n;
    PersistentSegTree(const std::vector<long long>& a) : n((int)a.size()) {
        nodes.reserve((4 + 20) * (n + 1));  // we are reserving to minimize the number of times we have to spam reallocate
        roots.push_back(build(a, 0, n - 1));
    }

    int newNode(long long s, int l, int r) {
        nodes.push_back({s, l, r});
        return (int)nodes.size() - 1;
    }
    // This is the initial build function, no different from a regular segment tree build
    int build(const std::vector<long long>& a, int l, int r) {
        if (l == r) return newNode(a[l], -1, -1);
        int m = (l + r) / 2;
        int lc = build(a, l, m);
        int rc = build(a, m + 1, r);
        return newNode(nodes[lc].sum + nodes[rc].sum, lc, rc);
    }
    /*
    - This inner update function is still doing fairly similar things, but notice instead of updating nodes it's just adding
    - to that same vector that we were initially working with
    - This is because our segment tree holds all of the nodes and we instead keep track of which nodes
    - are associated with each version
    - Ex: If only the node on the left side of a node is changed, then only the l parameter of this node will be changed 
    - The r parameter will still be the same index as the pervious version
    - Since the root of this version is different, we will have a new root index in the roots vector that points to this new node, and the old root index will still point to the old node
    */
    int update(int prev, int l, int r, int pos, long long val) {
        if (l == r) return newNode(val, -1, -1);
        int m = (l + r) / 2;
        int lc = nodes[prev].left;
        int rc = nodes[prev].right;
        if (pos <= m) lc = update(lc, l, m, pos, val);
        else          rc = update(rc, m + 1, r, pos, val);
        return newNode(nodes[lc].sum + nodes[rc].sum, lc, rc);
    }
    /*
    The node belongs to the version that we are in, so this functions like a normal query
    */
    long long query(int node, int l, int r, int ql, int qr) {
        if (qr < l || r < ql) return 0;
        if (ql <= l && r <= qr) return nodes[node].sum;
        int m = (l + r) / 2;
        return query(nodes[node].left,  l,     m, ql, qr)
             + query(nodes[node].right, m + 1, r, ql, qr);
    }

    void update(int version, int pos, long long val) {
        roots.push_back(update(roots[version], 0, n - 1, pos, val));
    }

    // We choose the version so that we can query the segment tree as it was after a certain number of updates
    long long query(int version, int ql, int qr) {
        return query(roots[version], 0, n - 1, ql, qr);
    }
};

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int n, q;
    if (!(cin >> n >> q) || n <= 0) return 0;
    vector<long long> values(n);
    for (int i = 0; i < n; ++i) {
        cin >> values[i];
    }

    PersistentSegTree st(values);

    for (int i = 0; i < q; ++i) {
        string op;
        cin >> op;
        if (op == "Q") {
            int version, left, right;
            cin >> version >> left >> right;
            cout << st.query(version, left, right) << '\n';
        } else if (op == "U") {
            int version, index;
            long long value;
            cin >> version >> index >> value;
            st.update(version, index, value);
        }
    }

    return 0;
}
