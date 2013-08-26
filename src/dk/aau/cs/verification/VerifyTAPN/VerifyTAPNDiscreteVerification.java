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
import pipe.gui.FileFinderImpl;
import pipe.gui.MessengerImpl;
import pipe.gui.Pipe;
import pipe.gui.widgets.InclusionPlaces;
import pipe.gui.widgets.InclusionPlaces.InclusionPlacesOption;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.visitors.HasDeadlockVisitor;
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
import dk.aau.cs.verification.QueryType;
import dk.aau.cs.verification.Stats;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;

public class VerifyTAPNDiscreteVerification implements ModelChecker{
	
	private static final String NEED_TO_LOCATE_VERIFYDTAPN_MSG = "TAPAAL needs to know the location of the file verifydtapn.\n\n"
			+ "Verifydtapn is a part of the TAPAAL distribution and it is\n"
			+ "normally located in the directory lib.";
		
		protected static String verifydtapnpath = "";
		
		private FileFinder fileFinder;
		private Messenger messenger;

		private ProcessRunner runner;
		
		public VerifyTAPNDiscreteVerification(FileFinder fileFinder, Messenger messenger) {
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
			return verifydtapnpath;
		}

		public String getVersion() { // atm. any version of VerifyTAPN will do
			String result = null;

			if (!isNotSetup()) {
				String[] commands;
				commands = new String[] { verifydtapnpath, "-v" };

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

			Pattern pattern = Pattern.compile("^VerifyDTAPN (\\d+\\.\\d+\\.\\d+)$");
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
				messenger.displayErrorMessage("The engine verifydtapn is not executable.\n"
						+ "The verifydtapn path will be reset. Please try again, "
						+ "to manually set the verifydtapn path.", "Verifydtapn Error");
				resetVerifytapn();
				return false;
			}
			
			String[] version = getVersion().split("\\.");
			String[] targetversion = Pipe.verifydtapnMinRev.split("\\.");
			
			for(int i = 0; i < targetversion.length; i++){
				if(version.length < i+1)	version[i] = "0";
				int diff = Integer.parseInt(version[i]) - Integer.parseInt(targetversion[i]);
				if(diff > 0){
					break;
				}else if(diff < 0){
					return false;
				}
			}
			
			return true;
		}

		private void resetVerifytapn() {
			verifydtapnpath = null;	
			Preferences.getInstance().setVerifydtapnLocation(null);
		}


		public void kill() {
			if (runner != null) {
				runner.kill();
			}
		}
		
		public void setPath(String path) throws IllegalArgumentException{
			ExecutabilityChecker.check(path);
			String oldPath = verifydtapnpath;
			verifydtapnpath = path;
			Preferences.getInstance().setVerifydtapnLocation(path);
			if(!isCorrectVersion()){
				messenger
				.displayErrorMessage(
						"The specified version of the file verifydtapn is too old.", "Verifydtapn Error");
				verifydtapnpath = oldPath;
				Preferences.getInstance().setVerifydtapnLocation(oldPath);
			}
		}

		public boolean setup() {
			if (isNotSetup()) {
				messenger.displayInfoMessage(NEED_TO_LOCATE_VERIFYDTAPN_MSG, "Locate verifydtapn");

				try {
					File file = fileFinder.ShowFileBrowserDialog("Verifydtapn", "",System.getProperty("user.home"));
					if(file != null){
						if(file.getName().matches("^verifydtapn.*(?:\\.exe)?$")){
							setPath(file.getAbsolutePath());
						}else{
							messenger.displayErrorMessage("The selected executable does not seem to be verifydtapn.");
						}
					}

				} catch (Exception e) {
					messenger.displayErrorMessage("There were errors performing the requested action:\n" + e.getMessage(), "Error");
				}

			}

			return !isNotSetup();
		}

		private boolean isNotSetup() {
			return verifydtapnpath == null || verifydtapnpath.equals("") || !(new File(verifydtapnpath).exists());
		}
		
