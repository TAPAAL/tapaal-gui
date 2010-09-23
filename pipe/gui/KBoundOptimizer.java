package pipe.gui;

import java.io.PrintStream;

import javax.swing.JOptionPane;

import pipe.dataLayer.DataLayer;
import dk.aau.cs.Messenger;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.SupQuery;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.TAPN.ModelTransformer;
import dk.aau.cs.TAPN.TAPNToNTASymmetryKBoundOptimizeTransformer;
import dk.aau.cs.TAPN.colorTranslations.ColoredDegree2BroadcastKBoundOptimizationTransformer;
import dk.aau.cs.petrinet.TimedArcPetriNet;

public class KBoundOptimizer extends KBoundAnalyzer {

	private int minBound = -1;
	public int getMinBound()
	{
		return minBound;
	}

	public KBoundOptimizer(DataLayer appModel, int k, Messenger messenger)
	{
		super(appModel, k, messenger);
	}

	@Override
	protected void printQuery(PrintStream stream) {
		super.printQuery(stream);
		stream.println("/*");
		stream.println(" boundedness query ");
		stream.println("*/");
		UPPAALQuery query = new SupQuery("usedExtraTokens");
		query.output(stream);
	}

	@Override
	protected ModelTransformer<TimedArcPetriNet, NTA> getReductionStrategy()
	{
		if(!appModel.isUsingColors()){
			return new TAPNToNTASymmetryKBoundOptimizeTransformer(k+1);
		}else{
			return new ColoredDegree2BroadcastKBoundOptimizationTransformer(k+1);
		}
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
	protected void showResult(RunVerificationBase a) {
		String resultmessage = "";

		String answerNetIsNotBounded = getAnswerNotBoundedString();

		//Display Answer
		if(!isBounded())
		{
			resultmessage = answerNetIsNotBounded; 
		// TODO: MJ -- fix
			//resultmessage+= "\nAnalysis time is estimated to: " + (a.verificationtime/1000.0) + "s";

			JOptionPane.showMessageDialog(CreateGui.getApp(),
					resultmessage,
					"Boundness Analyses Result",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
