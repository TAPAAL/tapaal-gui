package dk.aau.cs.verification.UPPAAL;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.translations.ModelTranslator;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.translations.tapn.BroadcastTranslation;
import dk.aau.cs.translations.tapn.CombiTranslation;
import dk.aau.cs.translations.tapn.Degree2BroadcastKBoundOptimizeTranslation;
import dk.aau.cs.translations.tapn.Degree2BroadcastTranslation;
import dk.aau.cs.translations.tapn.OptimizedStandardTranslation;
import dk.aau.cs.translations.tapn.StandardTranslation;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;

public class UppaalExporter {
	public ExportedModel export(dk.aau.cs.model.tapn.TimedArcPetriNet model, TAPNQuery query, ReductionOption reduction, boolean symmetry) throws Exception {
		File modelFile = createTempFile(".xml");
		File queryFile = createTempFile(".q");

		return export(model, query, reduction, modelFile, queryFile, symmetry);
	}

	public ExportedModel export(dk.aau.cs.model.tapn.TimedArcPetriNet model, TAPNQuery query, ReductionOption reduction, File modelFile, File queryFile, boolean symmetry) throws Exception {
		if (modelFile == null || queryFile == null) return null;

		ModelTranslator<dk.aau.cs.model.tapn.TimedArcPetriNet, TAPNQuery, dk.aau.cs.model.NTA.NTA, dk.aau.cs.model.NTA.UPPAALQuery> translator = null;
			
		if (reduction == ReductionOption.STANDARD) {
			translator = new StandardTranslation(symmetry); 
		} else if (reduction == ReductionOption.OPTIMIZEDSTANDARD || (reduction == ReductionOption.KBOUNDANALYSIS && !model.hasInhibitorArcs())) {
			translator = new OptimizedStandardTranslation(symmetry || reduction == ReductionOption.KBOUNDANALYSIS);
		} else if (reduction == ReductionOption.BROADCAST) {
			translator = new BroadcastTranslation(symmetry);
		} else if (reduction == ReductionOption.DEGREE2BROADCAST || (reduction == ReductionOption.KBOUNDANALYSIS && model.hasInhibitorArcs())) {
			translator = new Degree2BroadcastTranslation(symmetry || reduction == ReductionOption.KBOUNDANALYSIS);
		} else if (reduction == ReductionOption.KBOUNDOPTMIZATION) {
			translator = new Degree2BroadcastKBoundOptimizeTranslation();
		} else if (reduction == ReductionOption.COMBI){
			translator = new CombiTranslation(symmetry);
		} else {
			throw new RuntimeException("Invalid reduction selected. Please try again");
		}
		
		try { 
			Tuple<dk.aau.cs.model.NTA.NTA, dk.aau.cs.model.NTA.UPPAALQuery> translatedModel = translator.translate(model, query);
			translatedModel.value1().outputToUPPAALXML(new PrintStream(modelFile));
			translatedModel.value2().output(new PrintStream(queryFile));
		} catch(UnsupportedModelException e) {
			throw e;
		} catch(UnsupportedQueryException e) {
			throw e;
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
