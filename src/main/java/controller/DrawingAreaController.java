package controller;

import config.EditState;
import intf.Controller;
import model.DrawableBox;
import model.DrawableObject;
import util.Point;
import util.Size;
import view.DrawingAreaView;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DrawingAreaController implements Controller {

    List<DrawableObject> objects = new ArrayList<>();
    public double pixelPerUnit;

    private enum DrawingAreaState {
        NORMAL, DRAGGING_SCREEN, DRAGGING_OBJECT, PULLING_OBJECT_EDGE
    }

    private DrawingAreaState state = DrawingAreaState.NORMAL;
    private DrawingAreaView view;

    public void init(DrawingAreaView view){
        this.view = view;
    }

    public List<DrawableObject> objectsOnScreen(Point unitPosTopLeft, Size panelSize) {
        if (objects.isEmpty())
            objects.add(new DrawableBox("M0 0 L80 0 L100 100 L0 100 Q50 50 0 0", new Point(-5.0,-5.0), new Size(10, 10)));
        return objects.stream().filter((it) -> it.isInRectangle(unitPosTopLeft, panelSize)).collect(Collectors.toList());
    }

    private DrawableObject objectUnderMouse(){
        DrawableObject topObject = null;
        for (var obj : objects){
            if (obj.isMouseOver) topObject = obj;
        }
        return topObject;
    }

    public void mouseMovedTo(Point target) {  // Note: is not called when dragging
        for (var obj : objects){
            obj.isMouseOver = obj.checkMousePosition(target);
            EditState.isOverObject = obj.isMouseOver;
            EditState.isOverObjectEdge = obj.getSelectedBorder() != -1;
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
                dragOrigin = target;
            }
            case DRAGGING_SCREEN -> {
                view.moveScreen(dragMovement);
                dragOrigin = target.add(dragMovement);
            }
            case PULLING_OBJECT_EDGE -> {
                draggedObject.moveEdge(draggedEdge, new Point(0,0).subtract(dragMovement), dragOrigin);
                dragOrigin = target;
            }
        }
    }

    public void mousePressedAt(Point at) {
        dragOrigin = at;
        dragOrigin = at;

        if (state != DrawingAreaState.NORMAL) return;

        if (EditState.currentMouseMode == EditState.MouseMode.HAND){
            draggedObject = objectUnderMouse();
            if (draggedObject != null && EditState.isOverObjectEdge) {
                state = DrawingAreaState.PULLING_OBJECT_EDGE;
                draggedEdge = draggedObject.getSelectedBorder();
            }
            else if (draggedObject != null)
                state = DrawingAreaState.DRAGGING_OBJECT;
            else
                state = DrawingAreaState.DRAGGING_SCREEN;
        }
    }

    public void mouseReleasedAt(Point at) {
        state = DrawingAreaState.NORMAL;
    }
}
