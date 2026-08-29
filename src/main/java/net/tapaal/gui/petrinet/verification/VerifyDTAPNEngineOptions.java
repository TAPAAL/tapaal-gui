package net.tapaal.gui.petrinet.verification;

import java.util.EnumSet;

public class VerifyDTAPNEngineOptions extends EngineSupportOptions {

    public VerifyDTAPNEngineOptions() {
        super(
            "TAPAAL: Discrete Engine (verifydtapn)",
            EnumSet.of(
                EngineFeature.FASTEST_TRACE,
                EngineFeature.DEADLOCK_NET_DEGREE_2_EXP,
                EngineFeature.DEADLOCK_EG_OR_AF,
                EngineFeature.DEADLOCK_WITH_INHIB,
                EngineFeature.WEIGHTS,
                EngineFeature.INHIBITOR_ARCS,
                EngineFeature.URGENT_TRANSITIONS,
                EngineFeature.EG_OR_AF,
                EngineFeature.TIMED_NETS,
                EngineFeature.DEADLOCK_NET_DEGREE_GREATER_THAN_2,
                EngineFeature.GAMES,
                EngineFeature.EG_OR_AF_WITH_NET_DEGREE_GREATER_THAN_2,
                EngineFeature.COLORED,
                EngineFeature.SMC,
                EngineFeature.COLORED_PLACE_QUERIES,
                EngineFeature.NONZERO_INITIAL_TOKEN_AGES
            )
        );
    }
}
