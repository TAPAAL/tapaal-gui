package net.tapaal.gui.petrinet.verification;

import java.util.EnumSet;

public class VerifyTAPNEngineOptions extends EngineSupportOptions {

    public VerifyTAPNEngineOptions() {
        super(
            "TAPAAL: Continuous Engine (verifytapn)",
            EnumSet.of(
                EngineFeature.INHIBITOR_ARCS,
                EngineFeature.COLORED_INHIBITOR_ARCS,
                EngineFeature.STRICT_NETS,
                EngineFeature.TIMED_NETS,
                EngineFeature.COLORED,
                EngineFeature.COLORED_PLACE_QUERIES,
                EngineFeature.NONZERO_INITIAL_TOKEN_AGES
            )
        );
    }
}
