package util;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Point {
    public double x;
    public double y;

    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }

    public static Point fromPoint2D(Point2D p2d) {
        return new Point(p2d.getX(), p2d.getY());
    }

    public double distance(Point lastPoint) {
        return Math.sqrt(distanceSq(x, y, lastPoint.x, lastPoint.y));
    }

    private double distanceSq(double x1, double y1, double x2, double y2) {
        x2 -= x1;
        y2 -= y1;
        return x2 * x2 + y2 * y2;
    }

    public Point add(Point other){
        return new Point(x + other.x, y + other.y);
    }

    public Point subtract(Point other) {
        return new Point(x - other.x, y - other.y);
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }


    public Point2D toPoint2D() {
        return new Point2D.Double(x, y);
    }

    public Rectangle2D.Double surroundingRectangle(double borderWidth, Point offset) {
        return new Rectangle2D.Double(x - .5 * borderWidth - offset.x, y - .5 * borderWidth - offset.y, borderWidth, borderWidth);
    }

    public Point normalize() {
        double newX = 0;
        if (x > 0) newX = 1.0;
        if (x < 0) newX = -1.0;
        double newY = 0;
        if (y > 0) newY = 1.0;
        if (y < 0) newY = -1.0;
        if (y != 0.0 && Math.abs(x / y) > 5) newY = 0;
        if (x != 0.0 && Math.abs(y / x) > 5) newX = 0;
        return new Point(newX, newY);
    }
}
