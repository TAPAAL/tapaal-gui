package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ProductType;

import java.util.HashMap;
import java.util.Vector;

public class ExpressionContext {
    public HashMap<String, Color> binding;
    public HashMap<String, ColorType> colorTypes;

   /* lacking functionality to look up color element in ColorType color vector
    public Color findColor(String color) {
        if (color.compareTo("dot") == 0) {
            return DotConstant.getInstance();
        }
    }

        for (Map.Entry<String, ColorType> element : colorTypes.entrySet()) {
            String key = element.getKey();
            ColorType value = element.getValue();
            if (color.compareTo(value.getName()) == 0) {
                return value.getColors().
            }
        }
    */

   public ExpressionContext(HashMap<String, Color> binding, HashMap<String, ColorType> colorTypes) {
       this.binding = binding;
       this.colorTypes = colorTypes;
   }

   public ProductType findProductColorType(Vector<ColorType> types) {
        for (ColorType ct : colorTypes.values()) {
            if (ct instanceof ProductType) {
                ProductType pt = (ProductType) ct;
                if (pt.containsTypes(types)) {
                    return pt;
                }
            }
        }
        return null;
   }

}
