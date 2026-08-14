package net.tapaal.gui.petrinet.verification;

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
}
