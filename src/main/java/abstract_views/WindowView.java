package abstract_views;

import intf.View;

import javax.swing.*;

public abstract class WindowView implements View {
    protected View parent;
    public final JFrame window = new JFrame();
    public int x, y, width, height;

    public WindowView(View parent, int x, int y, int width, int height){
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

    @Override
    public void drawAfter() { }
}
