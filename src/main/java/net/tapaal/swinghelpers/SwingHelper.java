package net.tapaal.swinghelpers;
import java.awt.*;
public class SwingHelper {
    public static void setPreferredWidth(Component c, int width) {
        var pSize = c.getPreferredSize();
        c.setPreferredSize(new Dimension(width, pSize.height));
    }
}