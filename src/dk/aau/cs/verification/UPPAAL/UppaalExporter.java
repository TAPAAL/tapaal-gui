package dk.aau.cs.verification.UPPAAL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.degree2converters.NaiveDegree2Converter;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.translations.TranslationNamingScheme;
import dk.aau.cs.translations.tapn.BroadcastTranslation;
import dk.aau.cs.translations.tapn.Degree2BroadcastKBoundOptimizeTranslation;
import dk.aau.cs.translations.tapn.Degree2BroadcastTranslation;
import dk.aau.cs.translations.tapn.OptimizedStandardNamingScheme;
import dk.aau.cs.translations.tapn.OptimizedStandardSymmetryTranslation;
import dk.aau.cs.translations.tapn.OptimizedStandardTranslation;
import dk.aau.cs.translations.tapn.StandardNamingScheme;
import dk.aau.cs.translations.tapn.StandardSymmetryTranslation;
import dk.aau.cs.translations.tapn.StandardTranslation;

// TODO: remove the old model method parameters once all translations have been converted
public class UppaalExporter {
	public ExportedModel export(dk.aau.cs.model.tapn.TimedArcPetriNet newModel, TimedArcPetriNet model, TAPNQuery query,
			ReductionOption reduction) {
		File modelFile = createTempFile(".xml");
		File queryFile = createTempFile(".q");

		return export(newModel, model, query, reduction, modelFile, queryFile);
	}

	public ExportedModel export(dk.aau.cs.model.tapn.TimedArcPetriNet newModel, TimedArcPetriNet model, TAPNQuery query,
			ReductionOption reduction, File modelFile, File queryFile) {
		if (modelFile == null || queryFile == null)
			return null;

		
		int extraTokens = query.getTotalTokens() - model.getNumberOfTokens();
		TranslationNamingScheme namingScheme = null;
		if (reduction == ReductionOption.STANDARDSYMMETRY) {

			StandardSymmetryTranslation t = new StandardSymmetryTranslation();
			try {
				t.autoTransform((TAPN) model, new PrintStream(modelFile),
						new PrintStream(queryFile), query, extraTokens);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}

		} else if (reduction == ReductionOption.OPTIMIZEDSTANDARDSYMMETRY
				|| reduction == ReductionOption.KBOUNDANALYSIS) {
			OptimizedStandardSymmetryTranslation t = new OptimizedStandardSymmetryTranslation();
			try {
				t.autoTransform((TAPN) model, new PrintStream(modelFile),
						new PrintStream(queryFile), query, extraTokens);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		} else if (reduction == ReductionOption.OPTIMIZEDSTANDARD) {
			OptimizedStandardTranslation t = new OptimizedStandardTranslation();
			try {
				t.autoTransform((TAPN) model, new PrintStream(modelFile),
						new PrintStream(queryFile), query, extraTokens);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
			namingScheme = new OptimizedStandardNamingScheme();
		} else if (reduction == ReductionOption.BROADCAST
				|| reduction == ReductionOption.BROADCASTSYMMETRY) {
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
		} else if (reduction == ReductionOption.DEGREE2BROADCASTSYMMETRY
				|| reduction == ReductionOption.DEGREE2BROADCAST) {
			Degree2BroadcastTranslation broadcastTransformer = new Degree2BroadcastTranslation(
					extraTokens,
					reduction == ReductionOption.DEGREE2BROADCASTSYMMETRY);
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

			try {
				model.convertToConservative();
			} catch (Exception e1) {
				e1.printStackTrace();
				return null;
			}

			try {
				NaiveDegree2Converter deg2Converter = new NaiveDegree2Converter();
				model = deg2Converter.transform((TAPN) model);
			} catch (Exception e1) {
				e1.printStackTrace();
				return null;
			}

			// Create uppaal xml file
			try {
				StandardTranslation t2 = new StandardTranslation((TAPN) model,
						new PrintStream(modelFile), extraTokens);
				t2.transform();
				t2.transformQueriesToUppaal(extraTokens, query,
						new PrintStream(queryFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			namingScheme = new StandardNamingScheme();
		}
		return new ExportedModel(modelFile.getAbsolutePath(), queryFile
				.getAbsolutePath(), namingScheme);
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
