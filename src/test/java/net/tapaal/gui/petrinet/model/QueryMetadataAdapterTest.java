package net.tapaal.gui.petrinet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dk.aau.cs.TCTL.LTLFNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.verification.QueryType;

class QueryMetadataAdapterTest {

    @Test
    void mapsQueryCategoriesAtTheGuiModelBoundary() {
        for (net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory guiCategory
                : net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory.values()) {
            TAPNQuery.QueryCategory modelCategory = QueryMetadataAdapter.toModelCategory(guiCategory);

            assertEquals(guiCategory.name(), modelCategory.name());
            assertSame(guiCategory, QueryMetadataAdapter.toGuiCategory(modelCategory));
        }
    }

    @Test
    void mapsVerificationTypesToTheModel() {
        for (net.tapaal.gui.petrinet.verification.TAPNQuery.VerificationType guiType
                : net.tapaal.gui.petrinet.verification.TAPNQuery.VerificationType.values()) {
            assertEquals(guiType.name(), QueryMetadataAdapter.toModelVerificationType(guiType).name());
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
