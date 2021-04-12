package util;

import model.DrawableArrow;
import model.DrawableObject;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
steps:
- create playing field around all objects on Screen  + 1 point
- fill in start and end point (nearest location)
    - remember x,y biases to adjust in the final path
- for each object
    - get the bounding rectangle
    - check for every point in rectangle whether it is inside object
    - fill in values in playing field accordingly
- use playing field to calculate shortest path
- create lines from shortest path
- adjust first and last line acc. to biases
 */

public class PathFinder {

    static int[][] playingField;

    public static Path2D findShortestPath(DrawableArrow arrow, List<DrawableObject> nonArrowsOnScreen){
        List<Rectangle2D> bounds = nonArrowsOnScreen.stream().map(DrawableObject::getBounds).collect(Collectors.toList());

        // create playing field that surrounds all objects on screen
        Point topLeft = new Point(Double.MAX_VALUE, Double.MAX_VALUE);
        Point bottomRight = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE);
        for (var bound : bounds){
            if (bound.getX() < topLeft.x) topLeft.x = bound.getX();
            if (bound.getX() + bound.getWidth() > bottomRight.x) bottomRight.x = bound.getX() + bound.getWidth();
            if (bound.getY() < topLeft.y) topLeft.y = bound.getY();
            if (bound.getY() + bound.getHeight() > bottomRight.y) bottomRight.y = bound.getY() + bound.getHeight();
        }

        // add a thickness of 2: 1 for path finding, 1 for extra padding around boxes
        topLeft = new Point(Math.round(topLeft.x) - 2, Math.round(topLeft.y) - 2);
        bottomRight = new Point(Math.round(bottomRight.x) + 2, Math.round(bottomRight.y) + 2);
        playingField = new int[(int)(bottomRight.y - topLeft.y)][(int)(bottomRight.x - topLeft.x)];

        // find start and target point and remember bias
        var p1 = arrow.originObject.getPercentageLocationOnBorder(arrow.originPercentage, arrow.originBorder).add(arrow.originObject.location);
        var integerOriginPoint = new Point.Integer((int) Math.round(p1.x - topLeft.x), (int) Math.round(p1.y - topLeft.y));
        Point originBias = integerOriginPoint.subtract(p1.subtract(topLeft));

        var pFinal = arrow.targetObject.getPercentageLocationOnBorder(arrow.targetPercentage, arrow.targetBorder).add(arrow.targetObject.location);
        var integerTargetPoint = new Point.Integer((int) Math.round(pFinal.x - topLeft.x), (int) Math.round(pFinal.y - topLeft.y));
        Point targetBias = integerTargetPoint.subtract(pFinal.subtract(topLeft));



        // fill all objects onto playing field (at integer locations)
        var boundIndex = 0;
        for (var bound : bounds){
            for (int y = (int) Math.round(bound.getY()); y < Math.round(bound.getY() + bound.getHeight()); y++){
                for (int x = (int) Math.round(bound.getX()); x < Math.round(bound.getX() + bound.getWidth()); x++){
                    if (nonArrowsOnScreen.get(boundIndex).checkPointLocation(new Point(x, y), false)){
                        var point = new Point.Integer(x - (int)topLeft.x, y - (int)topLeft.y);
                        playingField[point.y][point.x] = -1;
                        var directNeighbors = getSurroundings(point, playingField, false);

                        // make every obstacle double-thick EXCEPT the target & origin points and their direct neighbors
                        if (!point.equals(integerOriginPoint) && !point.equals(integerTargetPoint)
                                && directNeighbors.stream().noneMatch((it)->it.equals(integerOriginPoint) || it.equals(integerTargetPoint))) {
                            for (var surrounding : getSurroundings(point, playingField, true)) {
                                playingField[surrounding.y][surrounding.x] = -1;
                            }
                        }
                    }

                }
            }
            boundIndex++;
        }

        // find path
        playingField[integerOriginPoint.y][integerOriginPoint.x] = 1;
        playingField[integerTargetPoint.y][integerTargetPoint.x] = 0;
        recursivePathFinder(integerOriginPoint, integerTargetPoint);


        // no path found
        // TODO: incrementally delete bounds
        if (playingField[integerTargetPoint.y][integerTargetPoint.x] <= 0){
            return new Path2D.Double();
        }

        // retrieve quickest path
        var runningPoint = integerTargetPoint;
        List<Point.Integer> pathPoints = new ArrayList<>();
        pathPoints.add(runningPoint);

