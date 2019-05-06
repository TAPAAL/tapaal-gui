package dk.aau.cs.verification.VerifyTAPN;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.tapaal.Preferences;
import net.tapaal.TAPAAL;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.FileFinder;
import pipe.gui.MessengerImpl;
import pipe.gui.Pipe;
import pipe.gui.widgets.InclusionPlaces;
import pipe.gui.widgets.InclusionPlaces.InclusionPlacesOption;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.util.ExecutabilityChecker;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.ProcessRunner;
import dk.aau.cs.verification.QueryResult;
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

	public String getVersion() {
		String result = null;

		if (!isNotSetup()) {
			String[] commands;
			commands = new String[] { verifytapnpath, "-v" };

			InputStream stream = null;
			try {
				Process child = Runtime.getRuntime().exec(commands);
				stream = child.getInputStream();
				if (stream != null) {
					result = readVersionNumberFrom(stream);
				}
				child.waitFor();
			} catch (IOException e) {
			} catch (InterruptedException e) {
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
			while(bufferedReader.readLine() != null){}	// Empty buffer
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
			return false;
		}
		
		File file = new File(getPath());
		if(!file.canExecute()){
			messenger.displayErrorMessage("The engine verifytapn is not executable.\n"
									+ "The verifytapn path will be reset. Please try again, "
									+ "to manually set the verifytapn path.", "Verifytapn Error");
			resetVerifytapn();
			return false;
		}

		if (getVersion() != null) {
			String[] version = getVersion().split("\\.");
			String[] targetversion = Pipe.verifytapnMinRev.split("\\.");

			for (int i = 0; i < targetversion.length; i++) {
				if (version.length < i + 1) version[i] = "0";
				int diff = Integer.parseInt(version[i]) - Integer.parseInt(targetversion[i]);
				if (diff > 0) {
					break;
				} else if (diff < 0) {
					return false;
				}
			}

			return true;
		} else {
			return false;
		}
	}

	private void resetVerifytapn() {
		verifytapnpath = null;	
		Preferences.getInstance().setVerifytapnLocation(null);
	}

	public void kill() {
		if (runner != null) {
			runner.kill();
		}
	}
	
	public void setPath(String path) throws IllegalArgumentException {
		ExecutabilityChecker.check(path);
		String oldPath = verifytapnpath; 
		verifytapnpath = path;
		Preferences.getInstance().setVerifytapnLocation(path);
		if(!isCorrectVersion()){
			messenger
			.displayErrorMessage(
					"The specified version of the file verifytapn is too old.", "Verifytapn Error");
			verifytapnpath = oldPath;
			Preferences.getInstance().setVerifytapnLocation(oldPath);
		}
	}

	public boolean setup() {
		if (isNotSetup()) {
			messenger.displayInfoMessage(NEED_TO_LOCATE_VERIFYTAPN_MSG, "Locate verifytapn");

			try {
				File file = fileFinder.ShowFileBrowserDialog("Verifytapn", "",System.getProperty("user.home"));
				if(file != null){
					if(file.getName().matches("^verifytapn.*(?:\\.exe)?$")){
						setPath(file.getAbsolutePath());
					}else{
						messenger.displayErrorMessage("The selected executable does not seem to be verifytapn.");
					}
				}

			} catch (Exception e) {
				messenger.displayErrorMessage("There were errors performing the requested action:\n" + e.getMessage(), "Error");
			}

		}

		return !isNotSetup();
	}

	private boolean isNotSetup() {
		return verifytapnpath == null || verifytapnpath.equals("") || !(new File(verifytapnpath)).exists();
	}
	
	public static boolean trySetup() {


			String verifytapn = null;

			//If env is set, it overwrites the value
			verifytapn = System.getenv("verifytapn");
			if (verifytapn != null && !verifytapn.isEmpty()) {
				if (new File(verifytapn).exists()){
					verifytapnpath = verifytapn;
					VerifyTAPN v = new VerifyTAPN(new FileFinder(), new MessengerImpl());
					if(v.isCorrectVersion()){
						return true;
					}else{
						verifytapn = null;
						verifytapnpath = null;
					}
				}
			}

			//If pref is set
			verifytapn = Preferences.getInstance().getVerifytapnLocation();
			if (verifytapn != null && !verifytapn.isEmpty()) {
				verifytapnpath = verifytapn;
				VerifyTAPN v = new VerifyTAPN(new FileFinder(), new MessengerImpl());
				if(v.isCorrectVersion()){
					return true;
				}else{
					verifytapn = null;
					verifytapnpath = null;
				}
			}

			//Search the installdir for verifytapn
			File installdir = TAPAAL.getInstallDir();

			String[] paths = {"/bin/verifytapn", "/bin/verifytapn64", "/bin/verifytapn.exe", "/bin/verifytapn64.exe"};
			for (String s : paths) {
				File verifytapnfile = new File(installdir + s);

				if (verifytapnfile.exists()){

					verifytapnpath = verifytapnfile.getAbsolutePath();
					VerifyTAPN v = new VerifyTAPN(new FileFinder(), new MessengerImpl());
					if(v.isCorrectVersion()){
						return true;
					}else{
						verifytapn = null;
						verifytapnpath = null;
					}

				}
			}

			return false;

	}

	public VerificationResult<TimedArcPetriNetTrace> verify(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, TAPNQuery query) throws Exception {	
		if(!supportsModel(model.value1(), options))
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

			Tuple<QueryResult, Stats> queryResult = parseQueryResult(standardOutput, model.value1().marking().size() + query.getExtraTokens(), query.getExtraTokens(), query);
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
	
	private Tuple<QueryResult, Stats> parseQueryResult(String output, int totalTokens, int extraTokens, TAPNQuery query) {
		VerifyTAPNOutputParser outputParser = new VerifyTAPNOutputParser(totalTokens, extraTokens, query);
		Tuple<QueryResult, Stats> result = outputParser.parseOutput(output);
		return result;
	}
	
	public boolean supportsModel(TimedArcPetriNet model, VerificationOptions options) {
		if(model.hasWeights() || 
				model.hasUrgentTransitions()) {
			return false;
		}
		
		return true;
	}
	
	public boolean supportsQuery(TimedArcPetriNet model, TAPNQuery query, VerificationOptions options) {
		if(query.getProperty() instanceof TCTLEGNode || 
				query.getProperty() instanceof TCTLAFNode ||
				query.hasDeadlock()) {
			return false;
		}
		
		return true;
	}
	
	// JS: this is not used any more
	//private boolean isQueryUpwardClosed(TAPNQuery query) {
	//	UpwardsClosedVisitor visitor = new UpwardsClosedVisitor();
	//	return visitor.isUpwardClosed(query.getProperty());
	//}

	
	public static void reset() {
		//Clear value
		verifytapnpath = "";
		Preferences.getInstance().setVerifytapnLocation(null);
		//Set the detault
		trySetup();
	}

	@Override
	public String toString() {
		return "verifytapn";
	}
	
	public boolean useDiscreteSemantics() {
		return false;
	}
}
