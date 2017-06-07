package dk.aau.cs.verification.VerifyTAPN;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.dataLayer.TAPNQuery.QueryCategory;

import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.translations.ReductionOption;

import dk.aau.cs.TCTL.visitors.CTLQueryVisitor;

public class VerifyTAPNExporter {
	public ExportedVerifyTAPNModel export(TimedArcPetriNet model, TAPNQuery query) {
		File modelFile = createTempFile(".xml");
		File queryFile;
		if (query.getCategory() == QueryCategory.CTL){
			queryFile = createTempFile(".xml");
		} else {
			queryFile = createTempFile(".q");
		}
		

		return export(model, query, modelFile, queryFile, null);
	}

	public ExportedVerifyTAPNModel export(TimedArcPetriNet model, TAPNQuery query, File modelFile, File queryFile, pipe.dataLayer.TAPNQuery dataLayerQuery) {
		if (modelFile == null || queryFile == null)
			return null;

		try{
			PrintStream modelStream = new PrintStream(modelFile);
			
			outputModel(model, modelStream);
			modelStream.close();
			
			PrintStream queryStream = new PrintStream(queryFile);
			if (query.getCategory() == QueryCategory.CTL){
			    CTLQueryVisitor XMLVisitor = new CTLQueryVisitor();
			    queryStream.append(XMLVisitor.getXMLQueryFor(query.getProperty(), null));
			} else {
			    queryStream.append(query.getProperty().toString());
			}
			
			queryStream.close();
		} catch(FileNotFoundException e) {
			System.err.append("An error occurred while exporting the model to verifytapn. Verification cancelled.");
			return null;
		}
		return new ExportedVerifyTAPNModel(modelFile.getAbsolutePath(), queryFile.getAbsolutePath());
	}
	
	private void outputModel(TimedArcPetriNet model, PrintStream modelStream) {
		modelStream.append("<pnml>\n");
		modelStream.append("<net id=\"" + model.name() + "\" type=\"P/T net\">\n");
		
		for(TimedPlace p : model.places())
			outputPlace(p, modelStream);
		
		for(TimedTransition t : model.transitions())
			outputTransition(t,modelStream);
		
		for(TimedInputArc inputArc : model.inputArcs())
			outputInputArc(inputArc, modelStream);
		
		for(TimedOutputArc outputArc : model.outputArcs())
			outputOutputArc(outputArc, modelStream);
		
		for(TransportArc transArc : model.transportArcs())
			outputTransportArc(transArc, modelStream);
		
		for(TimedInhibitorArc inhibArc : model.inhibitorArcs())
			outputInhibitorArc(inhibArc, modelStream);
		
		modelStream.append("</net>\n");
		modelStream.append("</pnml>");
	}
	
	private void outputPlace(TimedPlace p, PrintStream modelStream) {
		modelStream.append("<place ");
		
		modelStream.append("id=\"" + p.name() + "\" ");
		modelStream.append("name=\"" + p.name() + "\" ");
		modelStream.append("invariant=\"" + p.invariant().toString(false).replace("<", "&lt;") + "\" ");
		modelStream.append("initialMarking=\"" + p.numberOfTokens() + "\" ");
		
		modelStream.append("/>\n");
	}

	private void outputTransition(TimedTransition t, PrintStream modelStream) {
		modelStream.append("<transition ");
		
		modelStream.append("id=\"" + t.name() + "\" ");
		modelStream.append("name=\"" + t.name() + "\" ");
		modelStream.append("urgent=\"" + (t.isUrgent()? "true":"false") + "\"");
		
		modelStream.append("/>");
	}

	protected void outputInputArc(TimedInputArc inputArc, PrintStream modelStream) {
		modelStream.append("<inputArc ");
		
		modelStream.append("inscription=\"" + inputArc.interval().toString(false).replace("<", "&lt;") + "\" ");
		modelStream.append("source=\"" + inputArc.source().name() + "\" ");
		modelStream.append("target=\"" + inputArc.destination().name() + "\" ");
		if(inputArc.getWeight().value() > 1){
			modelStream.append("weight=\"" + inputArc.getWeight().nameForSaving(false) + "\"");
		}
		
		modelStream.append("/>\n");
	}

	protected void outputOutputArc(TimedOutputArc outputArc, PrintStream modelStream) {
		modelStream.append("<outputArc ");
		
		modelStream.append("inscription=\"1\" " );
		modelStream.append("source=\"" + outputArc.source().name() + "\" ");
		modelStream.append("target=\"" + outputArc.destination().name() + "\" ");
		if(outputArc.getWeight().value() > 1){
			modelStream.append("weight=\"" + outputArc.getWeight().nameForSaving(false) + "\"");
		}
		
		modelStream.append("/>\n");
	}

	protected void outputTransportArc(TransportArc transArc, PrintStream modelStream) {
		modelStream.append("<transportArc ");
		
		modelStream.append("inscription=\"" + transArc.interval().toString(false).replace("<", "&lt;") + "\" ");
		modelStream.append("source=\"" + transArc.source().name() + "\" ");
		modelStream.append("transition=\"" + transArc.transition().name() + "\" ");
		modelStream.append("target=\"" + transArc.destination().name() + "\" ");
		if(transArc.getWeight().value() > 1){
			modelStream.append("weight=\"" + transArc.getWeight().nameForSaving(false) + "\"");
		}
		
		modelStream.append("/>\n");
	}

	protected void outputInhibitorArc(TimedInhibitorArc inhibArc,	PrintStream modelStream) {
		modelStream.append("<inhibitorArc ");
		
		modelStream.append("inscription=\"" + inhibArc.interval().toString(false).replace("<", "&lt;") + "\" ");
		modelStream.append("source=\"" + inhibArc.source().name() + "\" ");
		modelStream.append("target=\"" + inhibArc.destination().name() + "\" ");
		if(inhibArc.getWeight().value() > 1){
			modelStream.append("weight=\"" + inhibArc.getWeight().nameForSaving(false) + "\"");
		}
		
		modelStream.append("/>\n");
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
