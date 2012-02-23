package dk.aau.cs.verification.VerifyTAPN;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.tapaal.TAPAAL;

import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.FileFinder;
import pipe.gui.widgets.InclusionPlaces;
import pipe.gui.widgets.InclusionPlaces.InclusionPlacesOption;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.visitors.UpwardsClosedVisitor;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.ProcessRunner;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.QueryType;
import dk.aau.cs.verification.Stats;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;

public class VerifyTAPN implements ModelChecker {
	private static final String NEED_TO_LOCATE_VERIFYTAPN_MSG = "TAPAAL needs to know the location of the file verifytapn.\n\n"
		+ "Verifytapn is a part of the TAPAAL distribution and it is\n"
		+ "normally located in the directory lib.";
	
	private static String verifytapnpath = "";
	
	private FileFinder fileFinder;
	private Messenger messenger;

	private ProcessRunner runner;
	
	public VerifyTAPN(FileFinder fileFinder, Messenger messenger) {
		this.fileFinder = fileFinder;
		this.messenger = messenger;
	}
	
	public boolean supportsStats(){
		return true;
	}
	
	public String getStatsExplanation(){
		StringBuffer buffer = new StringBuffer("<html>");
		buffer.append("<b>Discovered markings:</b> The number of found markings (each<br />");
		buffer.append("time a successor is calculated, this number is incremented)<br/>");
		buffer.append("<br/>");
		buffer.append("<b>Explored markings:</b> The number of markings taken out<br/>");
		buffer.append("of the waiting list during the search.<br />");
		buffer.append("<br/>");
		buffer.append("<b>Stored markings:</b> The number of markings found in the<br />");
		buffer.append("passed/waiting list at the end of verification.<br />");
		buffer.append("</html>");
		return buffer.toString();
	}
	
	public String getPath() {
		return verifytapnpath;
	}

	public String getVersion() { // atm. any version of VerifyTAPN will do
		String result = null;

		if (!isNotSetup()) {
			String[] commands;
			commands = new String[] { verifytapnpath, "-v" };

			InputStream stream = null;
			try {
				Process child = Runtime.getRuntime().exec(commands);
				child.waitFor();
				stream = child.getInputStream();
			} catch (IOException e) {
			} catch (InterruptedException e) {
			}

			if (stream != null) {
				result = readVersionNumberFrom(stream);
			}
		}

		return result;
	}

