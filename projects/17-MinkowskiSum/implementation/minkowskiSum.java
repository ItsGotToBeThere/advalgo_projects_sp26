import java.util.*;

public class minkowskiSum {
    static Scanner sc = new Scanner(System.in);
    static List<Point> input;
    public static void main(String[] args){
        int n = sc.nextInt(); // n = number of vertices in the first polygon
        int m = sc.nextInt(); // m = number of vertices in the second polygon
        Polygon A = parsePolygon(n); // takes next n lines to be the points of Polygon A
        Polygon B = parsePolygon(m); // takes next m lines to be the points of Polygon B
        Polygon SUM = A.add(B); // Minkowski Sum of Convex Polygons
        System.out.println(SUM);
    }

    // parses the next n lines for points, using them to construct a polygon
    public static Polygon parsePolygon(int n){
        input = new ArrayList<>(n);
        for(int i = 0; i < n; i++){
            double x = sc.nextDouble();
            double y = sc.nextDouble();
            input.add(new Point(x, y));
        }
        return new Polygon(input);
    }
}

class Polygon {
    List<Point> points;
    List<Edge> edges;
    public Polygon(List<Point> input){
        constructPoints(input);
        constructEdges();
    }
    // We construct the list of points by starting at one of the lowest points and moving counterclockwise
    public void constructPoints(List<Point> input){
        points = new ArrayList<>(input.size());
        Point root = input.get(0);
        int start = 0;
        // When we find a new lowest y-value, it becomes the new root
        // If we have a tie, choose the one with the lowest x-value
        for(int i = 1; i < input.size(); i++){
            Point p = input.get(i);
            if(p.y < root.y){
                root = p;
                start = i;
            }
            else if(p.y == root.y && p.x < root.x){
                root = p;
                start = i;
            }
        }
        // Cyclically add the roots counterclockwise starting from the root
        for(int i=0; i<input.size(); i++){
            points.add(input.get((i+start)%input.size()));
        }
    }
    // We construct the directed edges by starting at the root and moving counterclockwise
    public void constructEdges(){
        edges = new ArrayList<>(points.size());
        if(points.size() <= 1) return;
        for(int i=0; i<points.size()-1; i++){
            edges.add(new Edge(points.get(i), points.get(i+1)));
        }
        edges.add(new Edge(points.get(points.size()-1), points.get(0)));
    }
    // The Minkowski Sum of Convex Polygons
    public Polygon add(Polygon p){
        // Collect the edges into a list sorted by radial angle from the positive x-axis
        List<Edge> merged = mergeEdges(edges, p.edges);
        List<Point> res = new ArrayList<>();
        // Begin by adding the roots of each polygon. This is our initial vertex.
        Point cur = points.get(0).add(p.points.get(0));
        // Edge case if both inputs are single points.
        if(merged.isEmpty()){
            res.add(cur);
            return new Polygon(res);
        }
        // For every directed edge, move the current vertex in that direction (via vector addition).
        for(Edge e : merged){
            res.add(cur);
            cur = cur.add(e.direction());
        }
        // Use the list of vertices to construct the polygon which is the sum of this and p.
        return new Polygon(res);
    }
    // Merges two sorted list into one list sorted by radial angle from the positive x-axis.
    public static List<Edge> mergeEdges(List<Edge> list1, List<Edge> list2){
        int size = list1.size() + list2.size();
        List<Edge> res = new ArrayList<>(size);
        int p1 = 0;
        int p2 = 0;
        Edge last = null;
        // While there are still edges in both lists left to add,
        // look at the two vectors v1 and v2 which are pointed to
        // by p1 and p2, respectively. If v1 cross v2 is positive,
        // then that indicates a counter-clockwise turn from v1 to
        // v2, so v1 must have a smaller radial angle, and thus goes
        // first. If one list has already been exhausted, simply
        // add the remaining edges from the other list.
        while(p1<list1.size() || p2<list2.size()){
            Edge edge;
            if(p1>=list1.size()){
                edge = list2.get(p2++);
            }
            else if(p2>=list2.size()){
                edge = list1.get(p1++);
            }
            else{
                double orientation = list1.get(p1).orientation(list2.get(p2));
                if(orientation > 0){
                    edge = list1.get(p1++);
                }
                else{
                    edge = list2.get(p2++);
                }
            }
            // This eliminates collinear points from the output.
            // If the previous edge is parallel to the last edge,
            // merge them, remove the previous edge, and add the
            // new merged one.
            if(edge.isInDirectionOf(last)){
                edge = edge.merge(last);
                res.remove(res.size()-1);
            }
            res.add(edge);
            last = edge;
        }
        return res;
    }
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(Point p : points){
            sb.append(p);
            sb.append("\n");
        }
        return sb.toString();
    }
}

// Represents a directed edge from Point a to Point b
class Edge{
    Point a, b;
    final static Edge AXIS = new Edge(new Point(0,0), new Point(1,0)); // positive x-axis
    public Edge(Point a, Point b){
        this.a = a;
        this.b = b;
    }
    // The vector representation of the edge direction
    public Point direction(){
        return new Point(b.x-a.x, b.y-a.y);
    }
    // First checkes if the directions are parallel, then the dot product ensures they are not in opposite directions.
    public boolean isInDirectionOf(Edge e){
        if(cross(e)!=0) return false;
        return direction().dot(e.direction()) > 0;
    }
    // Vector addition of edges
    public Edge merge(Edge e){
        return new Edge(a, b.add(e.direction()));
    }
    // Cross product of the vector representation of the edges
    public double cross(Edge e){
        if(e==null) return Double.MIN_VALUE;
        double x1 = b.x-a.x;
        double y1 = b.y-a.y;
        double x2 = e.b.x-e.a.x;
        double y2 = e.b.y-e.a.y;
        return x1*y2 - x2*y1;
    }
    // Dot product of the vector representation of the edges
    public double dot(Edge e){
        return direction().dot(e.direction());
    }
    // Returns 1 if this edge has a larger radial angle than e and -1 if smaller (or in the same direction)
    public int orientation(Edge e){
        double c1 = AXIS.cross(this);
        double c2 = AXIS.cross(e);
        double d1 = AXIS.dot(this);
        double d2 = AXIS.dot(e);
        if(c1==0 && d1 > 0) return 1;
        if(c2==0 && d2 > 0) return -1;
        if(c1 > 0 && c2 < 0) return 1;
        if(c1 < 0 && c2 > 0) return -1;
        double c3 = cross(e);
        if(c3 > 0) return 1;
        return -1;
    }
    // This does what it sounds like
    public double magnitude(){
        return direction().magnitude();
    }
    public String toString(){
        return "[" + a.toString() + ", " + b.toString() + "]";
    }
}

// A simple pair of coordinates in the cartesian plane
class Point{
    double x, y;
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    // Its a dot product my guy
    public double dot(Point p){
        return x*p.x + y*p.y;
    }
    // Vector addition of points
    public Point add(Point p){
        return new Point(x+p.x, y+p.y);
    }
    // Euclidean distance from the origin to this point
    public double magnitude(){
        return Math.sqrt(x*x + y*y);
    }
    public String toString(){
        return x + " " + y;
    }
}