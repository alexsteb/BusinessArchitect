package util;

import model.DrawableArrow;
import org.apache.commons.math3.util.Pair;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ArrowOrganizer {

    /*
    * - generate full map and put all obstacles on it
    * - go through every line of every arrow and create a group (of ArrowLines) with every other line that
    * overlaps in at least one point (on parallel line) and has no obstacle in the rectangle between them
    *   - mark other lines in group as "grouped already"
    * - if line is first or last line -> group only with other lines originating from same object, make object wall the bounding obstacle
    *   - for those, get start positions / biases from getPercentageOnBorder() for that object
    * - for each group spread out in unit distance starting from the center
    *   - if there's no space left squeeze the lines tighter
    * - for each group of lines permutate their perpendicular position to minimize the line crossings within them and each their immediate predecessor and sucessor lines (reduce arrows to 3 lines max)
     */

    static Point.Integer totalTopLeft;

    public static void beautifyArrows(List<DrawableArrow> arrows) {
        // generate full map of current screen
        var totalPlayField = generateFullMap(arrows);

        //debugDrawMap(totalPlayField);

        // adjust lines to play field coordinates
        for (var arrow : arrows){
            for (var line : arrow.getLines()){
                line.a = line.a.toInteger().subtract(totalTopLeft);
                line.b = line.b.toInteger().subtract(totalTopLeft);
            }
        }

        // group arrow lines by common obstacles
        List<ArrowLineGroup> lineGroups = groupArrowLines(arrows, totalPlayField);

        // spread lines out between obstacles
        for (var lineGroup : lineGroups){
            spreadLinesInBoundaries(lineGroup);
        }

        //TODO:
        // - do the permutation
        // - change the unit size, when zooming

        // adjust lines back to screen location
        for (var arrow : arrows){
            for (var line : arrow.getLines()){
                line.a = new Point(line.a.toInteger().x + totalTopLeft.x, line.a.toInteger().y + totalTopLeft.y);
                line.b = new Point(line.b.toInteger().x + totalTopLeft.x, line.b.toInteger().y + totalTopLeft.y);
            }
        }

        // adjust lines by box of origin and target
        spreadArrowsOnBoxBorder(arrows);

    }

    private static void spreadArrowsOnBoxBorder(List<DrawableArrow> arrows) {
        var originMates = new ArrayList<ArrowGroup>();
        var targetMates = new ArrayList<ArrowGroup>();
        var groupedIndicesOrigin = new HashSet<Integer>();
        var groupedIndicesTarget = new HashSet<Integer>();

        for (var i = 0; i < arrows.size(); i++){
            var arrow1 = arrows.get(i);
            groupedIndicesOrigin.add(i);
            groupedIndicesTarget.add(i);
            var originGroup = new ArrowGroup();
            var targetGroup = new ArrowGroup();
            originGroup.arrows.add(arrow1);
            targetGroup.arrows.add(arrow1);

            for (var j = i + 1; j < arrows.size(); j++){
                if (!groupedIndicesOrigin.contains(j)) {
                    var arrow2 = arrows.get(j);
                    if (arrow1.originObject == arrow2.originObject && arrow1.originBorder == arrow2.originBorder) {
                        groupedIndicesOrigin.add(j);
                        originGroup.arrows.add(arrow2);
                    }
                }
                if (!groupedIndicesTarget.contains(j)) {
                    var arrow2 = arrows.get(j);
                    if (arrow1.targetObject == arrow2.targetObject && arrow1.targetBorder == arrow2.targetBorder) {
                        groupedIndicesTarget.add(j);
                        targetGroup.arrows.add(arrow2);
                    }
                }
            }
            originMates.add(originGroup);
            targetMates.add(targetGroup);
        }

        var bothGroups = new ArrayList<ArrayList<ArrowGroup>>();
        bothGroups.add(originMates);
        bothGroups.add(targetMates);

        for (var mates : bothGroups){
            for (var group : mates){
                if (group.arrows.size() > 1) {
                    var distanceInBetween = 1.0 / (group.arrows.size() + 1);
                    var runningPercentage = distanceInBetween;
                    for (var arrow : group.arrows){
                        var obj = (mates == originMates) ? arrow.originObject : arrow.targetObject;
                        var location = obj.getPercentageLocationOnBorder(runningPercentage, (mates == originMates) ? arrow.originBorder : arrow.targetBorder).add(obj.location);
                        runningPercentage += distanceInBetween;
                        var lines = arrow.getLines();
                        if (mates == originMates){
                            lines.get(0).a = location;
                            if (!lines.get(0).isVertical()) {
                                lines.get(0).b.x = location.x;
                                lines.get(1).a.x = location.x;
                            } else {
                                lines.get(0).b.y = location.y;
                                lines.get(1).a.y = location.y;
                            }
                        } else {
                            lines.get(lines.size() - 1).b = location;
                            if (lines.get(lines.size() - 1).isVertical()) {
                                lines.get(lines.size() - 1).a.x = location.x;
                                lines.get(lines.size() - 2).b.x = location.x;
                            } else {
                                lines.get(lines.size() - 1).a.y = location.y;
                                lines.get(lines.size() - 2).b.y = location.y;
                            }
                        }
                    }
                    System.out.println("");
                }
            }

        }
    }

    private static void spreadLinesInBoundaries(ArrowLineGroup lineGroup) {
        var isVertical = lineGroup.arrowLines.get(0).line.isVertical();
        Set<Integer> boundaries = new HashSet<>();
        for (var obstacle : lineGroup.nearestCommonObstacles){
            boundaries.add(isVertical ? (int) obstacle.a.x : (int) obstacle.a.y);
        }
        int from = Collections.min(boundaries);
        int to = Collections.max(boundaries);

        double distanceBetweenEach;
        double linesStartFrom;
        if (boundaries.size() == 2) {
            var space = to - from;
            distanceBetweenEach = 1.0;
            if (lineGroup.arrowLines.size() > space) {
                distanceBetweenEach = (double) space / (lineGroup.arrowLines.size() + 1);
            }
            linesStartFrom = distanceBetweenEach == 1.0 ? (space - lineGroup.arrowLines.size() + 1) / 2.0 : distanceBetweenEach;
        } else {
            distanceBetweenEach = 1.0;
            linesStartFrom = 1.0;
        }
        var index = 0;
        for (var arrowLine : lineGroup.arrowLines){
            var direction = 1;
            double newLocation;
            if (boundaries.size() == 1){
                direction = (int)(isVertical ? arrowLine.line.a.x - from : arrowLine.line.a.y - from) % 2;
                newLocation = (isVertical ? arrowLine.line.a.x : arrowLine.line.a.y) + direction * (linesStartFrom + index * distanceBetweenEach);
            } else {
                newLocation = from + linesStartFrom + index * distanceBetweenEach;
            }

            arrowLine.line = arrowLine.line.toDoubleLine();
            if (isVertical) {
                arrowLine.line.a.x = newLocation;
                arrowLine.line.b.x = newLocation;
                arrowLine.setSurroundingLines(true, newLocation);
            } else {
                arrowLine.line.a.y = newLocation;
                arrowLine.line.b.y = newLocation;
                arrowLine.setSurroundingLines(false, newLocation);
            }
            index++;
        }
    }

    private static List<ArrowLineGroup> groupArrowLines(List<DrawableArrow> arrows, int[][] playField) {
        var arrowLines = new ArrayList<ArrowLine>();
        arrows.forEach((arrow) -> arrow.getLines().forEach((line)-> arrowLines.add(new ArrowLine(line, arrow))));

        ArrowLine.playField = playField;

        // find direct parallels for each arrow line
        var lineGroups = new ArrayList<ArrowLineGroup>();
        for (int i = 1; i < arrowLines.size() - 1; i++){
            var line1 = arrowLines.get(i);
            if (line1.inGroup) continue;
            line1.inGroup = true;
            var lineGroup = new ArrowLineGroup();
            lineGroup.arrowLines.add(line1);

            for (int j = i + 1; j < arrowLines.size() - 1; j++){
                var line2 = arrowLines.get(j);
                if (line2.inGroup || line2.parent == line1.parent) continue;
                if (line1.unobstructedParallelTo(line2)){
                    lineGroup.arrowLines.add(line2);
                    line2.inGroup = true;
                }
            }
            lineGroups.add(lineGroup);
        }

        // find all obstacles on map that affect at least one line per group
        for (var lineGroup : lineGroups){
            Set<Line> obstacles = new HashSet<>();
            for (var line : lineGroup.arrowLines){
                obstacles.addAll(line.getObstacles());
            }
            lineGroup.nearestCommonObstacles.addAll(obstacles);
        }

        return lineGroups;
    }



    private static int[][] generateFullMap(List<DrawableArrow> arrows) {
        List<int[][]> playFields = arrows.stream().map((it)->it.playingField).collect(Collectors.toList());
        List<Point> playFieldTopLefts = arrows.stream().map((it)->it.playingFieldTopLeft).collect(Collectors.toList());

        totalTopLeft = new Point.Integer(Integer.MAX_VALUE, Integer.MAX_VALUE);
        var totalPlayFieldWidth = 1;
        var totalPlayFieldHeight = 1;
        for (int x = 0; x < playFields.size(); x++){
            var topLeft = playFieldTopLefts.get(x).toInteger();
            if (topLeft.x < totalTopLeft.x) totalTopLeft.x = topLeft.x;
            if (topLeft.y < totalTopLeft.y) totalTopLeft.y = topLeft.y;
        }
        for (int x = 0; x < playFieldTopLefts.size(); x++){
            var playField = playFields.get(x);
            var topLeft = playFieldTopLefts.get(x).toInteger();
            if (totalTopLeft.x + playField[0].length - topLeft.x > totalPlayFieldWidth) totalPlayFieldWidth = totalTopLeft.x + playField[0].length - topLeft.x;
            if (totalTopLeft.y + playField.length - topLeft.y > totalPlayFieldHeight) totalPlayFieldHeight = totalTopLeft.y + playField.length - topLeft.y;
        }
        int[][] totalPlayField = new int[totalPlayFieldHeight][totalPlayFieldWidth];

        // draw obstacles onto global map
        for (int i = 0; i < playFields.size(); i++){
            var difference = playFieldTopLefts.get(i).toInteger().subtract(totalTopLeft);
            for (int y = difference.y; y < playFields.get(i).length + difference.y; y++){
                for (int x = difference.x; x < playFields.get(i)[0].length + difference.x; x++){
                    var fieldValue = playFields.get(i)[y - difference.y][x - difference.x];
                    if (fieldValue != 0) totalPlayField[y][x] = fieldValue;
                }
            }
        }
        return totalPlayField;
    }

    private static void debugDrawMap(int[][] map){
        System.out.println("");
        var width = 3;
        for (int[] line : map){
            for (int value : line) {
                var strValue = "" + value;
                while (strValue.length() < width) strValue = " " + strValue;
                System.out.print(strValue);
            }
            System.out.println("");
        }
    }
}

