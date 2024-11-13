package dk.aau.cs.verification.observations;

import java.util.UUID;

import dk.aau.cs.verification.observations.expressions.ObsExpression;
import dk.aau.cs.verification.observations.expressions.ObsPlaceHolder;

public class Observation {
    private final String id;

    private String name;

    private ObsExpression expression = new ObsPlaceHolder();

    public Observation(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public Observation(String name) {
        this(name, UUID.randomUUID().toString());
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public ObsExpression getExpression() {
        return expression;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExpression(ObsExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return name;
    }
}
