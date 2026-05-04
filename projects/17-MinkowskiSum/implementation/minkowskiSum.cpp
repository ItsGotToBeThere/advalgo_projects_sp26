#include <vector>
#include <iostream>
using namespace std;

struct point {
    double x;
    double y;

    point(double x, double y) {
        this->x = x;
        this->y = y;
    };
};

// Takes two convex polygons, represented as sets of vertices in counterclockwise order
// and returns their minkowski sum, represented in the same way.
// For this implementation
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
        polySum.emplace_back(polyA[i].x + polyB[j].x, polyA[i].y + polyB[j].y);

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

int main() {
    // Read both polygons in
    int n, m;
    cin >> n >> m;
    vector<point> polyA;
    for (int i=0; i < n; i++) {
        double x, y;
        cin >> x >> y;
        polyA.emplace_back(x, y);
    }
    vector<point> polyB;
    for (int i=0; i < m; i++) {
        double x, y;
        cin >> x >> y;
        polyB.emplace_back(x, y);
    }

    // Get sum
    vector<point> polySum = minkowskiSum(polyA, polyB);
    for (auto& p : polySum) {
        cout << p.x << ' ' << p.y << endl;
    }
    cout << endl;
}
