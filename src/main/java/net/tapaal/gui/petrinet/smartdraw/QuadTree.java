package net.tapaal.gui.petrinet.smartdraw;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class QuadTree {
    final int capacity = 4;
    final Boundary boundary;
    final boolean isParent;

    List<Point> points = new ArrayList<>();

    QuadTree northWest;
    QuadTree northEast;
    QuadTree southWest;
    QuadTree southEast;

    public QuadTree(Boundary boundary, boolean isParent) {
        this.boundary = boundary;
        this.isParent = isParent;
    }

    public QuadTree(Boundary boundary) {
        this(boundary, false);
    }

    public boolean insert(Point point) {
        while (!boundary.contains(point)) {
            if (!isParent) {
                return false;
            } else {
                resize();
            }
        }

        if (points.size() < capacity && northWest == null) {
            points.add(point);
            return true;
        }

        if (northWest == null) {
            subdivide();
        }

        if (northWest.insert(point)) return true;
        if (northEast.insert(point)) return true;
        if (southWest.insert(point)) return true;
        if (southEast.insert(point)) return true;

        return false;
    }

    private void subdivide() {
        Point center = boundary.getCenter();
        int halfDimension = boundary.getHalfDimension() / 2;

        Boundary northWestBoundary = new Boundary(new Point(center.x - halfDimension, center.y - halfDimension), halfDimension);
        Boundary northEastBoundary = new Boundary(new Point(center.x + halfDimension, center.y - halfDimension), halfDimension);
        Boundary southWestBoundary = new Boundary(new Point(center.x - halfDimension, center.y + halfDimension), halfDimension);
        Boundary southEastBoundary = new Boundary(new Point(center.x + halfDimension, center.y + halfDimension), halfDimension);

        northWest = new QuadTree(northWestBoundary);
        northEast = new QuadTree(northEastBoundary);
        southWest = new QuadTree(southWestBoundary);
        southEast = new QuadTree(southEastBoundary);
    }

    public List<Point> queryRange(Boundary range) {
        List<Point> pointsInRange = new ArrayList<>();
        if (!boundary.intersects(range)) {
            return pointsInRange;
        }

        for (Point point : points) {
            if (range.contains(point)) {
                pointsInRange.add(point);
            }
        }

        if (northWest == null) {
            return pointsInRange;
        }

        pointsInRange.addAll(northWest.queryRange(range));
        pointsInRange.addAll(northEast.queryRange(range));
        pointsInRange.addAll(southWest.queryRange(range));
        pointsInRange.addAll(southEast.queryRange(range));

        return pointsInRange;
    }

    public boolean containsWithin(Point point, int range) {
        if (range < 1) return false;

        Boundary boundaryToCheck = new Boundary(point, range);
        return !queryRange(boundaryToCheck).isEmpty();
    }

    public boolean contains(Point point) {
        return containsWithin(point, 1);
    }

    private void resize() {
        boundary.resize();
    
        if (northWest != null) {
            northWest.resize();
            northEast.resize();
            southWest.resize();
            southEast.resize();
        }
    
        List<Point> pointsToMove = new ArrayList<>();
        for (Point point : points) {
            QuadTree quadrant = getQuadrantForPoint(point);
            if (quadrant == null || !quadrant.contains(point)) {
                pointsToMove.add(point);
            }
        }

        points.removeAll(pointsToMove); 
    
        for (Point point : pointsToMove) {
            insert(point);
        }
    }
    
    private QuadTree getQuadrantForPoint(Point point) {
        if (point.x < boundary.getCenter().x) {
            if (point.y < boundary.getCenter().y) {
                return northWest;
            } else {
                return southWest;
            }
        } else {
            if (point.y < boundary.getCenter().y) {
                return northEast;
            } else {
                return southEast;
            }
        }
    }

    @Override
    public String toString() {
        return "quadTree[points=" + points + ", " + boundary + ']';
    }
}
