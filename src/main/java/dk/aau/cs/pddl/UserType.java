package dk.aau.cs.pddl;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import java.util.ArrayList;

public class UserType {
    private String name;

    private ArrayList<String> objectNames;

    public UserType(String name, ArrayList<String> objectNames) {
        this.name = name;
        this.objectNames = objectNames;
    }

    public UserType(ColorType type) {
        this.name = type.getName();
        objectNames = new ArrayList<>() {{
            for(Color c: type)
                add(c.getName());
        }};
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getObjectNames() {
        return objectNames;
    }

}
