package net.tapaal.gui.petrinet.verification;

import java.util.EnumSet;

public class UPPAALBroadcastOptions extends EngineSupportOptions {

    public UPPAALBroadcastOptions() {
        super(
            "UPPAAL: Broadcast Reduction",
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
