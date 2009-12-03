package pipe.gui;

import java.io.PrintStream;

import javax.swing.JOptionPane;

import pipe.dataLayer.DataLayer;
import pipe.gui.Verification.RunUppaalVerification;
import dk.aau.cs.TAPN.uppaaltransform.AdvancedUppaalSym;
import dk.aau.cs.TAPN.uppaaltransform.AdvancedUppaalSymKBound;

public class KBoundOptimizer extends KBoundAnalyzer {

	private int minBound = -1;
	public int getMinBound()
	{
		return minBound;
	}
	
	public KBoundOptimizer(DataLayer appModel, int k)
	{
		super(appModel, k);
	}
	
	@Override
	protected void printQuery(PrintStream stream) {
		super.printQuery(stream);
		stream.println("/*");
		stream.println(" boundedness query ");
		stream.println("*/");
		stream.println("sup: usedExtraTokens");
	}
	
	@Override
	protected AdvancedUppaalSym getReductionStrategy()
	{
		return new AdvancedUppaalSymKBound();
	}
	
	@Override
	protected void parseLine(String line) {
		super.parseLine(line);
		
		if(isBounded() && line.contains("usedExtraTokens"))
		{
			String number = line.substring(line.lastIndexOf(" ")+1, line.length());
			minBound = Integer.parseInt(number);
		}
	}
	
	@Override
	protected void showResult(RunUppaalVerification a) {
		String resultmessage = "";
		
		String answerNetIsNotBounded = getAnswerNotBoundedString();
				
		//Display Answer
		if(!isBounded())
		{
			resultmessage = answerNetIsNotBounded; 
			resultmessage+= "\nAnalysis time is estimated to: " + (a.verificationtime/1000.0) + "s";
		
			JOptionPane.showMessageDialog(CreateGui.getApp(),
				resultmessage,
				"Boundness Analyses Result",
				JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
