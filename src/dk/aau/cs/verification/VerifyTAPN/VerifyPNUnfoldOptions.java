package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.verification.VerificationOptions;
import pipe.dataLayer.TAPNQuery;

import java.util.HashMap;
import java.util.Map;

public class VerifyPNUnfoldOptions extends VerificationOptions {

    private static final Map<TAPNQuery.SearchOption, String> searchMap = createSearchOptionsMap();
    private String modelOut;
    private String queryOut;
    private int numQueries;

    public VerifyPNUnfoldOptions(String modelOut, String queryOut, int numQueries) {
        this.modelOut = modelOut;
        this.queryOut = queryOut;
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
        result.append("--write-simplified " + queryOut + " --write-reduced " + modelOut + " -x 1");
        for(int i = 2; i <= numQueries; i++){
            result.append("," + i);
        }

        return result.toString();
    }

    private static final Map<TAPNQuery.SearchOption, String> createSearchOptionsMap() {
        HashMap<TAPNQuery.SearchOption, String> map = new HashMap<TAPNQuery.SearchOption, String>();
        map.put(TAPNQuery.SearchOption.BFS, " -s BFS");
        map.put(TAPNQuery.SearchOption.DFS, " -s DFS");
        map.put(TAPNQuery.SearchOption.RANDOM, " -s RDFS");
        map.put(TAPNQuery.SearchOption.HEURISTIC, " -s BestFS");
        map.put(TAPNQuery.SearchOption.OVERAPPROXIMATE, " -s OverApprox");

        return map;
    }
}
