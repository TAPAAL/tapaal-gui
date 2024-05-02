package dk.aau.cs.pddl.expression;

import dk.aau.cs.pddl.Parameter;

import java.util.ArrayList;
import java.util.HashMap;

public class Weights extends HashMap<ArrayList<IExpression_Value>, Integer> {
    public void add(Weights w) {
        w.forEach((k,v) -> {
            int prev_val = this.getOrDefault(k, 0);
            this.put(k, prev_val + v);
        });
    }

    public void multiply(int scalar) {
        this.replaceAll((key, value) -> value * scalar);
    }
}
