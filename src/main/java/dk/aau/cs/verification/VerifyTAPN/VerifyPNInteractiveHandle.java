package dk.aau.cs.verification.VerifyTAPN;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.TAPNComposer;
import net.tapaal.gui.petrinet.verification.Verifier;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;

public class VerifyPNInteractiveHandle {
    private Process verifypnProcess;
    private BufferedWriter writer;
    private BufferedReader reader;
    private BufferedReader errorReader;

    private TimedArcPetriNetNetwork network;
    private TAPNComposer composer;

    public VerifyPNInteractiveHandle(TimedArcPetriNetNetwork network, TAPNComposer composer) {
        this.network = network;
        this.composer = composer;
    }

    public boolean startInteractiveMode(String modelPath) {
        try {
            VerifyPN verifyPn = Verifier.getVerifyPN();
            List<String> initCommand = List.of(verifyPn.getPath(), modelPath, "-C", "--interactive-mode");
    
            ProcessBuilder pb = new ProcessBuilder(initCommand);
            verifypnProcess = pb.start();
    
            System.out.println("Started VerifyPN process with command: " + String.join(" ", initCommand));
            Thread.sleep(100);
            if (!verifypnProcess.isAlive()) {
                return false;
            }

            writer = new BufferedWriter(new OutputStreamWriter(verifypnProcess.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(verifypnProcess.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(verifypnProcess.getErrorStream()));
            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<TimedTransition, List<Map<Variable, Color>>> sendMarking(NetworkMarking marking) {
        try {
            String xmlResponse = sendMessage(marking.toXmlStr(composer), "valid-bindings");
            return parseTransitionWithBindings(xmlResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public NetworkMarking sendTransition(TimedTransition transition, Tuple<Variable, Color> binding) {
        try {
            String xmlResponse = sendMessage(transition.toBindingXmlStr(binding, composer), "marking");
            return parseMarking(xmlResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String sendMessage(String message, String responseTag) throws IOException {
        List<String> xmlResponses = sendMessage(message, List.of(responseTag));
        return xmlResponses.isEmpty() ? "" : xmlResponses.get(0);
    }

    private List<String> sendMessage(String message, List<String> responseTags) throws IOException {
        writer.write(message);
        final int numNewlines = 3;
        for (int i = 0; i < numNewlines; ++i) {
            writer.newLine();
        }

        writer.flush();

        List<String> responses = new ArrayList<>();
        StringBuilder currentResponse = null;
        String currentTag = null;

        String line;
        while ((line = reader.readLine()) != null && responses.size() < responseTags.size()) {
            String trimmedLine = line.trim();
            for (String tag : responseTags) {
                if (trimmedLine.equals("<" + tag + ">")) {
                    currentTag = tag;
                    currentResponse = new StringBuilder();
                    currentResponse.append(line).append("\n");
                    break;
                }
                
                if (trimmedLine.equals("</" + tag + ">") && currentTag != null && currentTag.equals(tag)) {
                    currentResponse.append(line).append("\n");
                    responses.add(currentResponse.toString().trim());
                    currentResponse = null;
                    currentTag = null;
                    break;
                }
            }
            
            if (currentResponse != null && !trimmedLine.equals("<" + currentTag + ">")) {
                currentResponse.append(line).append("\n");
            }
        }

        return responses;
    }

    private NetworkMarking parseMarking(String xmlResponse) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlResponse)));
        Element markingElement = (Element)document.getElementsByTagName("marking").item(0);
        return VerifyTAPNMarkingParser.parseComposedMarking(network, markingElement, composer);
    }

    private Map<TimedTransition, List<Map<Variable, Color>>> parseTransitionWithBindings(String xmlResponse) throws Exception {
        Map<TimedTransition, List<Map<Variable, Color>>> transitionBindings = new LinkedHashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlResponse)));
        
        NodeList transitionNodes = document.getElementsByTagName("transition");
        for (int i = 0; i < transitionNodes.getLength(); ++i) {
            Element transitionElement = (Element)transitionNodes.item(i);
            String transitionId = transitionElement.getAttribute("id");
            TimedTransition transition = composer.getTransitionByComposedName(transitionId);

            if (transition == null) {
                throw new IllegalArgumentException("Transition with ID " + transitionId + " not found in composer.");
            }

            List<Map<Variable, Color>> bindings = new ArrayList<>();
            NodeList bindingNodes = transitionElement.getElementsByTagName("binding");
            for (int j = 0; j < bindingNodes.getLength(); ++j) {
                Element bindingElement = (Element)bindingNodes.item(j);
                Map<Variable, Color> bindingMap = new LinkedHashMap<>();

                NodeList variableNodes = bindingElement.getElementsByTagName("variable");
                for (int k = 0; k < variableNodes.getLength(); ++k) {
                    Element variableElement = (Element)variableNodes.item(k);
                    String variableId = variableElement.getAttribute("id");
                    
                    Element colorElement = (Element)variableElement.getElementsByTagName("color").item(0);
                    String colorName = colorElement.getTextContent();

                    Variable variable = network.getVariableByName(variableId);
                    Color color = network.getColorByName(colorName);

                    if (variable == null || color == null) {
                        throw new IllegalArgumentException("Variable or color not found for ID: " + variableId + " or " + colorName);
                    }

                    bindingMap.put(variable, color);
                }

                if (!bindingMap.isEmpty()) {
                    bindings.add(bindingMap);
                }
            }

            if (!bindings.isEmpty()) {
                transitionBindings.put(transition, bindings);
            }
        }

        return transitionBindings;
    }

    public void stopInteractiveMode() {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (errorReader != null) errorReader.close();
            if (verifypnProcess != null) verifypnProcess.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}