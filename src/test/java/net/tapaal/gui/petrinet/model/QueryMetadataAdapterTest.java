package net.tapaal.gui.petrinet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dk.aau.cs.TCTL.LTLFNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.verification.QueryType;

class QueryMetadataAdapterTest {

    @Test
    void mapsVerificationTypesToTheModel() {
        for (net.tapaal.gui.petrinet.verification.TAPNQuery.VerificationType guiType
                : net.tapaal.gui.petrinet.verification.TAPNQuery.VerificationType.values()) {
            assertEquals(guiType.name(), QueryMetadataAdapter.toModelVerificationType(guiType).name());
        }
    }

    @Test
    void mapsVerificationOptionsToTheVerificationLayer() {
        for (net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption guiTraceOption
                : net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption.values()) {
            assertEquals(guiTraceOption.name(),
                QueryMetadataAdapter.toVerificationTraceOption(guiTraceOption).name());
        }
        for (net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption guiSearchOption
                : net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption.values()) {
            assertEquals(guiSearchOption.name(),
                QueryMetadataAdapter.toVerificationSearchOption(guiSearchOption).name());
        }
    }

    @Test
    void modelQueryRetainsSmcTypingAndSimulationBehavior() {
        TAPNQuery query = new TAPNQuery(new LTLFNode(new TCTLTrueNode()), 0);
        query.setCategory(TAPNQuery.QueryCategory.SMC);
        query.setVerificationType(TAPNQuery.VerificationType.SIMULATE);

        assertEquals(QueryType.PF, query.queryType());
        assertTrue(query.isSimulate());
    }
}
