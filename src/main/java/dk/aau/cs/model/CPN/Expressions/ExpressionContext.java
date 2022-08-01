package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ProductType;

import java.util.HashMap;
import java.util.Vector;

public class ExpressionContext {
    public final HashMap<String, Color> binding;
    public final HashMap<String, ColorType> colorTypes;

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
