package dk.aau.cs.model.tapn;

public abstract class Probability {
    public abstract double value();
    public abstract Probability copy();
    public abstract String toString(boolean displayConstantnames);
    public abstract String nameForSaving(boolean writeConstantNames);

    public boolean isInfinite() {
        return Double.isInfinite(value());
    }

    public static Probability parseProbability(String attribute, ConstantStore constants) {
        Probability weight;
        if(attribute.equals("inf")) return new DoubleProbability(Double.POSITIVE_INFINITY);
        try{
            double weightAsDouble = Double.parseDouble(attribute);
            weight = new DoubleProbability(weightAsDouble);
        } catch (NumberFormatException e){
            if(constants.containsConstantByName(attribute)){
                weight = new ConstantProbability(constants.getConstantByName(attribute));
            } else {
                throw new RuntimeException("A constant which was not declared was used in a transition weight");
            }
        }
        return weight;
    }
}
