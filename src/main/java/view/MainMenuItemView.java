package view;

import abstract_views.MenuItemView;
import alt.MainWindow;
import intf.View;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class MainMenuItemView extends MenuItemView {
    public MainMenuItemView(View parent, String text, int mnemonic, KeyStroke accelerator, Function<MenuItemView, Void> callback, Font font){
        super(parent, text, mnemonic, accelerator, callback, font, false);
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
        menuItem.setIcon(isChecked ? new ImageIcon(MainWindow.class.getResource("../checkmarkIcon.png")) : null);
    }
}