class ArrowGroup{
    public List<DrawableArrow> arrows;
    public ArrowGroup(){
        arrows = new ArrayList<>();
    }
}

class ArrowLineGroup{
    public List<ArrowLine> arrowLines;
    public List<Line> nearestCommonObstacles;

    public ArrowLineGroup(List<ArrowLine> arrowLines, List<Line> nearestObstacles){
        this.arrowLines = arrowLines;
        this.nearestCommonObstacles = nearestObstacles;
    }

    public ArrowLineGroup(){
        arrowLines = new ArrayList<>();
        nearestCommonObstacles = new ArrayList<>();
    }
}

class ArrowLine{
    public Line line;
    public DrawableArrow parent;
    public boolean inGroup = false;
    public static int[][] playField;
    private List<Line> obstacles = null;

    public ArrowLine(Line line, DrawableArrow parent){
        this.line = line;
        this.parent = parent;
    }

    public boolean unobstructedParallelTo(ArrowLine other) {
        if (line.isVertical() != other.line.isVertical()) return false;
        int minX = (int)Math.min(line.minX(), other.line.minX());
        int minY = (int)Math.min(line.minY(), other.line.minY());
        int maxX = (int)Math.max(line.maxX(), other.line.maxX());
        int maxY = (int)Math.max(line.maxY(), other.line.maxY());

        if (!line.isVertical() && (line.minX() >= other.line.maxX() || line.maxX() <= other.line.minX())) return false;
        if (line.isVertical() && (line.minY() >= other.line.maxY() || line.maxY() <= other.line.minY())) return false;

        for (int y = minY; y <= maxY; y++){
            for (int x = minX; x <= maxX; x++){
                if (playField[y][x] == -1) return false;
            }
        }
        return true;
    }

