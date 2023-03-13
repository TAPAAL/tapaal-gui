package dk.aau.cs.model.CPN;

import dk.aau.cs.model.CPN.Expressions.ColorExpression;
import dk.aau.cs.model.CPN.Expressions.TupleExpression;

import java.util.HashMap;
import java.util.Vector;

public class ProductType extends ColorType {

    private Vector<ColorType> constituents = new Vector<>();
    private final String name;
    private final HashMap<Vector<Color>, Color> colorCache = new HashMap<>(); // FIXME: why does it have color cache, why not use color from Color Type
    private boolean colorCacheDiry = true;

    @Override
    public Integer size() {
        return constituents.size();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProductType))
            return false;
        ProductType object = (ProductType) o;

        if (!object.name.equals(this.name))
            return false;

        if(!object.size().equals(size())){
            return false;
        }
        for(int i = 0; i < constituents.size(); i++){
            if(!constituents.get(i).equals(object.constituents.get(i))){
                return false;
            }
        }
        return true;
    }

    public ProductType(String name) {
        super(name);
        this.name = name;
    }

    public boolean contains(ColorType colorType){
        for (ColorType ct : constituents){
            if(ct.equals(colorType)){
                return true;
            }
        }
        return false;
    }
    public Vector<ColorType> getColorTypes() { return constituents; }

    public void addType(ColorType colortype) {
        constituents.add(colortype);
    }

    //Adding colors to product-types no longer makes sense.
    public void addColor(String colorName) {
        throw new RuntimeException("Can't add color to ProductColorType");
    }

    public String toString() {
        StringBuilder out = new StringBuilder("<html>" + name + "<b> is </b> &lt;");
        for (ColorType element : constituents) {
            out.append(element.getName()).append(", ");
        }
        out = new StringBuilder(out.substring(0, out.length() - 2));
        out.append("&gt; </html>");
        return out.toString();
    }

    private int getConstituentCombinationSize(){
        int result = 1;
        for (ColorType ct : constituents) {
            result *= ct.size();
        }
        return result;
    }

    @Override
    public boolean contains(Color color){
        Vector<Color> tupleColors = color.getTuple();
        if(tupleColors != null) {
            if(constituents.size() == tupleColors.size()) {
                //Are the colors in a tuple color ordered correctly?
                //maybe do more coarse check
                for(int i = 0; i < constituents.size(); i++) {
                    if(!constituents.elementAt(i).contains(tupleColors.elementAt(i))){
                        return false;
                    }
                }
                return true;
            }
        } else {
            for(ColorType ct : constituents) {
                if (ct.contains(color)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public Vector<Color> getColors(){

        // This is a quick re-implementation of the orginal code, it seems like it was supposed to
        //  generate the cartesian product, but failed to do so properly. This is a quick fix to see
        //  if fixing this would solve the problems. I tried to avoid changing anything else since
        //  there is a lot of caching and update errors thats possible with how it works need  -- kyrke 2023-03-13
        if (colorCacheDiry) {

            colorCache.clear();

            Vector<Vector<Color>> currentProduct = new Vector<>(new Vector<>());
            Vector<Vector<Color>> newProduct = new Vector<>(new Vector<>());
            for (var colorType : constituents) {
                for (var color : colorType.getColors()) {
                    if (currentProduct.size() == 0) {
                        var newColor = new Vector<Color>();
                        newColor.add(color);
                        newProduct.add(newColor);
                    } else {
                        for (Vector<Color> tmpColor : currentProduct) {
                            var newColor = new Vector<>(tmpColor);
                            newColor.add(color);
                            newProduct.add(newColor);
                        }
                    }

                }
                currentProduct = newProduct;
                newProduct = new Vector<>(new Vector<>());
            }

            for (Vector<Color> tupleColor : currentProduct) {
                colorCache.putIfAbsent(tupleColor, new Color(this, 0, tupleColor));
            }

            colorCacheDiry = false;

        }

        return new Vector<>(colorCache.values());
    }

    public boolean containsTypes(Vector<ColorType> colorTypes) {
        if (constituents.equals(colorTypes)) return true;

        boolean containsType = false;
        for (ColorType constituent : constituents) {
            for (ColorType ct : colorTypes) {
                if (constituent.getName().equals(ct.getName())) {
                    containsType = true;
                    break;
                }
            }
            if (!containsType) return false;
        }
        return true;
    }

    public Color getColor(Vector<Color> colors) {
        if (colorCacheDiry) { getColors(); } // FIXME hack to generate colors if not done yet
        Color result = colorCache.get(colors);
        if (result == null) {
            //throw new RuntimeException("Looking up unknow color" + colors);

            result = new Color(this, 0, colors); // FIXME, wtf? this just seems wrong, need to support .all tokens
            colorCache.put(colors, result);

        }
        return result;
    }

    @Override // FIXME this is wrong, should get first color from color cache
    public Color getFirstColor() {
        Vector<Color> colors = new Vector<>();
        for (ColorType ct : constituents) {
            colors.add(ct.getFirstColor());
        }
        return getColor(colors);
    }

    public Vector<ColorType> getConstituents(){
        return constituents;
    }
    public void setConstituents(Vector<ColorType> constituents) {
        colorCacheDiry = true;
        this.constituents = constituents;
    }

    public void replaceColorType(ColorType newColorType, ColorType oldColorType){
        colorCacheDiry = true;
        for (ColorType ct : constituents){
            if(ct.equals(oldColorType)){
                int index = constituents.indexOf(ct);
                constituents.set(index, newColorType);
            }
        }

    }
    @Override
    public ColorExpression createColorExpressionForFirstColor() {
        Vector<ColorExpression> tempVec = new Vector<>();
        for(ColorType colorType : getColorTypes()){
            tempVec.add(colorType.createColorExpressionForFirstColor());
        }
        return new TupleExpression(tempVec, this);
    }
}
