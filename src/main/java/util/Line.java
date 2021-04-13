package util;

import java.util.Objects;

public class Line {
    public Point a, b;
    public Line(Point a, Point b){
        this.a = a;
        this.b = b;
    }

    public double getLength(){
        return a.distance(b);
    }

    public boolean isVertical() {
        return a.subtract(b).x == 0;
    }

    public boolean isHorizontal() {
        return a.subtract(b).y == 0;
    }

    public Line toDoubleLine() {
        a = new Point(a.x, a.y);
        b = new Point(b.x, b.y);
        return this;
    }

    public double minY() {
        return Math.min(a.y, b.y);
    }

    public double maxY() {
        return Math.max(a.y, b.y);
    }

    public double minX() {
        return Math.min(a.x, b.x);
    }

    public double maxX() {
        return Math.max(a.x, b.x);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Line line = (Line) o;

        if (!Objects.equals(a, line.a)) return false;
        return Objects.equals(b, line.b);
    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        return result;
    }
}
