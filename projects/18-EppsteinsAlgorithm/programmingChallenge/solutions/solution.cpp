#include <vector>
#include <queue>
#include <iostream>
#include <memory>
#include <climits>
#include <algorithm>

//struct for storing information about edges, this is used throughout the algorithm for convenience
struct Edge {
    int from;
    int to;
    int weight;
    Edge(int from_, int to_, int weight_) : from(from_), to(to_), weight(weight_) {}
};


//basic heap class that implements a leftist heap. left_child and right_child have the same properties as
//they do in a regular heap . We also keep track of a null path length to ensure that the heap can maintain its leftward bias
//which is elaborated on more in the implementation of merge
template <typename T>
class LeftistHeap {
public:
    int null_path_length;
    std::shared_ptr<LeftistHeap<T>> left_child;
    std::shared_ptr<LeftistHeap<T>> right_child;
    T value;
    LeftistHeap<T>(const T& value_) : value(value_), left_child(nullptr), right_child(nullptr), null_path_length(0) {}
};


//values stored by leftist heaps. This is just a wrapper for edges in the current implementation, since the 
//leftist heap is meant to store sidetrack edges sorted by their edge weights in increasing order
class LeftistHeapNode {
public:
    LeftistHeapNode(Edge edge_) : edge(edge_) {}
    bool operator<(const LeftistHeapNode& other) const {
        return edge.weight < other.get_weight();
    }
    int get_weight() const {
        return edge.weight;
    }
    Edge edge;
};


//function for merging two leftist heaps in O(logn) time, n is the max size of either heap. The reason we use leftist heaps 
//instead of regular heaps is because the merge operation is guaranteed to execute in O(logn) time. A leftist heap is a regular binary
//heap with the property that the null path length of the left child is greater than or equal to that of the right child. The
//null path length is the length from any node to a node that isn't full (0 or 1 children). By ensuring that right child always has
//low null path length, we merge quickly by merging the heap with the larger root with the smaller heaps right child. 
template <typename T>
std::shared_ptr<LeftistHeap<T>> merge(std::shared_ptr<LeftistHeap<T>> heap1, std::shared_ptr<LeftistHeap<T>> heap2) {
    //base case, if either heap is null then we just return the other one
    if (!heap1) return heap2;
    if (!heap2) return heap1;
    
    //for convenience, we want to assume that heap1 is the heap with smaller root (so heap2 is merged into heap1s child)
    if (heap2->value < heap1->value) {
        std::swap(heap1, heap2);
    }
    
    //making a shallow copy of heap1's root. This is crucial to ensure that Eppstein's algorithm works efficiently, as it allows
    //nodes to share information without much overhead. Since the merge works in O(logn) time, we only have to make shallow copies of O(logn)
    //of the nodes in the original heaps when merging them together. Since the rest of the nodes have nothing done to them, by copying only
    //the logn nodes that are modified, we can continue using the original versions of heap1 and heap2 without worrying about their
    //structure being disrupted. 
    std::shared_ptr<LeftistHeap<T>> new_heap1 = std::make_shared<LeftistHeap<T>>(*heap1);

    //since heap2 is less than heap1s root, we merge it with heap1's right child
    new_heap1->right_child = merge(new_heap1->right_child, heap2);
    
    //to ensure leftward bias, we calculate the null path length, which is the shortest distance to a 
    //node without two children (node that isn't full). If the right child has greater null path length, we swap 
    //it with the left child, which is allowed because it doesn't violate the heap rule of children being smaller
    //than their parents. This ensures that the rightmost path down the heap is short, allowing us to merge efficiently. 
    int left_null_path_length = new_heap1->left_child ? new_heap1->left_child->null_path_length : -1;
    int right_null_path_length = new_heap1->right_child ? new_heap1->right_child->null_path_length : -1;
    
    if (left_null_path_length < right_null_path_length) {
        std::swap(new_heap1->left_child, new_heap1->right_child);
    }
    //updating null path length for parent to use, if necessary
    new_heap1->null_path_length = std::min(left_null_path_length, right_null_path_length) + 1;
    //return the new heap instead of modifying the old one to allow us to still use the old one
    return new_heap1;
}


//standard implementation of dijkstra's algorithm. Note that we keep track of both best costs as well as optimal prev nodes 
//so that we can reconstruct paths 
std::pair<std::vector<int>, std::vector<int>> dijkstras(std::vector<std::vector<Edge>>& graph, int start) {
    std::vector<int> distances(graph.size(), INT_MAX);
    std::vector<int> prev_nodes(graph.size(), -1);
    auto cmp = [](const Edge& a, const Edge& b) { return a.weight > b.weight; };
    std::priority_queue<Edge, std::vector<Edge>, decltype(cmp)> pq(cmp);
    
    pq.emplace(-1, start, 0);
    distances[start] = 0;

    while (!pq.empty()) {
        Edge curr = pq.top();
        pq.pop();
        int dist = curr.weight;
        int to = curr.to;
        int from = curr.from;

        if (dist > distances[to]) continue;

        prev_nodes[to] = from;
        
        for (Edge& edge : graph[to]) {
            if (dist + edge.weight < distances[edge.to]) {
                distances[edge.to] = dist + edge.weight;
                pq.emplace(to, edge.to, distances[edge.to]);
            }
        }
    }
    return {distances, prev_nodes};
}


