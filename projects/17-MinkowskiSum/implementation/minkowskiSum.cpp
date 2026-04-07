#include <bits/stdc++.h>
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
vector<point> minkowskiSum(vector<point>& a, vector<point>& b) {
    const int n = a.size(), m = b.size();
    vector<point> c;

    // Indices i and j are used as the starting positions for the algorithm,
    // as we must ensure that the sum of the first two vertices is a vertex
    // in the minkowski sum. Getting the bottom left points of both polygons 
    // accomplishes this.
    int i = 0;
    int j = 0;

    // Get index of bottom left point in a
    point bottomLeftPoint = a[0];
    for (int k=1; k < n; k++) {
        if (a[k].y < bottomLeftPoint.y || 
                (a[k].y == bottomLeftPoint.y && a[k].x < bottomLeftPoint.x)) {
            bottomLeftPoint = a[k];
            i = k;
        }
    }

    // Get index of bottom left point in b
    bottomLeftPoint = b[0];
    for (int k=1; k < m; k++) {
        if (b[k].y < bottomLeftPoint.y || 
                (b[k].y == bottomLeftPoint.y && b[k].x < bottomLeftPoint.x)) {
            bottomLeftPoint = b[k];
            j = k;
        }
    }


    int t = 0; // number of points we have moved through in a
    int s = 0; // number of points we have moved through in b
    while (t+s < n+m) {
        // Add new point corresponding to current index positions
        c.emplace_back(a[i].x + b[j].x, a[i].y + b[j].y);

        // Find indices from a cyclic shift
        int i1 = i+1;
        if (i1 == n) i1 = 0;
        int j1 = j+1;
        if (j1 == m) j1 = 0;

        // Abstractly stores which slope is higher between a[i] -> a[i+1]
        // and b[j] -> b[j+1]. Positive if first slope is higher,
        // zero if equivalent slopes, and negative otherwise
        double slopeDif = (a[i1].y - a[i].y)*(b[j1].x - b[j].x) - (b[j1].y - b[j].y) *(a[i1].x - a[i].x);

        // If the slope from a[i] -> a[i+1] is higher, we 
        // move to the next point in b. If they are equal,
        // we move to the next point in both. If a[i] -> a[i+1] is lower,
        // we move to the next point in a. This ensures we are iterating
        // forward by increasing slope of the edge segments
        if (slopeDif > 0) {
            j = j1;
            s++;
        } else if (slopeDif == 0) {
            i = i1;
            j = j1;
            t++; s++;
        } else {
            i = i1;
            t++;
        }
    }
    return c;
}

int main() {
    int n, m;
    cin >> n >> m;
    vector<point> a;
    for (int i=0; i < n; i++) {
        double x, y;
        cin >> x >> y;
        a.emplace_back(x, y);
    }
    vector<point> b;
    for (int i=0; i < m; i++) {
        double x, y;
        cin >> x >> y;
        b.emplace_back(x, y);
    }

    vector<point> c = minkowskiSum(a, b);
    for (auto& p : c) {
        cout << p.x << ' ' << p.y << endl;
    }
    cout << endl;
}
