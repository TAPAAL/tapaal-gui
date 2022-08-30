package dk.aau.cs.model.NTA;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NTA {
	private List<TimedAutomaton> automata;

	private String globalDeclarations = "";
	private String systemDeclarations = "";

	public NTA() {
		automata = new ArrayList<TimedAutomaton>();
	}

	public NTA(List<TimedAutomaton> automata) {
		this.automata = automata;
	}

	public NTA(List<TimedAutomaton> automata, String systemDeclarations,
			String globalDeclarations) {
		this(automata);

		this.systemDeclarations = systemDeclarations;
		this.globalDeclarations = globalDeclarations;
	}

	public List<TimedAutomaton> getTimedAutomata() {
		return automata;
	}

	public void setTimedAutomata(ArrayList<TimedAutomaton> automata) {
		this.automata = automata;
	}

	public String getGlobalDeclarations() {
		return globalDeclarations;
	}

	public void setGlobalDeclarations(String globalDecl) {
		globalDeclarations = globalDecl;
	}

	public String getSystemDeclarations() {
		return systemDeclarations;
	}

	public void setSystemDeclarations(String systemDecl) {
		systemDeclarations = systemDecl;
	}

	public void addTimedAutomaton(TimedAutomaton ta) {
		automata.add(ta);
	}

	public void addTimedAutomata(Collection<TimedAutomaton> tas) {
		automata.addAll(tas);
	}

	public void outputToUPPAALXML(PrintStream xmlFile) {
		xmlFile.println("<nta>");

		// global declarations
		xmlFile.append("<declaration>");
		xmlFile.append(globalDeclarations);
		xmlFile.append("</declaration>\n");

		// Timed Automata
		StringBuffer a = new StringBuffer();
		for (TimedAutomaton ta : automata) {
			a = ta.toXML();
			xmlFile.append(a);
		}

		// System declarations
		xmlFile.append("<system>");
		xmlFile.append(systemDeclarations);
		xmlFile.append("</system>\n");

		xmlFile.println("</nta>");

	}
}
