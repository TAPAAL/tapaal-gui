package net.tapaal.gui.petrinet.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.gui.petrinet.Template;
import pipe.gui.canvas.Zoomer;
import pipe.gui.petrinet.dataLayer.DataLayer;

/**
 * Owns the lifetime and association of one document's model templates and
 * their diagrams.  UI code can ask this object for an association, but does
 * not need to maintain parallel maps or mutate them directly.
 */
public final class DocumentSession {
    private final TimedArcPetriNetNetwork network;
    private final Map<TimedArcPetriNet, Template> templates = new LinkedHashMap<>();
    private final Map<DataLayer, TimedArcPetriNet> modelByDiagram = new IdentityHashMap<>();

    public DocumentSession(TimedArcPetriNetNetwork network) {
        if (network == null) {
            throw new IllegalArgumentException("network cannot be null");
        }
        this.network = network;
    }

    /** Registers templates already present in the model, for example after loading a file. */
    public void register(Template template) {
        if (template == null || template.model() == null || template.guiModel() == null) {
            throw new IllegalArgumentException("template and its model/diagram must be non-null");
        }
        Template previous = templates.put(template.model(), template);
        if (previous != null) {
            modelByDiagram.remove(previous.guiModel());
        }
        modelByDiagram.put(template.guiModel(), template.model());
    }

    /** Adds a new model template and registers its diagram in the same operation. */
    public void add(Template template) {
        network.add(template.model());
        register(template);
    }

    public void remove(Template template) {
        if (template == null) return;
        network.remove(template.model());
        templates.remove(template.model());
        modelByDiagram.remove(template.guiModel());
    }

    public Template templateFor(TimedArcPetriNet model) {
        return templates.get(model);
    }

    public TimedArcPetriNet modelFor(DataLayer diagram) {
        return modelByDiagram.get(diagram);
    }

    public DataLayer diagramFor(TimedArcPetriNet model) {
        Template template = templateFor(model);
        return template == null ? null : template.guiModel();
    }

    public Zoomer zoomerFor(TimedArcPetriNet model) {
        Template template = templateFor(model);
        return template == null ? null : template.zoomer();
    }

    public Collection<Template> templates() {
        List<Template> ordered = new ArrayList<>();
        for (TimedArcPetriNet model : network.allTemplates()) {
            Template template = templates.get(model);
            if (template != null) ordered.add(template);
        }
        return Collections.unmodifiableList(ordered);
    }

    public List<Template> activeTemplates() {
        List<Template> active = new ArrayList<>();
        for (TimedArcPetriNet model : network.activeTemplates()) {
            Template template = templates.get(model);
            if (template != null) active.add(template);
        }
        return active;
    }

    /**
     * Legacy integrations still accept a HashMap. Return a snapshot so those
     * integrations cannot corrupt the document's associations.
     */
    public HashMap<TimedArcPetriNet, DataLayer> diagramSnapshot() {
        HashMap<TimedArcPetriNet, DataLayer> snapshot = new HashMap<>();
        for (Map.Entry<TimedArcPetriNet, Template> entry : templates.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue().guiModel());
        }
        return snapshot;
    }
}
