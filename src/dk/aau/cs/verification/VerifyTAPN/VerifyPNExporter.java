package dk.aau.cs.verification.VerifyTAPN;

import java.io.PrintStream;

import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TransportArc;

public class VerifyPNExporter extends VerifyTAPNExporter {
	protected void outputInputArc(TimedInputArc inputArc, PrintStream modelStream) {
		modelStream.append("<inputArc ");
		modelStream.append("source=\"" + inputArc.source().name() + "\" ");
		modelStream.append("target=\"" + inputArc.destination().name() + "\">");
		modelStream.append("<inscription><value>" + inputArc.getWeight().nameForSaving(false) + "</value></inscription>");
		modelStream.append("</inputArc>\n");
	}

	protected void outputOutputArc(TimedOutputArc outputArc, PrintStream modelStream) {
		modelStream.append("<outputArc ");
		modelStream.append("source=\"" + outputArc.source().name() + "\" ");
		modelStream.append("target=\"" + outputArc.destination().name() + "\">");
		modelStream.append("<inscription><value>" + outputArc.getWeight().nameForSaving(false) + "</value></inscription>");
		modelStream.append("</outputArc>\n");
	}

	protected void outputTransportArc(TransportArc transArc, PrintStream modelStream) {
		modelStream.append("<transportArc ");
		modelStream.append("source=\"" + transArc.source().name() + "\" ");
		modelStream.append("transition=\"" + transArc.transition().name() + "\" ");
		modelStream.append("target=\"" + transArc.destination().name() + "\">");
		modelStream.append("<inscription><value>" + transArc.getWeight().nameForSaving(false) + "</value></inscription>");
		modelStream.append("</transportArc>\n");
	}

	protected void outputInhibitorArc(TimedInhibitorArc inhibArc,	PrintStream modelStream) {
		modelStream.append("<inhibitorArc ");
		modelStream.append("source=\"" + inhibArc.source().name() + "\" ");
		modelStream.append("target=\"" + inhibArc.destination().name() + "\">");
		modelStream.append("<inscription><value>" + inhibArc.getWeight().nameForSaving(false) + "</value></inscription>");
		modelStream.append("</inhibitorArc>\n");
	}
}
