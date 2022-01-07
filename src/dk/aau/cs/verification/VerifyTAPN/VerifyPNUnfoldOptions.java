package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.verification.VerificationOptions;
import net.tapaal.gui.verification.TAPNQuery;

import java.util.HashMap;
import java.util.Map;

public class VerifyPNUnfoldOptions extends VerificationOptions {

    private static final Map<TAPNQuery.SearchOption, String> searchMap = createSearchOptionsMap();
    private String modelOut;
    private String queryOut;
    private int numQueries;
    private boolean partition;
    private  boolean computeColorFixpoint;
    private boolean symmetricVars;

    public VerifyPNUnfoldOptions(String modelOut, String queryOut, int numQueries, boolean partition, boolean computeColorFixpoint, boolean useSymmetricVars) {
        this.modelOut = modelOut;
        this.queryOut = queryOut;
        this.numQueries = numQueries;
        this.partition = partition;
        this.computeColorFixpoint = computeColorFixpoint;
        symmetricVars = useSymmetricVars;
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
        //for now we don't want to do structural or query reductions, could be options later
        result.append("--write-unfolded-queries ");
        result.append(queryOut);
        result.append(" --write-unfolded-net ");
        result.append(modelOut);
        result.append(" --search-strategy OverApprox --reduction 0 --query-reduction 0 --xml-queries 1");
        for(int i = 2; i <= numQueries; i++){
            result.append("," + i);
        }

        if(!partition){
            result.append(" --disable-partitioning");
        }
        if(!computeColorFixpoint){
            result.append(" --disable-cfp");
        }

        if(!symmetricVars){
            result.append(" --disable-symmetry-vars");
        }

        return result.toString();
    }

    private static final Map<TAPNQuery.SearchOption, String> createSearchOptionsMap() {
        HashMap<TAPNQuery.SearchOption, String> map = new HashMap<TAPNQuery.SearchOption, String>();
        map.put(TAPNQuery.SearchOption.BFS, " --search-strategy BFS");
        map.put(TAPNQuery.SearchOption.DFS, " --search-strategy DFS");
        map.put(TAPNQuery.SearchOption.RANDOM, " --search-strategy RDFS");
        map.put(TAPNQuery.SearchOption.HEURISTIC, " --search-strategy BestFS");
        map.put(TAPNQuery.SearchOption.OVERAPPROXIMATE, " --search-strategy OverApprox");

        return map;
    }
}
