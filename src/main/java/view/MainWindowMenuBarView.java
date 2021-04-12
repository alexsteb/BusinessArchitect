package view;

import abstract_views.MenuBarView;
import abstract_views.MenuView;
import abstract_views.ViewGroup;
import config.EditState;
import controller.MainWindowMenuBarController;
import intf.View;
import util.R;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;



public class MainWindowMenuBarView extends MenuBarView {
    public MainWindowMenuBarController controller;
    public static MainWindowMenuBarView instance;
    public ViewGroup menus;

    public MainWindowMenuBarView(MainWindowMenuBarController controller, View parent, int width, int height) {
        super(parent, width, height);
        this.controller = controller;
        instance = this;
    }

    @Override
    public View body() {
        Font standardFont = new Font("Segoe UI", Font.PLAIN, 14);
        menus = MenuView.Configurator.buildFromConfig(this,
                new MenuView.Configurator[]{
                        new MenuView.Configurator(new MainMenuView(this, R.text("file"), KeyEvent.VK_D, standardFont),
                                new MenuView.Configurator[]{
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("new"), KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), null, standardFont), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("open"), KeyEvent.VK_O, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), null, standardFont), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("save"), KeyEvent.VK_S, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), null, standardFont), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("save_as"), KeyEvent.VK_U, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), null, standardFont), null),
                                        new MenuView.Configurator(new MainMenuView(this, R.text("export_as"), KeyEvent.VK_E, standardFont),
                                                new MenuView.Configurator[]{
                                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("powerpoint"), KeyEvent.VK_P, null, null, standardFont), null),
                                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("pdf"), KeyEvent.VK_D, null, null, standardFont), null),
                                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("image"), KeyEvent.VK_B, null, null, standardFont), null)
                                                }),
                                        new MenuView.Configurator(new MainMenuSeparatorView(this), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("exit"), KeyEvent.VK_B, KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK), MainWindowMenuBarController::endApplication, standardFont), null),
                                }),
                        new MenuView.Configurator(new MainMenuView(this, R.text("edit"), KeyEvent.VK_B, standardFont),
                                new MenuView.Configurator[]{
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("undo"), KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), null, standardFont), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("redo"), KeyEvent.VK_W, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), null, standardFont), null),
                                        new MenuView.Configurator(new MainMenuSeparatorView(this), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("cut"), KeyEvent.VK_S, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK), null, standardFont), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("copy"), KeyEvent.VK_K, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), null, standardFont), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("insert"), KeyEvent.VK_E, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), null, standardFont), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("delete"), KeyEvent.VK_L, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), null, standardFont), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("select_all"), KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK), null, standardFont), null)
                                }),
                        new MenuView.Configurator(new MainMenuView(this, R.text("window"), KeyEvent.VK_F, standardFont),
                                new MenuView.Configurator[]{
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("user_roles"), KeyEvent.VK_R, null, null, standardFont), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("box_drawer"), KeyEvent.VK_Z, null, null, standardFont), null)
                                }),
                        new MenuView.Configurator(new MainMenuView(this, R.text("mode"), KeyEvent.VK_M, standardFont),
                                new MenuView.Configurator[]{
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("hand"), KeyEvent.VK_H, KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), MainWindowMenuBarController::selectHand, standardFont), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("select"), KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), MainWindowMenuBarController::selectSelect, standardFont), null),
                                        new MenuView.Configurator(new MainMenuSeparatorView(this), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("box"), KeyEvent.VK_B, KeyStroke.getKeyStroke(KeyEvent.VK_B, 0),  MainWindowMenuBarController::selectBox, standardFont), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("arrow"), KeyEvent.VK_P, KeyStroke.getKeyStroke(KeyEvent.VK_P, 0),  MainWindowMenuBarController::selectArrow, standardFont), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("text"), KeyEvent.VK_T, KeyStroke.getKeyStroke(KeyEvent.VK_T, 0),  MainWindowMenuBarController::selectText, standardFont), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("file"), KeyEvent.VK_D, KeyStroke.getKeyStroke(KeyEvent.VK_D, 0),  MainWindowMenuBarController::selectFile, standardFont), null),
                                        new MenuView.Configurator(new MainMenuItemView(this, R.text("image"), KeyEvent.VK_I, KeyStroke.getKeyStroke(KeyEvent.VK_I, 0),  MainWindowMenuBarController::selectImage, standardFont), null)
                                }),
                        new MenuView.Configurator(new MainMenuView(this, "?", KeyEvent.VK_H, standardFont), null)
                });
        return menus;
    }

    @Override
    public void drawAfter() {
        super.drawAfter();
        if (EditState.currentMouseMode == EditState.MouseMode.NONE)
            EditState.selectMouseMode(EditState.MouseMode.HAND);
    }
}
