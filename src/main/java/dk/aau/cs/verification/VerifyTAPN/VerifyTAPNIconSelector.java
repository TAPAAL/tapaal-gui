package dk.aau.cs.verification.VerifyTAPN;

import javax.swing.ImageIcon;

import dk.aau.cs.verification.Boundedness;
import dk.aau.cs.verification.IconSelector;
import dk.aau.cs.verification.VerificationResult;

public class VerifyTAPNIconSelector extends IconSelector {

    @Override
    public ImageIcon getIconFor(VerificationResult<?> result) {
        if (result.getQueryResult().isApproximationInconclusive())
            return rerunIcon;
        switch (result.getQueryResult().queryType()) {
            case EF:
                if (result.isQuerySatisfied()) {
                    return satisfiedIcon;
                } else if (!result.isQuerySatisfied() && result.getQueryResult().boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded)) {
                    return notSatisfiedIcon;
                }
                break;
            case AG:
                if (!result.isQuerySatisfied() && (result.getBound() == -1 || result.getBound() >= result.getQueryResult().boundednessAnalysis().usedTokens())) {
                    return notSatisfiedIcon;
                } else if (result.isQuerySatisfied() && result.getQueryResult().boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded)) {
                    return satisfiedIcon;
                }
                break;
            case AF:
                if (!result.isQuerySatisfied() && result.getQueryResult().boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded)) {
                    return notSatisfiedIcon;
                } else if (result.isQuerySatisfied() && result.getQueryResult().boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded)) {
                    return satisfiedIcon;
                }
                break;
            case EG:
                if (result.isQuerySatisfied() && result.getQueryResult().boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded)) {
                    return satisfiedIcon;
                } else if (!result.isQuerySatisfied() && result.getQueryResult().boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded)) {
                    return notSatisfiedIcon;
                }
                break;
            case A:
                if (!result.isQuerySatisfied()) {
                    return notSatisfiedIcon;
                } else if (result.isQuerySatisfied() && (result.getBound() == -1 || result.getBound() >= result.getQueryResult().boundednessAnalysis().usedTokens())) {
                    return satisfiedIcon;
                }
                break;
            case E:
                if (!result.isQuerySatisfied() && (result.getBound() == -1 || result.getBound() >= result.getQueryResult().boundednessAnalysis().usedTokens())) {
                    return notSatisfiedIcon;
                } else if (result.isQuerySatisfied()) {
                    return satisfiedIcon;
                }
                break;
            case PF:
            case PG:
                if(result.getQueryResult().isQuantitative() || result.isQuerySatisfied()) {
                    return satisfiedIcon;
                } else {
                    return notSatisfiedIcon;
                }
            default:
                return null;
        }

        if (result.getQueryResult().isDiscreteIncludion()) {
            return rerunIcon;
        } else {
            return inconclusiveIcon;
        }
    }
}
