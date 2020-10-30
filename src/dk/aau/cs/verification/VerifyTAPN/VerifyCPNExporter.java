package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.model.tapn.*;

import java.io.PrintStream;

public class VerifyCPNExporter extends VerifyTACPNExporter{
    @Override
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
    @Override
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
        modelStream.append(colorInformationToXMLString(inhibArc.getArcExpression()));
        modelStream.append("</inhibitorArc>\n");
    }
}
