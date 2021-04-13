package view;

import config.EditState;
import controller.DrawingAreaController;
import intf.View;
import model.DrawableObject;

import util.Point;
import util.Size;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DrawingAreaView implements View {
    public DrawingArea area;
    View parent;
    Color backgroundColor;
    public static DrawingAreaView instance;
    public DrawingAreaController controller;

    public DrawingAreaView(DrawingAreaController controller, View parent, Color backgroundColor){
        this.controller = controller;
        this.parent = parent;
        this.backgroundColor = backgroundColor;
        controller.init(this);
        instance = this;
    }

    @Override
    public View getParent() {
        return parent;
    }

    @Override
    public void setParent(View parent) {
        this.parent = parent;
    }

    @Override
    public void drawBefore() {
        area = new DrawingArea(controller,2, Color.LIGHT_GRAY, 15.0);
        area.setBackground(new Color(250, 250, 250));
        area.setFocusable(true);
        ((ContentPaneView) parent).panel.add(area, BorderLayout.CENTER);
    }

    @Override
    public View body() {
        return null;
    }

    @Override
    public void drawAfter() {

    }

    public void moveScreen(Point movement) {
        area.moveScreen(movement);
    }

    public void redraw() {
        area.repaint();
    }

    public static class DrawingArea extends JPanel implements MouseListener, MouseWheelListener, MouseMotionListener, KeyListener {

        private static DrawingAreaController controller;
        private static Dimension panelPixelSize; 			  //Size of drawing area in pixels
        public static Size panelUnitSize; 	                  //Size of drawing area in units
        private static double dotDistanceDrawFactor = 1.0; 	  //2 = draw 1 dot for every 2 units, 0.5 = draw 2 dots for every 1 unit etc.

        public static Point center = new Point(0,0);    //unit location at center of window
        public static Point pixelCenter; 		              //center of window in pixel
        public static double zoom = 0; 						  //powers of 2 in range -1000 < 0 < +1000, greater numbers = zoomed out


        private final int dotSize; 	                          //size of oval shape
        private final Color dotColor;
        private final double dotDistance; 	                  //standard distance between dots
        private static final double cutOffFactorUpper = 1.5;  //half-way to double
        private static final double cutOffFactorLower = 0.75; //half-way to half
        
        private static Point unitPosTopLeft;

        public DrawingArea(DrawingAreaController controller, int dotSize, Color dotColor, double dotDistance) {
            super();
            DrawingArea.controller = controller;
            this.dotSize = dotSize;
            this.dotColor = dotColor;
            this.dotDistance = dotDistance;

            this.addMouseListener(this);
            this.addMouseWheelListener(this);
            this.addMouseMotionListener(this);
            this.addKeyListener( this);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            this.setCursor(EditState.getCurrentCursor());

            //Calculate drawing area's dimensions in internal units
            panelPixelSize = this.getSize();
            panelUnitSize = new Size(Math.pow(2, zoom) * panelPixelSize.getWidth() / dotDistance, Math.pow(2, zoom) * panelPixelSize.getHeight() / dotDistance);
            controller.pixelPerUnit =  dotDistance / Math.pow(2, zoom);

            // determine appropriate pixel distance between dots for current zoom level
            double cutOffBig = dotDistance * cutOffFactorUpper;
            double cutOffSmall = dotDistance * cutOffFactorLower;

            dotDistanceDrawFactor = 1;
            while (controller.pixelPerUnit * dotDistanceDrawFactor < cutOffSmall) {
                dotDistanceDrawFactor *= 2.0;
            }
            while (controller.pixelPerUnit * dotDistanceDrawFactor > cutOffBig) {
                dotDistanceDrawFactor /= 2.0;
            }

            pixelCenter = new Point(panelPixelSize.width / 2.0, panelPixelSize.height / 2.0);
            unitPosTopLeft = new Point (center.x - pixelCenter.x / controller.pixelPerUnit, center.y - pixelCenter.y / controller.pixelPerUnit);

            //Draw dot grid
            drawDots(g);

            //Draw all visible objects (pre-sorted)
            for (DrawableObject obj : controller.objectsOnScreen(unitPosTopLeft, panelUnitSize))
                obj.draw(g, unitPosTopLeft, panelUnitSize, controller.pixelPerUnit);

        }


        private void drawDots(Graphics g) {

            //Find first dot to draw in top-left corner
            Point firstVisibleDot = new Point(unitPosTopLeft.x + (-unitPosTopLeft.x % dotDistanceDrawFactor), unitPosTopLeft.y + (-unitPosTopLeft.y % dotDistanceDrawFactor));

            //Draw all visible dots
            g.setColor(dotColor);
            double currentXInitial = ((firstVisibleDot.x - unitPosTopLeft.x) * controller.pixelPerUnit);
            double currentY = ((firstVisibleDot.y - unitPosTopLeft.y) * controller.pixelPerUnit);

            while (currentY < panelPixelSize.height) {
                double currentX = currentXInitial;
                while (currentX < panelPixelSize.width) {
                    g.fillOval((int) currentX - (int)(dotSize / 2.0), (int) currentY - (int)(dotSize / 2.0), dotSize, dotSize);
                    currentX += dotDistanceDrawFactor * controller.pixelPerUnit;
                }
                currentY += dotDistanceDrawFactor * controller.pixelPerUnit;
            }
        }

        public void moveScreen(Point movement) {
            center = center.add(movement);
            repaint();
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {

        }

        private Point eventToPoint(MouseEvent e){
            return new Point(e.getX() / controller.pixelPerUnit, e.getY() / controller.pixelPerUnit).add(unitPosTopLeft);
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (unitPosTopLeft == null) return;
            controller.mousePressedAt(eventToPoint(e));
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (unitPosTopLeft == null) return;
            controller.mouseReleasedAt(eventToPoint(e));
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (unitPosTopLeft == null) return;
            controller.mouseDraggedTo(eventToPoint(e));
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (unitPosTopLeft == null) return;
            controller.mouseMovedTo(eventToPoint(e));
            this.repaint();
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            zoom = controller.mouseZoomed(zoom, e.getWheelRotation(), GetUnitMouse(e), center);
            this.repaint();
        }

        public static Point GetUnitMouse(MouseEvent e) {
            return new Point (e.getX() / controller.pixelPerUnit + unitPosTopLeft.x, e.getY() / controller.pixelPerUnit + unitPosTopLeft.y);
        }


        public void redraw() {
            this.repaint();
        }
    }
}
