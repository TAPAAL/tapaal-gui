package dk.aau.cs.verification.VerifyTAPN;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dk.aau.cs.io.LoadedModel;
import net.tapaal.gui.petrinet.Template;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.Transition;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

public class ColorBindingParser extends DefaultHandler {
    private Map<String, List<String>> bindings = new HashMap<>();

    private String currentElement = "";
    private String transitionId = "";
    private String variableId = "";
    private String color = "";

    /**
     * Parses output from the verifier and adds the color bindings to the transitions in the loaded model.
     * @param loadedModel The loaded model to add the bindings to.
     * @param output The output from the verifier.
     */
    public void addBindings(LoadedModel loadedModel, String output) {
        Map<String, List<String>> bindings = parseBindings(output);

        for (Template net : loadedModel.templates()) {
            for (Transition transition : net.guiModel().getTransitions()) {
                if (bindings.containsKey(transition.getName())) {
                    transition.setToolTipText(createTooltip(bindings, transition));
                }
            }
        }
    }

    /**
     * Parses output from the verifier and adds the color bindings to the transitions in the GUI model.
     * @param guiModel The GUI model to add the bindings to.
     * @param output The output from the verifier.
     */
    public void addBindings(DataLayer guiModel, String output) {
        Map<String, List<String>> bindings = parseBindings(output);

        for (Transition transition : guiModel.getTransitions()) {
            if (bindings.containsKey(transition.getName())) {
                transition.setToolTipText(createTooltip(bindings, transition));
            }
        }
    }

    public String createTooltip(Map<String, List<String>> bindings, Transition transition) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html>");

        for (String binding : bindings.get(transition.getName())) {
            sb.append(binding);
            sb.append("<br>");
        }

        sb.append("</html>");

        return sb.toString();
    }

    public Map<String, List<String>> parseBindings(String output) {
        InputStream stream = null;

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();

            int start = output.indexOf("<bindings>");

            // Return empty object if engine does not return any bindings
            if (start == -1) return new HashMap<>();

            int end = output.indexOf("</bindings>") + "</bindings>".length();

            String xml = output.substring(start, end);

            stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            parser.parse(new InputSource(new InputStreamReader(stream, StandardCharsets.UTF_8)), this);

        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        } finally {
            // Close stream
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bindings;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        currentElement = qName;

        switch (currentElement) {
            case "transition":
                transitionId = atts.getValue("id");
                break;
        
            case "variable":
                variableId = atts.getValue("id");
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("color")) {
            String binding = variableId + " -> " + color;
            bindings.computeIfAbsent(transitionId, k -> new ArrayList<>()).add(binding);
        } else if (qName.equalsIgnoreCase("transition")) {
            transitionId = "";
        } else if (qName.equalsIgnoreCase("variable")) {
            variableId = "";
            color = "";
        }
    
        currentElement = "";
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if ("color".equals(currentElement)) {
            color = new String(ch, start, length);
        }
    }
}