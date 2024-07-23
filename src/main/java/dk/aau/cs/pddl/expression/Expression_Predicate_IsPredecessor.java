package dk.aau.cs.pddl.expression;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.pddl.Parameter;

import java.util.ArrayList;

public class Expression_Predicate_IsPredecessor extends Expression_Predicate {
    public Expression_Predicate_IsPredecessor(Color left, Color right) {
        super("isPredecessor", new ArrayList<>() {{
            add(new Expression_ColorLiteral(left));
            add(new Expression_ColorLiteral(right));
        }});
    }

    public Expression_Predicate_IsPredecessor(Parameter left, Parameter right) {
        super("isPredecessor", new ArrayList<>() {{
            add(left);
            add(right);
        }});
    }
}