package net.tapaal.gui.petrinet.verification;

import java.util.EnumSet;

public class VerifyPNEngineOptions extends EngineSupportOptions {

    public VerifyPNEngineOptions() {
        super(
            "TAPAAL: Untimed Engine (verifypn)",
            EnumSet.of(
                EngineFeature.DEADLOCK_NET_DEGREE_2_EXP,
                EngineFeature.DEADLOCK_EG_OR_AF,
                EngineFeature.DEADLOCK_WITH_INHIB,
                EngineFeature.WEIGHTS,
                EngineFeature.INHIBITOR_ARCS,
                EngineFeature.EG_OR_AF,
                EngineFeature.DEADLOCK_NET_DEGREE_GREATER_THAN_2,
                EngineFeature.GAMES,
                EngineFeature.EG_OR_AF_WITH_NET_DEGREE_GREATER_THAN_2,
                EngineFeature.NESTED_QUANTIFICATIONS,
                EngineFeature.COLORED,
                EngineFeature.ONLY_UNTIMED,
                EngineFeature.COLORED_PLACE_QUERIES
            )
        );
    }
}