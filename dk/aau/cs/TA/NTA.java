package dk.aau.cs.TA;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class NTA 
{
	private List<TimedAutomata> automata;
	
	private String globalDeclarations = "";
	private String systemDeclarations = "";
	
	public NTA(){
		automata = new ArrayList<TimedAutomata>();
	}
	
	public NTA(List<TimedAutomata> automata)
	{
		this.automata = automata;	
	}
	
	public NTA(List<TimedAutomata> automata, String systemDeclarations, String globalDeclarations)
	{
		this(automata);
		
		this.systemDeclarations = systemDeclarations;
		this.globalDeclarations = globalDeclarations;
	}
	
	public List<TimedAutomata> getTimedAutomata()
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
	
	public void addTimedAutomata(TimedAutomata ta){
		automata.add(ta);
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
