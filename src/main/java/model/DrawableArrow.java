package model;

import config.EditState;
import util.Line;
import util.PathFinder;
import util.Point;
import util.Size;
import view.DrawingAreaView;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DrawableArrow extends DrawableObject {

    private Path2D path;
    private List<Line> lines = new ArrayList<>();
    private final float borderWidth = .1f;   //in units

    private EditState.ArrowStyle arrowStyle;
    public DrawableObject originObject, targetObject;
    public int originBorder, targetBorder;
    public double originPercentage = 0.5;
    public double targetPercentage = 0.5;
    private Point originLocation;
    private Point targetLocation;

    public int[][] playingField;
    public Point playingFieldTopLeft;


    public DrawableArrow(EditState.ArrowStyle arrowStyle, DrawableObject originObject, int originBorder, DrawableObject targetObject, int targetBorder) {
        super();
        this.arrowStyle = arrowStyle;
        this.originObject = originObject;
        this.targetObject = targetObject;
        this.originBorder = originBorder;
        this.targetBorder = targetBorder;
        originObject.addAttachedArrow(this);
        targetObject.addAttachedArrow(this);
    }

    @Override
    public void draw(Graphics g, Point topLeft, Size panelSize, double pixelPerUnit) {
        Graphics2D g2d = (Graphics2D)g.create();

        g2d.translate(utp(0.0, topLeft.x, pixelPerUnit),utp(0.0, topLeft.y, pixelPerUnit));

        g2d.scale(pixelPerUnit, pixelPerUnit);

        BasicStroke bs = new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(bs);

        // create final path from lines
        var path = new Path2D.Double();
        if (lines.size() > 0) {
            path.moveTo(lines.get(0).a.x, lines.get(0).a.y);
            for (var line : lines) {
                path.lineTo(line.b.x, line.b.y);
            }
        }

        g2d.draw(path);

        g2d.scale(1.0 / pixelPerUnit, 1.0 / pixelPerUnit);
    }

    public void recalculate(Point topLeft, Size panelSize){
        var objects = DrawingAreaView.instance.controller.objects;
        var nonArrows = objects.stream().filter((it)->
                        !(it instanceof DrawableArrow) && it.isInRectangle(topLeft, panelSize)
                ).collect(Collectors.toList());
        lines = PathFinder.findShortestPath(this, nonArrows, topLeft, panelSize);
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
        var rect = new Rectangle2D.Double(origin.x, origin.y, size.width, size.height);
        for (var line : lines){
            if (rect.intersectsLine(line.a.x, line.a.y, line.b.x, line.b.y)) return true;
        }
        return false;
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

    public List<Line> getLines() {
        return lines;
    }

    public Point getOriginLocation(boolean calculateNew) {
        if (originLocation == null || calculateNew){
            originLocation = originObject.getPercentageLocationOnBorder(originPercentage, originBorder).add(originObject.location);
        }
        return originLocation;
    }

    public Point getTargetLocation(boolean calculateNew) {
        if (targetLocation == null || calculateNew){
            targetLocation = targetObject.getPercentageLocationOnBorder(targetPercentage, targetBorder).add(targetObject.location);
        }
        return targetLocation;
    }
}
