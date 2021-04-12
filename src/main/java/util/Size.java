package util;

public class Size {
    public double width;
    public double height;

    public Size(double width, double height){
        this.width = width;
        this.height = height;
    }

    public Size add(Size other) {
        return new Size(width + other.width, height + other.height);
    }

    public Size divided_by(Size other) {
        return new Size(width / other.width, height / other.height);
    }

    @Override
    public String toString() {
        return "Size{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}
