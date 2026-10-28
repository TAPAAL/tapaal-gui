package net.tapaal.gui;

import pipe.gui.petrinet.PetriNetTab;

import java.util.Optional;

/**
 * The narrow application seam for resolving the active tab.
 *
 * Implementations return empty when no tab is active. A returned tab is the
 * tab that owns the current interaction, not a selected-index calculation
 * that callers need to repeat.
 */
public interface TabInteraction {
    Optional<PetriNetTab> getCurrentTab();
}