//class that does all the work for Eppstein's algorithm. 
class TransformedGraph {
public:
    TransformedGraph(std::vector<Edge>& edge_list, const int n_nodes, const int source, const int target) : source(source), target(target) {
        //creating the graph and its transpose from the provided edges
        std::vector<std::vector<Edge>> graph(n_nodes);
        std::vector<std::vector<Edge>> graph_transpose(n_nodes);
        for (Edge& edge : edge_list) {
            graph[edge.from].push_back(edge);
            graph_transpose[edge.to].emplace_back(edge.to, edge.from, edge.weight);
        }
        //calculating the optimal distances from each node to the target as well as the optimal next node from each node to 
        //get to the target. Here, we implemented this by running dijikstra's starting from the target on the transpose of the 
        //original graph. This gets us shortest paths from the target to any node in the reversed graph, and reversing any of those paths
        //gets you the original shortest path from any node to the target. 
        auto res = dijkstras(graph_transpose, target);
        const std::vector<int>& distances_to_target = res.first;
        nxt_nodes = res.second;
        
        //computing all sidetrack edges in the graph. Sidetrack(u, v, w) tells us how much extra cost we incur by 
        //going from node u to node v on an edge with weight w. We calculate it as the d_v + w - d_u, where d_i is the shortest distance
        //from node i to the target which we calculated in the previous step and stored in distances_to_target. 
        std::vector<std::vector<Edge>> sidetrack_graph(n_nodes);
        for (int from = 0; from < n_nodes; from++) {
            //can't get to target from here
            if (distances_to_target[from] == INT_MAX) continue;
            bool found_nxt = false;
            //note that we don't count the optimal edge as a sidetrack edge. Since there can be repeated edges in Eppstein's,
            //we set a flag that is set to true when we have found one edge that matches with the optimal edge. We can include the 
            //other edges that match the optimal edge, but we need to remove one of them corresponding to the optimal edge (u, nxt[u]). 
            for (Edge& edge : graph[from]) {
                if (distances_to_target[edge.to] == INT_MAX) continue;
                if (!found_nxt && edge.to == nxt_nodes[from] && distances_to_target[from] - distances_to_target[edge.to] == edge.weight) {
                    found_nxt = true;
                    continue;
                }
                int sidetrack_weight = edge.weight + distances_to_target[edge.to] - distances_to_target[from];
                sidetrack_graph[from].emplace_back(edge.from, edge.to, sidetrack_weight);
            }
        }
        
        //creating a leftist heap for the sidetrack edges coming out of each node. If we don't do this, then
        //every time we visit a node, we will have to add all of its outgoing sidetrack edges to the priority queue. This unnecessarily
        //blows up the runtime to kmlogkm and we can optimize this by storing the outgoing sidetrack edges in a priority queue, making
        //it so that we only insert up to 3 items in the priority queue for each iteration (two children and the root priority queue of wherever
        //the sidetrack edge takes you to). This way, we don't add every sidetrack edge to the priority queue at once when we visit a node. 
        //We just add the edges with smallest weights first, and then add their children with higher weights later if necessary.  
        std::vector<std::shared_ptr<LeftistHeap<LeftistHeapNode>>> adj_heaps(n_nodes, nullptr);
        for (int from = 0; from < n_nodes; from++) {
            for (Edge& sidetrack_edge : sidetrack_graph[from]) {
                LeftistHeapNode new_node(sidetrack_edge);
                //adding a new item to a leftist heap is that same as merging the original leftist heap and a new leftist heap of size 1
                adj_heaps[from] = merge(adj_heaps[from], std::make_shared<LeftistHeap<LeftistHeapNode>>(new_node));
            }
        }

        //For Eppstein's algorithm to work correctly, if a node has successor nodes nxt[node], nxt[nxt[node]], ... 
        //in its optimal path to the target, we need to add all outgoing sidetrack edges from those nodes to the list of sidetrack
        //edges for the original node. This is why we need an efficient merge operation for the leftist heap that leaves the original
        //heaps unchanged, as it allows us to easily add extra sidetrack edges to a node's list of sidetrack edges. Details on how this is
        //implemented are near where merge_nxt_heaps is implemented. 
        merge_nxt_heaps(adj_heaps, nxt_nodes, target);
        this->adj_heaps = adj_heaps;
        this->d_source_target = distances_to_target[source];
    }
    
