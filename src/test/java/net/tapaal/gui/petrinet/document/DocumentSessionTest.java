package net.tapaal.gui.petrinet.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.gui.petrinet.Template;
import pipe.gui.canvas.Zoomer;
import pipe.gui.petrinet.dataLayer.DataLayer;

class DocumentSessionTest {

    @Test
    void ownsModelDiagramAndZoomAssociation() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        TimedArcPetriNet model = new TimedArcPetriNet("Template");
        DataLayer diagram = new DataLayer();
        Template template = new Template(model, diagram, new Zoomer());
        network.add(model);

        DocumentSession session = new DocumentSession(network);
        session.register(template);

        assertSame(template, session.templateFor(model));
        assertSame(model, session.modelFor(diagram));
        assertSame(diagram, session.diagramFor(model));
        assertSame(template.zoomer(), session.zoomerFor(model));
        assertEquals(List.of(template), List.copyOf(session.templates()));
    }

    @Test
    void addAndRemoveKeepTheNetworkAndAssociationInSync() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        DocumentSession session = new DocumentSession(network);
        Template template = new Template(new TimedArcPetriNet("Template"), new DataLayer(), new Zoomer());

        session.add(template);
        assertSame(template.model(), network.getTAPNByName("Template"));
        assertSame(template, session.templateFor(template.model()));

        session.remove(template);
        assertEquals(0, network.allTemplates().size());
        assertEquals(null, session.templateFor(template.model()));
        assertEquals(null, session.modelFor(template.guiModel()));
    }

    @Test
    void callersCannotMutateTheSessionTemplateCollection() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        DocumentSession session = new DocumentSession(network);
        Template template = new Template(new TimedArcPetriNet("Template"), new DataLayer(), new Zoomer());
        session.register(template);

        assertThrows(UnsupportedOperationException.class, () -> session.templates().clear());
    }

    @Test
    void exposesTemplatesInTheNetworkOrder() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        TimedArcPetriNet firstModel = new TimedArcPetriNet("First");
        TimedArcPetriNet secondModel = new TimedArcPetriNet("Second");
        network.add(firstModel);
        network.add(secondModel);
        Template first = new Template(firstModel, new DataLayer(), new Zoomer());
        Template second = new Template(secondModel, new DataLayer(), new Zoomer());

        DocumentSession session = new DocumentSession(network);
        session.register(first);
        session.register(second);
        network.swapTemplates(0, 1);

        assertEquals(List.of(second, first), List.copyOf(session.templates()));
    }
}
