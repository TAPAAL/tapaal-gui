package net.tapaal.gui.petrinet.verification;

public class EngineSupportOptions {
    private final boolean[] optionsArray;
    private final String nameString;

    public EngineSupportOptions(String nameString, boolean supportFastestTrace, boolean supportDeadlockNetdegree2EForAG, boolean supportDeadlockEGorAF, boolean supportDeadlockWithInhib,
                                boolean supportWeights, boolean supportInhibArcs, boolean supportUrgentTransitions, boolean supportEGorAF, boolean supportStrictNets, boolean supportTimedNets,
                                boolean supportDeadlockNetdegreeGreaterThan2, boolean supportGames, boolean supportEGorAFWithNetDegreeGreaterThan2, boolean supportNestedQuantifications,
                                boolean supportColored, boolean supportOnlyUntimed, boolean supportSmc){
        this.nameString = nameString;
        this.optionsArray = new boolean[]{supportFastestTrace, supportDeadlockNetdegree2EForAG, supportDeadlockEGorAF, supportDeadlockWithInhib,
            supportWeights, supportInhibArcs, supportUrgentTransitions, supportEGorAF, supportStrictNets, supportTimedNets, supportDeadlockNetdegreeGreaterThan2,
            supportGames, supportEGorAFWithNetDegreeGreaterThan2, supportNestedQuantifications, supportColored, supportOnlyUntimed, supportSmc};
    }

    public String getNameString() {
        return nameString;
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
