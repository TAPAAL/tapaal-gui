package net.tapaal.gui.petrinet.dialog;

import dk.aau.cs.translations.ReductionOption;
import net.tapaal.gui.petrinet.verification.UPPAALBroadcastDegree2Options;
import net.tapaal.gui.petrinet.verification.UPPAALBroadcastOptions;
import net.tapaal.gui.petrinet.verification.UPPAALCombiOptions;
import net.tapaal.gui.petrinet.verification.UPPAALOptimizedStandardOptions;
import net.tapaal.gui.petrinet.verification.UPPAALStandardOptions;
import net.tapaal.gui.petrinet.verification.VerifyDTAPNEngineOptions;
import net.tapaal.gui.petrinet.verification.VerifyPNEngineOptions;
import net.tapaal.gui.petrinet.verification.VerifyTAPNEngineOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryDialogReductionOptionTest {
    @Test
    void engineNamesMapToTheirReductionOptions() {
        assertEquals(ReductionOption.OPTIMIZEDSTANDARD,
            QueryDialog.getReductionOption(new UPPAALOptimizedStandardOptions().getNameString()));
        assertEquals(ReductionOption.STANDARD,
            QueryDialog.getReductionOption(new UPPAALStandardOptions().getNameString()));
        assertEquals(ReductionOption.COMBI,
            QueryDialog.getReductionOption(new UPPAALCombiOptions().getNameString()));
        assertEquals(ReductionOption.BROADCAST,
            QueryDialog.getReductionOption(new UPPAALBroadcastOptions().getNameString()));
        assertEquals(ReductionOption.DEGREE2BROADCAST,
            QueryDialog.getReductionOption(new UPPAALBroadcastDegree2Options().getNameString()));
        assertEquals(ReductionOption.VerifyTAPN,
            QueryDialog.getReductionOption(new VerifyTAPNEngineOptions().getNameString()));
        assertEquals(ReductionOption.VerifyDTAPN,
            QueryDialog.getReductionOption(new VerifyDTAPNEngineOptions().getNameString()));
        assertEquals(ReductionOption.VerifyPN,
            QueryDialog.getReductionOption(new VerifyPNEngineOptions().getNameString()));
    }
}
