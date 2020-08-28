package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.verification.VerificationOptions;
import pipe.dataLayer.TAPNQuery;

public class VerifyPNUnfoldOptions extends VerificationOptions {

    private String modelOut;
    private String queryOut;
    private String verifydtapnOptions;
    public VerifyPNUnfoldOptions(String modelOut, String queryOut, String verifydtapnOptions) {
        this.modelOut = modelOut;
        this.queryOut = queryOut;
        this.verifydtapnOptions = verifydtapnOptions;
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
        String options =  "-q-out " + queryOut + " -m-out " + modelOut;

        if (verifydtapnOptions.equals("tt"))
        options += " -verifydtapn " + verifydtapnOptions;

        return options;
    }
}
