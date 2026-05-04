# Point class with add, subtract, and cross product
class Point:
    def __init__(self, x, y):
        self.x = x
        self.y = y

    def __add__(self, other):
        return Point(self.x + other.x, self.y + other.y)
    
    def __sub__(self, other):
        return Point(self.x - other.x, self.y - other.y)
    
    def cross(self, other):
        return self.x * other.y - self.y * other.x

# reorders (shifts) points in a polygon s.t. first element is lowest y, if equal y lowest x
# returns list of Points sorted ccw
def reorderPolygon(polygon):
    minVertexIdx = 0
    for i in range(len(polygon)):
        if (polygon[i].y < polygon[minVertexIdx].y 
            or (polygon[i].y == polygon[minVertexIdx].y and polygon[i].x < polygon[minVertexIdx].x)):
            minVertexIdx = i
    return polygon[minVertexIdx:] + polygon[:minVertexIdx]

# Poly1 -> first polygon, list of Points sorted ccw
# Poly2 -> second polygon, list of Points sorted ccw
# return -> minkowski sum of convex polygons, list of Points sorted ccw
def minkowskiSum(poly1, poly2):
    poly1 = reorderPolygon(poly1)
    poly2 = reorderPolygon(poly2)

    # two points loop around for simpler implementation
    # makes sure each edge is accounted for
    poly1.append(poly1[0])
    poly1.append(poly1[1])
    poly2.append(poly2[0])
    poly2.append(poly2[1])

    # pointers for iterating through the polygons
    ptr1 = 0
    ptr2 = 0

    polySum = []
    while (ptr1 < len(poly1)-2 or ptr2 < len(poly2)-2):
        # append sum of current points
        polySum.append(poly1[ptr1] + poly2[ptr2])

        # compute cross product, determines which edge is more ccw
        cross = (poly1[ptr1+1] - poly1[ptr1]).cross(poly2[ptr2+1] - poly2[ptr2])
        if cross > 0:
            ptr1 += 1
        elif cross < 0:
            ptr2 += 1
        else: # if equally ccw increment both
            ptr1 += 1
            ptr2 += 1
    
    return polySum


# read inputs, form:
# n m 
# n lines containing x y
# m lines containing x y

n, m = (int(i) for i in input().split(" "))
polygon1, polygon2 = [], []

for i in range(n):
    x, y = (float(j) for j in input().split(" "))
    polygon1.append(Point(x, y))

for i in range(m):
    x, y = (float(j) for j in input().split(" "))
    polygon2.append(Point(x, y))

# outputs new Minkowski Sum polygon, prints each point on a new line ccw
newPolygon = minkowskiSum(polygon1, polygon2)
for pt in newPolygon:
    print(f"{pt.x} {pt.y}")
