package dk.aau.cs.model.tapn;

import dk.aau.cs.util.Require;

public class ConstantProbability extends Probability {
    private Constant constant;

    public ConstantProbability(Constant constant) {
        Require.that(constant != null, "Argument must be a non-null constant");
        this.constant = constant;
    }

    public double value() {
        return (double) constant.value();
    }

    public Probability copy() {
        return new ConstantProbability(constant.copy());
    }

    public Constant constant() {
        return constant;
    }

    public void setConstant(Constant newConstant) {
        Require.that(newConstant != null, "Constant cannot be null");

        constant = newConstant;
    }

    public String toString() {
        return constant.name();
    }

    public String toString(boolean displayConstantNames) {
        if(displayConstantNames){
            return constant.name();
        } else if(constant.value() != 1){
            return String.valueOf(constant.value());
        } else {
            return "";
        }
    }

    public String nameForSaving(boolean writeConstantNames){
        if(writeConstantNames){
            return constant.name();
        } else {
            return Integer.toString(constant.value());
        }

    }
}
