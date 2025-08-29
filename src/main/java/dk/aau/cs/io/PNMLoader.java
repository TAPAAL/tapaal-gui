package dk.aau.cs.io;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.tapaal.gui.petrinet.TAPNLens;
import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Expressions.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.tapaal.gui.petrinet.NameGenerator;
import dk.aau.cs.model.tapn.IntWeight;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.FormatException;
import dk.aau.cs.util.Require;
import pipe.gui.petrinet.dataLayer.DataLayer;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import net.tapaal.gui.petrinet.Template;
import pipe.gui.Constants;
import pipe.gui.TAPAALGUI;
import pipe.gui.canvas.Zoomer;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.PlaceTransitionObject;
import pipe.gui.petrinet.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;

public class PNMLoader {
    private TAPNLens lens;

    enum GraphicsType { Position, Offset }

    private final NameGenerator nameGenerator = new NameGenerator();
    private final IdResolver idResolver = new IdResolver();
    private final HashSet<String> arcs = new HashSet<String>();
    private final HashMap<String, TimedPlace> places = new HashMap<String, TimedPlace>();
    private final HashMap<String, TimedTransition> transitions = new HashMap<String, TimedTransition>();
    private final HashMap<String, ColorType> colortypes = new HashMap<String, ColorType>();
    private final HashMap<String, Variable> variables = new HashMap<String, Variable>();

    //If the net is too big, do not make the graphics
    private int netSize = 0;
    private final int maxNetSize = 4000;
    private boolean hasPositionalInfo = false;
    private final LoadTACPN loadTACPN;

    public PNMLoader() {
        this.lens = TAPNLens.Default;
        loadTACPN = new LoadTACPN();
    }

