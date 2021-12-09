package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.io.writeTACPN;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.verification.NameMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import pipe.dataLayer.DataLayer;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.Transition;

import javax.xml.crypto.Data;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.PrintStream;
import java.util.Collection;

public class VerifyCPNExporter extends VerifyTAPNExporter{
    protected void outputPlace(TimedPlace p, PrintStream modelStream, Collection<DataLayer> guiModels, NameMapping mapping) {
        modelStream.append("<place ");
        modelStream.append("id=\"" + p.name() + "\" ");
        modelStream.append("name=\"" + p.name() + "\" ");
        modelStream.append("initialMarking=\"" + p.numberOfTokens() + "\" ");
        modelStream.append(">\n");
        modelStream.append(colorInformationToXMLString(p));
        modelStream.append("</place>\n");
    }
    protected void outputTransition(TimedTransition t, PrintStream modelStream, Collection<DataLayer> guiModels, NameMapping mapping) {
        modelStream.append("<transition ");
        modelStream.append("player=\"" + (t.isUncontrollable() ? "1" : "0") + "\" ");
        modelStream.append("id=\"" + t.name() + "\" ");
        modelStream.append("name=\"" + t.name() + "\" ");
        modelStream.append(">\n");
        modelStream.append(colorInformationToXMLString(t));
        modelStream.append("</transition>\n");
    }
    @Override
    protected void outputInputArc(TimedInputArc inputArc, PrintStream modelStream) {
        modelStream.append("<inputArc ");
        modelStream.append("source=\"" + inputArc.source().name() + "\" ");
        modelStream.append("target=\"" + inputArc.destination().name() + "\">");
        modelStream.append("<inscription><value>" + inputArc.getWeight().nameForSaving(false) + "</value></inscription>");
        modelStream.append(colorInformationToXMLString(inputArc.getArcExpression()));
        modelStream.append("</inputArc>\n");
    }
    @Override
    protected void outputOutputArc(TimedOutputArc outputArc, PrintStream modelStream) {
        modelStream.append("<outputArc ");
        modelStream.append("source=\"" + outputArc.source().name() + "\" ");
        modelStream.append("target=\"" + outputArc.destination().name() + "\">");
        modelStream.append("<inscription><value>" + outputArc.getWeight().nameForSaving(false) + "</value></inscription>");
        modelStream.append(colorInformationToXMLString(outputArc.getExpression()));
        modelStream.append("</outputArc>\n");
    }
    @Override
    protected void outputInhibitorArc(TimedInhibitorArc inhibArc, PrintStream modelStream) {
        modelStream.append("<inhibitorArc ");
        modelStream.append("source=\"" + inhibArc.source().name() + "\" ");
        modelStream.append("target=\"" + inhibArc.destination().name() + "\">");
        modelStream.append("<inscription><value>" + inhibArc.getWeight().nameForSaving(false) + "</value></inscription>");
        modelStream.append("</inhibitorArc>\n");
    }

    protected void outputDeclarations(PrintStream modelStream) {
        writeTACPN colorWriter = new writeTACPN(model.parentNetwork());
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = builderFactory.newDocumentBuilder();

        } catch (ParserConfigurationException e) {
            System.out.println("Failed to create document builder");
        }
        Document document = builder.newDocument();

        Element placeHolderNode = document.createElement("placeHolder");
        document.appendChild(placeHolderNode);
        colorWriter.appendDeclarations(document, placeHolderNode);
        DOMImplementationLS lsImpl = (DOMImplementationLS)placeHolderNode.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
        LSSerializer serializer = lsImpl.createLSSerializer();
        serializer.getDomConfig().setParameter("xml-declaration", false); //by default its true, so set it to false to get String without xml-declaration
        String str = serializer.writeToString(placeHolderNode);
        str = str.replace("<placeHolder>", "");
        str = str.replace("</placeHolder>", "");
        modelStream.append(str);
    }

    protected String colorInformationToXMLString(Object element){
        writeTACPN colorWriter = new writeTACPN(model.parentNetwork());
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = builderFactory.newDocumentBuilder();

        } catch (ParserConfigurationException e) {
            System.out.println("Failed to create document builder");
        }
        Document document = builder.newDocument();

        Element placeHolderNode = document.createElement("placeHolder");
        document.appendChild(placeHolderNode);
        if(element instanceof TimedPlace){
            colorWriter.appendColoredPlaceDependencies((TimedPlace)element,document, placeHolderNode);
        } else if (element instanceof ArcExpression){
            placeHolderNode.appendChild(colorWriter.createArcExpressionElement(document, (ArcExpression)element));
        } else if (element instanceof TimedTransition){
            colorWriter.appendColoredTransitionDependencies((TimedTransition)element, document, placeHolderNode);
        }
        DOMImplementationLS lsImpl = (DOMImplementationLS)placeHolderNode.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
        LSSerializer serializer = lsImpl.createLSSerializer();
        serializer.getDomConfig().setParameter("xml-declaration", false); //by default its true, so set it to false to get String without xml-declaration
        String str = serializer.writeToString(placeHolderNode);
        str = str.replace("<placeHolder>", "");
        str = str.replace("</placeHolder>", "");


        return str;
    }
}
