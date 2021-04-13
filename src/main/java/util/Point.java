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

    public double distanceToRect(Rectangle2D rect){
        var dx = Math.max(0.0, Math.max(rect.getMinX() - x, x - rect.getMaxX()));
        var dy = Math.max(0.0, Math.max(rect.getMinY() - y, y - rect.getMaxY()));
        return Math.sqrt(dx*dx + dy*dy);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (Double.compare(point.x, x) != 0) return false;
        return Double.compare(point.y, y) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
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

    public Size toSize() {
        return new Size(x, y);
    }

    public Integer toInteger() {
        var integer = new Integer((int)x, (int)y);
        return integer;
    }



    static class Integer extends Point {
        public int x;
        public int y;
        public Integer(int x, int y){
            super(x, y);
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }

        @Override
        public Integer add(Point other){
            return new Integer((int)(x + other.x), (int)(y + other.y));
        }

        @Override
        public Integer subtract(Point other) {
            return new Integer((int)(x - other.x), (int)(y - other.y));
        }

        public Integer add(Integer other){
            return new Integer(x + other.x, y + other.y);
        }
        public Integer subtract(Integer other) {
            return new Integer(x - other.x, y - other.y);
        }

        public Point toDoublePoint() {
            return new Point(x, y);
        }

        public Integer minOf(Integer other) {
            return new Integer(Math.min(x, other.x), Math.min(y, other.y));
        }
        public Integer maxOf(Integer other) {
            return new Integer(Math.max(x, other.x), Math.max(y, other.y));
        }
    }
}
