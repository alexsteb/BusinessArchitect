package abstract_views;

import intf.View;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public abstract class MenuSeparatorView extends MenuView {
    public JSeparator separator = new JSeparator();

    public MenuSeparatorView(View parent){
        super(parent, "", -1, null);
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
        ((MenuView) parent).menu.add(separator);
    }

    @Override
    public View body() {
        return null;
    }

    @Override
    public void drawAfter() {

    }
}