    //function for getting the k shortest paths, stopping as soon as a walk's cost exceeds budget
    //(so we never enumerate past the answer). The path of the LAST in-budget walk is always
    //reconstructed at the end from its sidetrack edges; intermediate walks carry only their costs.
    //include_paths: if true, additionally reconstruct the full path at every pop (useful for
    //debugging or callers that genuinely want all the walks); if false, skip that per-pop work.
    //If the shortest walk is already over budget (or target unreachable), returns an empty vector.
    std::vector<std::pair<std::vector<int>, int>> k_shortest_paths(int k, bool include_paths, int budget) {

        //this is the object inserted into the priority queue. It keeps track of distance, which is used as the key
        //for the priority queue. It also has a list of sidetrack edges already traversed so that the path can be reconstructed later
        //Finally, it has a pointer to the leftist heap node of the sidetrack edge it represents so that we can access its children later.
        struct State {
            int dist;
            std::vector<Edge> sidetrack_edges;
            std::shared_ptr<LeftistHeap<LeftistHeapNode>> next;

            State(int d, std::vector<Edge> s, std::shared_ptr<LeftistHeap<LeftistHeapNode>> h) : dist(d), sidetrack_edges(s), next(h) {}

            bool operator<(const State& other) const {
                return dist > other.dist;
            }
        };
        //storing the final results, which are pairs of paths and costs
        std::vector<std::pair<std::vector<int>, int>> final_res;
        if (d_source_target == INT_MAX) return final_res; // target unreachable from source, just return an empty list
        if (d_source_target > budget) return final_res;   // even the shortest walk is over budget

        //sidetrack edges of the most recently in-budget enumerated walk; used at the end to
        //rebuild exactly one path — the answer — from its sidetracks
        std::vector<Edge> last_sidetrack_edges;

        // 1st shortest path is the one with 0 sidetracks
        final_res.emplace_back(
            include_paths ? construct_path_from_sidetrack_edges(last_sidetrack_edges) : std::vector<int>{},
            d_source_target);
        if (k == 1 || !adj_heaps[source]) {
            final_res.back().first = construct_path_from_sidetrack_edges(last_sidetrack_edges);
            return final_res;
        }

        //storing first item in source node's pq
        std::priority_queue<State> pq;
        pq.emplace(adj_heaps[source]->value.edge.weight, std::vector<Edge>{adj_heaps[source]->value.edge}, adj_heaps[source]);

        //enumerate walks in non-decreasing cost order until we either exhaust k, drain the pq, or
        //pop a walk whose cost exceeds budget (at which point every later walk is also out of
        //budget and we can stop).
        while (!pq.empty() && final_res.size() < (size_t)k) {
            State curr = pq.top();
            pq.pop();
            int cost = d_source_target + curr.dist;
            if (cost > budget) break;
            final_res.emplace_back(
                include_paths ? construct_path_from_sidetrack_edges(curr.sidetrack_edges) : std::vector<int>{},
                cost);
            last_sidetrack_edges = curr.sidetrack_edges;
            auto H = curr.next;
            
            //if we consider the left child, then we have to remove the edge corresponding the the current node, since it means
            //we're not actually using the current node's sidetrack edge, but using the sidetrack edge of something further down the 
            //list of sidetrack edges for the current node. This is why we replace new_sidetrack_edges.back() with the left child if we add left_child
            //to the priority queue and why we subtract the weight of the parent. 
            if (H->left_child) {
                std::vector<Edge> new_sidetrack_edges = curr.sidetrack_edges;
                new_sidetrack_edges.back() = H->left_child->value.edge;
                int new_dist = curr.dist + H->left_child->value.edge.weight - H->value.edge.weight; 
                //subtract off weight of current sidetrack edge since we're not actually using it, just replacing it with left instead
                pq.emplace(new_dist, new_sidetrack_edges, H->left_child);
            }
            // same logic as left child case
            if (H->right_child) {
                std::vector<Edge> new_sidetrack_edges = curr.sidetrack_edges;
                new_sidetrack_edges.back() = H->right_child->value.edge;
                int new_dist = curr.dist + H->right_child->value.edge.weight - H->value.edge.weight;
                pq.emplace(new_dist, new_sidetrack_edges, H->right_child);
            }
            //in this case, we are deciding to use the current nodes sidetrack edge, so we keep it in the list of edges and 
            //now add the root of the priority queue corresponding to the node that the current node's sidetrack edge takes you to
            auto next_heap = adj_heaps[H->value.edge.to];
            if (next_heap) {
                std::vector<Edge> new_sidetrack_edges = curr.sidetrack_edges;
                new_sidetrack_edges.push_back(next_heap->value.edge);
                int new_dist = curr.dist + next_heap->value.edge.weight;
                pq.emplace(new_dist, new_sidetrack_edges, next_heap);
            }
        }
        //reconstruct the path for the last in-budget walk — the answer main actually needs — from
        //the sidetrack edges we saved during enumeration
        if (!final_res.empty()) {
            final_res.back().first = construct_path_from_sidetrack_edges(last_sidetrack_edges);
        }
        return final_res;
    }

private:
    int source;
    int target;
    std::vector<std::shared_ptr<LeftistHeap<LeftistHeapNode>>> adj_heaps;
    std::vector<int> nxt_nodes;
    int d_source_target;
    
