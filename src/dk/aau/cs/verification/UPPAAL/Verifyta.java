package dk.aau.cs.verification.UPPAAL;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.tapaal.Preferences;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.FileFinder;
import pipe.gui.MessengerImpl;
import pipe.gui.Pipe;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.visitors.HasDeadlockVisitor;
import dk.aau.cs.model.NTA.trace.UppaalTrace;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.ExecutabilityChecker;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.ProcessRunner;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;

public class Verifyta implements ModelChecker {
	private static final String NEED_TO_LOCATE_VERIFYTA_MSG = "TAPAAL needs to know the location of the file verifyta.\n\n"
			+ "Verifyta is a part of the UPPAAL distribution and it is\n"
			+ "normally located in uppaal/bin-Linux or uppaal/bin-Win32,\n"
			+ "depending on the operating system used.";

	private static String verifytapath = ""; // MJ -- Should be part of a
												// configuration file that can
												// be accessed
	private FileFinder fileFinder;
	private Messenger messenger;

	private ProcessRunner runner;

	/**
	 * Default constructor, used for reading version number (if verifyta is set)
	 * used eg. in the about dialog.
	 */
	public Verifyta() {
	}

	public Verifyta(FileFinder fileFinder, Messenger messenger) {
		this.fileFinder = fileFinder;
		this.messenger = messenger;
	}

	public boolean setup() { // TODO: Should maybe eliminate boolean return
								// value from here?
		if (isNotSetup()) {
			messenger.displayInfoMessage(NEED_TO_LOCATE_VERIFYTA_MSG,
					"Locate UPPAAL Verifyta");

			try {
				File file = fileFinder.ShowFileBrowserDialog("Verifyta", "",System.getProperty("user.home"));
				
				if(file != null){
					if(file.getName().matches("^verifyta(?:\\d.*)?(?:\\.exe)?$")){
						setPath(file.getAbsolutePath());
					}else{
						messenger.displayErrorMessage("The selected executable does not seem to be verifyta.");
					}
				}

			} catch (Exception e) {
				messenger.displayErrorMessage(
						"There were errors performing the requested action:\n"
								+ e.getMessage(), "Error");
			}

		}

		return !isNotSetup();
	}

	public String getPath() {
		return verifytapath;
	}

	public String getVersion() {
		String result = null;

		if (!isNotSetup()) {
			String[] commands;
			commands = new String[] { verifytapath, "-v" };

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

	public boolean isCorrectVersion() {
		if (isNotSetup()) {
			return false;
		}

		String versionAsString = getVersion();

		if (versionAsString == null) {
			messenger
					.displayErrorMessage(
							"The program can not be verified as being verifyta.\n"
									+ "The verifyta path will be reset. Please try again, "
									+ "to manually set the verifyta path.",
							"Verifyta Error");
			resetVerifyta();
			return false;
		} else {

			String[] version = getVersion().split("\\.");
			String[] targetversion = Pipe.verifytaMinRev.split("\\.");

			for (int i = 0; i < targetversion.length; i++) {
				if (version.length < i + 1) version[i] = "0";
				int diff = Integer.parseInt(version[i]) - Integer.parseInt(targetversion[i]);
				if (diff > 0) {
					break;
				} else if (diff < 0) {
					return false;
				}
			}

		}

		return true;
	}

	private void resetVerifyta() {
		verifytapath = null;
		Preferences.getInstance().setVerifytaLocation(verifytapath);
	}

	private boolean isNotSetup() {
		return verifytapath == null || verifytapath.equals("") || !(new File(verifytapath)).exists();
	}

	private String readVersionNumberFrom(InputStream stream) {
		String result = null;
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(stream));

		String versioninfo = null;
		try {
			versioninfo = bufferedReader.readLine();
			while(bufferedReader.readLine() != null){}	// Empty buffer
		} catch (IOException e) {
			result = null;
		}

		Pattern pattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+)");
		Matcher m = pattern.matcher(versioninfo);
		m.find();
		result = m.group(1);
		return result;
	}

