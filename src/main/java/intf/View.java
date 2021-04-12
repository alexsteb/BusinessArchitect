package intf;

public interface View {
    View getParent();
    void setParent(View parent);

    /**
     * Called whenever the View is created.
     * Should contain all UI initialization code specific to this object (not its children).
     */
    void drawBefore();

    /**
     * Return a direct child view or child container view to be recursively drawn by the UIController.
     */
    View body();

    /**
     * Called after body is drawn.
     */
    void drawAfter();
}
