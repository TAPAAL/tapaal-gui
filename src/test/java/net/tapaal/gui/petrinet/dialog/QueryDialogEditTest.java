package net.tapaal.gui.petrinet.dialog;

import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.translations.ReductionOption;
import net.tapaal.gui.petrinet.TAPNLens;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import pipe.gui.swingcomponents.EscapableDialog;
import org.junit.jupiter.api.Test;

import java.awt.Frame;
import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotSame;

class QueryDialogEditTest {
    @Test
    void editingAnExistingQueryUsesAnIndependentPropertyTree() throws Exception {
        var network = new TimedArcPetriNetNetwork();
        var template = new TimedArcPetriNet("Template");
        template.add(new LocalTimedPlace("P"));
        network.add(template);
        var query = new TAPNQuery(
            "query",
            1,
            new TCTLEFNode(new TCTLPlaceNode("Template", "P")),
            TAPNQuery.TraceOption.NONE,
            TAPNQuery.SearchOption.HEURISTIC,
            ReductionOption.VerifyTAPN,
            false,
            false,
            false,
            false,
            TAPNQuery.HashTableSize.MB_16,
            TAPNQuery.ExtrapolationOption.AUTOMATIC,
            false
        );
        var dialog = new EscapableDialog((Frame) null, "Query", true);
        var queryDialog = new QueryDialog(
            dialog,
            QueryDialog.QueryDialogueOption.Save,
            query,
            network,
            new HashMap<>(),
            new TAPNLens(false, false, false, false),
            null
        );

        Field property = QueryDialog.class.getDeclaredField("newProperty");
        property.setAccessible(true);

        assertNotSame(query.getProperty(), property.get(queryDialog));
    }
}
