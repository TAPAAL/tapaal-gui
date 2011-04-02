package dk.aau.cs.verification.UPPAAL;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.translations.ModelTranslator;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.translations.tapn.BroadcastTranslation;
import dk.aau.cs.translations.tapn.Degree2BroadcastKBoundOptimizeTranslation;
import dk.aau.cs.translations.tapn.Degree2BroadcastTranslation;
import dk.aau.cs.translations.tapn.OptimizedStandardTranslation;
import dk.aau.cs.translations.tapn.StandardTranslation;
import dk.aau.cs.util.Tuple;

public class UppaalExporter {
	public ExportedModel export(dk.aau.cs.model.tapn.TimedArcPetriNet newModel, TAPNQuery query, ReductionOption reduction) {
		File modelFile = createTempFile(".xml");
		File queryFile = createTempFile(".q");

		return export(newModel, query, reduction, modelFile, queryFile);
	}

	public ExportedModel export(dk.aau.cs.model.tapn.TimedArcPetriNet newModel, TAPNQuery query, ReductionOption reduction, File modelFile, File queryFile) {
		if (modelFile == null || queryFile == null) return null;

		ModelTranslator<dk.aau.cs.model.tapn.TimedArcPetriNet, TAPNQuery, dk.aau.cs.model.NTA.NTA, dk.aau.cs.model.NTA.UPPAALQuery> translator = null;
			
		if (reduction == ReductionOption.STANDARD || reduction == ReductionOption.STANDARDSYMMETRY) {
			translator = new StandardTranslation(reduction == ReductionOption.STANDARDSYMMETRY); 
		} else if (reduction == ReductionOption.OPTIMIZEDSTANDARD 
				|| reduction == ReductionOption.OPTIMIZEDSTANDARDSYMMETRY
				|| reduction == ReductionOption.KBOUNDANALYSIS) {
			translator = new OptimizedStandardTranslation(reduction == ReductionOption.OPTIMIZEDSTANDARDSYMMETRY || reduction == ReductionOption.KBOUNDANALYSIS);
		} else if (reduction == ReductionOption.BROADCAST || reduction == ReductionOption.BROADCASTSYMMETRY) {
			translator = new BroadcastTranslation(reduction == ReductionOption.BROADCASTSYMMETRY);
		} else if (reduction == ReductionOption.DEGREE2BROADCASTSYMMETRY || reduction == ReductionOption.DEGREE2BROADCAST) {
			translator = new Degree2BroadcastTranslation(reduction == ReductionOption.DEGREE2BROADCASTSYMMETRY);
		} else if (reduction == ReductionOption.KBOUNDOPTMIZATION) {
			translator = new Degree2BroadcastKBoundOptimizeTranslation();
		} else {
			throw new RuntimeException("Invalid reduction selected. Please try again");
		}
		
		try { 
			Tuple<dk.aau.cs.model.NTA.NTA, dk.aau.cs.model.NTA.UPPAALQuery> translatedModel = translator.translate(newModel, query);
			translatedModel.value1().outputToUPPAALXML(new PrintStream(modelFile));
			translatedModel.value2().output(new PrintStream(queryFile));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return new ExportedModel(modelFile.getAbsolutePath(), queryFile.getAbsolutePath(), translator.namingScheme());
	}

	private File createTempFile(String ending) {
		File file = null;
		try {
			file = File.createTempFile("verifyta", ending);

		} catch (IOException e2) {
			e2.printStackTrace();
			return null;
		}
		return file;
	}
}
