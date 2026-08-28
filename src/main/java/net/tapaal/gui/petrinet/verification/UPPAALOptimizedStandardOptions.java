package net.tapaal.gui.petrinet.verification;

import java.util.EnumSet;

public class UPPAALOptimizedStandardOptions extends EngineSupportOptions {

    public UPPAALOptimizedStandardOptions() {
        super(
            "UPPAAL: Optimised Standard Reduction",
            EnumSet.of(
                EngineFeature.EG_OR_AF,
                EngineFeature.STRICT_NETS,
                EngineFeature.TIMED_NETS
            )
        );
    }
}
