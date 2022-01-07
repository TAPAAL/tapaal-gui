package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.verification.NameMapping;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import pipe.gui.petrinet.dataLayer.DataLayer;
import net.tapaal.gui.petrinet.Template;
import pipe.gui.canvas.Zoomer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class VerifyTACPNExporter extends  VerifyTAPNExporter {
    protected void outputModel(TimedArcPetriNet model, File modelFile, NameMapping mapping, DataLayer guiModel) throws FileNotFoundException {
        ArrayList<Template> templates = new ArrayList<>(1);
        ArrayList<TAPNQuery> queries = new ArrayList<>(1);
        templates.add(new Template(model, guiModel, new Zoomer()));

        TimedArcPetriNetNetworkWriter writerTACPN = new TimedArcPetriNetNetworkWriter(model.parentNetwork(), templates, queries, model.parentNetwork().constants());
        try {
            writerTACPN.savePNML(modelFile);
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }
}
