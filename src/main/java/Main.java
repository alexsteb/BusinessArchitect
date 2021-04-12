import controller.MainWindowController;
import controller.UIController;
import view.MainWindowView;


import java.awt.*;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                // center window on screen
                Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
                int x = (int)(size.width * 0.33f);
                int y = (int)(size.height * 0.25f);
                int width = (int)(size.width * 0.33f);
                int height = (int)(size.height * 0.5f);

                // draw main window
                MainWindowView window = new MainWindowView(new MainWindowController(), x, y, width, height);
                UIController.drawView(window);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}

