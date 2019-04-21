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
import pipe.dataLayer.TAPNQuery.QueryCategory;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.FileFinder;
import pipe.gui.MessengerImpl;
import pipe.gui.Pipe;
import pipe.gui.widgets.InclusionPlaces;
import pipe.gui.widgets.InclusionPlaces.InclusionPlacesOption;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.TCTLAFNode;
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

public class VerifyPN implements ModelChecker{
	
	private static final String NEED_TO_LOCATE_verifypn_MSG = "TAPAAL needs to know the location of the file verifypn.\n\n"
			+ "Verifypn is a part of the TAPAAL distribution and it is\n"
			+ "normally located in the directory lib.";
		
		protected static String verifypnpath = "";
		
		private FileFinder fileFinder;
		private Messenger messenger;

		private ProcessRunner runner;
		private boolean ctlOutput = false;
		
		public VerifyPN(FileFinder fileFinder, Messenger messenger) {
			this.fileFinder = fileFinder;
			this.messenger = messenger;
		}
		
		public boolean supportsStats(){
			return true;
		}
		
		public String getStatsExplanation(){
			StringBuffer buffer = new StringBuffer("<html>");
			if(ctlOutput){
				buffer.append("The number of configurations, markings and hyper-edges explored during<br />" +
						"the on-the-fly generation of the dependency graph for the given net and<br />" +
						"query before a conclusive answer was reached.");
				buffer.append("</html>");
			} else {
				buffer.append("<b>Discovered markings:</b> The number of found markings (each<br />");
				buffer.append("time a successor is calculated, this number is incremented)<br/>");
				buffer.append("<br/>");
				buffer.append("<b>Explored markings:</b> The number of markings taken out<br/>");
				buffer.append("of the waiting list during the search.<br />");
				buffer.append("<br/>");
				buffer.append("<b>Stored markings:</b> The number of markings found in the<br />");
				buffer.append("passed/waiting list at the end of verification.<br />");
				buffer.append("</html>");
			}
			return buffer.toString();
		}

		public String getPath() {
			return verifypnpath;
		}

