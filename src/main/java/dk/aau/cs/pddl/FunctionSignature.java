package dk.aau.cs.pddl;

import dk.aau.cs.model.tapn.TimedPlace;

import java.util.ArrayList;

public class FunctionSignature {
    private String name;
    private ArrayList<Parameter> parameters;

    public FunctionSignature(String name, ArrayList<Parameter> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public FunctionSignature(TimedPlace place, ArrayList<Parameter> parameters) {
        this.name = place.name();
        this.parameters = parameters;
    }

    public FunctionSignature(String name) {
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
