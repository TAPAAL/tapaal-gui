package net.tapaal.gui.petrinet.verification;

import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import net.tapaal.gui.petrinet.TAPNLens;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KBoundAnalyzerRawVerificationTest {
    @Test
    void workflowCheckWithoutQueryUsesNormalKBoundOptions() {
        var options = analyzer(null).verificationOptions().toString();

        assertTrue(options.contains("--k-bound 4"));
        assertTrue(options.contains("--search-strategy BFS"));
        assertTrue(options.contains("--inclusion-places *ALL*"));
    }

    @Test
    void queryRawVerificationIsForwarded() {
        var query = new TAPNQuery(
            "bound",
            4,
            new TCTLTrueNode(),
            TAPNQuery.TraceOption.NONE,
            TAPNQuery.SearchOption.DEFAULT,
            ReductionOption.VerifyTAPN,
            false,
            false,
            false,
            false,
            TAPNQuery.HashTableSize.MB_16,
            TAPNQuery.ExtrapolationOption.AUTOMATIC,
            false
        );
        query.setRawVerification(true);
        query.setRawVerificationPrompt("--engine-raw");

        assertEquals("--engine-raw", analyzer(query).verificationOptions().toString());
    }

    private static KBoundAnalyzer analyzer(TAPNQuery query) {
        return new KBoundAnalyzer(
            new TimedArcPetriNetNetwork(),
            new TAPNLens(true, false, false, false),
            null,
            4,
            new VerifyTAPN(null, null),
            null,
            null,
            query
        );
    }
}
