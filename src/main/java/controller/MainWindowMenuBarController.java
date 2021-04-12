package controller;

import abstract_views.MenuItemView;
import config.EditState;
import intf.Controller;
import intf.View;

public class MainWindowMenuBarController implements Controller {
    public static Void endApplication(View source){
        System.exit(0);
        return null;
    }

    public static Void selectHand(View source) { EditState.selectMouseMode(EditState.MouseMode.HAND); return null; }
    public static Void selectSelect(View source) { EditState.selectMouseMode(EditState.MouseMode.SELECT); return null; }
    public static Void selectBox(View source) { EditState.selectMouseMode(EditState.MouseMode.BOX); return null; }
    public static Void selectArrow(View source) { EditState.selectMouseMode(EditState.MouseMode.ARROW); return null; }
    public static Void selectText(View source) { EditState.selectMouseMode(EditState.MouseMode.TEXT); return null; }
    public static Void selectFile(View source) { EditState.selectMouseMode(EditState.MouseMode.FILE); return null; }
    public static Void selectImage(View source) { EditState.selectMouseMode(EditState.MouseMode.IMAGE); return null; }
}
