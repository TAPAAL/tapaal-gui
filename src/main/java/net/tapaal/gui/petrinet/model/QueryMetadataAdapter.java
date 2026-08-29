package net.tapaal.gui.petrinet.model;

/**
 * Converts query metadata at the GUI/application boundary. The model query
 * owns its enums while the GUI query keeps its richer editor-facing metadata.
 */
public final class QueryMetadataAdapter {
    private QueryMetadataAdapter() {
    }

    public static dk.aau.cs.model.tapn.TAPNQuery.VerificationType toModelVerificationType(
        net.tapaal.gui.petrinet.verification.TAPNQuery.VerificationType verificationType) {
        return dk.aau.cs.model.tapn.TAPNQuery.VerificationType.valueOf(verificationType.name());
    }

    public static dk.aau.cs.verification.VerificationOptions.TraceOption toVerificationTraceOption(
        net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption traceOption) {
        return dk.aau.cs.verification.VerificationOptions.TraceOption.valueOf(traceOption.name());
    }

    public static dk.aau.cs.verification.VerificationOptions.SearchOption toVerificationSearchOption(
        net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption searchOption) {
        return dk.aau.cs.verification.VerificationOptions.SearchOption.valueOf(searchOption.name());
    }
}
