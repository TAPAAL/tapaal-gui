package dk.aau.cs.pddl.expression;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.pddl.Parameter;

import java.util.ArrayList;

public class Expression_Predicate_IsSuccessor extends Expression_Predicate {
    public Expression_Predicate_IsSuccessor(Color left, Color right) {
        super("isSuccessor", new ArrayList<>() {{
            add(new Expression_ColorLiteral(left));
            add(new Expression_ColorLiteral(right));
        }});
    }

    public Expression_Predicate_IsSuccessor(Parameter left, Parameter right) {
        super("isSuccessor", new ArrayList<>() {{
            add(left);
            add(right);
        }});
    }
}