package dk.aau.cs.verification.UPPAAL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.translations.TranslationNamingScheme;
import dk.aau.cs.translations.tapn.BroadcastTranslation;
import dk.aau.cs.translations.tapn.Degree2BroadcastKBoundOptimizeTranslation;
import dk.aau.cs.translations.tapn.Degree2BroadcastTranslation;
import dk.aau.cs.translations.tapn.OptimizedStandardTranslation;
import dk.aau.cs.translations.tapn.StandardTranslation;

public class UppaalExporter {
	public ExportedModel export(dk.aau.cs.model.tapn.TimedArcPetriNet newModel, TAPNQuery query, ReductionOption reduction) {
		File modelFile = createTempFile(".xml");
		File queryFile = createTempFile(".q");

		return export(newModel, query, reduction, modelFile, queryFile);
	}

	public ExportedModel export(dk.aau.cs.model.tapn.TimedArcPetriNet newModel, TAPNQuery query, ReductionOption reduction, File modelFile, File queryFile) {
		if (modelFile == null || queryFile == null)
			return null;

		
		int extraTokens = query.getTotalTokens() - newModel.marking().size();
		TranslationNamingScheme namingScheme = null;
		if (reduction == ReductionOption.STANDARD || reduction == ReductionOption.STANDARDSYMMETRY) {
			StandardTranslation standardTranslation = new StandardTranslation(extraTokens, reduction == ReductionOption.STANDARDSYMMETRY); 
			namingScheme = standardTranslation.namingScheme();
			try {
				dk.aau.cs.TA.NTA nta = standardTranslation.transformModel(newModel);
				nta.outputToUPPAALXML(new PrintStream(modelFile));
				dk.aau.cs.TA.UPPAALQuery uppaalQuery = standardTranslation.transformQuery(query);
				uppaalQuery.output(new PrintStream(queryFile));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else if (reduction == ReductionOption.OPTIMIZEDSTANDARD 
				|| reduction == ReductionOption.OPTIMIZEDSTANDARDSYMMETRY
				|| reduction == ReductionOption.KBOUNDANALYSIS) {
			OptimizedStandardTranslation translater = new OptimizedStandardTranslation(extraTokens, reduction == ReductionOption.OPTIMIZEDSTANDARDSYMMETRY || reduction == ReductionOption.KBOUNDANALYSIS);
			namingScheme = translater.namingScheme();
			try { 
				dk.aau.cs.TA.NTA nta = translater.transformModel(newModel);
				nta.outputToUPPAALXML(new PrintStream(modelFile));
				dk.aau.cs.TA.UPPAALQuery uppaalQuery = translater.transformQuery(query);
				uppaalQuery.output(new PrintStream(queryFile));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else if (reduction == ReductionOption.BROADCAST || reduction == ReductionOption.BROADCASTSYMMETRY) {
			BroadcastTranslation broadcastTransformer = new BroadcastTranslation(extraTokens, reduction == ReductionOption.BROADCASTSYMMETRY);
			namingScheme = broadcastTransformer.namingScheme();
			try {
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(newModel);
				nta.outputToUPPAALXML(new PrintStream(modelFile));
				dk.aau.cs.TA.UPPAALQuery uppaalQuery = broadcastTransformer.transformQuery(query);
				uppaalQuery.output(new PrintStream(queryFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else if (reduction == ReductionOption.DEGREE2BROADCASTSYMMETRY || reduction == ReductionOption.DEGREE2BROADCAST) {
			Degree2BroadcastTranslation broadcastTransformer = new Degree2BroadcastTranslation(extraTokens, reduction == ReductionOption.DEGREE2BROADCASTSYMMETRY);
			namingScheme = broadcastTransformer.namingScheme();
			try {
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(newModel);
				nta.outputToUPPAALXML(new PrintStream(modelFile));
				dk.aau.cs.TA.UPPAALQuery uppaalQuery = broadcastTransformer.transformQuery(query);
				uppaalQuery.output(new PrintStream(queryFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else if (reduction == ReductionOption.KBOUNDOPTMIZATION) {
			Degree2BroadcastKBoundOptimizeTranslation transformer = new Degree2BroadcastKBoundOptimizeTranslation(extraTokens);

			try {
				dk.aau.cs.TA.NTA nta = transformer.transformModel(newModel);
				nta.outputToUPPAALXML(new PrintStream(modelFile));
				dk.aau.cs.TA.UPPAALQuery uppaalQuery = transformer.transformQuery(query);
				uppaalQuery.output(new PrintStream(queryFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			throw new RuntimeException("Invalid reduction selected. Please try again");
		}
		return new ExportedModel(modelFile.getAbsolutePath(), queryFile.getAbsolutePath(), namingScheme);
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