    public List<Line> getObstacles() {
        if (obstacles != null) return obstacles;
        obstacles = new ArrayList<>();

        if (line.isVertical()){
            var leftObstacleX = -1;
            var rightObstacleX = -1;

            verticalOuterLeft:
            for (int x = (int)line.a.x - 1; x >= 0; x--){
                for (int y = (int)line.minY(); y <= (int)line.maxY(); y++){
                    if (playField[y][x] == -1) {
                        leftObstacleX = x;
                        break verticalOuterLeft;
                    }
                }
            }
            verticalOuterRight:
            for (int x = (int)line.a.x + 1; x < playField[0].length; x++){
                for (int y = (int)line.minY(); y <= (int)line.maxY(); y++){
                    if (playField[y][x] == -1) {
                        rightObstacleX = x;
                        break verticalOuterRight;
                    }
                }
            }

            // save found obstacles
            for (var obstacleLocation : new int[]{leftObstacleX, rightObstacleX}){
                if (obstacleLocation < 0) continue;
                var obstacleFrom = -1;
                for (int y = 0; y < playField.length; y++){
                    var playFieldLocation = playField[y][obstacleLocation];
                    if (playFieldLocation == -1 && obstacleFrom == -1) obstacleFrom = y;
                    if (playFieldLocation != -1 && obstacleFrom != -1) {
                        obstacles.add(new Line(new Point.Integer(obstacleLocation, obstacleFrom), new Point.Integer(obstacleLocation, y - 1)));
                        obstacleFrom = -1;
                    }
                }
                if (obstacleFrom != -1) obstacles.add(new Line(new Point.Integer(obstacleLocation, obstacleFrom), new Point.Integer(obstacleLocation, playField.length - 1)));
            }
            obstacles = obstacles.stream().filter((it) -> it.maxY() >= line.minY() && it.minY() <= line.maxY()).collect(Collectors.toList());
        } else {
            var topObstacleY = -1;
            var bottomObstacleY = -1;

            horizontalOuterTop:
            for (int y = (int)line.a.y - 1; y >= 0; y--){
                for (int x = (int)line.minX(); x <= (int)line.maxX(); x++){
                    if (playField[y][x] == -1) {
                        topObstacleY = y;
                        break horizontalOuterTop;
                    }
                }
            }
            horizontalOuterBottom:
            for (int y = (int)line.a.y + 1; y < playField.length; y++){
                for (int x = (int)line.minX(); x <= (int)line.maxX(); x++){
                    if (playField[y][x] == -1) {
                        bottomObstacleY = y;
                        break horizontalOuterBottom;
                    }
                }
            }

            // save found obstacles
            for (var obstacleLocation : new int[]{topObstacleY, bottomObstacleY}){
                if (obstacleLocation < 0) continue;
                var obstacleFrom = -1;
                for (int x = 0; x < playField[0].length; x++){
                    var playFieldLocation = playField[obstacleLocation][x];
                    if (playFieldLocation == -1 && obstacleFrom == -1) obstacleFrom = x;
                    if (playFieldLocation != -1 && obstacleFrom != -1) {
                        obstacles.add(new Line(new Point.Integer(obstacleFrom, obstacleLocation), new Point.Integer(x - 1, obstacleLocation)));
                        obstacleFrom = -1;
                    }
                }
                if (obstacleFrom != -1) obstacles.add(new Line(new Point.Integer(obstacleFrom, obstacleLocation), new Point.Integer(playField[0].length - 1, obstacleLocation)));
            }
            obstacles = obstacles.stream().filter((it) -> it.maxX() >= line.minX() && it.minX() <= line.maxX()).collect(Collectors.toList());
        }

        return obstacles;
    }

    public void setSurroundingLines(boolean setX, double newValue) {
        var myIndex = parent.getLines().indexOf(this.line);
        if (myIndex > 0){
            var previous = parent.getLines().get(myIndex - 1).b;
            if (setX) previous.x = newValue;
                else previous.y = newValue;
        }
        if (myIndex < parent.getLines().size() - 1){
            var next = parent.getLines().get(myIndex + 1).a;
            if (setX) next.x = newValue;
                else next.y = newValue;
        }
    }
}