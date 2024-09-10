package dk.aau.cs.model.CPN;

import java.util.Objects;

public class Variable {
    private String name;
    private String id;
    private ColorType colorType;

    public Variable(String name, String id, ColorType colorType) {
        this.name = name;
        this.id = id;
        this.colorType = colorType;
    }
    public Variable(String name, ColorType colorType) {
        this.name = name;
        this.id = name;
        this.colorType = colorType;
    }
    public void setName(String name){
        this.name = name;
    }

    public void setId(String id) { this.id = id; }

    public String getName() {return this.name;}

    public String getId() {return this.id;}

    public ColorType getColorType() {return colorType;}

    public void setColorType(ColorType newColorType){
        colorType = newColorType;
    }

    public String toString() {
        return "<html>" + name + "<b> in </b>" + colorType.getName() + "</html>";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Variable other = (Variable) obj;
        return this.name.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Variable getExprWithNewColorType(ColorType ct) {
        if (colorType.getName().equals(ct.getName())) {
            return new Variable(name, id, ct);
        }
        return this;
    }
}
