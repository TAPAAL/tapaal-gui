package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.verification.VerificationOptions;
import net.tapaal.gui.petrinet.verification.TAPNQuery;

public class VerifyDTAPNUnfoldOptions extends VerificationOptions {
    private final String modelOut;
    private final String queryOut;
    private final int tokenSize;
    private final int numQueries;

    public VerifyDTAPNUnfoldOptions(String modelOut, String queryOut, int tokenSize, int numQueries) {
        this.modelOut = modelOut;
        this.queryOut = queryOut;
        this.tokenSize = tokenSize;
        this.numQueries = numQueries;
    }

    
    @Override
    public boolean enabledOverApproximation() {
        return false;
    }

    @Override
    public boolean enabledUnderApproximation() {
        return false;
    }

    @Override
    public int approximationDenominator() {
        return 0;
    }

    @Override
    public int extraTokens() {
        return 0;
    }

    @Override
    public TAPNQuery.TraceOption traceOption() {
        return null;
    }

    @Override
    public void setTraceOption(TAPNQuery.TraceOption option) { }

    @Override
    public TAPNQuery.SearchOption searchOption() {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(writeUnfolded());
        result.append(" --search-strategy OverApprox --xml-queries ");
        for (int i = 0; i < numQueries; ++i){
            if (i != 0) result.append(",");
            result.append(i + 1);
        }
        result.append(" --bindings ");

        return result.toString();
    }

    private String writeUnfolded() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("win")) {
            return " --write-unfolded-queries " + "\"" + queryOut + "\"" + " --write-unfolded-net " + "\"" + modelOut + "\"";
        }

        return " --write-unfolded-queries " + queryOut + " --write-unfolded-net " + modelOut + ' ';
    }
}
