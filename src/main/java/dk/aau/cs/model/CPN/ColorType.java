package dk.aau.cs.model.CPN;

import dk.aau.cs.model.CPN.Expressions.ColorExpression;
import dk.aau.cs.model.CPN.Expressions.UserOperatorExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Vector;

public class ColorType implements Iterable<Color> {
    public static final ColorType COLORTYPE_DOT = new ColorType("dot") {{addColor("dot");}};
    private final Vector<Color> colors = new Vector<>();
    private final String id;
    private final String name;

    public ColorType(String name) { this(name, name); } //id is unused. Solution for now.

    public ColorType(String name, String id) { this.name = name; this.id = id;}

    public void addColor(Color c) {
        colors.add(c);
    }
    public void addColor(String colorName) {
        addColor(new Color(this, colors.size(), colorName));
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Integer size() {
        return colors.size();
    }

    public @NotNull Iterator<Color> iterator() {
        return colors.iterator();
    }

    public Vector<Color> getColors() {
        return colors;
    }

    public boolean isIdentical(ColorType newColorType) {
        boolean firstColorIdentical = getFirstColor().getColorName().equals(newColorType.getFirstColor().getColorName());
        boolean lastColorIdentical = getColors().lastElement().getColorName().equals(newColorType.getColors().lastElement().getColorName());

        return firstColorIdentical && lastColorIdentical && !equals(newColorType);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColorType) || o instanceof ProductType)
            return false;
        ColorType object = (ColorType)o;

        if (!object.name.equals(this.name))
            return false;
 
        if (!object.size().equals(size())){
            return false;
        }
        for (int i = 0; i < colors.size(); i++){
            if(!colors.get(i).equals(object.colors.get(i))){
                return false;
            }
        }

        return true;
    }

    public String toString() {
        StringBuilder out = new StringBuilder("<html>" + name + "<b> is </b>[");
        for (Color element : colors) {
            out.append(element.getColorName()).append(", ");
        }
        out = new StringBuilder(out.substring(0, out.length() - 2));
        out.append("]" + "</html>");
        return out.toString();
    }

    public Color successorTo(Color color, int offset) {
        int index = colors.indexOf(color);
        if (index == -1) {
            return null;
        } else {
            return colors.get((index + offset) % size());

        }
    }

    public Color getFirstColor() {
        return colors.firstElement();
    }

    public boolean isProductColorType(){
        return getFirstColor().getTuple() != null && !getFirstColor().getTuple().isEmpty();
    }

    public Vector<ColorType> getProductColorTypes(){
        Vector<ColorType> colorTypes = new Vector<>();
        for(Color color : getFirstColor().getTuple()){
            colorTypes.addElement(color.getColorType());
        }

        return colorTypes;
    }

    public ColorType copy(){
        ColorType ct = new ColorType(this.name);
        for(Color color : this.colors){
            ct.addColor(color.getName());
        }
        return ct;
    }

    public boolean contains(Color color){
        for (Color c : colors) {
            if(c.getColorName().equals(color.getColorName())) {
                return true;
            }
        }
        return false;
    }

    public Color getColorByName(String name){
        for (Color c : colors) {
            if(c.getColorName().equals(name)){
                return c;
            }
        }
        return null;
    }

    public ColorExpression createColorExpressionForFirstColor() {
        return new UserOperatorExpression(getFirstColor());
    }

    public boolean isIntegerRange(){
        for(Color c : colors){
            if(!c.getColorName().matches("-?(0|[1-9]\\d*)"))
                return false;
        }
        return true;
    }
}
