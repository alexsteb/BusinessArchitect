package model;


import util.Size;
import util.Point;
import java.awt.*;


public abstract class DrawableObject {

    public Point location; //top left corner
    public Size size;
    public String type; //Box, Arrow, File, Image, Text
    public boolean isSelected;
    public boolean isMouseOver;


    //Draw object with topLeft being the panel's top-left corner in units
    public abstract void draw(Graphics g, Point topLeft, double pixelPerUnit);

    //Check whether object is selectable at provided x,y position
    public abstract boolean checkMousePosition(Point unitMousePosition);

    //Check whether mouse position is at a (linear) border of object and return the index of said border in the shape's path
    protected abstract int isAtAnyBorder(Point unitMousePosition);

    //Check whether object is contained within rectangle bounds
    public abstract boolean isInRectangle(Point origin, Size size);

    public abstract double getBorderWidth();
    public abstract int getSelectedBorder();


    public abstract void moveEdge(int draggedEdge, Point dragMovement, Point dragOrigin);
}
