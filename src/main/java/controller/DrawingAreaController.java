package controller;

import config.EditState;
import intf.Controller;
import model.DrawableArrow;
import model.DrawableBox;
import model.DrawableObject;
import util.ArrowOrganizer;
import util.Point;
import util.Size;
import view.DrawingAreaView;


import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DrawingAreaController implements Controller {

    public List<DrawableObject> objects = new ArrayList<>();
    public double pixelPerUnit;



    private enum DrawingAreaState {
        NORMAL, DRAGGING_SCREEN, DRAGGING_OBJECT, PULLING_OBJECT_EDGE, DRAWING_ARROW, DRAWING_BOX
    }

    private DrawingAreaState state = DrawingAreaState.NORMAL;
    private DrawingAreaView view;

    public void init(DrawingAreaView view){
        this.view = view;
    }

    private Point unitPosTopLeft;
    private Size panelSize;

    public List<DrawableObject> objectsOnScreen(Point unitPosTopLeft, Size panelSize) {
        this.unitPosTopLeft = unitPosTopLeft;
        this.panelSize = panelSize;
        //TODO delete
        if (objects.isEmpty()) {
            objects.add(new DrawableBox("M0 0 L100 0 L100 100 L0 100 L0 0", new Point(-25.0,-5.0), new Size(10, 10)));
            objects.add(new DrawableBox("M0 0 L80 0 L100 100 L0 100 Q50 50 0 0", new Point(-5.0,-5.0), new Size(10, 10)));
            objects.add(new DrawableBox("M0 0 L100 0 L100 100 L0 100 L0 0", new Point(15.0,-5.0), new Size(10, 10)));
            objects.add(new DrawableArrow(EditState.ArrowStyle.LINE, objects.get(0), 1, objects.get(2), 4));
            objects.add(new DrawableArrow(EditState.ArrowStyle.LINE, objects.get(0), 1, objects.get(2), 4));
        }

        return objects.stream().filter((it) -> it.isInRectangle(unitPosTopLeft, panelSize)).collect(Collectors.toList());
    }

    public List<DrawableObject> objectsOnScreen() {
        return objectsOnScreen(unitPosTopLeft, panelSize);
    }

    private DrawableObject objectUnderMouse(){
        DrawableObject topObject = null;
        for (var obj : objects){
            if (obj.isMouseOver) topObject = obj;
        }
        return topObject;
    }


    public void deleteSelected() {
        var selectedBoxes = objects.stream().filter((it) -> it.isSelected && !(it instanceof DrawableArrow)).collect(Collectors.toList());
        selectedBoxes.forEach((it) -> {
                    var arrows = it.getAttachedArrows();
                    arrows.forEach((arrow) -> objects.remove(arrow));
                });
        objects = objects.stream().filter((it) -> !it.isSelected).collect(Collectors.toList());
        view.redraw();
    }

    private void recalculateAllArrowsOnScreen(){
        var screenRect = new Rectangle2D.Double(unitPosTopLeft.x, unitPosTopLeft.y, panelSize.width, panelSize.height);
        var arrowsOnScreen = objects.stream().filter((it)->it instanceof DrawableArrow &&
                (screenRect.contains(((DrawableArrow) it).getOriginLocation(false).toPoint2D())
                || screenRect.contains(((DrawableArrow) it).getTargetLocation(false).toPoint2D()))
        ).collect(Collectors.toList());

        // perform initial path-finding and save obstacle locations ("playing field")
        arrowsOnScreen.forEach((it)->((DrawableArrow) it).recalculate(unitPosTopLeft, panelSize));

        // center and rearrange arrow paths to avoid overlapping and to minimize crossings
        ArrowOrganizer.beautifyArrows(arrowsOnScreen.stream().map((it)->(DrawableArrow) it).collect(Collectors.toList()));
    }

    public double mouseZoomed(double zoom, int rotation, Point unitMouse, Point center) {
        if (rotation != 0) {
            zoom += 0.1 * rotation;

            //Move viewport towards mouse
            if (rotation < 0) { //only when zooming in
                double xOffset = (unitMouse.x - center.x) / 10.0;
                double yOffset = (unitMouse.y - center.y) / 10.0;
                center.x += xOffset;
                center.y += yOffset;
            }
        }
        recalculateAllArrowsOnScreen();
        return zoom;
    }

    public void mouseMovedTo(Point target) {  // Note: is not called when dragging
        EditState.isOverObject = false;
        EditState.isOverObjectEdge = false;
        for (var obj : objects){
            obj.isMouseOver = obj.checkPointLocation(target, true);
            EditState.isOverObject |= obj.isMouseOver;
            EditState.isOverObjectEdge |= obj.getSelectedBorder() != -1;
        }
    }

    private Point dragOrigin;
    private DrawableObject draggedObject;
    private int draggedEdge = -1;

    public void mouseDraggedTo(Point target) {
        Point dragMovement = dragOrigin.subtract(target);
        switch (state){
            case DRAGGING_OBJECT -> {
                draggedObject.location = draggedObject.location.subtract(dragMovement);
                recalculateAllArrowsOnScreen();
                dragOrigin = target;
            }
            case DRAGGING_SCREEN -> {
                view.moveScreen(dragMovement);
                recalculateAllArrowsOnScreen();
                dragOrigin = target.add(dragMovement);
            }
            case PULLING_OBJECT_EDGE -> {
                draggedObject.moveEdge(draggedEdge, new Point(0,0).subtract(dragMovement), dragOrigin);
                recalculateAllArrowsOnScreen();
                dragOrigin = target;
            }
            case DRAWING_BOX -> {
                DrawableBox newObject = (DrawableBox) objects.get(objects.size() - 1);
                newObject.size = target.subtract(dragOrigin).toSize();
            }
        }
    }

    private DrawableObject arrowOrigin;
    private int arrowOriginBorder;

    public void mousePressedAt(Point at) {
        if (state == DrawingAreaState.DRAWING_ARROW){
            var topObject = objectUnderMouse();
            if (topObject != null && topObject.getSelectedBorder() != -1){
                objects.add(new DrawableArrow(EditState.currentArrowStyle, arrowOrigin, arrowOriginBorder, topObject, topObject.getSelectedBorder()));
                recalculateAllArrowsOnScreen();
                state = DrawingAreaState.NORMAL;
                EditState.currentMouseMode = EditState.MouseMode.HAND;
            }
        }

        if (state != DrawingAreaState.NORMAL) return;
        dragOrigin = at;

        var objectUnderMouse = objectUnderMouse();
        //if (objectUnderMouse != null) objectUnderMouse.getAttachedArrows().forEach(DrawableArrow::requestRecalc);

        switch(EditState.currentMouseMode){
            case HAND -> {
                draggedObject = objectUnderMouse;
                if (draggedObject != null && EditState.isOverObjectEdge) {
                    state = DrawingAreaState.PULLING_OBJECT_EDGE;
                    draggedEdge = draggedObject.getSelectedBorder();
                }
                else if (draggedObject != null)
                    state = DrawingAreaState.DRAGGING_OBJECT;
                else
                    state = DrawingAreaState.DRAGGING_SCREEN;
            }
            case BOX -> {
                state = DrawingAreaState.DRAWING_BOX;
                objects.add(
                        new DrawableBox(EditState.currentShapePath, at, new Size(0,0))
                );
            }
            case SELECT -> {
                if (objectUnderMouse != null)
                    objectUnderMouse.isSelected = !objectUnderMouse.isSelected;
            }
            case ARROW -> {
                if (objectUnderMouse != null && objectUnderMouse.getSelectedBorder() != -1){
                    arrowOrigin = objectUnderMouse;
                    arrowOriginBorder = objectUnderMouse.getSelectedBorder();
                    state = DrawingAreaState.DRAWING_ARROW;
                }
            }
        }
        view.redraw();
    }

    public void mouseReleasedAt(Point at) {
        if (state == DrawingAreaState.DRAWING_ARROW) return;
        if (state == DrawingAreaState.DRAWING_BOX){
            objects.get(objects.size() - 1).normalizeMirroring();
            EditState.currentMouseMode = EditState.MouseMode.HAND;
        }

        state = DrawingAreaState.NORMAL;
        view.redraw();
    }
}
