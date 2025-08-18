package dk.aau.cs.verification.VerifyTAPN;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.debug.Logger;
import net.tapaal.gui.petrinet.verification.Verifier;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import com.sun.jna.Platform;

public class VerifyPNInteractiveHandle {
    private Process verifypnProcess;
    private BufferedWriter writer;
    private BufferedReader reader;
    private BufferedReader errorReader;

    private TimedArcPetriNetNetwork network;
    private TAPNComposer composer;
    private NameMapping nameMapping;

    private boolean isShutdownHookRegistered;

    public VerifyPNInteractiveHandle(TimedArcPetriNetNetwork network, TAPNComposer composer, NameMapping nameMapping) {
        this.network = network;
        this.composer = composer;
        this.nameMapping = nameMapping;
    }

    public boolean startInteractiveMode(String modelPath) {
        try {
            VerifyPN verifyPn = Verifier.getVerifyPN();

            if (Platform.isWindows()) {
                modelPath = '"' + modelPath + '"';
            }

            verifyPn.setup(); // Ensure the path is set up correctly

            List<String> initCommand = List.of(verifyPn.getPath(), modelPath, "-C", "--interactive-mode");
    
            ProcessBuilder pb = new ProcessBuilder(initCommand);
            verifypnProcess = pb.start();
    
            Logger.log("Running: " + String.join(" ", initCommand));

            Thread.sleep(100);
            if (!verifypnProcess.isAlive()) {
                return false;
            }

            writer = new BufferedWriter(new OutputStreamWriter(verifypnProcess.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(verifypnProcess.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(verifypnProcess.getErrorStream()));

            registerShutdownHook();

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

    public NetworkMarking sendTransition(TimedTransition transition, Map<Variable, Color> bindings) {
        try {
            String xmlResponse = sendMessage(transition.toBindingXmlStr(bindings, composer), "marking");
            return parseMarking(xmlResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String sendMessage(String message, String responseTag) throws Exception {
        // Clear leftover lines in the reader
        while (reader.ready()) reader.readLine();

        writer.write(message);
        final int numNewlines = 3;
        for (int i = 0; i < numNewlines; ++i) {
            writer.newLine();
        }

        writer.flush();

        StringBuilder response = new StringBuilder();
        boolean insideTag = false;

        String line;
        while ((line = reader.readLine()) != null) {
            String trimmedLine = line.trim();
            if (trimmedLine.equals("<" + responseTag + ">")) {
                insideTag = true;
                response.append(line).append("\n");
            } else if (trimmedLine.equals("</" + responseTag + ">")) {
                response.append(line).append("\n");
                break;
            } else if (insideTag) {
                response.append(line).append("\n");
            }
        }

        return response.toString().trim();
    }

    private NetworkMarking parseMarking(String xmlResponse) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlResponse)));
        Element markingElement = (Element)document.getElementsByTagName("marking").item(0);
        return VerifyTAPNMarkingParser.parseComposedMarking(network, markingElement, nameMapping);
    }

    private Map<TimedTransition, List<Map<Variable, Color>>> parseTransitionWithBindings(String xmlResponse) throws Exception {
        Map<TimedTransition, List<Map<Variable, Color>>> transitionBindings = new HashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlResponse)));
        
        NodeList transitionNodes = document.getElementsByTagName("transition");
        for (int i = 0; i < transitionNodes.getLength(); ++i) {
            Element transitionElement = (Element)transitionNodes.item(i);
            String transitionId = transitionElement.getAttribute("id");

            Tuple<String, String> originalName = nameMapping.map(transitionId);
            TimedTransition transition; 
            if (!originalName.value1().isEmpty()) {
                transition = network.getTAPNByName(originalName.value1()).getTransitionByName(originalName.value2());
            } else {
                transition = network.getSharedTransitionByName(originalName.value2()).transitions().iterator().next();
            }                                

            if (transition == null) {
                throw new IllegalArgumentException("Transition with ID " + transitionId + " not found in composer.");
            }

            List<Map<Variable, Color>> validBindings = new ArrayList<>();
            NodeList bindingNodes = transitionElement.getElementsByTagName("binding");
            for (int j = 0; j < bindingNodes.getLength(); ++j) {
                Element bindingElement = (Element)bindingNodes.item(j);
                Map<Variable, Color> singleBinding = new HashMap<>();
                
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

                    singleBinding.put(variable, color);
                }
                
                if (!singleBinding.isEmpty()) {
                    validBindings.add(singleBinding);
                }
            }
            
            transitionBindings.put(transition, validBindings);
        }

        return transitionBindings;
    }

    private void registerShutdownHook() {
        if (!isShutdownHookRegistered) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                stopInteractiveMode();
            }));

            isShutdownHookRegistered = true;
        }
    }

    public void stopInteractiveMode() {
        try {
            if (writer != null) {
                writer.close();
                writer = null;
            }

            if (reader != null) {
                reader.close();
                reader = null;
            }

            if (errorReader != null) {
                errorReader.close();
                errorReader = null;
            }

            if (verifypnProcess != null) {
                verifypnProcess.destroy();

                try {
                    verifypnProcess.waitFor(3, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (verifypnProcess.isAlive()) {
                    verifypnProcess.destroyForcibly();
                }

                verifypnProcess = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}