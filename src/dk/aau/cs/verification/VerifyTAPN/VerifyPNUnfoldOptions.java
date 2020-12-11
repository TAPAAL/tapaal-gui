package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.verification.VerificationOptions;
import pipe.dataLayer.TAPNQuery;

public class VerifyPNUnfoldOptions extends VerificationOptions {

    private String modelOut;
    private String queryOut;
    private String verifydtapnOptions;
    private boolean reduceQuery;
    private boolean enableStructuralReductions;
    public VerifyPNUnfoldOptions(String modelOut, String queryOut, String verifydtapnOptions, boolean reduceQuery, boolean enableStructuralReductions) {
        this.modelOut = modelOut;
        this.queryOut = queryOut;
        this.verifydtapnOptions = verifydtapnOptions;
        this.reduceQuery = reduceQuery;
        this.enableStructuralReductions = enableStructuralReductions;
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
    public void setTraceOption(TAPNQuery.TraceOption option) {

    }

    @Override
    public TAPNQuery.SearchOption searchOption() {
        return null;
    }

    @Override
    public String toString() {
        String options =  "--write-simplified " + queryOut + " --write-reduced " + modelOut;

        if(!reduceQuery){
            options += " -q 0 ";
        }
        if(!enableStructuralReductions){
            options += " -r 0 ";
        }

        if (verifydtapnOptions.equals("tt"))
        options += " -verifydtapn " + verifydtapnOptions;

        return options;
    }
}
