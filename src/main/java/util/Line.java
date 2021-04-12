package util;

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
}
