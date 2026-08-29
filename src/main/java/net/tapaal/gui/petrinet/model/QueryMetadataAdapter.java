package net.tapaal.gui.petrinet.model;

/**
 * Converts query metadata at the GUI/application boundary. The model query
 * owns its enums while the GUI query keeps its richer editor-facing metadata.
 */
public final class QueryMetadataAdapter {
    private QueryMetadataAdapter() {
    }

    public static dk.aau.cs.model.tapn.TAPNQuery.QueryCategory toModelCategory(
        net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory category) {
        return dk.aau.cs.model.tapn.TAPNQuery.QueryCategory.valueOf(category.name());
    }

    public static dk.aau.cs.model.tapn.TAPNQuery.VerificationType toModelVerificationType(
        net.tapaal.gui.petrinet.verification.TAPNQuery.VerificationType verificationType) {
        return dk.aau.cs.model.tapn.TAPNQuery.VerificationType.valueOf(verificationType.name());
    }

    public static net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory toGuiCategory(
        dk.aau.cs.model.tapn.TAPNQuery.QueryCategory category) {
        return net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory.valueOf(category.name());
    }
}
