package pipe.gui.graph;

import java.awt.Color;

public class ColorGenerator {
    private static final float SATURATION = 0.8f;
    private static final float BRIGHTNESS = 0.9f;
    
    private int colorCount = 0;
    
    // From https://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/
    public Color nextColor() {
        var goldenRatioConjugate = 0.618033988749895f;
        var hue = (colorCount * goldenRatioConjugate) % 1;
        ++colorCount;
        return Color.getHSBColor(hue, SATURATION, BRIGHTNESS);
    }
}
