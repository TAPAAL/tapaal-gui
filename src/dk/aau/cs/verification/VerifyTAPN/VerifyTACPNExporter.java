package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.io.writeTACPN;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import dk.aau.cs.model.tapn.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.PrintStream;
//TODO: This class needs time implementation
//Firstly the engine needs to be able to handle time
public class VerifyTACPNExporter extends VerifyTAPNExporter {
    protected void outputPlace(TimedPlace p, PrintStream modelStream) {
        modelStream.append("<place ");

        modelStream.append("id=\"" + p.name() + "\" ");
        modelStream.append("name=\"" + p.name() + "\" ");
        modelStream.append("invariant=\"" + p.invariant().toString(false).replace("<", "&lt;") + "\" ");
        modelStream.append("initialMarking=\"" + p.numberOfTokens() + "\" ");

        modelStream.append(">\n");
        modelStream.append(colorInformationToXMLString(p));
        modelStream.append("</place>\n");
    }

    protected void outputTransition(TimedTransition t, PrintStream modelStream) {
        modelStream.append("<transition ");

        modelStream.append("player=\"" + (t.isUncontrollable() ? "1" : "0") + "\" ");
        modelStream.append("id=\"" + t.name() + "\" ");
        modelStream.append("name=\"" + t.name() + "\" ");
        modelStream.append("urgent=\"" + (t.isUrgent()? "true":"false") + "\"");
        modelStream.append(">\n");
        modelStream.append(colorInformationToXMLString(t));
        modelStream.append("</transition>\n");
    }

    protected void outputInputArc(TimedInputArc inputArc, PrintStream modelStream) {
        modelStream.append("<inputArc ");

        modelStream.append("inscription=\"" + inputArc.interval().toString(false).replace("<", "&lt;") + "\" ");
        modelStream.append("source=\"" + inputArc.source().name() + "\" ");
        modelStream.append("target=\"" + inputArc.destination().name() + "\" ");
        if(inputArc.getWeight().value() > 1){
            modelStream.append("weight=\"" + inputArc.getWeight().nameForSaving(false) + "\"");
        }
        modelStream.append(">\n");
        modelStream.append(colorInformationToXMLString(inputArc.getArcExpression()));
        modelStream.append("</inputArc>\n");

        modelStream.append("/>\n");
    }

    protected void outputOutputArc(TimedOutputArc outputArc, PrintStream modelStream) {
        modelStream.append("<outputArc ");

        modelStream.append("inscription=\"1\" " );
        modelStream.append("source=\"" + outputArc.source().name() + "\" ");
        modelStream.append("target=\"" + outputArc.destination().name() + "\" ");
        if(outputArc.getWeight().value() > 1){
            modelStream.append("weight=\"" + outputArc.getWeight().nameForSaving(false) + "\"");
        }
        modelStream.append(">\n");
        modelStream.append(colorInformationToXMLString(outputArc.getExpression()));
        modelStream.append("</outputArc>\n");
        modelStream.append("/>\n");
    }

    protected void outputTransportArc(TransportArc transArc, PrintStream modelStream) {
        modelStream.append("<transportArc ");

        modelStream.append("inscription=\"" + transArc.interval().toString(false).replace("<", "&lt;") + "\" ");
        modelStream.append("source=\"" + transArc.source().name() + "\" ");
        modelStream.append("transition=\"" + transArc.transition().name() + "\" ");
        modelStream.append("target=\"" + transArc.destination().name() + "\" ");
        if(transArc.getWeight().value() > 1){
            modelStream.append("weight=\"" + transArc.getWeight().nameForSaving(false) + "\"");
        }
        modelStream.append("/>\n");
        //TODO: engine cannot handle time yet
		/*modelStream.append(">\n");
		modelStream.append(colorInformationToXMLString(transArc.getInputExpression()));
		modelStream.append(colorInformationToXMLString(transArc.getOutputExpression()));
        modelStream.append("</transportarc>\n");*/
    }

    protected void outputInhibitorArc(TimedInhibitorArc inhibArc,	PrintStream modelStream) {
        modelStream.append("<inhibitorArc ");

        modelStream.append("inscription=\"" + inhibArc.interval().toString(false).replace("<", "&lt;") + "\" ");
        modelStream.append("source=\"" + inhibArc.source().name() + "\" ");
        modelStream.append("target=\"" + inhibArc.destination().name() + "\" ");
        if(inhibArc.getWeight().value() > 1){
            modelStream.append("weight=\"" + inhibArc.getWeight().nameForSaving(false) + "\"");
        }

        modelStream.append(">\n");
        modelStream.append(colorInformationToXMLString(inhibArc.getArcExpression()));
        modelStream.append("</inhibitorarc>\n");
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
