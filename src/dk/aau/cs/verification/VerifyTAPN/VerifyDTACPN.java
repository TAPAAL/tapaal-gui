package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.TAPAALGUI;
import pipe.gui.FileFinder;

public class VerifyDTACPN extends VerifyDTAPN {

    public VerifyDTACPN(FileFinder fileFinder, Messenger messenger) {
        super(fileFinder, messenger);
    }

    @Override
    public VerificationResult<TimedArcPetriNetTrace> verify(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, TAPNQuery query, DataLayer guiModel, net.tapaal.gui.petrinet.verification.TAPNQuery dataLayerQuery) throws Exception {
        if (!supportsModel(model.value1(), options)) {
            throw new UnsupportedModelException("Verifydtapn does not support the given model.");
        }

        if (!supportsQuery(model.value1(), query, options)) {
            throw new UnsupportedQueryException("Verifydtapn does not support the given query-option combination. ");
        }

        if (((VerifyTAPNOptions) options).discreteInclusion()) mapDiscreteInclusionPlacesToNewNames(options, model);
        if (TAPAALGUI.getCurrentTab().getLens().isGame() && !TAPAALGUI.getCurrentTab().getLens().isTimed() && !TAPAALGUI.getCurrentTab().getLens().isColored()) {
            addGhostPlace(model.value1());
        }

        VerifyTAPNExporter exporter = new VerifyTACPNExporter();

        ExportedVerifyTAPNModel exportedModel = exporter.export(model.value1(), query, TAPAALGUI.getCurrentTab().getLens(),model.value2(), guiModel, dataLayerQuery);

        if (exportedModel == null) {
            messenger.displayErrorMessage("There was an error exporting the model");
        }

        return verify(options, model, exportedModel, query, dataLayerQuery);
    }
}
