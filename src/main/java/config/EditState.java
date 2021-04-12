package config;

import intf.View;
import util.R;
import view.MainMenuItemView;
import view.MainMenuView;
import view.MainWindowMenuBarView;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class EditState {


    public static boolean isOverObject;
    public static boolean isOverObjectEdge;
    public static String currentShapePath = "M0 0 L100 0 L100 100 L0 100 L50 50 L0 0";

    public static Cursor getCurrentCursor() {
        return switch(currentMouseMode){
            case HAND -> isOverObjectEdge ?
                    Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR) : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            case ARROW -> Cursor.getDefaultCursor();
            default -> Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        };
    }

    public enum ArrowStyle {
        LINE, ARROW
    }

    public static ArrowStyle currentArrowStyle = ArrowStyle.LINE;


    public enum MouseMode {
        NONE, HAND, SELECT, BOX, ARROW, TEXT, FILE, IMAGE
    }

    public static MouseMode currentMouseMode = MouseMode.NONE;

    private static final Map<MouseMode, String> modeNameMap = new HashMap<>() {{
        put(MouseMode.HAND, "hand");
        put(MouseMode.SELECT, "select");
        put(MouseMode.BOX, "box");
        put(MouseMode.ARROW, "arrow");
        put(MouseMode.TEXT, "text");
        put(MouseMode.FILE, "file");
        put(MouseMode.IMAGE, "image");
    }};

    private static void chooseModeInMenu(MouseMode mode) {
        var selectedName = R.text(modeNameMap.get(mode));
        var menus = MainWindowMenuBarView.instance.menus;
        for (View menu : menus.content){
            if (((MainMenuView) menu).menu.getText().equals(R.text("mode"))) {
                for (View menuItem : ((MainMenuView) menu).submenu.content){
                    if (!(menuItem instanceof MainMenuItemView)) continue;
                    ((MainMenuItemView) menuItem).setChecked(
                            ((MainMenuItemView) menuItem).menuItem.getText().equals(selectedName)
                    );
                }
            }
        }
    }

    public static void selectMouseMode(MouseMode newMode){
        chooseModeInMenu(newMode);
        currentMouseMode = newMode;
    }
}
