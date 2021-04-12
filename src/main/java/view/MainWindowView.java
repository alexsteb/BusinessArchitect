package view;

import abstract_views.MenuBarView;
import abstract_views.ViewGroup;
import controller.MainWindowController;
import controller.MainWindowMenuBarController;
import abstract_views.WindowView;
import intf.View;

import java.util.Arrays;


public class MainWindowView extends WindowView {
    private MenuBarView menuBar;
    private ContentPaneView contentPaneView;
    public MainWindowController controller;

    public MainWindowView(MainWindowController controller, int x, int y, int width, int height) {
        super(null, x, y, width, height);
        this.controller = controller;
        this.controller.init(x, y, width, height);
    }

    @Override
    public void drawBefore() {
        window.setBounds(controller.x, controller.y, controller.width, controller.height);
        super.drawBefore();
    }

    @Override
    public View body() {
        menuBar = new MainWindowMenuBarView(new MainWindowMenuBarController(), this, 0, 40);
        contentPaneView = new ContentPaneView(this);

        return new ViewGroup(this,
                Arrays.asList(
                        menuBar,
                        contentPaneView
                )
        );
    }
}