	private String readVersionNumberFrom(InputStream stream) {
		String result = null;
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));

		String versioninfo = null;
		try {
			versioninfo = bufferedReader.readLine();
		} catch (IOException e) {
			result = null;
		}

		Pattern pattern = Pattern.compile("^VerifyTAPN (\\d+\\.\\d+\\.\\d+)$");
		Matcher m = pattern.matcher(versioninfo);
		m.find();
		result = m.group(1);
		return result;
	}

	public boolean isCorrectVersion() {
		if (isNotSetup()) {
			messenger.displayErrorMessage(
					"No verifytapn specified: The verification is cancelled",
					"Verification Error");
			return false;
		}
		
		File file = new File(getPath());
		if(!file.canExecute()){
			messenger.displayErrorMessage("The program can not be verified as being verifytapn.\n"
									+ "The verifytapn path will be reset. Please try again, "
									+ "to manually set the verifytapn path.", "Verifytapn Error");
			resetVerifytapn();
			return false;
		}
		
		return true;
	}

	private void resetVerifytapn() {
		verifytapnpath = null;	
	}

	public void kill() {
		if (runner != null) {
			runner.kill();
		}
	}
	
	public void setVerifyTapnPath(String path) {
		verifytapnpath = path;
	}

	public boolean setup() {
		if (isNotSetup()) {
			messenger.displayInfoMessage(NEED_TO_LOCATE_VERIFYTAPN_MSG, "Locate verifytapn");

			try {
				File file = fileFinder.ShowFileBrowserDialog("Verifytapn", "");
				if(file != null){
					if(file.getName().matches("^verifytapn.*(?:\\.exe)?$")){
						verifytapnpath = file.getAbsolutePath();
					}else{
						messenger.displayErrorMessage("The selected executable does not seem to be verifytapn.");
					}
				}

			} catch (Exception e) {
				messenger.displayErrorMessage("There were errors performing the requested action:\n" + e, "Error");
			}

		}

		return !isNotSetup();
	}

	private boolean isNotSetup() {
		return verifytapnpath == null || verifytapnpath.equals("");
	}
	
	public static boolean trySetup() {
		
		String verifytapn = null;
		
		//If env is set, it overwrites the value
		verifytapn = System.getenv("verifytapn");
		if (verifytapn != null && !verifytapn.isEmpty()) {
			verifytapnpath = verifytapn;
			return true;
		}
		
		//If a value is saved in conf
		//TODO: kyrke
		
		//Search the installdir for verifytapn
		File installdir = TAPAAL.getInstallDir();
		
		String[] paths = {"/bin/verifytapn", "/bin/verifytapn64"};
		for (String s : paths) {
			File verifytapnfile = new File(installdir + s);

			if (verifytapnfile.exists()){

				verifytapnpath = verifytapnfile.getAbsolutePath();
				return true;

			}
		}
		
		
		
		return false;
	}

	public VerificationResult<TimedArcPetriNetTrace> verify(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, TAPNQuery query) throws Exception {	
		if(!supportsModel(model.value1()))
			throw new UnsupportedModelException("Verifytapn does not support the given model.");
		
		if(!supportsQuery(model.value1(), query, options))
			throw new UnsupportedQueryException("Verifytapn does not support the given query.");
		
//		if(((VerifyTAPNOptions)options).discreteInclusion() && !isQueryUpwardClosed(query))
//			throw new UnsupportedQueryException("Discrete inclusion check only supports upward closed queries.");
		
		if(((VerifyTAPNOptions)options).discreteInclusion()) mapDiscreteInclusionPlacesToNewNames(options, model);
		
		VerifyTAPNExporter exporter = new VerifyTAPNExporter();
		ExportedVerifyTAPNModel exportedModel = exporter.export(model.value1(), query);

		if (exportedModel == null) {
			messenger.displayErrorMessage("There was an error exporting the model");
		}

		return verify(options, model, exportedModel, query);
	}

	private void mapDiscreteInclusionPlacesToNewNames(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model) {
		VerifyTAPNOptions verificationOptions = (VerifyTAPNOptions)options;
		
		if(verificationOptions.inclusionPlaces().inclusionOption() == InclusionPlacesOption.AllPlaces) 
			return;
		
		List<TimedPlace> inclusionPlaces = new ArrayList<TimedPlace>();
		for(TimedPlace p : verificationOptions.inclusionPlaces().inclusionPlaces()) {
			if(p instanceof LocalTimedPlace) {
				LocalTimedPlace local = (LocalTimedPlace)p;
				if(local.model().isActive()){
					inclusionPlaces.add(model.value1().getPlaceByName(model.value2().map(local.model().name(), local.name())));
				}
			}
			else // shared place
				inclusionPlaces.add(model.value1().getPlaceByName(model.value2().map("", p.name())));
		}
		
		((VerifyTAPNOptions)options).setInclusionPlaces(new InclusionPlaces(InclusionPlacesOption.UserSpecified, inclusionPlaces));
	}

	private VerificationResult<TimedArcPetriNetTrace> verify(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, ExportedVerifyTAPNModel exportedModel, TAPNQuery query) {
		((VerifyTAPNOptions)options).setTokensInModel(model.value1().marking().size()); // TODO: get rid of me
		runner = new ProcessRunner(verifytapnpath, createArgumentString(exportedModel.modelFile(), exportedModel.queryFile(), options));
		runner.run();

		if (runner.error()) {
			return null;
		} else {
			String errorOutput = readOutput(runner.errorOutput());
			String standardOutput = readOutput(runner.standardOutput());

			Tuple<QueryResult, Stats> queryResult = parseQueryResult(standardOutput, model.value1().marking().size() + query.getExtraTokens(), query.getExtraTokens(), query.queryType());
			if (queryResult == null || queryResult.value1() == null) {
				return new VerificationResult<TimedArcPetriNetTrace>(errorOutput + System.getProperty("line.separator") + standardOutput, runner.getRunningTime());
			} else {
				TimedArcPetriNetTrace tapnTrace = parseTrace(errorOutput, options, model, exportedModel, query, queryResult.value1());
				return new VerificationResult<TimedArcPetriNetTrace>(queryResult.value1(), tapnTrace, runner.getRunningTime(), queryResult.value2()); 
			}
		}
	}

	private TimedArcPetriNetTrace parseTrace(String output, VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, ExportedVerifyTAPNModel exportedModel, TAPNQuery query, QueryResult queryResult) {
		if (((VerifyTAPNOptions) options).trace() == TraceOption.NONE) return null;
		
		VerifyTAPNTraceParser traceParser = new VerifyTAPNTraceParser(model.value1());
		TimedArcPetriNetTrace trace = traceParser.parseTrace(new BufferedReader(new StringReader(output)));
		
		if (trace == null) {
			if (((VerifyTAPNOptions) options).trace() != TraceOption.NONE) {
				if((query.getProperty() instanceof TCTLEFNode && !queryResult.isQuerySatisfied()) || (query.getProperty() instanceof TCTLAGNode && queryResult.isQuerySatisfied()))
					return null;
				else
					messenger.displayErrorMessage("Verifytapn cannot generate the requested trace for the model. Try another trace option.");
			}
		} 
		return trace;
	}

	private String createArgumentString(String modelFile, String queryFile, VerificationOptions options) {
		StringBuffer buffer = new StringBuffer(options.toString());
		buffer.append(' ');
		buffer.append(modelFile);
		buffer.append(' ');
		buffer.append(queryFile);

		return buffer.toString();
	}
	
	private String readOutput(BufferedReader reader) {
		try {
			if (!reader.ready())
				return "";
		} catch (IOException e1) {
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
				buffer.append(System.getProperty("line.separator"));
			}
		} catch (IOException e) {
		}

		return buffer.toString();
	}
	
	private Tuple<QueryResult, Stats> parseQueryResult(String output, int totalTokens, int extraTokens, QueryType queryType) {
		VerifyTAPNOutputParser outputParser = new VerifyTAPNOutputParser(totalTokens, extraTokens, queryType);
		Tuple<QueryResult, Stats> result = outputParser.parseOutput(output);
		return result;
	}
	
	
	boolean supportsModel(TimedArcPetriNet model) {
		return true;
	}
	
	boolean supportsQuery(TimedArcPetriNet model, TAPNQuery query, VerificationOptions options) {
		if(query.getProperty() instanceof TCTLEGNode || query.getProperty() instanceof TCTLAFNode) {
			return false;
		}
		
		return true;
	}
	
	private boolean isQueryUpwardClosed(TAPNQuery query) {
		UpwardsClosedVisitor visitor = new UpwardsClosedVisitor();
		return visitor.isUpwardClosed(query.getProperty());
	}

	public static void reset() {
		verifytapnpath = "";
	}

	
}
