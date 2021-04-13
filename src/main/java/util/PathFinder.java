package util;

import model.DrawableArrow;
import model.DrawableObject;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PathFinder {

    static int[][] playingField;

    public static ArrayList<Line> findShortestPath(DrawableArrow arrow, List<DrawableObject> nonArrowsOnScreen, Point screenTopLeft, Size screenSize){
        List<DrawableObject> objects = nonArrowsOnScreen;
        List<Rectangle2D> bounds = nonArrowsOnScreen.stream().map(DrawableObject::getBounds).collect(Collectors.toList());
        var screenRect = new Rectangle2D.Double(screenTopLeft.x, screenTopLeft.y, screenSize.width, screenSize.height);

        Point.Integer topLeft = null;
        Point.Integer bottomRight = null;
        Point originBias = null;
        Point targetBias = null;
        Point.Integer integerOriginPoint = null;
        Point.Integer integerTargetPoint = null;
        var foundPath = false;

        while(!foundPath) {
            // create playing field that surrounds all objects on screen
            var topLeftDouble = new Point(Double.MAX_VALUE, Double.MAX_VALUE);
            var bottomRightDouble = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE);

            for (var bound : bounds) {
                if (bound.getX() < topLeftDouble.x) topLeftDouble.x = bound.getX();
                if (bound.getX() + bound.getWidth() > bottomRightDouble.x) bottomRightDouble.x = bound.getX() + bound.getWidth();
                if (bound.getY() < topLeftDouble.y) topLeftDouble.y = bound.getY();
                if (bound.getY() + bound.getHeight() > bottomRightDouble.y) bottomRightDouble.y = bound.getY() + bound.getHeight();
            }

            // add a thickness of 2: 1 for path finding, 1 for extra padding around boxes
            topLeft = new Point.Integer((int)Math.round(topLeftDouble.x) - 2, (int)Math.round(topLeftDouble.y) - 2);
            bottomRight = new Point.Integer((int)Math.round(bottomRightDouble.x) + 2, (int)Math.round(bottomRightDouble.y) + 2);


            // find start and target point and remember bias - move to screen border if outside
            var p1 = arrow.getOriginLocation(true);
            p1 = moveIntoRectangle(p1, screenRect);
            integerOriginPoint = new Point.Integer((int) Math.round(p1.x - topLeft.x), (int) Math.round(p1.y - topLeft.y));
            originBias = integerOriginPoint.toDoublePoint().subtract(p1.subtract(topLeft));

            var pFinal = arrow.getTargetLocation(true);
            pFinal = moveIntoRectangle(pFinal, screenRect);
            integerTargetPoint = new Point.Integer((int) Math.round(pFinal.x - topLeft.x), (int) Math.round(pFinal.y - topLeft.y));
            targetBias = integerTargetPoint.toDoublePoint().subtract(pFinal.subtract(topLeft));

            // arrow origins or targets out of screen to the right -> increase play field
            bottomRight = bottomRight.maxOf(integerTargetPoint).maxOf(integerOriginPoint);

            // having arrow origins or targets out of screen at negative locations -> shift everything to the right (arrays can't go negative)
            var outOfScreenBias = new Point.Integer(0, 0);
            if (integerOriginPoint.x < 0) outOfScreenBias.x = -integerOriginPoint.x;
            if (integerOriginPoint.y < 0) outOfScreenBias.y = -integerOriginPoint.y;
            if (integerTargetPoint.x < 0 && integerTargetPoint.x < integerOriginPoint.x) outOfScreenBias.x = -integerTargetPoint.x;
            if (integerTargetPoint.y < 0 && integerTargetPoint.y < integerOriginPoint.y) outOfScreenBias.y = -integerTargetPoint.y;
            topLeft = topLeft.subtract(outOfScreenBias);
            integerOriginPoint = integerOriginPoint.add(outOfScreenBias);
            integerTargetPoint = integerTargetPoint.add(outOfScreenBias);

            playingField = new int[(bottomRight.y - topLeft.y)][(bottomRight.x - topLeft.x)];


            // fill all objects onto playing field (at integer locations)
            var boundIndex = 0;
            for (var bound : bounds) {
                for (int y = (int) Math.round(bound.getY()); y < Math.round(bound.getY() + bound.getHeight()); y++) {
                    for (int x = (int) Math.round(bound.getX()); x < Math.round(bound.getX() + bound.getWidth()); x++) {
                        if (objects.get(boundIndex).checkPointLocation(new Point(x, y), false)) {
                            var point = new Point.Integer(x - (int) topLeft.x, y - (int) topLeft.y);
                            playingField[point.y][point.x] = -1;
                            var directNeighbors = getSurroundings(point, playingField, false);

                            // make every obstacle double-thick EXCEPT the target & origin points and their direct neighbors
                            Point.Integer finalIntegerOriginPoint = integerOriginPoint;
                            Point.Integer finalIntegerTargetPoint = integerTargetPoint;
                            if (!point.equals(integerOriginPoint) && !point.equals(integerTargetPoint)
                                    && directNeighbors.stream().noneMatch((it) -> it.equals(finalIntegerOriginPoint) || it.equals(finalIntegerTargetPoint))) {
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
            try {
                playingField[integerOriginPoint.y][integerOriginPoint.x] = 1;
                playingField[integerTargetPoint.y][integerTargetPoint.x] = 0;
            } catch(ArrayIndexOutOfBoundsException e){
                System.out.println("");
            }
            recursivePathFinder(integerOriginPoint, integerTargetPoint);


            // one-by-one remove bounds, until path can be found
            foundPath = playingField[integerTargetPoint.y][integerTargetPoint.x] > 0;
            if (!foundPath){
                // If all bounds were deleted and still no path can be found
                if (bounds.isEmpty()) return new ArrayList<>();

                var minDistance = Double.MAX_VALUE;
                Rectangle2D minDistanceBound = null;
                for (var bound : bounds){
                    // do not remove origin & target objects
                    if (bound.equals(arrow.originObject.getBounds()) || bound.equals(arrow.targetObject.getBounds())) continue;
                    var distance = integerTargetPoint.distanceToRect(bound);
                    if (distance < minDistance) {
                        minDistance = distance;
                        minDistanceBound = bound;
                    }
                }
                if (minDistanceBound != null) {
                    objects.remove(bounds.indexOf(minDistanceBound));
                    bounds.remove(minDistanceBound);
                } else {
                    return new ArrayList<>();  // nothing to improve
                }
            }
        }

        // retrieve quickest path
        var runningPoint = integerTargetPoint;
        var lastDirection = new Point.Integer(0,0);
        List<Point.Integer> pathPoints = new ArrayList<>();
        pathPoints.add(runningPoint);

        outerLoop:
        while (!runningPoint.equals(integerOriginPoint)){
            var surroundings = getSurroundings(runningPoint, playingField, false);
            var currentValue = playingField[runningPoint.y][runningPoint.x];

            var fittingLocations = surroundings.stream().filter((location)->
                        playingField[location.y][location.x] == currentValue - 1
            ).collect(Collectors.toList());

            // favor points that go the same direction
            Point.Integer direction = new Point.Integer(0,0);
            for (var location : fittingLocations){
                direction = location.subtract(runningPoint).normalize().toInteger();
                if (direction.equals(lastDirection)){
                    pathPoints.add(0, location);
                    runningPoint = location;
                    continue outerLoop;
                }
            }
            pathPoints.add(0, fittingLocations.get(0));
            runningPoint = fittingLocations.get(0);
            lastDirection = direction;
        }

        // clean up playing field
        var outPlayingField = new int[(int) (bottomRight.y - topLeft.y)][(int) (bottomRight.x - topLeft.x)];
        for (int y = 0; y < playingField.length; y++){
            for (int x = 0; x < playingField[0].length; x++){
                var value = playingField[y][x];
                if (value <= 0) {
                    outPlayingField[y][x] = value;
                }
            }
        }
        for (var point : pathPoints){
            outPlayingField[point.y][point.x] = 2;
        }
        outPlayingField[integerOriginPoint.y][integerOriginPoint.x] = 3;
        outPlayingField[integerTargetPoint.y][integerTargetPoint.x] = 4;

        arrow.playingField = outPlayingField;
        arrow.playingFieldTopLeft = topLeft;



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

        // adjust first and last line for biases (non-integer locations)
        if (lines.size() > 1){
            var firstLine = lines.get(0).toDoubleLine();
            if (firstLine.isVertical()){
                firstLine.b.x = firstLine.b.x - originBias.x;
            } else {
                firstLine.b.y = firstLine.b.y - originBias.y;
            }
            firstLine.a = firstLine.a.subtract(originBias);
            lines.get(1).a = firstLine.b;

            var lastLine = lines.get(lines.size() - 1).toDoubleLine();
            if (lastLine.isVertical()){
                lastLine.a.x = lastLine.a.x - targetBias.x;
            } else {
                lastLine.a.y = lastLine.a.y - targetBias.y;
            }
            lastLine.b = lastLine.b.subtract(targetBias);
            lines.get(lines.size() - 2).b = lastLine.a;
        }

        if (lines.size() == 1){
            var line = lines.get(0).toDoubleLine();
            if (line.isVertical()){
                line.a.y = line.a.y - originBias.y;
                line.b.y = line.b.y - targetBias.y;
            } else {
                line.a.x = line.a.x - originBias.x;
                line.b.x = line.b.x - targetBias.x;
            }
        }

        // add screen location to lines
        for (var line : lines){
            line.a = line.a.add(topLeft);
            line.b = line.b.add(topLeft);
        }

        return lines;

    }

    private static Point moveIntoRectangle(Point p, Rectangle2D.Double rect) {
        if (p.x < rect.getMinX()) p.x = rect.getMinX()-1;
        if (p.x > rect.getMaxX()) p.x = rect.getMaxX()+1;
        if (p.y < rect.getMinY()) p.y = rect.getMinY()-1;
        if (p.y > rect.getMaxY()) p.y = rect.getMaxY()+1;
        return p;
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