    //helper function for constructing path from list of sidetrack edges. The idea here is that, given any list of sidetrack edges,
    //if we fill in the gaps with optimal edges, we get a full path from source to target. This is why finding the K shortest paths going from
    //the source to anywhere in the transformed graph of sidetrack edges is the same as finding the K shortest paths 
    //from source to target on the original graph. Each list of sidetrack edges is the extra nodes we travel to on a 
    //suboptimal path from the source to the target. The cost of the path is the sum of the cost of the sidetrack path and cost
    //the optimal path from the source to the target, since the cost of sidetrack nodes is the extra cost we incur by going
    //down that suboptimal path. 
    std::vector<int> construct_path_from_sidetrack_edges(const std::vector<Edge>& sidetrack_edges) {
        int curr = source;
        size_t i = 0;
        std::vector<int> path;
        while (curr != target || i < sidetrack_edges.size()) {
            path.push_back(curr);
            if (i < sidetrack_edges.size() && sidetrack_edges[i].from == curr) {
                curr = sidetrack_edges[i].to;
                i++;
            } else {
                curr = nxt_nodes[curr];
            }
            if (curr == -1) break; //fallback for safety, doesn't actually get called if the rest of the implementation is right
        }
        path.push_back(target);
        return path;
    }
    

    //merging heaps of all nxt[node], nxt[nxt[node]], ... with heap of node. First we construct a graph where each node, n, points
    //to all nodes n' that it is the successor of in the nxt array, so all nodes whose optimal next node is n (all nodes n' such that
    //nxt[n'] = n). Then, we merge the heaps of n' and n and store the result in adj_lists[n']. This way, each node needs only
    //one merge to get all sidetrack nodes from its successors. We implement this by doing a BFS. 
    void merge_nxt_heaps(std::vector<std::shared_ptr<LeftistHeap<LeftistHeapNode>>>& adj_heaps, const std::vector<int>& nxt_nodes, int target) {
        std::vector<std::vector<int>> nxt_nodes_graph_transpose(adj_heaps.size());
        for (int i = 0; i < nxt_nodes.size(); i++) {
            if (nxt_nodes[i] != -1) { 
                nxt_nodes_graph_transpose[nxt_nodes[i]].push_back(i);
            }
        }
        
        std::queue<int> q;
        std::vector<int> visited(adj_heaps.size(), 0);
        q.push(target);
        
        while (!q.empty()) {
            int curr = q.front();
            q.pop();
            
            if (!visited[curr]) {
                visited[curr] = 1;
                if (curr != target) {
                    adj_heaps[curr] = merge(adj_heaps[curr], adj_heaps[nxt_nodes[curr]]); 
                    //merge predecessor with successor
                }
                for (int j : nxt_nodes_graph_transpose[curr]) {
                    if (!visited[j]) {
                        q.push(j);
                    }
                }
            }
        }
    }
};


//processing input and formatting output to give us paths and weights
int main() {
    int n, m, source, target, budget;
    std::cin >> n >> m >> source >> target;
    source--; target--;
    std::vector<Edge> edges;
    for (int i = 0; i < m; i++) {
        int from, to, weight;
        std::cin >> from >> to >> weight;
        edges.emplace_back(from - 1, to - 1, weight);
        edges.emplace_back(to - 1, from - 1, weight);
    }
    std::cin >> budget;
    TransformedGraph eppsteins(edges, n, source, target);

    //enumerate walks in non-decreasing cost order up to K_MAX, stopping early once costs exceed
    //budget. The call returns empty iff there is no in-budget walk; otherwise the last entry
    //holds the best in-budget walk with its path already reconstructed.
    const int K_MAX = 100000;
    auto res = eppsteins.k_shortest_paths(K_MAX, false, budget);
    if (res.empty()) {
        std::cout << -1 << std::endl;
    } else {
        const auto& path = res.back().first;
        for (size_t i = 0; i < path.size(); i++) {
            std::cout << (path[i] + 1);
            std::cout << (i + 1 == path.size() ? '\n' : ' ');
        }
    }
    return 0;
}