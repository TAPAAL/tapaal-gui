package dk.aau.cs.verification.VerifyTAPN;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
    private String currentElement = "";
    private String transitionId = "";
    private String variableId = "";
    private String color = "";
    private Map<String, String> bindings = new HashMap<>();

    public static void addBindings(LoadedModel loadedModel, String output) {
        ColorBindingParser parser = new ColorBindingParser();
        Map<String, String> bindings = parser.parseBindings(output);

        for (Template net : loadedModel.templates()) {
            for (Transition transition : net.guiModel().getTransitions()) {
                transition.setToolTipText(bindings.get(transition.getName()));
            }
        }
    }

    public static void addBindings(DataLayer guiModel, String output) {
        ColorBindingParser parser = new ColorBindingParser();
        Map<String, String> bindings = parser.parseBindings(output);

        for (Transition transition : guiModel.getTransitions()) {
            transition.setToolTipText(bindings.get(transition.getName()));
        }
    }

    private Map<String, String> parseBindings(String output) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();

            int start = output.indexOf("<bindings>");
            int end = output.indexOf("</bindings>") + "</bindings>".length();

            String xml = output.substring(start, end);

            InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            parser.parse(new InputSource(new InputStreamReader(stream, StandardCharsets.UTF_8)), this);
        } catch (Exception e) {
            e.printStackTrace();
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
        if (qName.equalsIgnoreCase("transition")) {
            if (!variableId.isEmpty() && !color.isEmpty()) {
                bindings.put(transitionId, variableId + " -> " + color);
            }

            transitionId = "";
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