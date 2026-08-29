package net.tapaal.gui.petrinet.verification;

import java.util.EnumSet;

public class UPPAALStandardOptions extends EngineSupportOptions {

    public UPPAALStandardOptions() {
        super(
            "UPPAAL: Standard Reduction",
            EnumSet.of(
                EngineFeature.STRICT_NETS,
                EngineFeature.TIMED_NETS
            )
        );
    }
}