		public String getVersion() {
			String result = null;

			if (!isNotSetup()) {
				String[] commands;
				commands = new String[] { verifypnpath, "-v" };

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

			Pattern pattern = Pattern.compile("^VerifyPN.*(\\d+\\.\\d+\\.\\d+).*$", Pattern.DOTALL);
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
			if (!file.canExecute()) {
				messenger.displayErrorMessage("The engine verifypn is not executable.\n"
						+ "The verifypn path will be reset. Please try again, "
						+ "to manually set the verifypn path.", "VerifyPN Error");
				resetVerifypn();
				return false;
			}

			if (getVersion() != null) {

				String[] version = getVersion().split("\\.");
				String[] targetversion = Pipe.verifypnMinRev.split("\\.");

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

		private void resetVerifypn() {
			verifypnpath = null;	
			Preferences.getInstance().setVerifypnLocation(null);
		}


		public void kill() {
			if (runner != null) {
				runner.kill();
			}
		}
		
		public void setPath(String path) throws IllegalArgumentException {
			ExecutabilityChecker.check(path);
			String oldPath = verifypnpath;
			verifypnpath = path;
			Preferences.getInstance().setVerifypnLocation(path);
			if(!isCorrectVersion()){
				messenger
				.displayErrorMessage(
						"The specified version of the file verifypn is too old.", "Verifypn Error");
				verifypnpath = oldPath;
				Preferences.getInstance().setVerifypnLocation(oldPath);
			}
		}

		public boolean setup() {
			if (isNotSetup()) {
				messenger.displayInfoMessage(NEED_TO_LOCATE_verifypn_MSG, "Locate verifypn");

				try {
					File file = fileFinder.ShowFileBrowserDialog("Verifypn", "",System.getProperty("user.home"));
					if(file != null){
						if(file.getName().matches("^verifypn.*(?:\\.exe)?$")){
							setPath(file.getAbsolutePath());
						}else{
							messenger.displayErrorMessage("The selected executable does not seem to be verifypn.");
						}
					}

				} catch (Exception e) {
					messenger.displayErrorMessage("There were errors performing the requested action:\n" + e.getMessage(), "Error");
				}

			}

			return !isNotSetup();
		}

		private boolean isNotSetup() {
			return verifypnpath == null || verifypnpath.equals("") || !(new File(verifypnpath).exists());
		}
		
		public static boolean trySetup() {

				String verifypn = null;

				//If env is set, it overwrites the value
				verifypn = System.getenv("verifypn");
				if (verifypn != null && !verifypn.isEmpty()) {
					if (new File(verifypn).exists()){
						verifypnpath = verifypn;
						VerifyPN v = new VerifyPN(new FileFinder(), new MessengerImpl());
						if(v.isCorrectVersion()){
							return true;
						}else{
							verifypn = null;
							verifypnpath = null;
						}
					}
				}

				//If pref is set
				verifypn = Preferences.getInstance().getVerifypnLocation();
				if (verifypn != null && !verifypn.isEmpty()) {
					verifypnpath = verifypn;
					VerifyPN v = new VerifyPN(new FileFinder(), new MessengerImpl());
					if(v.isCorrectVersion()){
						return true;
					}else{
						verifypn = null;
						verifypnpath = null;
					}
				}

				//Search the installdir for verifytapn
				File installdir = TAPAAL.getInstallDir();

				String[] paths = {"/bin/verifypn", "/bin/verifypn64", "/bin/verifypn.exe", "/bin/verifypn64.exe"};
				for (String s : paths) {
					File verifypnfile = new File(installdir + s);

					if (verifypnfile.exists()){

						verifypnpath = verifypnfile.getAbsolutePath();
						VerifyPN v = new VerifyPN(new FileFinder(), new MessengerImpl());
						if(v.isCorrectVersion()){
							return true;
						}else{
							verifypn = null;
							verifypnpath = null;
						}

					}
				}


				return false;

		}

		public VerificationResult<TimedArcPetriNetTrace> verify(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, TAPNQuery query) throws Exception {	
			if(!supportsModel(model.value1(), options))
				throw new UnsupportedModelException("Verifypn does not support the given model.");
			
			if(!supportsQuery(model.value1(), query, options))
				throw new UnsupportedQueryException("Verifypn does not support the given query.");
			
//			if(((VerifyTAPNOptions)options).discreteInclusion() && !isQueryUpwardClosed(query))
//				throw new UnsupportedQueryException("Discrete inclusion check only supports upward closed queries.");
			
			if(((VerifyTAPNOptions)options).discreteInclusion()) mapDiscreteInclusionPlacesToNewNames(options, model);
			
			VerifyPNExporter exporter = new VerifyPNExporter();
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
			runner = new ProcessRunner(verifypnpath, createArgumentString(exportedModel.modelFile(), exportedModel.queryFile(), options));
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
					ctlOutput = queryResult.value1().isCTL;
					boolean approximationResult = queryResult.value2().discoveredStates() == 0;	// Result is from over-approximation
					TimedArcPetriNetTrace tapnTrace = parseTrace(errorOutput, options, model, exportedModel, query, queryResult.value1());
					return new VerificationResult<TimedArcPetriNetTrace>(queryResult.value1(), tapnTrace, runner.getRunningTime(), queryResult.value2(), approximationResult);
				}
			}
		}

		private TimedArcPetriNetTrace parseTrace(String output, VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, ExportedVerifyTAPNModel exportedModel, TAPNQuery query, QueryResult queryResult) {
			if (((VerifyTAPNOptions) options).trace() == TraceOption.NONE) return null;
			
			VerifyTAPNTraceParser traceParser = new VerifyTAPNTraceParser(model.value1());
			TimedArcPetriNetTrace trace = traceParser.parseTrace(new BufferedReader(new StringReader(output)));

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
			Tuple<QueryResult, Stats> result = null;
			if (output.contains("Processed N. Edges:")){
				VerifyPNCTLOutputParser outputParser = new VerifyPNCTLOutputParser(totalTokens, extraTokens, query);
				result = outputParser.parseOutput(output);
				result.value1().isCTL=true;
			} else {
				VerifyTAPNOutputParser outputParser = new VerifyPNOutputParser(totalTokens, extraTokens, query);
				result = outputParser.parseOutput(output);
			}
			return result;
		}
		
		
		public boolean supportsModel(TimedArcPetriNet model, VerificationOptions options) {
			return model.isUntimed() || options.searchOption() == SearchOption.OVERAPPROXIMATE;
		}
	
		public boolean supportsQuery(TimedArcPetriNet model, TAPNQuery query, VerificationOptions options) {
			if(query.getCategory() == QueryCategory.CTL){
				return true;
			}
			if(query.getProperty() instanceof TCTLEGNode || query.getProperty() instanceof TCTLAFNode) {
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
			verifypnpath = "";
			Preferences.getInstance().setVerifypnLocation(null);
			//Set the detault
			trySetup();
		}

		@Override
		public String toString() {
			return "verifypn";
		}

		@Override
		public boolean useDiscreteSemantics() {
			// TODO Auto-generated method stub
			return false;
		}
}
