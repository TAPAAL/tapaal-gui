package dk.aau.cs.pddl;

import dk.aau.cs.pddl.expression.IExpression_Value;

public class Parameter implements IExpression_Value {
    private String name;

    private UserType userType;


    public Parameter(String name, UserType userType) {
        this.name = name;
        this.userType = userType;
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

    @Override
    public String toString() {
        return "?" + name;
    }
}
