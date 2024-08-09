package dk.aau.cs.model.tapn;

public class DoubleProbability extends Probability {

    final double value;

    public DoubleProbability(double value) {
        this.value = value;
    }

    @Override
    public double value() {
        return value;
    }

    @Override
    public Probability copy() {
        return new DoubleProbability(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public String toString(boolean displayConstantNames) {
        return toString();
    }

    public String nameForSaving(boolean writeConstantNames){
        return Double.toString(value);
    }
}

