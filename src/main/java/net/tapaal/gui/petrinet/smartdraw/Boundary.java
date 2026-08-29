package net.tapaal.gui.petrinet.smartdraw;
import java.awt.Point;

public class Boundary {
    public static final int DEFAULT_HALF_DIMENSION = 10000;
    public static final Point DEFAULT_CENTER = new Point(0, 0);

    private final Point center;
    private int halfDimension;
    
    public Boundary(Point center, int halfDimension) {
        this.halfDimension = Math.max(halfDimension, 1);
        this.center = center;
    }

    public void resize() {
        if (halfDimension <= Integer.MAX_VALUE / 2) {
            halfDimension *= 2;
        } else {
            halfDimension = Integer.MAX_VALUE;
        }
    }

    public Point getCenter() {
        return center;
    }

    public int getHalfDimension() {
        return halfDimension;
    }

    public boolean contains(Point point) {
        return Math.abs(center.x - point.x) <= halfDimension && Math.abs(center.y - point.y) <= halfDimension;
    }

    public boolean intersects(Boundary other) {
        return Math.abs(center.x - other.center.x) < halfDimension + other.halfDimension
                && Math.abs(center.y - other.center.y) < halfDimension + other.halfDimension;
    }

    @Override
    public String toString() {
        return "boundary[" +
                "center=" + center +
                ", halfDimension=" + halfDimension +
                ']';
    }
}