		public static boolean trySetup() {

			String verifydtapn = null;

			//If env is set, it overwrites the value
			verifydtapn = System.getenv("verifydtapn");
			if (verifydtapn != null && !verifydtapn.isEmpty()) {
				if (new File(verifydtapn).exists()){
					verifydtapnpath = verifydtapn;
					VerifyTAPNDiscreteVerification v = new VerifyTAPNDiscreteVerification(new FileFinderImpl(), new MessengerImpl());
					if(v.isCorrectVersion()){
						return true;
					}else{
						verifydtapn = null;
						verifydtapnpath = null;
					}
				}
			}

			//If pref is set
			verifydtapn = Preferences.getInstance().getVerifydtapnLocation();
			if (verifydtapn != null && !verifydtapn.isEmpty()) {
				verifydtapnpath = verifydtapn;
				return true;
			}

			//Search the installdir for verifytapn
			File installdir = TAPAAL.getInstallDir();

			String[] paths = {"/bin/verifydtapn", "/bin/verifydtapn64", "/bin/verifydtapn.exe", "/bin/verifydtapn64.exe"};
			for (String s : paths) {
				File verifydtapnfile = new File(installdir + s);

				if (verifydtapnfile.exists()){

					verifydtapnpath = verifydtapnfile.getAbsolutePath();
					VerifyTAPNDiscreteVerification v = new VerifyTAPNDiscreteVerification(new FileFinderImpl(), new MessengerImpl());
					if(v.isCorrectVersion()){
						return true;
					}else{
						verifydtapn = null;
						verifydtapnpath = null;
					}

				}
			}



			return false;
		}

		public VerificationResult<TimedArcPetriNetTrace> verify(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, TAPNQuery query) throws Exception {	
			if(!supportsModel(model.value1()))
				throw new UnsupportedModelException("Verifydtapn does not support the given model.");
			
                        if(!supportsQuery(model.value1(), query, options)){
                            throw new UnsupportedQueryException("Verifydtapn does not support the given query-option combination. ");
                        }
			//if(!supportsQuery(model.value1(), query, options))
				//throw new UnsupportedQueryException("Verifydtapn does not support the given query.");
			
//			if(((VerifyTAPNOptions)options).discreteInclusion() && !isQueryUpwardClosed(query))
//				throw new UnsupportedQueryException("Discrete inclusion check only supports upward closed queries.");
			
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
			runner = new ProcessRunner(verifydtapnpath, createArgumentString(exportedModel.modelFile(), exportedModel.queryFile(), options));
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
			if (((VerifyTAPNOptions) options).trace() != TraceOption.SOME) return null;
			
			VerifyTAPNTraceParser traceParser = new VerifyTAPNTraceParser(model.value1());
			TimedArcPetriNetTrace trace = traceParser.parseTrace(new BufferedReader(new StringReader(output)));
			
			if (trace == null) {
				if (((VerifyTAPNOptions) options).trace() != TraceOption.NONE) {
					if((query.getProperty() instanceof TCTLEFNode && !queryResult.isQuerySatisfied()) || 
							(query.getProperty() instanceof TCTLAGNode && queryResult.isQuerySatisfied()) || 
							(query.getProperty() instanceof TCTLEGNode && !queryResult.isQuerySatisfied()) || 
							(query.getProperty() instanceof TCTLAFNode && queryResult.isQuerySatisfied())){
						return null;
					} else{
						messenger.displayErrorMessage("Verifydtapn cannot generate the requested trace for the model. Try another trace option.");
					}
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
		
		
		boolean supportsModel(TimedArcPetriNet model) {
			return model.isNonStrict();
		}
		
		boolean supportsQuery(TimedArcPetriNet model, TAPNQuery query, VerificationOptions options) {
                        // if liveness, has deadlock proposition and uses timedarts, it is not supported
			if((query.getProperty() instanceof TCTLEGNode || query.getProperty() instanceof TCTLAFNode)
                                && new HasDeadlockVisitor().hasDeadLock(query.getProperty()) 
                                && ((VerifyDTAPNOptions)options).timeDarts()){
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
			verifydtapnpath = "";
			Preferences.getInstance().setVerifydtapnLocation(null);
			//Set the detault
			trySetup();
		}


}
