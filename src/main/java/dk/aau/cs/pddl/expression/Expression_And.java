package dk.aau.cs.pddl.expression;

import dk.aau.cs.model.CPN.Expressions.ArcExpression;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Expression_And extends BaseExpression implements IExpression {

    public Expression_And() {

    }

    @Override
    public String getName() {
        return "and";
    }

//    @Override
//    public String toString() {
//        return "Cheesy Expression_And";
//    }

    @Override
    public String toString() {
        return super.toString();
    }
}
