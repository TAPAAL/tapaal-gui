package dk.aau.cs.model.CPN;

import java.util.Iterator;
import java.util.Vector;

public class ColorType implements Iterable<Color> {
    public static final ColorType COLORTYPE_DOT = new ColorType("dot") {{addColor("dot");}};
    private Vector<Color> colors = new Vector<Color>();
    private String id;
    private String name;

    public ColorType(String name) { this(name, name); } //id is unused. Solution for now.

    public ColorType(String name, String id) { this.name = name; this.id = id;}

    public void addColor(String colorName) {
        colors.add(new Color(this, colors.size(), colorName));
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

    public Iterator<Color> iterator() {
        return colors.iterator();
    }

    public Vector<Color> getColors(){
        return colors;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColorType))
            return false;
        ColorType object = (ColorType)o;

        if (!object.name.equals(this.name))
            return false;

        if (!object.colors.equals(this.colors))
            return false;

        return true;
    }

    public String toString() {
        String out = "<html>" + name + "<b> is </b>[";
        for (Color element : colors) {
            out += element.getColorName() + ", ";
        }
        out = out.substring(0, out.length() - 2);
        out += "]" + "</html>";
        return out;
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

    public ColorType copy(){
        ColorType ct = new ColorType(this.name);
        for(Color color : this.colors){
            ct.addColor(color.getName());
        }
        return ct;
    }

    public boolean contains(Color color){
        return colors.contains(color);
    }
}