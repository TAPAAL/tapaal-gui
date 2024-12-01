package dk.aau.cs.verification.observations.expressions;

public class ObsExprPosition {
    private final int start;
    private final int end;
    private final ObsExpression object;

    public ObsExprPosition(int start, int end, ObsExpression object) {
        this.start = start;
        this.end = end;
        this.object = object;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public ObsExpression getObject() {
        return object;
    }

    public ObsExprPosition addOffset(int offset) {
        return new ObsExprPosition(start + offset, end + offset, object);
    }
}