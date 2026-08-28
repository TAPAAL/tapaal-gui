package net.tapaal.gui.petrinet.verification;

import dk.aau.cs.translations.ReductionOption;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class EngineSupportOptions {
    private final String nameString;
    private final Set<EngineFeature> supportedFeatures;

    public EngineSupportOptions(String nameString, Set<EngineFeature> supportedFeatures) {
        this.nameString = nameString;
        this.supportedFeatures = supportedFeatures.isEmpty()
            ? EnumSet.noneOf(EngineFeature.class)
            : EnumSet.copyOf(supportedFeatures);
    }

    public String getNameString() {
        return nameString;
    }

    public boolean supports(EngineFeature feature) {
        return supportedFeatures.contains(feature);
    }

    public boolean supportsColoredPlaceQueries() {
        return supports(EngineFeature.COLORED_PLACE_QUERIES);
    }

    public boolean supportsNonzeroInitialTokenAges() {
        return supports(EngineFeature.NONZERO_INITIAL_TOKEN_AGES);
    }

    public boolean areOptionsSupported(Collection<EngineFeature> queryOptions) {
        return supportedFeatures.containsAll(queryOptions);
    }

    public static EngineSupportOptions fromReductionOption(ReductionOption reductionOption) {
        if (reductionOption == null) return null;
        switch (reductionOption) {
            case VerifyDTAPN:
                return new VerifyDTAPNEngineOptions();
            case VerifyPN:
                return new VerifyPNEngineOptions();
            case VerifyTAPN:
                return new VerifyTAPNEngineOptions();
            case BROADCAST:
                return new UPPAALBroadcastOptions();
            case DEGREE2BROADCAST:
                return new UPPAALBroadcastDegree2Options();
            case COMBI:
                return new UPPAALCombiOptions();
            case STANDARD:
                return new UPPAALStandardOptions();
            case OPTIMIZEDSTANDARD:
                return new UPPAALOptimizedStandardOptions();
            default:
                return null;
        }
    }
}
