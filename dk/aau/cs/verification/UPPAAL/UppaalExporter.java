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
import dk.aau.cs.translations.coloredtapn.ColoredBroadcastTransformer;
import dk.aau.cs.translations.coloredtapn.ColoredDegree2BroadcastTransformer;
import dk.aau.cs.translations.tapn.Degree2BroadcastTransformer;
import dk.aau.cs.translations.tapn.TAPNToNTABroadcastTransformer;

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
		} else if(reduction == ReductionOption.DEGREE2BROADCASTSYMMETRY || reduction == ReductionOption.DEGREE2BROADCAST){
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
		
		return new ExportedModel(xmlfile.getAbsolutePath(), qfile.getAbsolutePath());
	}
	
	public ExportedModel export(TimedArcPetriNet model, TAPNQuery query, ReductionOption reduction){
		File xmlfile = createTempFile(".xml");
		File qfile = createTempFile(".q");
		if(xmlfile == null || qfile == null) return null;
		
		int extraTokens = query.getTotalTokens() - model.getNumberOfTokens();
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

			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(model);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery uppaalQuery = broadcastTransformer.transformQuery(query);
				uppaalQuery.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {

			try {
				model.convertToConservative();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				model = ((TAPN)model).convertToDegree2();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}



			//Create uppaal xml file
			try {
				TAPNtoUppaalTransformer t2 = new TAPNtoUppaalTransformer((TAPN)model, new PrintStream(xmlfile), extraTokens);
				t2.transform();
				t2.transformQueriesToUppaal(extraTokens, query, new PrintStream(qfile));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}
		return new ExportedModel(xmlfile.getAbsolutePath(), qfile.getAbsolutePath());
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
