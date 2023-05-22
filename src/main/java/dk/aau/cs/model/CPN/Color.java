package dk.aau.cs.model.CPN;

import java.util.Vector;

public class Color {

    private final Vector<Color> tuple;
    private final ColorType colorType;
    private final String colorName;
    private final Integer id;
    public static final Color STAR_COLOR = new Color(new ColorType("*"), 0, "*");

    private Color(ColorType colorType, Integer id, Vector<Color> colors, String colorName) {
        this.tuple = colors;
        this.colorType = colorType;
        this.colorName = colorName;
        this.id = id;
    }

    public Color(ColorType colorType, Integer id, Vector<Color> colors) {
        this(colorType, id, colors, "");
    }

    public Color(ColorType colorType, Integer id, String color) {
        //XXX: 2021-07-06: need to remove the null, empty list or subtyping to only allow consistent internal state
        this (colorType, id, null, color);
    }


    public String getName() {
        return colorName;
    }

    public String getColorName() {return colorName;}

    public Vector<Color> getTuple() {return tuple;}

    public ColorType getColorType() {
        return colorType;
    }

    public boolean contains(Color color) {
        if(tuple != null) {
            for (Color c : tuple) {
                if (c.contains(color)) {
                    return true;
                }
            }
            return false;
        } else {
            return equals(color);
        }
    }

    public Color successor() {
        return colorType.successorTo(this, 1);
    }

    public Color predecessor() {
        return colorType.successorTo(this, -1);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Color))
            return false;
        Color object = (Color) o;

        if(object.getColorName().equals( "dot") && this.colorName.equals("dot")){
            return true;
        }

        if (!object.getName().equals(this.getName()))
            return false;

        if (!object.colorType.getName().equals(this.colorType.getName()))
            return false;

        if (object.tuple == null && this.tuple != null)
            return false;

        if (this.tuple == null && object.tuple != null)
            return false;

        if (this.tuple == null && object.tuple == null)
            return true;

        if (this.tuple.size() != object.tuple.size())
            return false;

        for (int i = 0; i < this.getTuple().size(); i++) {
            if (!this.getTuple().get(i).equals(object.tuple.get(i)))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 37;
        if (tuple != null)
            result = 31 * result + tuple.hashCode();

        if (colorType != null)
            result = 31 * result + colorType.hashCode();

        if (colorName != null)
            result = 31 * result + colorName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        if (tuple != null) {
            StringBuilder out = new StringBuilder("(");
            for (Color color : tuple) {
                out.append(color.toString()).append(", ");
            }
            out = new StringBuilder(out.substring(0, out.length() - 2));
            out.append(")");
            return out.toString();
        }
        else
            return colorName;
    }

    public Color deepCopy() {
        if (tuple == null) {
            return  new Color(colorType.copy(), id, colorName);
        }
        Vector<Color> colors = new Vector<>();
        for(Color color : tuple) {
            colors.add(color.deepCopy());
        }
        return new Color(colorType.copy(), id, colors);
    }

    public Color getExprWithNewColorType(ColorType ct) {
        if (colorType.getName().equals(ct.getName()) || ct.contains(this)) {
            return new Color(ct, id, tuple, colorName);
        }
        return this;
    }
}