    public LoadedModel load(File file) throws FormatException{
        try {
            return load(new FileInputStream(file));
        } catch (FileNotFoundException e){
            return null;
        } catch (NullPointerException e){
            if (!isNetDrawable()) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(TAPAALGUI.getApp(), "The net is too large to be loaded and drawn in the GUI", "Error", JOptionPane.ERROR_MESSAGE);
                });
                return null;
            }

            throw new FormatException("the PNML file cannot be parsed due to syntax errors", e);
        }
    }

    public LoadedModel load(InputStream file) throws FormatException{
        Document doc = loadDocument(file);

        return parse(doc);
    }

    private Document loadDocument(InputStream file) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return builder.parse(file);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return null;
        }
    }

    private LoadedModel parse(Document doc) throws FormatException {
        idResolver.clear();

        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();

        //We assume there is only one net per file (this is what we call a TAPN Network)
        Node pnmlElement = doc.getElementsByTagName("pnml").item(0);
        Node netNode = getFirstDirectChild(pnmlElement, "net");

        lens = new TAPNLens(false, false, getFirstDirectChild(netNode, "declaration") != null, false);

        String name = getTAPNName(netNode);

        TimedArcPetriNet tapn = new TimedArcPetriNet(name);
        tapn.setCheckNames(false);
        network.add(tapn);
        nameGenerator.add(tapn);

        //We assume there is only one page pr. file (this is what we call a net)
        Template template = new Template(tapn, new DataLayer(), new Zoomer());

        parseTimedArcPetriNet(netNode, tapn, template, network);
        template.setHasPositionalInfo(hasPositionalInfo);

        network.setPaintNet(isNetDrawable());
        tapn.setCheckNames(true);

        return new LoadedModel(network, List.of(template), new ArrayList<TAPNQuery>(), new ArrayList<>(), lens);
    }

    private String getTAPNName(Node netNode) {
        if(!(netNode instanceof Element)){
            return nameGenerator.getNewTemplateName();
        }
        String result = null;

        Node name =  getFirstDirectChild(netNode, "name");
        if(name != null){
            result = getFirstDirectChild(name, "text").getTextContent();
        }

        if(name == null || name.equals("")){
            return nameGenerator.getNewTemplateName();
        }

        return NamePurifier.purify(result);
    }

    private void parseTimedArcPetriNet(Node netNode, TimedArcPetriNet tapn, Template template, TimedArcPetriNetNetwork network) throws FormatException {
        if (lens.isColored()) {
            Node declarations = getFirstDirectChild(netNode, "declaration");
            loadTACPN.parseDeclarations(declarations, network);
        }

        //We assume there is only one page pr. file (this is what we call a net)
        Node node = getFirstDirectChild(netNode, "page").getFirstChild();
        Node first = node;

        //Calculate netsize
        while(node != null){
            netSize += 1;
            node = node.getNextSibling();
        }

        node = first;
        //We parse the places and transitions first
        while(node != null){
            String tag = node.getNodeName();
            if(tag.equals("place")){
                parsePlace(node, tapn, template);
            } else if(tag.equals("transition")){
                parseTransition(node, tapn, template);
            }
            node = node.getNextSibling();
        }
        //We parse the transitions last, as we need the places and transitions it refers to
        node = first;
        while(node != null){
            String tag = node.getNodeName();
            if(tag.equals("arc")){
                parseArc(node, template);
            }
            node = node.getNextSibling();
        }
    }
    private void parsePlace(Node node, TimedArcPetriNet tapn, Template template) throws FormatException {
        if (!(node instanceof Element)) {
            return;
        }

        Name name = parseName(getFirstDirectChild(node, "name"));
        if (name == null) {
            name = new Name(nameGenerator.getNewPlaceName(template.model()));
        }
        Point position = parseGraphics(getFirstDirectChild(node, "graphics"), GraphicsType.Position);
        String id = NamePurifier.purify(((Element) node).getAttribute("id"));
        ArcExpression colorMarking = null;
        TimedPlace place;
        InitialMarking marking = parseMarking(getFirstDirectChild(node, "initialMarking"));
        ColorType colorType = ColorType.COLORTYPE_DOT;
        Node typeNode = getFirstDirectChild(node, "type");
        if (typeNode != null) {
            try {
                colorType = loadTACPN.parseUserSort(typeNode);
            } catch (FormatException e) {
                e.printStackTrace();
            }
        }
        Node markingNode = getFirstDirectChild(node, "hlinitialMarking");
        if (markingNode instanceof Element) {
            try {
                colorMarking = loadTACPN.parseArcExpression(((Element) markingNode).getElementsByTagName("structure").item(0));
            } catch (FormatException e) {
                e.printStackTrace();
            }
        }
        place = new LocalTimedPlace(id, colorType);

        Require.that(places.put(id, place) == null && !transitions.containsKey(id),
            "The name: " + id + ", was already used");
        tapn.add(place);

        if (isNetDrawable()) {
            //We parse the id as both the name and id as in tapaal name = id, and name/id has to be unique
            TimedPlaceComponent placeComponent;
            placeComponent = new TimedPlaceComponent(position.x, position.y, id, name.point.x, name.point.y, lens);
            placeComponent.setUnderlyingPlace(place);
            template.guiModel().addPetriNetObject(placeComponent);
        }

        idResolver.add(tapn.name(), id, id);


        if (colorMarking != null) {
            ExpressionContext context = new ExpressionContext(new HashMap<String, Color>(), loadTACPN.getColortypes());
            ColorMultiset cm = colorMarking.eval(context);
            place.setTokenExpression(loadTACPN.constructCleanAddExpression(colorMarking));
            for (TimedToken ct : cm.getTokens(place)) {
                tapn.parentNetwork().marking().add(ct);
            }

        } else {
            for (int i = 0; i < marking.marking; i++) {
                tapn.parentNetwork().marking().add(new TimedToken(place, ColorType.COLORTYPE_DOT.getFirstColor()));
            }
            if (marking.marking > 1) {
                Vector<ColorExpression> v = new Vector<>();
                v.add(new DotConstantExpression());
                Vector<ArcExpression> numbOfExpression = new Vector<>();
                numbOfExpression.add(new NumberOfExpression(marking.marking, v));
                place.setTokenExpression(new AddExpression(numbOfExpression));
            }
        }
    }


    private static Node skipWS(Node node) {
        if (node != null && !(node instanceof Element)) {
            return skipWS(node.getNextSibling());
        } else {
            return node;
        }
    }

    private static Node getAttribute(Node node, String attribute) {
        return node.getAttributes().getNamedItem(attribute);
    }


    private InitialMarking parseMarking(Node node) {
        if(!(node instanceof Element)){
            return new InitialMarking();
        }

        Point offset = parseGraphics(getFirstDirectChild(node, "graphics"), GraphicsType.Offset);

        int marking = Integer.parseInt(getFirstDirectChild(node, "text").getTextContent());

        return new InitialMarking(marking, offset);
    }

    private void parseTransition(Node node, TimedArcPetriNet tapn, Template template) throws FormatException {
        if(!(node instanceof Element)){
            return;
        }

        Point position = parseGraphics(getFirstDirectChild(node, "graphics"), GraphicsType.Position);
        Name name = parseName(getFirstDirectChild(node, "name"));
        if(name == null){
            name = new Name(nameGenerator.getNewTransitionName(template.model()));
        }
        String id = NamePurifier.purify(((Element) node).getAttribute("id"));

        GuardExpression guardExpression = null;
        Node conditionNode = getFirstDirectChild(node, "condition");
        if (conditionNode != null) {
            guardExpression = loadTACPN.parseGuardExpression(getFirstDirectChild(conditionNode, "structure"));
        }

        TimedTransition transition = new TimedTransition(id, guardExpression);
        Require.that(transitions.put(id, transition) == null && !places.containsKey(id),
            "The id: " + id + ", was already used");
        tapn.add(transition);

        if(isNetDrawable()){
            TimedTransitionComponent transitionComponent =
                //We parse the id as both the name and id as in tapaal name = id, and name/id has to be unique
                new TimedTransitionComponent(position.x, position.y, id, name.point.x, name.point.y, 0, lens);
            transitionComponent.setUnderlyingTransition(transition);
            template.guiModel().addPetriNetObject(transitionComponent);
        }
        idResolver.add(tapn.name(), id, id);
    }

    private void parseArc(Node node, Template template) throws FormatException {
        if(!(node instanceof Element)){
            return;
        }

        Element element = (Element) node;

        String id = element.getAttribute("id");
        String sourceId = NamePurifier.purify(element.getAttribute("source"));
        String targetId = NamePurifier.purify(element.getAttribute("target"));
        String type = element.getAttribute("type");

        String sourceName = idResolver.get(template.model().name(), sourceId);
        String targetName = idResolver.get(template.model().name(), targetId);

        TimedPlace sourcePlace = places.get(sourceName);
        TimedPlace targetPlace = places.get(targetName);

        TimedTransition sourceTransition = transitions.get(sourceName);
        TimedTransition targetTransition = transitions.get(targetName);

        PlaceTransitionObject source = template.guiModel().getPlaceTransitionObject(sourceName);
        PlaceTransitionObject target = template.guiModel().getPlaceTransitionObject(targetName);

        //Inscription
        int weight = 1;
        Node inscription  = getFirstDirectChild(node, "inscription");
        if(inscription != null){
            Node text = getFirstDirectChild(inscription, "text");
            if(text != null){
                String weightString = text.getTextContent().trim();
                try {
                    weight = Integer.parseInt(weightString);
                } catch (NumberFormatException ignored) {} //Default values is 1
            }
        }

        int _startx = 0, _starty = 0, _endx = 0, _endy = 0;

        if(isNetDrawable()){
            // add the insets and offset
            _startx = source.getX() + source.centreOffsetLeft();
            _starty = source.getY() + source.centreOffsetTop();

            _endx = target.getX() + target.centreOffsetLeft();
            _endy = target.getY() + target.centreOffsetTop();
        }

        Arc tempArc;
        ArcExpression arcExpression = null;
        Node hlInscriptionNode = getFirstDirectChild(node, "hlinscription");
        if (hlInscriptionNode != null) {
            arcExpression = loadTACPN.parseArcExpression(getFirstDirectChild(hlInscriptionNode, "structure"));
        }

        if(type != null && type.equals("inhibitor")) {
            tempArc = parseAndAddTimedInhibitorArc(id, sourcePlace, targetTransition, source, target, weight, arcExpression, _endx, _endy, template);
        } else if(sourcePlace != null && targetTransition != null) {
            tempArc = parseInputArc(id, sourcePlace, targetTransition, source, target, weight, arcExpression, _endx, _endy, template);
        } else if(sourceTransition != null && targetPlace != null) {
            tempArc = parseOutputArc(id,  sourceTransition, targetPlace, source, target, weight, arcExpression, _endx, _endy, template);
        } else {
            throw new FormatException("Arcs must be only between places and transitions");
        }

        if(isNetDrawable()) parseArcPath(element, tempArc);
    }

    private void parseArcPath(Element arc, Arc tempArc) {
        Element element = (Element) getFirstDirectChild(arc, "graphics");
        if(element == null) return;
        NodeList nodelist = element.getElementsByTagName("position");
        if (nodelist.getLength() > 0) {
            for (int i = 0; i < nodelist.getLength(); i++) {
                Node node = nodelist.item(i);
                if (node instanceof Element) {
                    Element position = (Element) node;
                    if ("position".equals(position.getNodeName())) {
                        String arcTempX = position.getAttribute("x");
                        String arcTempY = position.getAttribute("y");

                        double arcPointX = Double.parseDouble(arcTempX);
                        double arcPointY = Double.parseDouble(arcTempY);
                        arcPointX += Constants.ARC_CONTROL_POINT_CONSTANT + 1;
                        arcPointY += Constants.ARC_CONTROL_POINT_CONSTANT + 1;

                        //We add the point at i+1 as the starting and end points of
                        //the arc is already in the path as point number 0 and 1
                        tempArc.getArcPath().addPoint(i+1,arcPointX, arcPointY, false);
                    }
                }
            }
        }
    }

    private Name parseName(Node node){
        if(!(node instanceof Element)){
            return null;
        }
        Point offset = parseGraphics(getFirstDirectChild(node, "graphics"), GraphicsType.Offset);

        String name = getFirstDirectChild(node, "text").getTextContent();
        if(name == null || name.equals("")){
            return null;
        }

        name = NamePurifier.purify(name);
        return new Name(name, offset);
    }

    private Point parseGraphics(Node node, GraphicsType type){
        if(!(node instanceof Element)){
            if(type == GraphicsType.Offset)
                return new Point(0, -10);
            else
                return new Point(100, 100);
        }

        hasPositionalInfo = true;
        Element offset = (Element)getFirstDirectChild(node, type == GraphicsType.Offset ? "offset" : "position");

        String x = offset.getAttribute("x");
        String y = offset.getAttribute("y");

        int xd = Math.round(Float.parseFloat(x));
        int yd = Math.round(Float.parseFloat(y));

        return new Point(xd, yd);
    }

    private static class Name{
        final String name;
        final Point point;

        public Name(String newPlaceName) {
            this(newPlaceName, new Point());
        }

        public Name(String name, Point p) {
            this.name = name;
            this.point = p;
        }

        @Override
        public String toString() {
            return name + ";" + point;
        }
    }

    private static class InitialMarking{
        final int marking;
        final Point point;

        public InitialMarking() {
            this(0, new Point());
        }


        public InitialMarking(int marking, Point p) {
            this.marking = marking;
            this.point = p;
        }

        @Override
        public String toString() {
            return marking + ";" + point;
        }
    }

    private TimedInputArcComponent parseInputArc(String arcId, TimedPlace place, TimedTransition transition, PlaceTransitionObject source,
                                                 PlaceTransitionObject target, int weight, ArcExpression arcExpression, int _endx,
                                                 int _endy, Template template) throws FormatException {
        TimedInputArc inputArc = new TimedInputArc(place, transition, TimeInterval.ZERO_INF, new IntWeight(weight), arcExpression);

        Require.that(places.containsKey(inputArc.source().name()),	"The source place must be part of the petri net.");
        Require.that(transitions.containsKey(inputArc.destination().name()), "The destination transition must be part of the petri net");
        if(!arcs.add(inputArc.source().name() + "-in-" + inputArc.destination().name())) {
            throw new FormatException("Multiple arcs between a place and a transition is not allowed");
        }

        TimedInputArcComponent arc = null;

        if(isNetDrawable()){
            arc = new TimedInputArcComponent(new TimedOutputArcComponent(source, target, weight, arcId), lens);
            arc.setUnderlyingArc(inputArc);

            template.guiModel().addPetriNetObject(arc);
        }

        template.model().add(inputArc);

        return arc;


    }

    private Arc parseOutputArc(String arcId, TimedTransition transition, TimedPlace place, PlaceTransitionObject source,
                               PlaceTransitionObject target, int weight, ArcExpression arcExpression, int _endx,
                               int _endy, Template template) throws FormatException {
        TimedOutputArc outputArc = new TimedOutputArc(transition, place, new IntWeight(weight),arcExpression);

        Require.that(places.containsKey(outputArc.destination().name()), "The destination place must be part of the petri net.");
        Require.that(transitions.containsKey(outputArc.source().name()), "The source transition must be part of the petri net");
        if(!arcs.add(outputArc.source().name() + "-out-" + outputArc.destination().name())) {
            throw new FormatException("Multiple arcs between a place and a transition is not allowed");
        }

        TimedOutputArcComponent arc = null;

        if(isNetDrawable()){
            arc = new TimedOutputArcComponent(
                source, target, weight,	arcId);
            arc.setUnderlyingArc(outputArc);

            template.guiModel().addPetriNetObject(arc);

        }

        template.model().add(outputArc);
        return arc;
    }

    private Arc parseAndAddTimedInhibitorArc(String arcId, TimedPlace place, TimedTransition transition, PlaceTransitionObject source,
                                             PlaceTransitionObject target, int weight, ArcExpression arcExpression, int _endx,
                                             int _endy, Template template) {
        TimedInhibitorArcComponent tempArc = new TimedInhibitorArcComponent(
            new TimedInputArcComponent(
                new TimedOutputArcComponent(source, target, weight, arcId)
            ));

        TimedInhibitorArc inhibArc = new TimedInhibitorArc(place, transition, TimeInterval.ZERO_INF, new IntWeight(weight), arcExpression);

        tempArc.setUnderlyingArc(inhibArc);
        template.guiModel().addPetriNetObject(tempArc);
        template.model().add(inhibArc);

        return tempArc;
    }

    private boolean isNetDrawable() {
        return netSize <= maxNetSize;
    }

    Node getFirstDirectChild(Node parent, String tagName){
        NodeList children = parent.getChildNodes();
        for(int i = 0; i < children.getLength(); i++){
            if(children.item(i).getNodeName().equals(tagName)){
                return children.item(i);
            }
        }
        return null;
    }
}
