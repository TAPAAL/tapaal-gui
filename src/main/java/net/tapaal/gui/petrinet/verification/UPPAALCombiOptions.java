package net.tapaal.gui.petrinet.verification;

import java.util.EnumSet;

public class UPPAALCombiOptions extends EngineSupportOptions {

    public UPPAALCombiOptions() {
        super(
            "UPPAAL: Optimized Broadcast Reduction",
            EnumSet.of(
                EngineFeature.DEADLOCK_NET_DEGREE_2_EXP,
                EngineFeature.WEIGHTS,
                EngineFeature.INHIBITOR_ARCS,
                EngineFeature.URGENT_TRANSITIONS,
                EngineFeature.EG_OR_AF,
                EngineFeature.STRICT_NETS,
                EngineFeature.TIMED_NETS,
                EngineFeature.EG_OR_AF_WITH_NET_DEGREE_GREATER_THAN_2
            )
        );
    }
}
