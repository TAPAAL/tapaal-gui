package dk.aau.cs.pddl.expression;

import dk.aau.cs.model.tapn.TimedPlace;

import java.util.HashMap;

public class PlaceWeights extends HashMap<TimedPlace, Weights> {
    public void add(PlaceWeights pw) {
        pw.forEach((k,v) -> {
            if (this.containsKey(k))
                this.get(k).add(v);
            else
                this.put(k, v);
        });
    }

    public void multiply(int scalar) {
        this.forEach((k,v) -> {
            v.multiply(scalar);
        });
    }
}
