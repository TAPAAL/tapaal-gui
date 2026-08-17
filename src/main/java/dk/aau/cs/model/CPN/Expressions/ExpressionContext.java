package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ProductType;

import java.util.Map;
import java.util.Vector;

public class ExpressionContext {
    public final Map<String, Color> binding;
    public final Map<String, ColorType> colorTypes;

   public ExpressionContext(Map<String, Color> binding, Map<String, ColorType> colorTypes) {
       this.binding = binding;
       this.colorTypes = colorTypes;
   }

    public ProductType findProductColorType(Vector<ColorType> types) {
        for (ColorType ct : colorTypes.values()) {
            if (ct instanceof ProductType) {
                if (((ProductType)ct).getColorTypes().size() != types.size()) {
                    continue;
                }

                ProductType pt = (ProductType) ct;
                boolean allMatch = true;
                for (int i = 0; i < types.size(); ++i) {
                    if (!pt.getColorTypes().get(i).getName().equals(types.get(i).getName())) {
                        allMatch = false;
                        break;
                    }
                }

                if (allMatch) {
                    return pt;
                }
            }
        }

        return null;
    }
}
