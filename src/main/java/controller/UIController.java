package controller;

import intf.View;

import java.awt.event.MouseMotionListener;

public class UIController {
    public static void drawView(View v){
        View content = v.body();
        v.drawBefore();
        if (content != null) drawView(content);
        v.drawAfter();
    }
}
