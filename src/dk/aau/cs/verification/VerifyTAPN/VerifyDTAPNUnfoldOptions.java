package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.verification.VerificationOptions;
import pipe.dataLayer.TAPNQuery;

import java.io.File;
import java.io.IOException;

public class VerifyDTAPNUnfoldOptions extends VerificationOptions {
    private String modelOut;
    private String queryOut;
    private File queryLibFile;
    private int tokenSize;

    public VerifyDTAPNUnfoldOptions(String modelOut, String queryOut, int tokenSize) {
        this.modelOut = modelOut;
        this.queryOut = queryOut;
        this.tokenSize = tokenSize;
        setup();
    }

    void setup(){
        //Create file for internal library communication
        try {
            queryLibFile = File.createTempFile("libQuery", ".q");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        result.append("-k " + tokenSize + " -q " + queryLibFile.getAbsolutePath() + " -q-xml " + queryOut + " -f " + modelOut);

        return result.toString();
    }
}
