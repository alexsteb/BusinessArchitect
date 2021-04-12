package abstract_views;

import intf.View;

import javax.swing.*;
import java.awt.*;

public abstract class MenuBarView implements View {
    public final JMenuBar menuBar = new JMenuBar();
    protected final int width, height;
    protected View parent;

    public MenuBarView(View parent, int width, int height){
        this.width = width;
        this.height = height;
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
        menuBar.setPreferredSize(new Dimension(width, height));
        ((WindowView) parent).window.setJMenuBar(menuBar);
    }

    @Override
    public void drawAfter() {
    }
}
