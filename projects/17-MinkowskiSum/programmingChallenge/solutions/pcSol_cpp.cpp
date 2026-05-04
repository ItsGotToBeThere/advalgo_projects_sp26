#include <bits/stdc++.h>
using namespace std;

struct point {
    long long x, y;
};

// Takes two convex polygons, represented as sets of vertices in counterclockwise order
// and returns their minkowski sum, represented in the same way.
vector<point> minkowskiSum(vector<point>& polyA, vector<point>& polyB) {
    const int n = polyA.size(), m = polyB.size();
    vector<point> polySum;

    // Indices i and j are used as the starting positions for the algorithm,
    // as we must ensure that the sum of the first two vertices is a vertex
    // in the minkowski sum. Getting the bottom left points of both polygons 
    // accomplishes this.
    int i = 0;
    int j = 0;

    // Get index of bottom left point in A
    point bottomLeftPoint = polyA[0];
    for (int k=1; k < n; k++) {
        if (polyA[k].y < bottomLeftPoint.y || 
                (polyA[k].y == bottomLeftPoint.y && polyA[k].x < bottomLeftPoint.x)) {
            bottomLeftPoint = polyA[k];
            i = k;
        }
    }

    // Get index of bottom left point in B
    bottomLeftPoint = polyB[0];
    for (int k=1; k < m; k++) {
        if (polyB[k].y < bottomLeftPoint.y || 
                (polyB[k].y == bottomLeftPoint.y && polyB[k].x < bottomLeftPoint.x)) {
            bottomLeftPoint = polyB[k];
            j = k;
        }
    }


    int t = 0; // number of points we have moved through in A
    int s = 0; // number of points we have moved through in B
    while (t+s < n+m) {
        // Add new point corresponding to current index positions
        polySum.push_back({polyA[i].x + polyB[j].x, polyA[i].y + polyB[j].y});

        // Find indices from a cyclic shift
        int i1 = i+1;
        if (i1 == n) i1 = 0;
        int j1 = j+1;
        if (j1 == m) j1 = 0;

        // By math, stores which polar angle is higher between A[i] -> A[i+1]
        // and B[j] -> B[j+1]. Positive if first polar angle is higher,
        // zero if equivalent polar angles, and negative otherwise.
        // More concretely, this is the cross product of (polyB[j1] - polyB[j]) and (polyA[i1] - polyA[i])
        double crossProd = (polyA[i1].y - polyA[i].y)*(polyB[j1].x - polyB[j].x) - (polyB[j1].y - polyB[j].y) *(polyA[i1].x - polyA[i].x);

        // The cross product tells us which edge has higher polar angle. 
        // If A[i] -> A[i+1] is higher polar angle,
        // move to the next point in B. If they are equal,
        // we move to the next point in both. If A[i] -> A[i+1] is lower,
        // we move to the next point in A. This ensures we are iterating
        // forward by increasing polar angle of the edge segments
        if (crossProd > 0) {
            j = j1;
            s++;
        } else if (crossProd == 0) {
            i = i1;
            j = j1;
            t++; s++;
        } else {
            i = i1;
            t++;
        }
    }
    return polySum;
}

// Compute minimum distance between two islands
// Note that this does not work if they are intersecting,
// but we are given that this is not the case.
double minDistance(vector<point>& A, vector<point> B) {
    // Minimum distance is the distance between (0, 0)
    // and the Minkowski sum A + (-B), so first compute -B
    // Note that B also stays in counter-clockwise order
    for (int i=0; i < B.size(); i++) {
        B[i] = {-B[i].x, -B[i].y};
    }

    // Get Minkowski sum
    vector<point> C = minkowskiSum(A, B);
    C.push_back(C[0]);

    // Get distance from (0, 0) to C
    // Do this by getting distance to each point in C,
    // and to each segment in C
    double minDist = INT_MAX;
    for (point& p : C) {
        double dist = sqrt(p.x*p.x + p.y*p.y);
        minDist = min(minDist, dist);
    }
    // For distance from point to edge, project (0, 0) point
    // onto each line. If the projection is not on the segment,
    // then we can ignore it. Otherwise, consider the
    // projected point in computing minDist.
    for (int i=0; i < C.size()-1; i++) {
        point a = C[i];
        point b = C[i+1];
        point p = {0, 0};

        double abx = b.x - a.x;
        double aby = b.y - a.y;
        double apx = p.x - a.x;
        double apy = p.y - a.y;
        double coeff = (abx*apx + aby*apy)/(abx*abx + aby*aby);

        // Projected point
        pair<double, double> q = {a.x + abx*coeff, a.y + aby*coeff};

        // Check if projection is on segment
        if (q.first >= min(a.x, b.x) && q.first <= max(a.x, b.x)
                && q.second >= min(a.y, b.y) && q.second <= max(a.y, b.y)) {
            minDist = min(minDist, sqrt(q.first*q.first + q.second*q.second));
        }
    }

    return minDist;
}

