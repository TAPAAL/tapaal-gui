package dk.aau.cs.verification;

import pipe.gui.petrinet.dataLayer.DataLayer;

/** Composition contract for callers that also need the composed diagram. */
public interface ITAPNGuiComposer extends ITAPNComposer {
    DataLayer getGuiModel();
}
