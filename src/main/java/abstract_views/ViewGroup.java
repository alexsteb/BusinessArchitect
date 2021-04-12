package abstract_views;

import controller.UIController;
import intf.View;

import java.util.List;

public class ViewGroup implements View {
    private View parent;
    public List<View> content;

    public ViewGroup(View parent, List<View> content){
        this.parent = parent;
        this.content = content;
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
    public View body() {
        return null;
    }

    @Override
    public void drawBefore() {
        for (View v : content){
            UIController.drawView(v);
        }
    }

    @Override
    public void drawAfter() {

    }
}
