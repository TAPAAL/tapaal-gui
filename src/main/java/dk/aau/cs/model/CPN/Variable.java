package dk.aau.cs.model.CPN;

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
        this.colorType = colorType;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getName() {return this.name;}

    public String getId() {return this.id;}

    public ColorType getColorType() {return colorType;}

    public void setColorType(ColorType newColorType){
        colorType = newColorType;
    }

    public String toString() {
        return "<html>" + name + "<b> in </b>" + colorType.getName() + "</html>";
    }

    public Variable getExprWithNewColorType(ColorType ct) {
        if (colorType.getName().equals(ct.getName())) {
            return new Variable(name, id, ct);
        }
        return this;
    }
}
