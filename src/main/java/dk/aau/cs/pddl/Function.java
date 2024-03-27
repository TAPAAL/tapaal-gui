package dk.aau.cs.pddl;

import java.util.ArrayList;

public class Function {
    private String name;
    private ArrayList<Parameter> parameters;

    public Function(String name, ArrayList<Parameter> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public Function(String name) {
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
