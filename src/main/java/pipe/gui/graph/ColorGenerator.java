package pipe.gui.graph;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

public class ColorGenerator {
    private static final float SATURATION = 0.8f;
    private static final float BRIGHTNESS = 0.9f;
    
    private int colorCount = 0;
    
    // From https://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/
    public Color nextColor() {
        float goldenRatioConjugate = 0.618033988749895f;
        float hue = (colorCount * goldenRatioConjugate) % 1;
        ++colorCount;
        return Color.getHSBColor(hue, SATURATION, BRIGHTNESS);
    }
    
    public List<Color> generateColors(int count) {
        List<Color> colors = new LinkedList<>();
        for (int i = 0; i < count; ++i) {
            colors.add(nextColor());
        }

        return colors;
    }
}