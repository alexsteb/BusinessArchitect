package view;

import abstract_views.ViewGroup;
import controller.DrawingAreaController;
import intf.View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

public class ContentPaneView implements View {
    JPanel panel;
    View parent;
    DrawingAreaView drawingArea;

    public ContentPaneView(View parent){
        this.parent = parent;
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
        panel = new JPanel();
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.setLayout(new BorderLayout(0, 0));
        panel.setFocusable(true);
        ((MainWindowView)parent).window.setContentPane(panel);
    }

    @Override
    public View body() {
        drawingArea = new DrawingAreaView(new DrawingAreaController(), this, new Color(255, 250, 250));
        return new ViewGroup(this, Arrays.asList(
                drawingArea
        ));
    }

    @Override
    public void drawAfter() {

    }
}
