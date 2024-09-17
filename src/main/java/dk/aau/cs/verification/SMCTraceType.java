package dk.aau.cs.verification;

public class SMCTraceType {
    public static final String ANY = "Any";
    public static final String SATISFIED = "Satisfied";
    public static final String NOT_SATISFIED = "Not satisfied";

    private static final int ANY_INT = 0;
    private static final int SATISFIED_INT = 1;
    private static final int NOT_SATISFIED_INT = 2;

    private final String type;

    public SMCTraceType() {
        this.type = ANY;
    }

    public SMCTraceType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    public String getArg() {
        StringBuilder sb = new StringBuilder();
        sb.append("--smc-traces-type ");

        if (type.equals(SATISFIED)) {
            sb.append(SATISFIED_INT);
        } else if (type.equals(NOT_SATISFIED)) {
            sb.append(NOT_SATISFIED_INT);
        } else {
            sb.append(ANY_INT);
        }

        sb.append(" ");

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SMCTraceType) {
            return type.equals(((SMCTraceType) obj).type);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
