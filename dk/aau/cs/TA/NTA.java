package dk.aau.cs.TA;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import dk.aau.cs.petrinet.Transition;

public class NTA 
{
	private ArrayList<TimedAutomata> automata = new ArrayList<TimedAutomata>();
	
	private String globalDeclarations = "";
	private String systemDeclarations = "";
	
	public NTA(ArrayList<TimedAutomata> automata)
	{
		this.automata = automata;	
	}
	
	public NTA(ArrayList<TimedAutomata> automata, String systemDeclarations, String globalDeclarations)
	{
		this(automata);
		
		this.systemDeclarations = systemDeclarations;
		this.globalDeclarations = globalDeclarations;
	}
	
	public ArrayList<TimedAutomata> getTimedAutomata()
	{
		return automata;
	}
	
	public void setTimedAutomata(ArrayList<TimedAutomata> automata)
	{
		this.automata = automata;
	}
	
	public String getGlobalDeclarations()
	{
		return globalDeclarations;
	}
	
	public void setGlobalDeclarations(String globalDecl)
	{
		globalDeclarations = globalDecl;
	}
	
	public String getSystemDeclarations()
	{
		return systemDeclarations;
	}
	
	public void setSystemDeclarations(String systemDecl)
	{
		systemDeclarations = systemDecl;
	}
	
	public void outputToUPPAALXML(PrintStream xmlFile)
	{
		xmlFile.println("<nta>");
		
		// global declarations
		xmlFile.append("<declaration>");
		xmlFile.append(globalDeclarations);	
		xmlFile.append("</declaration>\n");
		
		// Timed Automata
		StringBuffer a = new StringBuffer();
		for(TimedAutomata ta : automata)
		{
			a = ta.toXML();
			xmlFile.append(a);
		}
		
		//System declarations
		xmlFile.append("<system>");
		xmlFile.append(systemDeclarations);
		xmlFile.append("</system>\n");
		
		xmlFile.println("</nta>");

	}
}
