from heapq import *

N, M, s, t, K = [int(i) for i in input().split()]
inf = 10 ** 18

# Persistent Leftist Heap Implementation
# Used to store sidetrack edges efficiently. "Persistent" means inserting
# into a heap returns a new heap while keeping the old one intact, which
# lets child nodes in the shortest-path tree inherit their parent's heap
# without copying.
class EHeap:
    def __init__(self, rank, key, value, left, right):
        self.rank = rank      # rank = length of rightmost path (for balancing)
        self.key = key        # sidetrack cost delta(e) = w(e) + d(head) - d(tail)
        self.value = value    # the node this sidetrack edge points to
        self.left = left      # left child
        self.right = right    # right child

    @staticmethod
    def insert(a, k, v):
        # Insert a new (key, value) into heap a, return a new root.
        # If heap is empty or new key is smaller, it becomes the new root.
        if not a or k < a.key:
            return EHeap(1, k, v, a, None)
        # Otherwise, recursively insert into the right subtree.
        l, r = a.left, EHeap.insert(a.right, k, v)
        # Maintain leftist property: left child's rank >= right child's rank.
        if not l or r.rank > l.rank:
            l, r = r, l
        return EHeap(r.rank + 1 if r else 1, a.key, a.value, l, r)

    def __lt__(self, _):
        # Needed so heapq can break ties when two (cost, EHeap) entries
        # have the same cost. The actual ordering is by cost, not by heap.
        return False

# Standard Dijkstra's algorithm on the reverse graph from destination t.
# Returns:
#   d[v]   = shortest distance from v to t in the original graph
#   suc[v] = next node on the shortest path from v to t (parent in SP tree)
def dijkstras(g, s):
    pq = [(0, s)]
    d = [inf for i in range(N)]
    d[s] = 0
    suc = [-1 for i in range(N)]
    while len(pq) > 0:
        dist, node = heappop(pq)
        if dist != d[node]:
            continue
        for to, weight in g[node]:
            if weight + dist < d[to]:
                heappush(pq, (weight + dist, to))
                d[to] = weight + dist
                suc[to] = node
    return d, suc

# Read edges: adj is the original graph, adjr is the reversed graph.
# We reverse edges so Dijkstra from t on adjr gives dist-to-t for all nodes.
adj = [[] for i in range(N)]
adjr = [[] for i in range(N)]
for i in range(M):
    u, v, w = [int(i) for i in input().split()]
    adj[u].append([v, w])
    adjr[v].append([u, w])

# Run Dijkstra on reversed graph from t to get shortest distances to t.
d, suc = dijkstras(adjr, t)

if d[s] == inf:
    # No path from s to t at all, so all K answers are -1.
    for i in range(K):
        print(-1)
else:
    # Build the shortest-path tree rooted at t using successor pointers.
    # tree[u] = list of nodes whose successor on the SP to t is u.
    tree = [[] for i in range(N)]
    for i in range(N):
        if suc[i] != -1:
            tree[suc[i]].append(i)

    # For each node, build a persistent leftist heap of its sidetrack edges.
    # A sidetrack edge at node u is any outgoing edge (u->v) that is not
    # the tree edge (u -> suc[u]). Its key is delta(e) = w + d[v] - d[u],
    # representing how much extra cost you pay by taking this detour.

    # BFS from t down the SP tree. Each child inherits its parent's heap
    # (persistence means the parent's heap is unchanged), then inserts
    # its own sidetrack edges on top.
    h = [None for i in range(N)]
    bfs = [t]
    for u in bfs:
        seenp = False  # have we skipped the tree edge (u -> suc[u]) yet?
        for to, weight in adj[u]:
            if d[to] == inf:
                continue
            c = weight + d[to] - d[u]  # sidetrack cost
            # Skip exactly one tree edge: the one to suc[u] with delta = 0.
            # If no seenp, then its possible we skip multiple parallel edges
            if not seenp and to == suc[u] and c == 0:
                seenp = True
                continue
            # Add sidetrack edge to EHeap
            h[u] = EHeap.insert(h[u], c, to)
        # Propagate heap to children in the SP tree (persistent, no copy needed).
        for v in tree[u]:
            h[v] = h[u]
            bfs.append(v)

    # Eppstein's K shortest paths enumeration.
    # The 1st shortest path is just d[s]. Each subsequent path is found by
    # exploring sidetrack edges via a priority queue of (path_cost, heap_node).
    #
    # From a heap node ch with current path cost cd, we can branch 3 ways:
    #   1. Follow the sidetrack: go to ch.value and use its heap root
    #      cost = cd + h[ch.value].key
    #   2. Take the left child of ch (next-best sidetrack at same/ancestor node)
    #      cost = cd + ch.left.key - ch.key (swap this sidetrack for a costlier one)
    #   3. Take the right child of ch (same idea)
    #      cost = cd + ch.right.key - ch.key
    ans = [d[s]]
    if h[s]:
        # Note we are already starting with the lowest sidetrack edge
        pq = [(d[s] + h[s].key, h[s])]
        while pq and len(ans) < K:
            cd, ch = heappop(pq)
            ans.append(cd)
            # Branch 1: follow the sidetrack to ch.value, then take its best sidetrack
            if h[ch.value]:
                heappush(pq, (cd + h[ch.value].key, h[ch.value]))
            # Branch 2: swap current sidetrack for the left child (next-cheapest)
            # ch.left.key - ch.key is because we already added the ch.key cost into cd previously
            if ch.left:
                heappush(pq, (cd + ch.left.key - ch.key, ch.left))
            # Branch 3: swap current sidetrack for the right child
            if ch.right:
                heappush(pq, (cd + ch.right.key - ch.key, ch.right))

    # NOTE: Outputs match files. Actual path of graph is not included!
    # NOTE: This program prints 
    for x in ans:
        print(x)
