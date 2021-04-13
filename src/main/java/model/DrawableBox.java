package model;

import config.EditState;
import org.apache.commons.math3.util.Pair;
import util.SVGPath;
import util.Size;
import util.Point;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class DrawableBox extends DrawableObject {

    private static final Color backgroundColor = Color.WHITE;
    private static final Color borderColor = Color.BLACK;
    private static final Color SELECTED_COLOR = new Color(200,200,200);
    private static final Color MOUSE_OVER_COLOR = new Color(240,240,240);
    private static final Color CIRCLE_COLOR = new Color(140,0, 0);

    private List<DrawableArrow> attachedArrows = new ArrayList<>();

    public boolean isFilled = true;
    private final float borderWidth = .1f;   //in units
    private int selectedBorder = -1;

    public Path2D path;
    public String pathText;
    public String modifiedPathText = "";

    private final AffineTransform aft = new AffineTransform();

    private final Path2D edgeCircle = SVGPath.getPath("M0 -5 C6.7 -5 6.7 5 0 5 C-6.7 5 -6.7 -5 0 -5");

    public DrawableBox(String path, Point location, Size size){
        this.pathText = path;
        this.location = location;
        this.size = size;
    }

    @Override
    public void draw(Graphics g, Point topLeft, Size panelSize, double pixelPerUnit) {
        Graphics2D g2d = (Graphics2D)g.create();

        g2d.translate(utp(location.x, topLeft.x, pixelPerUnit),utp(location.y, topLeft.y, pixelPerUnit));

        // scale to current zoom factor
        var pair = createModifiedPath(pathText, (x) -> x * size.width / 100.0, (y) -> y * size.height / 100.0);
        path = pair.getFirst();
        modifiedPathText = pair.getSecond();

        // scale to unit and apply any transformations
        g2d.scale(pixelPerUnit, pixelPerUnit);
        path.transform(aft);
        aft.setToScale(1,1);

        path.setWindingRule(Path2D.WIND_EVEN_ODD);	//Needs to be set. PPT only supports the even-odd fill-rule

        // draw background
        if (isSelected){
            g2d.setColor(SELECTED_COLOR);
        } else{
            g2d.setColor(isMouseOver ? MOUSE_OVER_COLOR : backgroundColor);
        }

        if (isFilled) g2d.fill(path);

        // draw border
        g2d.setColor(borderColor);
        BasicStroke bs = new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        g2d.setStroke(bs);
        g2d.draw(path);

        // reset scale
        g2d.scale(1.0 / pixelPerUnit, 1.0 / pixelPerUnit);

        // draw a circle on border if mouse over at 50% point
        g2d.setColor(CIRCLE_COLOR);
        if (selectedBorder != -1 && EditState.currentMouseMode == EditState.MouseMode.ARROW){
            Point circleLocation = getPercentageLocationOnBorder(.5, selectedBorder);
            g2d.translate(circleLocation.x * pixelPerUnit, circleLocation.y * pixelPerUnit);
            g2d.fill(edgeCircle);
            g2d.draw(edgeCircle);
        }

    }

    @Override
    public Point getPercentageLocationOnBorder(double percentage, int borderIndex) {
        var iterator = path.getPathIterator(null);
        Point lastLocation = new Point(0,0);
        double[] coords = new double[6];
        for (int wait = 0; wait < borderIndex; wait++) {
            var type = iterator.currentSegment(coords);
            lastLocation = getLastLocation(lastLocation, coords, type);
            iterator.next();
        }

        var type = iterator.currentSegment(coords);

        if (type == PathIterator.SEG_LINETO){
            return percOnLine(percentage, lastLocation, new Point(coords[0], coords[1]));
        }
        if (type == PathIterator.SEG_QUADTO){  // https://stackoverflow.com/a/63059651
            coords = new double[]{
                    lastLocation.x + 0.66 * (coords[0] - lastLocation.x), lastLocation.y + 0.66 * (coords[1] - lastLocation.y),
                    coords[2] + 0.66 * (coords[0] - coords[2]), coords[3] + 0.66 * (coords[1] - coords[3]),
                    coords[2], coords[3]
            };
        }

        var B = new Point(coords[0], coords[1]);
        var C = new Point(coords[2], coords[3]);
        var D = new Point(coords[4], coords[5]);
        var AB = percOnLine(percentage, lastLocation, B);
        var BC = percOnLine(percentage, B, C);
        var CD = percOnLine(percentage, C, D);
        var ABBC = percOnLine(percentage, AB, BC);
        var BCCD = percOnLine(percentage, BC, CD);
        return percOnLine(percentage, ABBC, BCCD);
    }


    private static Point percOnLine(double x, Point l1, Point l2) {
        //Return point at x% along line l1-l2
        return new Point(l1.x + x * (l2.x - l1.x),l1.y + x * (l2.y - l1.y));
    }

    private Pair<Path2D, String> createModifiedPath(String pathText, Function<Double, Double> modifyX, Function<Double, Double> modifyY) {
        java.util.List<String> tokens = SVGPath.tokenize(pathText);
        StringBuilder newPath = new StringBuilder();

        List<String> subTokens = new ArrayList<>();

        tokens.add("M"); //for the final loop
        List<String> possibleTokens = Arrays.asList("M", "L", "C", "Q");
        for (String token : tokens) {
            if (possibleTokens.contains(token.toUpperCase())) {
                assert (token.toUpperCase().equals(token)); //Should have been transformed in model.PathInterpreter

                if (subTokens.size() > 0) {
                    StringBuilder result = new StringBuilder(subTokens.get(0));
                    for (int x = 1; x < subTokens.size(); x += 2) {
                        String t1 = String.valueOf(modifyX.apply(Double.parseDouble(subTokens.get(x))));
                        String t2 = String.valueOf(modifyY.apply(Double.parseDouble(subTokens.get(x + 1))));
                        result.append(" ").append(t1).append(" ").append(t2);
                    }

                    newPath.append(result).append(" ");
                    subTokens = new ArrayList<>();
                }
            }

            subTokens.add(token);
        }

        var returnString = newPath.toString().trim();
        return new Pair<>(SVGPath.getPath(returnString), returnString);
    }


    @Override
    public boolean checkPointLocation(Point unitMousePosition, boolean updateBorder) {
        if (path == null) return false;
        boolean isInPath = path.intersects(unitMousePosition.surroundingRectangle(borderWidth * 4, location));
        if (updateBorder && isInPath){
            selectedBorder = isAtAnyBorder(unitMousePosition);
        } else {
            selectedBorder = -1;
        }
        return isInPath;
    }


    @Override
    protected int isAtAnyBorder(Point unitMousePosition) {
        var mouseRect = unitMousePosition.surroundingRectangle(borderWidth * 4, location);
        var iterator = path.getPathIterator(null);
        var index = 0;
        Point lastLocation = null;
        do {
            double[] coords = new double[6];
            int type = iterator.currentSegment(coords);
            if (lastLocation != null){
                boolean onLine = switch (type){
                    case PathIterator.SEG_LINETO -> new Line2D.Double(lastLocation.x, lastLocation.y, coords[0], coords[1]).intersects(mouseRect);
                    case PathIterator.SEG_QUADTO -> new QuadCurve2D.Double(lastLocation.x, lastLocation.y, coords[0], coords[1], coords[2], coords[3]).intersects(mouseRect);
                    case PathIterator.SEG_CUBICTO -> new CubicCurve2D.Double(lastLocation.x, lastLocation.y, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]).intersects(mouseRect);
                    default -> false;
                };
                if (onLine) return index;
            }

            lastLocation = getLastLocation(lastLocation, coords, type);

            iterator.next();
            index++;
        } while (!iterator.isDone());

        return -1;
    }


    @Override
    public void moveEdge(int draggedEdgeIndex, Point dragMovement, Point dragOrigin) {
        // determine location relative to center of shape
        Point center = new Point(path.getBounds2D().getCenterX(), path.getBounds2D().getCenterY());
        Point relativeDragPoint = dragOrigin.subtract(location).subtract(center).normalize();

        // determine percentage size increase of shape to keep edge below cursor when dragging
        Size originalSize = new Size(path.getBounds2D().getWidth(), path.getBounds2D().getHeight());
        Point direction = dragMovement.normalize();
        Size unitGrowth = new Size(dragMovement.x * Math.abs(direction.x) * relativeDragPoint.x, dragMovement.y * Math.abs(direction.y) * relativeDragPoint.y);
        Size percentageGrowth = unitGrowth.add(originalSize).divided_by(originalSize);

        // grow when pulled away from center, shrink when towards
        aft.setToScale(aft.getScaleX() * percentageGrowth.width, aft.getScaleY() * percentageGrowth.height);

        // move shape when top/left of center
        if (relativeDragPoint.y < 0.0){
            location.y += dragMovement.y;
        }
        if (relativeDragPoint.x < 0.0){
            location.x += dragMovement.x;
        }
    }

    @Override
    public void normalizeMirroring() {

        if (size.width >= 0 && size.height >= 0) return;

        Function<Double, Double> modifyX = (x)->x;
        Function<Double, Double> modifyY = (y)->y;

        // depending on mirror axes move shape and set the modify lambda to mirror around original center
        if (size.width < 0){
            size.width = -size.width;
            location.x -= size.width;
            modifyX = (x) -> 2 * SVGPath.getPath(pathText).getBounds2D().getCenterX() - x;
        }
        if (size.height < 0){
            size.height = -size.height;
            location.y -= size.height;
            modifyY = (y) -> 2 * SVGPath.getPath(pathText).getBounds2D().getCenterY() - y;
        }

        var pair = createModifiedPath(pathText, modifyX, modifyY);
        path = pair.getFirst();
        pathText = pair.getSecond();
    }

    @Override
    protected void addAttachedArrow(DrawableArrow arrow) {
        attachedArrows.add(arrow);
    }

    @Override
    protected void removeAttachedArrow(DrawableArrow arrow) {
        attachedArrows.remove(arrow);
    }


    @Override
    public List<DrawableArrow> getAttachedArrows() {
        return attachedArrows;
    }

    @Override
    public Rectangle2D getBounds() {
        if (path == null){
            return new Rectangle2D.Double(location.x, location.y, size.width, size.height);
        }
        var bounds = path.getBounds2D();

        return new Rectangle2D.Double(bounds.getX() + location.x, bounds.getY() + location.y, bounds.getWidth(), bounds.getHeight());
    }


    private Point getLastLocation(Point lastLocation, double[] coords, int type){
        return switch(type){
            case PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> new Point(coords[0], coords[1]);
            case PathIterator.SEG_QUADTO -> new Point(coords[2], coords[3]);
            case PathIterator.SEG_CUBICTO -> new Point(coords[4], coords[5]);
            default -> lastLocation;
        };
    }

    @Override
    public boolean isInRectangle(Point origin, Size rectSize) {
        var pathRect = getBounds();
        return !(pathRect.getMinX() + pathRect.getWidth() < origin.x || pathRect.getMinY() + pathRect.getHeight() < origin.y || pathRect.getMinX() > origin.x + rectSize.width || pathRect.getMinY() > origin.y + rectSize.height);
    }

    @Override
    public double getBorderWidth() {
        return borderWidth;
    }

    @Override
    public int getSelectedBorder() {
        return selectedBorder;
    }

}
