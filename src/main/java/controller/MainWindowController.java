package controller;

import intf.Controller;
import view.MainWindowView;

public class MainWindowController implements Controller {
    public int x, y, width, height;
    public void init(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void synchronize(MainWindowView window) {
        this.x = window.window.getX();
        this.y = window.window.getY();
        this.width = window.window.getWidth();
        this.height = window.window.getHeight();
    }
}
