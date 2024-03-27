package dk.aau.cs.pddl;

import dk.aau.cs.model.CPN.ColorType;

public class UserType {
    private String Name;


    public UserType(String name) {
        Name = name;
    }

    public UserType(ColorType type) {
        Name = type.getName();
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

}
