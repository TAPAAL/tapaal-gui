package dk.aau.cs.verification.VerifyTAPN;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import net.tapaal.gui.petrinet.verification.Verifier;

public class InteractiveHandle {
    private Process engineProcess;
    private BufferedWriter writer;
    private BufferedReader reader;
    private BufferedReader errorReader;

    private TimedArcPetriNetNetwork network;
    private TAPNComposer composer;
    private NameMapping nameMapping;
    private boolean isTimed;

    private boolean isShutdownHookRegistered;

    public InteractiveHandle(TimedArcPetriNetNetwork network, TAPNComposer composer, NameMapping nameMapping) {
        this.network = network;
        this.composer = composer;
        this.nameMapping = nameMapping;
    }

    public boolean startInteractiveMode(String modelPath, boolean isTimed) {
        this.isTimed = isTimed;
        try {
            ModelChecker engine = isTimed ? Verifier.getVerifyDTAPN() : Verifier.getVerifyPN();
            engine.setup();
            List<String> initCommand = isTimed
                ? List.of(engine.getPath(), modelPath, "--interactive-mode")
                : List.of(engine.getPath(), modelPath, "-C", "--interactive-mode");

            var pb = new ProcessBuilder(initCommand);
            engineProcess = pb.start();

            if (shouldLog()) {
                Logger.log("Running: " + String.join(" ", initCommand));
            }

            Thread.sleep(100);
            if (!engineProcess.isAlive()) {
                return false;
            }

            writer = new BufferedWriter(new OutputStreamWriter(engineProcess.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(engineProcess.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(engineProcess.getErrorStream()));
            var errorStream = errorReader;
            var errorDrainer = new Thread(() -> {
                try {
                    String line;
                    while ((line = errorStream.readLine()) != null) {
                        if (shouldLog()) {
                            Logger.log("Interactive engine: " + line);
                        }
                    }
                } catch (IOException ignored) {}
            }, "interactive-engine-stderr");
            errorDrainer.setDaemon(true);
            errorDrainer.start();

            registerShutdownHook();

            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Run with -Ddebug.interactive=true to enable logging
    private boolean shouldLog() {
        return Boolean.getBoolean("debug.interactive");
    }

    public static class BindingsResult {
        public final Map<TimedTransition, List<Map<Variable, Color>>> validBindings;
        public final Map<TimedTransition, List<Map<Variable, Color>>> delayEnabledBindings;
        public final Map<TimedTransition, Map<Map<Variable, Color>, BigDecimal>> bindingDelayMap;

        public BindingsResult(
            Map<TimedTransition, List<Map<Variable, Color>>> validBindings,
            Map<TimedTransition, List<Map<Variable, Color>>> delayEnabledBindings,
            Map<TimedTransition, Map<Map<Variable, Color>, BigDecimal>> bindingDelayMap
        ) {
            this.validBindings = validBindings;
            this.delayEnabledBindings = delayEnabledBindings;
            this.bindingDelayMap = bindingDelayMap;
        }

        public BindingsResult(
            Map<TimedTransition, List<Map<Variable, Color>>> validBindings,
            Map<TimedTransition, List<Map<Variable, Color>>> delayEnabledBindings
        ) {
            this(validBindings, delayEnabledBindings, new HashMap<>());
        }
    }

    private BindingsResult lastBindingsResult = new BindingsResult(new HashMap<>(), new HashMap<>());

    public BindingsResult getLastBindingsResult() {
        return lastBindingsResult;
    }

    public BindingsResult sendMarking(NetworkMarking marking) {
        try {
            var xmlResponse = sendMessage(marking.toXmlStr(composer), "valid-bindings");
            lastBindingsResult = parseBindingsResult(xmlResponse);
            return lastBindingsResult;
        } catch (Exception e) {
            e.printStackTrace();
            return new BindingsResult(new HashMap<>(), new HashMap<>());
        }
    }

    public NetworkMarking sendDelay(BigDecimal delay) {
        try {
            var amount = delay.intValue();
            var msg = "<delay value=\"" + amount + "\"/>";
            var xmlResponse = sendMessage(msg, "valid-bindings");
            lastBindingsResult = parseBindingsResult(xmlResponse);
            return parseMarking(xmlResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public NetworkMarking sendTransition(TimedTransition transition, Map<Variable, Color> bindings) {
        try {
            var expectedTag = isTimed ? "valid-bindings" : "marking";
            var xmlResponse = sendMessage(transition.toBindingXmlStr(bindings, composer), expectedTag);
            if (isTimed) {
                lastBindingsResult = parseBindingsResult(xmlResponse);
            }

            return parseMarking(xmlResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String sendMessage(String message, String responseTag) throws Exception {
        if (shouldLog()) {
            Logger.log("--- Sending to Engine ---");
            Logger.log(message);
            Logger.log("-------------------------");
        }

        // Clear leftover lines in the reader
        while (reader.ready()) {
            reader.readLine();
        }

        writer.write(message);
        final int numNewlines = 3;
        for (int i = 0; i < numNewlines; ++i) {
            writer.newLine();
        }

        writer.flush();

        var response = new StringBuilder();
        boolean insideTag = false;
        boolean foundEndTag = false;

        String line;
        while ((line = reader.readLine()) != null) {
            var trimmedLine = line.trim();
            if (isTimed) {
                response.append(line).append("\n");
                if (trimmedLine.contains("</" + responseTag + ">")) {
                    foundEndTag = true;
                    break;
                }
            } else {
                if (trimmedLine.equals("<" + responseTag + ">")) {
                    insideTag = true;
                    response.append(line).append("\n");
                } else if (trimmedLine.equals("</" + responseTag + ">")) {
                    response.append(line).append("\n");
                    foundEndTag = true;
                    break;
                } else if (insideTag) {
                    response.append(line).append("\n");
                }
            }
        }

        if (!foundEndTag) {
            throw new IOException("Did not receive complete response from engine. Response so far: " + response.toString());
        }

        var result = response.toString().trim();
        if (shouldLog()) {
            Logger.log("--- Received response from Engine ---");
            Logger.log(result);
            Logger.log("-------------------------------------");
        }

        return result;
    }

    private static Document parseXml(String xmlResponse) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        var builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader("<root>" + xmlResponse + "</root>")));
    }

    private NetworkMarking parseMarking(String xmlResponse) throws Exception {
        var document = parseXml(xmlResponse);
        var markingNodes = document.getElementsByTagName("marking");
        if (markingNodes.getLength() == 0) {
            return null;
        }

        var markingElement = (Element)markingNodes.item(0);
        return VerifyTAPNMarkingParser.parseComposedMarking(network, markingElement, nameMapping);
    }

    private BindingsResult parseBindingsResult(String xmlResponse) throws Exception {
        var document = parseXml(xmlResponse);

        Map<TimedTransition, List<Map<Variable, Color>>> validBindings = new HashMap<>();
        Map<TimedTransition, List<Map<Variable, Color>>> delayEnabledBindings = new HashMap<>();
        Map<TimedTransition, Map<Map<Variable, Color>, BigDecimal>> bindingDelayMap = new HashMap<>();

        var validNodes = document.getElementsByTagName("valid-bindings");
        if (validNodes.getLength() > 0) {
            parseBindingsFromNode((Element)validNodes.item(0), bindingDelayMap, validBindings, delayEnabledBindings);
        }

        return new BindingsResult(validBindings, delayEnabledBindings, bindingDelayMap);
    }

    private void parseBindingsFromNode(
        Element parentElement,
        Map<TimedTransition, Map<Map<Variable, Color>, BigDecimal>> bindingDelayMap,
        Map<TimedTransition, List<Map<Variable, Color>>> validBindingsByTransition,
        Map<TimedTransition, List<Map<Variable, Color>>> delayBindingsByTransition
    ) {
        if (parentElement == null) {
            return;
        }

        var transitionNodes = parentElement.getElementsByTagName("transition");
        for (int i = 0; i < transitionNodes.getLength(); ++i) {
            var transitionElement = (Element)transitionNodes.item(i);
            var transitionId = transitionElement.getAttribute("id");

            TimedTransition transition;
            if (nameMapping != null) {
                var originalName = nameMapping.map(transitionId);
                transition = (originalName.value1() == null || originalName.value1().isEmpty())
                    ? network.getSharedTransitionByName(originalName.value2()).transitions().iterator().next()
                    : network.getTAPNByName(originalName.value1()).getTransitionByName(originalName.value2());
            } else {
                transition = null;
                for (var template : network.allTemplates()) {
                    transition = template.getTransitionByName(transitionId);
                    if (transition != null) {
                        break;
                    }
                }
            }

            List<Map<Variable, Color>> validBindings = new ArrayList<>();
            List<Map<Variable, Color>> delayBindings = new ArrayList<>();
            var bindingNodes = transitionElement.getElementsByTagName("binding");
            for (int j = 0; j < bindingNodes.getLength(); ++j) {
                var bindingElement = (Element)bindingNodes.item(j);
                var singleBinding = new HashMap<Variable, Color>();

                var variableNodes = bindingElement.getElementsByTagName("variable");
                for (int k = 0; k < variableNodes.getLength(); ++k) {
                    var variableElement = (Element)variableNodes.item(k);
                    var variableId = variableElement.getAttribute("id");

                    var colorNodes = variableElement.getElementsByTagName("color");
                    var colorElement = (Element)colorNodes.item(0);
                    var colorName = colorElement.getTextContent();

                    var variable = network.getVariableById(variableId);
                    var color = network.getColorByName(colorName);

                    singleBinding.put(variable, color);
                }

                if (bindingDelayMap != null && bindingElement.hasAttribute("min-delay")) {
                    bindingDelayMap
                        .computeIfAbsent(transition, ignored -> new HashMap<>())
                        .put(singleBinding, new BigDecimal(bindingElement.getAttribute("min-delay")));
                    delayBindings.add(singleBinding);
                } else {
                    validBindings.add(singleBinding);
                }
            }

            if (!validBindings.isEmpty()) validBindingsByTransition.put(transition, validBindings);
            if (!delayBindings.isEmpty()) delayBindingsByTransition.put(transition, delayBindings);
        }
    }

    private void registerShutdownHook() {
        if (!isShutdownHookRegistered) {
            Runtime.getRuntime().addShutdownHook(new Thread(this::stopInteractiveMode));
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

            if (engineProcess != null) {
                engineProcess.destroy();

                try {
                    engineProcess.waitFor(3, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (engineProcess.isAlive()) {
                    engineProcess.destroyForcibly();
                }

                engineProcess = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
