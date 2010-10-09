package dk.aau.cs.verification.UPPAAL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;


import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.TAPN.uppaaltransform.AdvancedUppaalNoSym;
import dk.aau.cs.TAPN.uppaaltransform.AdvancedUppaalSym;
import dk.aau.cs.TAPN.uppaaltransform.NaiveUppaalSym;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TAPNtoUppaalTransformer;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.colors.ColoredTimedArcPetriNet;
import dk.aau.cs.translations.ModelTransformer;
import dk.aau.cs.translations.QueryTransformer;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.translations.TranslationNamingScheme;
import dk.aau.cs.translations.coloredtapn.ColoredBroadcastTransformer;
import dk.aau.cs.translations.coloredtapn.ColoredDegree2BroadcastKBoundOptimizationTransformer;
import dk.aau.cs.translations.coloredtapn.ColoredDegree2BroadcastTransformer;
import dk.aau.cs.translations.tapn.Degree2BroadcastTransformer;
import dk.aau.cs.translations.tapn.TAPNToNTABroadcastTransformer;
import dk.aau.cs.translations.tapn.TAPNToNTASymmetryKBoundOptimizeTransformer;
import dk.aau.cs.translations.tapn.TAPNToNTASymmetryTransformer;

public class UppaalExporter {
	public ExportedModel export(ColoredTimedArcPetriNet model, TAPNQuery query, ReductionOption reduction){
		File xmlfile = createTempFile(".xml");
		File qfile = createTempFile(".q");
		if(xmlfile == null || qfile == null) return null;

		int extraTokens = query.getTotalTokens() - model.getNumberOfTokens();

		ModelTransformer<TimedArcPetriNet, NTA> modelTransformer = null;
		QueryTransformer<TAPNQuery, UPPAALQuery> queryTransformer = null;

		if(reduction == ReductionOption.BROADCAST || reduction == ReductionOption.BROADCASTSYMMETRY){
			ColoredBroadcastTransformer transformer = new ColoredBroadcastTransformer(extraTokens, reduction == ReductionOption.BROADCASTSYMMETRY);
			modelTransformer = transformer;
			queryTransformer = transformer;
		}  else if(reduction == ReductionOption.KBOUNDANALYSIS){
			Degree2BroadcastTransformer broadcastTransformer = new ColoredDegree2BroadcastTransformer(extraTokens, true);
			modelTransformer = broadcastTransformer;
			queryTransformer = broadcastTransformer;
		}else if(reduction == ReductionOption.KBOUNDOPTMIZATION){
			Degree2BroadcastTransformer broadcastTransformer = new ColoredDegree2BroadcastKBoundOptimizationTransformer(extraTokens);
			modelTransformer = broadcastTransformer;
			queryTransformer = broadcastTransformer;
		}else{
			Degree2BroadcastTransformer broadcastTransformer = new ColoredDegree2BroadcastTransformer(extraTokens, reduction == ReductionOption.DEGREE2BROADCASTSYMMETRY);
			modelTransformer = broadcastTransformer;
			queryTransformer = broadcastTransformer;
		}
		
		try{
			NTA nta = modelTransformer.transformModel(model);
			nta.outputToUPPAALXML(new PrintStream(xmlfile));
			UPPAALQuery uppaalQuery = queryTransformer.transformQuery(query);
			uppaalQuery.output(new PrintStream(qfile));
		}catch(FileNotFoundException e){
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return new ExportedModel(xmlfile.getAbsolutePath(), qfile.getAbsolutePath(), null);
	}

	public ExportedModel export(TimedArcPetriNet model, TAPNQuery query, ReductionOption reduction){
		File xmlfile = createTempFile(".xml");
		File qfile = createTempFile(".q");
		if(xmlfile == null || qfile == null) return null;

		int extraTokens = query.getTotalTokens() - model.getNumberOfTokens();
		TranslationNamingScheme namingScheme = null;
		if (reduction == ReductionOption.STANDARDSYMMETRY){

			NaiveUppaalSym t = new NaiveUppaalSym();
			try {
				t.autoTransform((TAPN)model, new PrintStream(xmlfile), new PrintStream(qfile), query, extraTokens);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}

		} else if (reduction == ReductionOption.OPTIMIZEDSTANDARDSYMMETRY){
			AdvancedUppaalSym t = new AdvancedUppaalSym();
			try {
				t.autoTransform((TAPN)model, new PrintStream(xmlfile), new PrintStream(qfile), query, extraTokens);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}else if (reduction == ReductionOption.OPTIMIZEDSTANDARD){
			AdvancedUppaalNoSym t = new AdvancedUppaalNoSym();
			try {
				t.autoTransform((TAPN)model, new PrintStream(xmlfile), new PrintStream(qfile), query, extraTokens);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}	
		} else if(reduction == ReductionOption.BROADCAST || reduction == ReductionOption.BROADCASTSYMMETRY){
			TAPNToNTABroadcastTransformer broadcastTransformer = new TAPNToNTABroadcastTransformer(extraTokens, reduction == ReductionOption.BROADCASTSYMMETRY);
			namingScheme = broadcastTransformer.namingScheme();
			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(model);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery uppaalQuery = broadcastTransformer.transformQuery(query);
				uppaalQuery.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else if(reduction == ReductionOption.DEGREE2BROADCASTSYMMETRY || reduction == ReductionOption.DEGREE2BROADCAST){
			Degree2BroadcastTransformer broadcastTransformer = new Degree2BroadcastTransformer(extraTokens, reduction == ReductionOption.DEGREE2BROADCASTSYMMETRY);

			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(model);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery uppaalQuery = broadcastTransformer.transformQuery(query);
				uppaalQuery.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}else if(reduction == ReductionOption.KBOUNDANALYSIS){
			TAPNToNTASymmetryTransformer transformer = new TAPNToNTASymmetryTransformer(extraTokens);

			try{
				dk.aau.cs.TA.NTA nta = transformer.transformModel(model);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery uppaalQuery = transformer.transformQuery(query);
				uppaalQuery.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else if(reduction == ReductionOption.KBOUNDOPTMIZATION){
			TAPNToNTASymmetryTransformer transformer = new TAPNToNTASymmetryKBoundOptimizeTransformer(extraTokens);

			try{
				dk.aau.cs.TA.NTA nta = transformer.transformModel(model);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery uppaalQuery = transformer.transformQuery(query);
				uppaalQuery.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}else {

			try {
				model.convertToConservative();
			} catch (Exception e1) {
				e1.printStackTrace();
				return null;
			}

			try {
				model = ((TAPN)model).convertToDegree2();
			} catch (Exception e1) {
				e1.printStackTrace();
				return null;
			}



			//Create uppaal xml file
			try {
				TAPNtoUppaalTransformer t2 = new TAPNtoUppaalTransformer((TAPN)model, new PrintStream(xmlfile), extraTokens);
				t2.transform();
				t2.transformQueriesToUppaal(extraTokens, query, new PrintStream(qfile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}


		}
		return new ExportedModel(xmlfile.getAbsolutePath(), qfile.getAbsolutePath(), namingScheme);
	}

	private File createTempFile(String ending) {
		File file=null;
		try {
			file = File.createTempFile("verifyta", ending);

		} catch (IOException e2) {
			e2.printStackTrace();
			return null;
		}
		return file;
	}
}
