package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.verification.VerificationOptions;
import pipe.dataLayer.TAPNQuery;

import java.util.HashMap;
import java.util.Map;

public class VerifyPNUnfoldOptions extends VerificationOptions {

    private static final Map<TAPNQuery.SearchOption, String> searchMap = createSearchOptionsMap();
    private String modelOut;
    private String queryOut;
    private String verifydtapnOptions;
    private int numQueries;
    private boolean reduceQuery;
    private boolean enableStructuralReductions;
    private TAPNQuery.SearchOption searchOption;
    public VerifyPNUnfoldOptions(String modelOut, String queryOut, String verifydtapnOptions, TAPNQuery.SearchOption search, boolean reduceQuery, boolean enableStructuralReductions, int numQueries) {
        this.modelOut = modelOut;
        this.queryOut = queryOut;
        this.verifydtapnOptions = verifydtapnOptions;
        this.reduceQuery = reduceQuery;
        this.enableStructuralReductions = enableStructuralReductions;
        this.numQueries = numQueries;
        searchOption = search;
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
        StringBuilder result = new StringBuilder();
        result.append("--write-simplified " + queryOut + " --write-reduced " + modelOut + " -x 1");
        for(int i = 2; i <= numQueries; i++){
            result.append("," + i);
        }

        /*result.append(searchMap.get(searchOption));

        if(!reduceQuery){
            result.append(" -q 0 ");
        }
        if(!enableStructuralReductions){
            result.append(" -r 0 ");
        }*/

        if (verifydtapnOptions.equals("tt"))
        result.append(" -verifydtapn " + verifydtapnOptions);

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