	public static boolean trySetup() {

		try {
			String verifyta = null;

			//Get from evn (overwrite other values)
			verifyta = System.getenv("verifyta");
			if (verifyta != null && !verifyta.equals("")) {
				if (new File(verifyta).exists()){
					verifytapath = verifyta;
					Verifyta v = new Verifyta(new FileFinder(), new MessengerImpl());
					if(v.isCorrectVersion()){
						return true;
					}else{
						verifyta = null;
						verifytapath = null;
					}
				}
			}

			verifyta = Preferences.getInstance().getVerifytaLocation();
			if (verifyta != null && !verifyta.equals("")) {
				verifytapath = verifyta;
				Verifyta v = new Verifyta(new FileFinder(), new MessengerImpl());
				if(v.isCorrectVersion()){
					return true;
				}else{
					verifyta = null;
					v.resetVerifyta();
				}
			}

			return false;
		} catch (Exception e) {
			return false;
		}
	}

	private String createArgumentString(String modelFile, String queryFile, VerificationOptions options) {
		StringBuffer buffer = new StringBuffer(options.toString());
		buffer.append(' ');
		buffer.append(modelFile);
		buffer.append(' ');
		buffer.append(queryFile);

		return buffer.toString();
	}

	public void kill() {
		if (runner != null) {
			runner.kill();
		}
	}
	
	public void setPath(String path) throws IllegalArgumentException{
		ExecutabilityChecker.check(path);
		String oldPath = verifytapath;
		verifytapath = path; 
		Preferences.getInstance().setVerifytaLocation(verifytapath);
		if(!isCorrectVersion()){
			messenger
			.displayErrorMessage(
					"The specified version of the file verifyta is too old.\n\n"
							+ "Get the latest development version of UPPAAL from \n"
							+ "www.uppaal.org.", "Verifyta Error");
			verifytapath = oldPath;
			Preferences.getInstance().setVerifytaLocation(oldPath);
		}
	}
	
	public boolean supportsStats(){
		return false;
	}
	
	public String getStatsExplanation(){
		return "";
	}
	
	@Override
	public boolean supportsModel(TimedArcPetriNet model, VerificationOptions options) {
		//The combi translation supports all models.
		return true;
	}

	@Override
	public boolean supportsQuery(TimedArcPetriNet model, TAPNQuery query,
			VerificationOptions options) {
		
		//The only translation to UPPAAL that supports these are Combi
		if((model.hasUrgentTransitions() || model.hasWeights()) && ((VerifytaOptions)options).getReduction() != ReductionOption.COMBI){
			return false;
		}
		
		if(query.hasDeadlock()){
				// Only broadcast translations and combi supports deadlock.
				if(((VerifytaOptions) options).getReduction() != ReductionOption.BROADCAST &&
					((VerifytaOptions) options).getReduction() != ReductionOption.DEGREE2BROADCAST &&
					((VerifytaOptions)options).getReduction() != ReductionOption.COMBI){
						return false;
				}
				
				// Broadcast translations and combi do not support EG and AF queries that contain a deadlock.
				if(query.getProperty() instanceof TCTLEGNode || 
						query.getProperty() instanceof TCTLAFNode){
					return false;
				}
				
				// No translation supports inhibitor arcs with deadlock queries
				if(model.hasInhibitorArcs()){
					return false;
				}
		}
		
		if (((VerifytaOptions) options).traceOption() == TraceOption.SOME && ((VerifytaOptions) options).symmetry() == true) {
			return false;
		}
		
		return true;
	}

