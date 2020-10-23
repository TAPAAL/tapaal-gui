package dk.aau.cs.model.CPN;

import java.util.Vector;

public class Color {

    private Vector<Color> tuple;
    private ColorType colorType;
    private String colorName;
    private Integer id;
    public static final Color STAR_COLOR = new Color(new ColorType("*"), 0, "*");

    public Color(ColorType colorType, Integer id, Vector<Color> colors) {
        this.tuple = colors;
        this.colorType = colorType;
        this.colorName = "";
        this.id = id;

    }

    public Color(ColorType colorType, Integer id, String color) {
        this.tuple = null;
        this.colorType = colorType;
        this.colorName = color;
        this.id = id;
    }


    public String getName() {
        return colorName;
    }

    public String getColorName() {return colorName;}

    public Vector<Color> getTuple() {return tuple;}

    public ColorType getColorType() {
        return colorType;
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
            String out = "(";
            for (Color color : tuple) {
                out += color.toString() + ", ";
            }
            out = out.substring(0, out.length() -2);
            out += ")";
            return out;
        }
        else
            return colorName;
    }

    public Color deepCopy() {
        if (tuple.isEmpty()) {
            return  new Color(colorType.copy(), id, colorName);
        }
        Vector<Color> colors = new Vector<>();
        for(Color color : tuple) {
            colors.add(color.deepCopy());
        }
        return new Color(colorType.copy(), id, colors);
    }
}
