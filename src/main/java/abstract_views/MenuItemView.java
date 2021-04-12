package abstract_views;

import alt.MainWindow;
import intf.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class MenuItemView extends MenuView {
    public JMenuItem menuItem = new JMenuItem();
    protected final KeyStroke accelerator;
    protected final Function<MenuItemView, Void> callback;
    public boolean isChecked;

    public MenuItemView(View parent, String text, int mnemonic, KeyStroke accelerator, Function<MenuItemView, Void> callback, Font font, boolean checked){
        super(parent, text, mnemonic, font);
        this.accelerator = accelerator;
        this.callback = callback;
        this.isChecked = checked;
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
        menuItem = new JMenuItem(text);
        menuItem.setFont(font);
        if (isChecked) menuItem.setIcon(new ImageIcon(MainWindow.class.getResource("../checkmarkIcon.png")));
        menuItem.setAccelerator(accelerator);
        menuItem.setMnemonic(mnemonic);
        if (callback != null) menuItem.addActionListener(e -> callback.apply(MenuItemView.this));
        ((MenuView)parent).menu.add(menuItem);
    }

    @Override
    public View body() {
        return null;
    }

    @Override
    public void drawAfter() {

    }
}