	public VerificationResult<TimedArcPetriNetTrace> verify(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, TAPNQuery query) throws Exception {
		
		if(!model.value1().isDegree2() && new HasDeadlockVisitor().hasDeadLock(query.getProperty()))
			throw new UnsupportedModelException("\nBecause the query contains a deadlock proposition, the selected engine\nsupports only nets where transitions have at most two input places.");
		
		if(!supportsModel(model.value1(), options))
			throw new UnsupportedModelException("Verifyta does not support the given model.");
		
		if(!supportsQuery(model.value1(), query, options))
			throw new UnsupportedQueryException("Verifyta does not support the given query.");
		
		UppaalExporter exporter = new UppaalExporter();
		ExportedModel exportedModel = exporter.export(model.value1(), query, ((VerifytaOptions) options).getReduction(), ((VerifytaOptions) options).symmetry());

		if (exportedModel == null) {
			messenger.displayErrorMessage("There was an error exporting the model");
		}

		return verify(options, model.value1(), exportedModel, query);
	}

	private VerificationResult<TimedArcPetriNetTrace> verify(VerificationOptions options, TimedArcPetriNet model, ExportedModel exportedModel, TAPNQuery query) {
		runner = new ProcessRunner(verifytapath, createArgumentString(exportedModel.modelFile(), exportedModel.queryFile(), options));
		runner.run();

		if (runner.error()) {
			return null;
		} else {
			String errorOutput = readOutput(runner.errorOutput());
			String standardOutput = readOutput(runner.standardOutput());

			QueryResult queryResult = parseQueryResult(standardOutput, query);

			if (queryResult == null) {
				return new VerificationResult<TimedArcPetriNetTrace>(errorOutput + System.getProperty("line.separator") + standardOutput, runner.getRunningTime());
			} else {
				TimedArcPetriNetTrace tapnTrace = parseTrace(errorOutput, options, model, exportedModel, query, queryResult);
				return new VerificationResult<TimedArcPetriNetTrace>(queryResult, tapnTrace, runner.getRunningTime());
			}
		}
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

	private QueryResult parseQueryResult(String output, TAPNQuery query) {
		VerifytaOutputParser outputParser = new VerifytaOutputParser(query);
		QueryResult queryResult = outputParser.parseOutput(output);
		return queryResult;
	}

	private TimedArcPetriNetTrace parseTrace(String output, VerificationOptions options, TimedArcPetriNet model, ExportedModel exportedModel, TAPNQuery query, QueryResult queryResult) {
		TimedArcPetriNetTrace tapnTrace = null;

		VerifytaTraceParser traceParser = new VerifytaTraceParser();
		UppaalTrace trace = traceParser.parseTrace(new BufferedReader(new StringReader(output)), (VerifytaOptions) options);

		if (trace == null) {
			if (((VerifytaOptions) options).traceOption() != TraceOption.NONE) {
				if((query.getProperty() instanceof TCTLEFNode && !queryResult.isQuerySatisfied()) || (query.getProperty() instanceof TCTLAGNode && queryResult.isQuerySatisfied()) || 
				   (query.getProperty() instanceof TCTLEGNode && !queryResult.isQuerySatisfied()) || (query.getProperty() instanceof TCTLAFNode && queryResult.isQuerySatisfied()))
					return null;
				else
					messenger.displayErrorMessage("UPPAAL could not generate the requested trace for the model. Try another trace option.");
			}
		} else {
			if (exportedModel.namingScheme() == null) { // TODO: get rid of
				messenger.displayErrorMessage("Traces are currently not supported on the chosen translation");
			} else {
				tapnTrace = interpretTimedTrace(model, exportedModel, trace);
			}
		}
		return tapnTrace;
	}

	private TimedArcPetriNetTrace interpretTimedTrace(TimedArcPetriNet model, ExportedModel exportedModel, UppaalTrace trace) {
		VerifytaTraceInterpreter traceIntepreter = new VerifytaTraceInterpreter(model, exportedModel.namingScheme());
		return traceIntepreter.interpretTrace(trace);
	}

	public static void reset() {
		verifytapath = "";
		Preferences.getInstance().setVerifytaLocation(null);
	}

	public boolean useDiscreteSemantics() {
		return false;
	}
}
