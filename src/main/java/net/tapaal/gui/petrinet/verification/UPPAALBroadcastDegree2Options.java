package net.tapaal.gui.petrinet.verification;

import java.util.EnumSet;

public class UPPAALBroadcastDegree2Options extends EngineSupportOptions {

    public UPPAALBroadcastDegree2Options() {
        super(
            "UPPAAL: Broadcast Degree 2 Reduction",
            EnumSet.of(
                EngineFeature.DEADLOCK_NET_DEGREE_2_EXP,
                EngineFeature.INHIBITOR_ARCS,
                EngineFeature.EG_OR_AF,
                EngineFeature.STRICT_NETS,
                EngineFeature.TIMED_NETS,
                EngineFeature.EG_OR_AF_WITH_NET_DEGREE_GREATER_THAN_2
            )
        );
    }
}
