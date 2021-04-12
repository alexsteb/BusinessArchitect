package util;

import java.util.ResourceBundle;

public class R {
    static ResourceBundle bundle = ResourceBundle.getBundle("strings");
    public static String text(String id){
        return bundle.getString(id);
    }
}
