package model;

import config.EditState;
import util.PathFinder;
import util.Point;
import util.Size;
import view.DrawingAreaView;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DrawableArrow extends DrawableObject {

    private boolean requestRecalc = true;  // on attached box move
    private Path2D path;
    private final float borderWidth = .1f;   //in units

    public DrawableObject originObject, targetObject;
    public int originBorder, targetBorder;
    public double originPercentage = 0.5;
    public double targetPercentage = 0.5;

    public DrawableArrow(EditState.ArrowStyle arrowStyle, DrawableObject originObject, int originBorder, DrawableObject targetObject, int targetBorder) {
        super();
        originObject.addAttachedArrow(this);
        targetObject.addAttachedArrow(this);
        this.originObject = originObject;
        this.targetObject = targetObject;
        this.originBorder = originBorder;
        this.targetBorder = targetBorder;
    }

    @Override
    public void draw(Graphics g, Point topLeft, double pixelPerUnit) {
        Graphics2D g2d = (Graphics2D)g.create();
        if (requestRecalc) {
            var objectsOnScreen = DrawingAreaView.instance.controller.objectsOnScreen(topLeft, DrawingAreaView.DrawingArea.panelUnitSize);
            var nonArrowsOnScreen = objectsOnScreen.stream().filter((it)->!(it instanceof DrawableArrow)).collect(Collectors.toList());
            path = PathFinder.findShortestPath(this, nonArrowsOnScreen);
            requestRecalc = false;
        }
        g2d.translate(utp(0.0, topLeft.x, pixelPerUnit),utp(0.0, topLeft.y, pixelPerUnit));

        g2d.scale(pixelPerUnit, pixelPerUnit);

        BasicStroke bs = new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(bs);

        g2d.draw(path);

        g2d.scale(1.0 / pixelPerUnit, 1.0 / pixelPerUnit);
    }

    public void requestRecalc(){
        this.requestRecalc = true;
    }

    @Override
    public boolean checkPointLocation(Point unitMousePosition, boolean updateBorder) {
        return false;
    }

    @Override
    protected int isAtAnyBorder(Point unitMousePosition) {
        return 0;
    }

    @Override
    public boolean isInRectangle(Point origin, Size size) {
        return true;
    }

    @Override
    public double getBorderWidth() {
        return 0;
    }

    @Override
    public int getSelectedBorder() {
        return -1;
    }

    @Override
    public void moveEdge(int draggedEdge, Point dragMovement, Point dragOrigin) {

    }

    @Override
    public void normalizeMirroring() {

    }

    @Override
    protected void addAttachedArrow(DrawableArrow arrow) { }

    @Override
    protected void removeAttachedArrow(DrawableArrow arrow) { }

    @Override
    public List<DrawableArrow> getAttachedArrows() {
        return new ArrayList<>();
    }

    @Override
    public Rectangle2D getBounds() {
        return null;
    }

    @Override
    public Point getPercentageLocationOnBorder(double v, int originBorder) {
        return null;
    }

}
