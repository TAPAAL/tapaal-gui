package dk.aau.cs.pddl;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.pddl.expression.IExpression_Value;

public class ColorLiteral implements IExpression_Value {
    private String name;

    private UserType userType;


    public ColorLiteral(String name, UserType userType) {
        this.name = name;
        this.userType = userType;
    }

    public ColorLiteral(Color color) {
        this.name = color.getName();
        this.userType = new UserType(color.getColorType());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public UserType getType() {
        return userType;
    }

    public void setType(UserType type) {
        userType = type;
    }

}
