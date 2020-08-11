package dk.aau.cs.model.CPN.ExpressionSupport;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;

import java.util.ArrayList;
import java.util.List;

public class ExprValues { // We use the ExprValues object to hold all values we can extract from our expressions, we need this for typechecking.
    private List<Variable> variables;
    private List<Color> colors;
    private List<ColorType> colorTypes;

    public ExprValues() {
        variables = new ArrayList<Variable>();
        colors = new ArrayList<Color>();
        colorTypes = new ArrayList<ColorType>();
    }

    public void addVariable(Variable variable) {
        variables.add(variable);
    }

    public void addColor(Color color) {
        colors.add(color);
    }

    public void addColorType(ColorType colorType) {
        colorTypes.add(colorType);
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public List<Color> getColors() {
        return colors;
    }

    public List<ColorType> getColorTypes() {return colorTypes;}
}
