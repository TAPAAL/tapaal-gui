package dk.aau.cs.verification.observations.expressions;

public class ObsConstant extends ObsLeaf {
    private final int tokens;

    public ObsConstant(int tokens) {
        this.tokens = tokens;
    }

    @Override
    public String toString() {
        return Integer.toString(tokens);
    }

    @Override
    public ObsExpression copy() {
        return new ObsConstant(tokens);
    }
}
