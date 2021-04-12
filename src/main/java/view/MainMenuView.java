package view;

import abstract_views.MenuBarView;
import abstract_views.MenuView;
import abstract_views.ViewGroup;
import intf.View;

import javax.swing.*;
import java.awt.Font;



public class MainMenuView extends MenuView {

    public MainMenuView(View parent, String text, int mnemonic, Font font){
        super(parent, text, mnemonic, font);
        menu = new JMenu(text);
    }

    @Override
    public void drawBefore() {
        menu.setMnemonic(mnemonic);
        menu.setFont(font);
        if (parent instanceof MenuBarView){
            ((MenuBarView) parent).menuBar.add(menu);
        } else if (parent instanceof MenuView){
            ((MenuView) parent).menu.add(menu);
        }
    }

    @Override
    public View body() {
        return submenu;
    }


    @Override
    public void drawAfter() {

    }

}
