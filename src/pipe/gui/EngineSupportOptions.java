package pipe.gui;

import java.util.ArrayList;

public class EngineSupportOptions {
    private String nameString;
    private boolean supportFastestTrace;
    private boolean supportDeadlockNetdegree2EForAG;
    private boolean supportDeadlockWithInhib;
    private boolean supportWeights;
    private boolean supportInhibArcs;
    private boolean supportUrgentTransitions;
    private boolean supportEGorAF;
    private boolean supportStrictNets;
    private boolean supportTimedNets;
    boolean supportDeadlockNetdegreeGreaterThan2;
    private boolean[] optionsArray;
    public EngineSupportOptions(String nameString, boolean supportFastestTrace, boolean supportDeadlockNetdegree2EForAG, boolean supportDeadlockEGorAF, boolean supportDeadlockWithInhib,
                                boolean supportWeights, boolean supportInhibArcs, boolean supportUrgentTransitions, boolean supportEGorAF, boolean supportStrictNets, boolean supportTimedNets, boolean supportDeadlockNetdegreeGreaterThan2){
        this.nameString = nameString;
        this.supportFastestTrace =  supportFastestTrace;
        this.supportDeadlockNetdegree2EForAG =  supportDeadlockNetdegree2EForAG;
        this.supportDeadlockWithInhib =  supportDeadlockWithInhib;
        this.supportWeights =  supportWeights;
        this.supportInhibArcs =  supportInhibArcs;
        this.supportUrgentTransitions =  supportUrgentTransitions;
        this.supportEGorAF =  supportEGorAF;
        this.supportStrictNets =  supportStrictNets;
        this.supportTimedNets = supportTimedNets;
        this.supportDeadlockNetdegreeGreaterThan2 = supportDeadlockNetdegreeGreaterThan2;
        this.optionsArray = new boolean[]{supportFastestTrace, supportDeadlockNetdegree2EForAG, supportDeadlockEGorAF, supportDeadlockWithInhib, supportWeights, supportInhibArcs, supportUrgentTransitions, supportEGorAF, supportStrictNets, supportTimedNets, supportDeadlockNetdegreeGreaterThan2};
    }
    public String getNameString(){
        return nameString;
    }
    public boolean getSupportFastestTrace(){
        return supportFastestTrace;
    }
    public boolean getSupportDeadlockNetdegree2EForAG(){
        return supportDeadlockNetdegree2EForAG;
    }
    public boolean getSupportDeadlockWithInhib(){
        return supportDeadlockWithInhib;
    }
    public boolean getSupportWeights(){
        return supportWeights;
    }
    public boolean getSupportInhibArcs(){
        return supportInhibArcs;
    }
    public boolean getSupportUrgentTransitions(){
        return supportUrgentTransitions;
    }
    public boolean getSupportEGorAF(){
        return supportEGorAF;
    }
    public boolean getSupportStrictNets(){
        return supportStrictNets;
    }
    public boolean[] getOptionsAsArray(){
        return optionsArray;
    }
    public boolean getSupportTimedNets(){
        return supportTimedNets;
    }
    public boolean getSupportDeadlockNetdegreeGreaterThan2(){
        return supportDeadlockNetdegreeGreaterThan2;
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
