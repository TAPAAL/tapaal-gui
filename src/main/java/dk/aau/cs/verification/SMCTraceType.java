package dk.aau.cs.verification;

public class SMCTraceType {
    public static final String ANY = "Any";
    public static final String SATISFIED = "Satisfied";
    public static final String NOT_SATISFIED = "Not satisfied";

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
            sb.append(1);
        } else if (type.equals(NOT_SATISFIED)) {
            sb.append(2);
        } else {
            sb.append(0);
        }

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
