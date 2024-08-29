package dk.aau.cs.pddl;

import java.util.ArrayList;

public class Predicate {
    private String name;
    private ArrayList<Parameter> parameters;


    public Predicate(String name, ArrayList<Parameter> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public Predicate(String name) {
        this.name = name;
        this.parameters = new ArrayList<Parameter>();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ArrayList<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(ArrayList<Parameter> parameters) {
        this.parameters = parameters;
    }

}
