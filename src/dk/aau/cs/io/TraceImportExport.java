package dk.aau.cs.io;

import pipe.gui.CreateGui;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.swing.JOptionPane;
import pipe.gui.widgets.filebrowser.FileBrowser;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTimedTransitionStep;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTraceStep;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.TAPNTraceDecomposer;
import static dk.aau.cs.verification.VerifyTAPN.TraceType.EG_DELAY_FOREVER;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNTraceParser;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class TraceImportExport {

    public static void exportTrace() {
        String path = null;
        try {
            ByteArrayOutputStream os = prepareTraceStream();

            FileBrowser fb = FileBrowser.constructor("Export Trace", "trc");
            // path = fb.saveFile(CreateGui.appGui.getCurrentTabName().substring(0, CreateGui.appGui.getCurrentTabName().lastIndexOf('.')) + "-trace");
            path = fb.saveFile(CreateGui.getAppGui().getCurrentTabName().substring(0, CreateGui.getAppGui().getCurrentTabName().lastIndexOf('.')));

            FileOutputStream fs = new FileOutputStream(path);
            fs.write(os.toByteArray());
            fs.close();
        } catch (ParserConfigurationException e) {
            System.err
                    .println("ParserConfigurationException thrown in exportTrace() "
                            + ": Animator Class : filename=\"");
        } catch (DOMException e) {
            System.err
                    .println("DOMException thrown in exportTrace() "
                            + ": Animator Class : filename=\""
                            + path + "\" transformer=\"");
        } catch (TransformerConfigurationException e) {
            System.err
                    .println("TransformerConfigurationException thrown in exportTrace()) "
                            + ": Animator Class :  filename=\""
                            + path
                            + "\" transformer=\"");
        } catch (TransformerException e) {
            System.err
                    .println("TransformerException thrown in exportTrace() : Animator Class : filename=\""
                            + path
                            + "\"" + e);
        } catch (NullPointerException e) {
            // Aborted by user
        } catch (IOException e) {
            JOptionPane.showMessageDialog(CreateGui.getApp(), "Error exporting trace.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static ByteArrayOutputStream prepareTraceStream() throws IOException, ParserConfigurationException, DOMException, TransformerConfigurationException, TransformerException {
        Document document;
        Transformer transformer;
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // Build a Trace XML Document
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        document = builder.newDocument();

        Element traceRootNode = document.createElement("trace"); // Trace Top Level
        document.appendChild(traceRootNode);

        // Output the trace to XML document
        TAPNComposer composer = new TAPNComposer(new pipe.gui.MessengerImpl(), CreateGui.getCurrentTab().getGuiModels(), false, true);

        for (TAPNNetworkTraceStep step : CreateGui.getAnimator().getActionHistory()) {
            if (step.isLoopStep()) {
                Element loopElement = document.createElement("loop");
                traceRootNode.appendChild(loopElement);
            }

            if (step instanceof TAPNNetworkTimedTransitionStep) {
                TimedTransition transition = ((TAPNNetworkTimedTransitionStep) step).getTransition();
                Element transitionElement = document.createElement("transition"); // Create transition
                transitionElement.setAttribute("id", composer.composedTransitionName(transition));
                traceRootNode.appendChild(transitionElement);

                List<TimedToken> consumedTokens = ((TAPNNetworkTimedTransitionStep) step).getConsumedTokens();
                for (TimedToken token : ((TAPNNetworkTimedTransitionStep) step).getConsumedTokens()) {
                    Element tokenElement = document.createElement("token");
                    tokenElement.setAttribute("place", composer.composedPlaceName(token.place()));
                    tokenElement.setAttribute("age", token.age().toString());
                    transitionElement.appendChild(tokenElement);
                }
            }

            if (step instanceof TAPNNetworkTimeDelayStep) {
                BigDecimal delay = ((TAPNNetworkTimeDelayStep) step).getDelay();
                Element delayElement = document.createElement("delay"); // Create delay
                traceRootNode.appendChild(delayElement);
                delayElement.setTextContent(delay.toString());
            }

        }

        if (CreateGui.getAnimator().getTrace() != null && CreateGui.getAnimator().getTrace().getTraceType() == EG_DELAY_FOREVER) {
            Element delayForeverElement = document.createElement("delay");
            traceRootNode.appendChild(delayForeverElement);
            delayForeverElement.setTextContent("forever");
        }

        document.normalize();
        // Create Transformer with XSL Source File
        transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(document);

        StreamResult result = new StreamResult(os);
        transformer.transform(source, result);

        return os;
    }

    public static void importTrace() {
        if (pipe.gui.CreateGui.getCurrentTab().getAnimationHistorySidePanel().getListModel().size() > 1) {
            int answer = JOptionPane.showConfirmDialog(CreateGui.getApp(),
                    "You are about to import a trace. This removes the current trace.",
                    "Import Trace", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return;
            }
        }

        FileBrowser fb = FileBrowser.constructor ("Import Trace", "trc");
        File f = fb.openFile();

        if (f == null) {
            return;
        }

        CreateGui.getAnimator().reset(true);

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));

            TAPNComposer composer = new TAPNComposer(new pipe.gui.MessengerImpl(), CreateGui.getCurrentTab().getGuiModels(), false, true);
            Tuple<TimedArcPetriNet, NameMapping> model = composer.transformModel(CreateGui.getCurrentTab().network());
            VerifyTAPNTraceParser traceParser = new VerifyTAPNTraceParser(model.value1());
            TimedArcPetriNetTrace traceComposed = traceParser.parseTrace(br);
            TAPNTraceDecomposer decomposer = new TAPNTraceDecomposer(traceComposed, CreateGui.getCurrentTab().network(), model.value2());

            CreateGui.getAnimator().setTrace(decomposer.decompose());

        } catch (FileNotFoundException e) {
            // Will never happen
        } catch (Exception e) { //IOException
            CreateGui.getAnimator().reset(true);
            JOptionPane.showMessageDialog(CreateGui.getApp(), "Error importing trace. Does the trace belong to this model?", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }
}
