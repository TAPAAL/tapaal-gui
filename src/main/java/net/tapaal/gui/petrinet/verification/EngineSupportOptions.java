package net.tapaal.gui.petrinet.verification;

public class EngineSupportOptions {
    public final String nameString;
    public final boolean supportFastestTrace;
    public final boolean supportDeadlockNetdegree2EForAG;
    public final boolean supportDeadlockWithInhib;
    public final boolean supportWeights;
    public final boolean supportInhibArcs;
    public final boolean supportUrgentTransitions;
    public final boolean supportEGorAF;
    public final boolean supportStrictNets;
    public final boolean supportTimedNets;
    public final boolean supportDeadlockNetdegreeGreaterThan2;
    public final boolean supportGames;
    public final boolean supportEGorAFWithNetDegreeGreaterThan2;
    public final boolean supportNestedQuantifications;
    public final boolean supportColored;
    public final boolean supportOnlyUntimed;
    public final boolean supportSmc;

    public final boolean[] optionsArray;
    public EngineSupportOptions(String nameString, boolean supportFastestTrace, boolean supportDeadlockNetdegree2EForAG, boolean supportDeadlockEGorAF, boolean supportDeadlockWithInhib,
                                boolean supportWeights, boolean supportInhibArcs, boolean supportUrgentTransitions, boolean supportEGorAF, boolean supportStrictNets, boolean supportTimedNets,
                                boolean supportDeadlockNetdegreeGreaterThan2, boolean supportGames, boolean supportEGorAFWithNetDegreeGreaterThan2, boolean supportNestedQuantifications,
                                boolean supportColored, boolean supportOnlyUntimed, boolean supportSmc){
        this.nameString = nameString;
        this.supportFastestTrace = supportFastestTrace;
        this.supportDeadlockNetdegree2EForAG = supportDeadlockNetdegree2EForAG;
        this.supportDeadlockWithInhib = supportDeadlockWithInhib;
        this.supportWeights = supportWeights;
        this.supportInhibArcs = supportInhibArcs;
        this.supportUrgentTransitions = supportUrgentTransitions;
        this.supportEGorAF = supportEGorAF;
        this.supportStrictNets = supportStrictNets;
        this.supportTimedNets = supportTimedNets;
        this.supportDeadlockNetdegreeGreaterThan2 = supportDeadlockNetdegreeGreaterThan2;
        this.supportGames = supportGames;
        this.supportEGorAFWithNetDegreeGreaterThan2 = supportEGorAFWithNetDegreeGreaterThan2;
        this.supportNestedQuantifications = supportNestedQuantifications;
        this.supportColored = supportColored;
        this.supportOnlyUntimed = supportOnlyUntimed;
        this.supportSmc = supportSmc;
        this.optionsArray = new boolean[]{supportFastestTrace, supportDeadlockNetdegree2EForAG, supportDeadlockEGorAF, supportDeadlockWithInhib,
            supportWeights, supportInhibArcs, supportUrgentTransitions, supportEGorAF, supportStrictNets, supportTimedNets, supportDeadlockNetdegreeGreaterThan2,
            supportGames, supportEGorAFWithNetDegreeGreaterThan2, supportNestedQuantifications, supportColored, supportOnlyUntimed, supportSmc};
    }

    public boolean areOptionsSupported(boolean[] queryOptions){
        for(int i = 0; i < optionsArray.length; i++){
            if(queryOptions[i] == true && optionsArray[i] != true){
                return false;
            }
        }
        return true;
    }

}