        while (!runningPoint.equals(integerOriginPoint)){
            var surroundings = getSurroundings(runningPoint, playingField, false);
            var currentValue = playingField[runningPoint.y][runningPoint.x];
            for (var location : surroundings){
                if (playingField[location.y][location.x] == currentValue - 1){
                    pathPoints.add(0, location);
                    runningPoint = location;
                    break;
                }
            }
        }

        // combine path points to single lines
        var lines = new ArrayList<Line>();
        Point.Integer lastPoint = null;
        Point.Integer lineOrigin = null;
        for (var point : pathPoints){
            if (lastPoint != null){
                var difference = point.subtract(lineOrigin);
                var hadCorner = difference.x * difference.y != 0.0;
                if (hadCorner){
                    lines.add(new Line(lineOrigin, lastPoint));
                    lineOrigin = lastPoint;
                }
                if (point == pathPoints.get(pathPoints.size() - 1)){
                    lines.add(new Line(lineOrigin, point));
                }
            }
            lastPoint = point;
            if (lineOrigin == null) lineOrigin = point;
        }

        // adjust first and last line for biases
        if (lines.size() > 1){
            var firstLine = lines.get(0);
            if (firstLine.isVertical()){
                firstLine.b.x = firstLine.b.x - originBias.x;
            } else {
                firstLine.b.y = firstLine.b.y - originBias.y;
            }
            firstLine.a = firstLine.a.subtract(originBias);

            var lastLine = lines.get(lines.size() - 1);
            if (lastLine.isVertical()){
                lastLine.a.x = lastLine.a.x - targetBias.x;
            } else {
                lastLine.a.y = lastLine.a.y - targetBias.y;
            }
            lastLine.b = lastLine.b.subtract(targetBias);
        }

        if (lines.size() == 1){
            var line = lines.get(0);
            if (line.isVertical()){
                line.a.y = line.a.y - originBias.y;
                line.b.y = line.b.y - targetBias.y;
            } else {
                line.a.x = line.a.x - originBias.x;
                line.b.x = line.b.x - targetBias.x;
            }
        }

        var path = new Path2D.Double();
        path.moveTo(lines.get(0).a.x + topLeft.x, lines.get(0).a.y + topLeft.y);
        for (var line : lines){
            path.lineTo(line.b.x + topLeft.x, line.b.y + topLeft.y);
        }

        return path;

    }

    private static List<Point.Integer> getSurroundings(Point.Integer point, int[][] field, boolean diagonally){
        // set up surrounding points if not at edge
        List<Point.Integer> surroundingLocations = new ArrayList<>();
        if (diagonally){
            if (point.x > 0 && point.y > 0) surroundingLocations.add(new Point.Integer(point.x - 1, point.y - 1));
            if (point.x < field[0].length - 1 && point.y > 0) surroundingLocations.add(new Point.Integer(point.x + 1, point.y - 1));
            if (point.x > 0 && point.y < field.length - 1) surroundingLocations.add(new Point.Integer(point.x - 1, point.y + 1));
            if (point.x < field[0].length - 1 && point.y < field.length - 1) surroundingLocations.add(new Point.Integer(point.x + 1, point.y + 1));
        } else {
            if (point.x > 0) surroundingLocations.add(new Point.Integer(point.x - 1, point.y));
            if (point.x < field[0].length - 1) surroundingLocations.add(new Point.Integer(point.x + 1, point.y));
            if (point.y > 0) surroundingLocations.add(new Point.Integer(point.x, point.y - 1));
            if (point.y < field.length - 1) surroundingLocations.add(new Point.Integer(point.x, point.y + 1));
        }
        return  surroundingLocations;
    }


    private static void recursivePathFinder(Point.Integer point, Point.Integer target){
     var valueAtPoint = playingField[point.y][point.x];
        List<Point.Integer> surroundingLocations = getSurroundings(point, playingField, false);

        // check every surrounding point - if new (0) or higher, add as new waypoint and recursively enter
        for (var location : surroundingLocations){
            if (location.equals(target)){
                playingField[location.y][location.x] = valueAtPoint + 1;
                return;
            }

            var valueAtLocation = playingField[location.y][location.x];
            if (valueAtLocation == 0 || valueAtLocation > valueAtPoint + 1){
                playingField[location.y][location.x] = valueAtPoint + 1;
                recursivePathFinder(location, target);
            }
        }
    }
}
