package abstract_views;

import intf.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MenuView implements View {
    public JMenu menu = new JMenu();
    protected final String text;
    protected final int mnemonic;
    protected final Font font;
    protected View parent;
    public ViewGroup submenu = null;

    public MenuView(View parent, String text, int mnemonic, Font font){
        this.parent = parent;
        this.text = text;
        this.mnemonic = mnemonic;
        this.font = font;
    }


    @Override
    public View getParent() {
        return parent;
    }

    @Override
    public void setParent(View parent) {
        this.parent = parent;
    }


    public static class Configurator {
        MenuView menu;
        Configurator[] submenuConfigs;

        public Configurator(MenuView menu, Configurator[] submenuConfigs){
            this.menu = menu;
            this.submenuConfigs = submenuConfigs;
        }

        public static ViewGroup buildFromConfig(View parent, Configurator[] configs){
            return new ViewGroup(parent,
                    Arrays.stream(configs).map((it) -> buildMenu(parent, it)).collect(Collectors.toList()));
        }

        private static MenuView buildMenu(View parent, Configurator config){
            MenuView menu = config.menu;
            menu.setParent(parent);
            List<View> submenus = config.submenuConfigs == null ? Collections.emptyList() :
                    Arrays.stream(config.submenuConfigs).map((it) -> buildMenu(menu, it)).collect(Collectors.toList());
            menu.submenu = new ViewGroup(menu, submenus);
            return menu;
        }
    }
}
