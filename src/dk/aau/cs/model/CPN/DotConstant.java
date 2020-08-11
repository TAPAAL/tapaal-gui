package dk.aau.cs.model.CPN;

public class DotConstant extends Color {
    /*
    uses a singleton design pattern with lazy evaluation
    https://www.journaldev.com/1377/java-singleton-design-pattern-best-practices-examples
     */
    private static DotConstant instance;

    public DotConstant() {
        super(null, null, "dot");
    }

    public static DotConstant getInstance() {
        if (instance == null) {
            instance = new DotConstant();
        }
        return instance;
    }
}