// Return graph with n vertices representing the n islands
// and where there is an edge between every pair of islands
// giving the minimum distance between them
//
// NOTE: some edges may be invalid (i.e. go through another
// island). However, if an edge intersects a middle island,
// it will not be included in the MST, as it would be better
// to go to the middle island and then go to the last island.
vector<vector<pair<int, double>>> constructGraph(vector<vector<point>>& polygons) {
    const int n = polygons.size();
    vector<vector<pair<int, double>>> adj(n);

    for (int i=0; i < n; i++) {
        for (int j=i+1; j < n; j++) {
            double dist = minDistance(polygons[i], polygons[j]);
            adj[i].push_back({j, dist});
            adj[j].push_back({i, dist});
        }
    }


    return adj;
}


struct DisjointSetUnion {
    int n;
    vector<int> parent;
    vector<int> size;

    DisjointSetUnion(int n) {
        this->n = n;
        parent = vector<int>(n);
        size = vector<int>(n, 1);
        for (int i=0; i < n; i++) {
            parent[i] = i;
        }
    }

    int find(int v) {
        if (v == parent[v])
            return v;
        return find(parent[v]);
    }

    void make_set(int v) {
        parent[v] = v;
        size[v] = 1;
    }

    void merge(int a, int b) {
        a = find(a);
        b = find(b);
        if (a != b) {
            if (size[a] < size[b])
                swap(a, b);
            parent[b] = a;
            size[a] += size[b];
        }
    }
};

// Perform Kruskall's algorithm to get minimum spanning tree
// Prim's algorithm would also work just as well.
vector<vector<pair<int, double>>> getMST(vector<vector<pair<int, double>>>& adj) {
    const int n = adj.size();
    vector<tuple<double, int, int>> edges;
    for (int i=0; i < adj.size(); i++) {
        for (int j=0; j < adj[i].size(); j++) {
            edges.push_back({adj[i][j].second, i, adj[i][j].first});
        }
    }
    DisjointSetUnion dsu(n);
    sort(edges.begin(), edges.end());
    vector<vector<pair<int, double>>> mstGraph(n);
    for (auto& e : edges) {
        double w = get<0>(e);
        int u = get<1>(e);
        int v = get<2>(e);
        if (dsu.find(u) != dsu.find(v)) {
            mstGraph[u].push_back({v, w});
            mstGraph[v].push_back({u, w});
            dsu.merge(u, v);
        }
    }
    return mstGraph;
}

int main() {
    int n;
    cin >> n;
    vector<vector<point>> polygons(n);
    for (int i=0; i < n; i++) {
        int m;
        cin >> m;
        for (int j=0; j < m; j++) {
            int x, y;
            cin >> x >> y;
            polygons[i].push_back({x, y});
        }
    }

    vector<vector<pair<int, double>>> adj = constructGraph(polygons);

    vector<vector<pair<int, double>>> mst = getMST(adj);

    // Sum up weights across all edges
    double cost = 0;
    for (int i=0; i < n; i++) {
        for (pair<int, double>& v : mst[i]) {
            cost += v.second;
        }
    }

    // Notice that each edge was counted twice, so fix that
    cost /= 2;

    cout << fixed << setprecision(7);
    cout << cost << endl;
}
