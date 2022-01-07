package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.verification.VerificationOptions;
import net.tapaal.gui.verification.TAPNQuery;

public class VerifyDTAPNUnfoldOptions extends VerificationOptions {
    private String modelOut;
    private String queryOut;
    private int tokenSize;
    private int numQueries;

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
        result.append("--write-unfolded-queries ");
        result.append(queryOut);
        result.append(" --write-unfolded-net ");
        result.append(modelOut);
        result.append(" --search-strategy OverApprox --xml-queries ");
        for(int i = 0; i < numQueries; ++i){
            if(i != 0) result.append(",");
            result.append(i + 1);
        }

        return result.toString();
    }
}
