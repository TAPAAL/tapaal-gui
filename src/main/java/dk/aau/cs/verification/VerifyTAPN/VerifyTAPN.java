package dk.aau.cs.verification.VerifyTAPN;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.TapnEngineXmlLoader;
import dk.aau.cs.util.FormatException;
import dk.aau.cs.verification.*;
import net.tapaal.Preferences;
import net.tapaal.TAPAAL;
import net.tapaal.gui.petrinet.TAPNLens;
import net.tapaal.gui.petrinet.verification.UnfoldNet;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.dataLayer.DataLayer;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;
import pipe.gui.FileFinder;
import pipe.gui.MessengerImpl;
import pipe.gui.Constants;
import net.tapaal.gui.petrinet.verification.InclusionPlaces;
import net.tapaal.gui.petrinet.verification.InclusionPlaces.InclusionPlacesOption;
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
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;

import javax.swing.*;

public class VerifyTAPN implements ModelChecker {
	private static final String NEED_TO_LOCATE_VERIFYTAPN_MSG = "TAPAAL needs to know the location of the file verifytapn.\n\n"
		+ "Verifytapn is a part of the TAPAAL distribution and it is\n"
		+ "normally located in the directory lib.";

    private static final String VERIFYTAPN_VERSION_PATTERN = "^VerifyTAPN (\\d+\\.\\d+\\.\\d+)$";

	private static String verifytapnpath = "";
	
	protected final FileFinder fileFinder;
	protected final Messenger messenger;

	protected ProcessRunner runner;

	public VerifyTAPN(FileFinder fileFinder, Messenger messenger) {
		this.fileFinder = fileFinder;
		this.messenger = messenger;
	}
	
	public boolean supportsStats(){
		return true;
	}

    public String[] getStatsExplanations(){
        String[] explanations = new String[3];
        explanations[0] = "The number of found markings (each time a successor is calculated, this number is incremented)";
        explanations[1] = "The number of markings taken out of the waiting list during the search.";
        explanations[2] = "The number of markings found in the passed/waiting list at the end of verification.";
        return explanations;
    }

	public String getPath() {
		return verifytapnpath;
	}
    public String getVersion() {
        return EngineHelperFunctions.getVersion(new String[]{verifytapnpath, "-v"}, VERIFYTAPN_VERSION_PATTERN);
    }
    public String getVersion(String path) {
        return EngineHelperFunctions.getVersion(new String[]{path, "-v"}, VERIFYTAPN_VERSION_PATTERN);
    }

    public boolean isCorrectVersion() {
        return isCorrectVersion(getPath());
    }

    public boolean isCorrectVersion(String path) {

        if ((path == null || path.isBlank() || !(new File(path).exists()))) {
            return false;
        }

        File file = new File(path);
        if (!file.canExecute()) {
            messenger.displayErrorMessage("The engine verifytapn is not executable.\n"
                + "The verifytapn path will be reset. Please try again, "
                + "to manually set the verifytapn path.", "Verifytapn Error");
            return false;
        }

        String version = getVersion(path);

        if (version != null) {
            return EngineHelperFunctions.versionIsEqualOrGreater(version, Constants.VERIFYTAPN_MIN_REV);
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
        if (isCorrectVersion(path)) {
            verifytapnpath = path;
            Preferences.getInstance().setVerifytapnLocation(path);
        } else {
            messenger.displayErrorMessage(
                "The specified version of the file verifytapn is too old.", "Verifytapn Error"
            );
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
			String verifytapn;

			// If env is set, it overwrites the value
			verifytapn = System.getenv("verifytapn");
			if (verifytapn != null && !verifytapn.isEmpty()) {
				if (new File(verifytapn).exists()) {
					verifytapnpath = verifytapn;
					VerifyTAPN v = new VerifyTAPN(new FileFinder(), new MessengerImpl());
					if (v.isCorrectVersion()) {
						return true;
					} else {
						verifytapn = null;
						verifytapnpath = null;
					}
				}
			}

			// If pref is set
			verifytapn = Preferences.getInstance().getVerifytapnLocation();
			if (verifytapn != null && !verifytapn.isEmpty()) {
				verifytapnpath = verifytapn;
				VerifyTAPN v = new VerifyTAPN(new FileFinder(), new MessengerImpl());
				if (v.isCorrectVersion()) {
					return true;
				} else {
					verifytapn = null;
					verifytapnpath = null;
				}
			}

			// Search the installdir for verifytapn
			File installdir = TAPAAL.getInstallDir();

			String[] paths = {"/bin/verifytapn", "/bin/verifytapn64", "/bin/verifytapn.exe", "/bin/verifytapn64.exe"};
			for (String s : paths) {
				File verifytapnfile = new File(installdir + s);

				if (verifytapnfile.exists()) {

					verifytapnpath = verifytapnfile.getAbsolutePath();
					VerifyTAPN v = new VerifyTAPN(new FileFinder(), new MessengerImpl());
					if (v.isCorrectVersion()) {
						return true;
					} else {
						verifytapn = null;
						verifytapnpath = null;
					}

				}
			}
			return false;

	}

	public VerificationResult<TimedArcPetriNetTrace> verify(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, TAPNQuery query, DataLayer guiModel, net.tapaal.gui.petrinet.verification.TAPNQuery dataLayerQuery, TAPNLens lens) throws Exception {
		if(!supportsModel(model.value1(), options)) {
            throw new UnsupportedModelException("Verifytapn does not support the given model.");
        }
		
		if(!supportsQuery(model.value1(), query, options)) {
            throw new UnsupportedQueryException("Verifytapn does not support the given query.");
        }
		
		if(((VerifyTAPNOptions)options).discreteInclusion()) mapDiscreteInclusionPlacesToNewNames(options, model);

        ExportedVerifyTAPNModel exportedModel;
        if ((lens != null && lens.isColored() || model.value1().parentNetwork().isColored())) {
            VerifyTAPNExporter exporter = new VerifyTACPNExporter();
            exportedModel = exporter.export(model.value1(), query, lens, model.value2(), guiModel, dataLayerQuery);
        } else {
            VerifyTAPNExporter exporter = new VerifyTAPNExporter();
            exportedModel = exporter.export(model.value1(), query, lens, model.value2(), guiModel, dataLayerQuery);
        }

        if (exportedModel == null) {
            messenger.displayErrorMessage("There was an error exporting the model");
        }

		return verify(options, model, exportedModel, query, dataLayerQuery, lens);
	}

    @Override
    public VerificationResult<TimedArcPetriNetTrace> verifyManually(String options, Tuple<TimedArcPetriNet, NameMapping> model, TAPNQuery query, net.tapaal.gui.petrinet.verification.TAPNQuery dataLayerQuery, TAPNLens lens) throws Exception {
        return null;
    }

    protected void mapDiscreteInclusionPlacesToNewNames(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model) {
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

	protected VerificationResult<TimedArcPetriNetTrace> verify(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, ExportedVerifyTAPNModel exportedModel, TAPNQuery query, net.tapaal.gui.petrinet.verification.TAPNQuery dataLayerQuery,  TAPNLens lens) {
		((VerifyTAPNOptions)options).setTokensInModel(model.value1().marking().size()); // TODO: get rid of me
		runner = new ProcessRunner(verifytapnpath, createArgumentString(exportedModel.modelFile(), exportedModel.queryFile(), options));
		runner.run();

		if (runner.error()) {
			return null;
		} else {
            PetriNetTab newTab = null;
            String errorOutput = readOutput(runner.errorOutput());
			String standardOutput = readOutput(runner.standardOutput());

			Tuple<QueryResult, Stats> queryResult = parseQueryResult(standardOutput, model.value1().marking().size() + query.getExtraTokens(), query.getExtraTokens(), query);
			if (queryResult == null || queryResult.value1() == null) {
				return new VerificationResult<TimedArcPetriNetTrace>(errorOutput + System.getProperty("line.separator") + standardOutput, runner.getRunningTime());
			} else {
                TimedArcPetriNetTrace tapnTrace = null;

                boolean isColored = (lens != null && lens.isColored() || model.value1().parentNetwork().isColored());
                boolean showTrace = ((query.getProperty() instanceof TCTLEFNode && queryResult.value1().isQuerySatisfied()) ||
                    (query.getProperty() instanceof TCTLAGNode && !queryResult.value1().isQuerySatisfied()) ||
                    (query.getProperty() instanceof TCTLEGNode && queryResult.value1().isQuerySatisfied()) ||
                    (query.getProperty() instanceof TCTLAFNode && !queryResult.value1().isQuerySatisfied()));

                if(options.traceOption() != TraceOption.NONE && isColored && showTrace) {
                    TapnEngineXmlLoader tapnLoader = new TapnEngineXmlLoader();
                    File fileOut = new File(options.unfoldedModelPath());
                    File queriesOut = new File(options.unfoldedQueriesPath());
                    try {
                        LoadedModel loadedModel = tapnLoader.load(fileOut);
                        TAPNComposer newComposer = new TAPNComposer(new MessengerImpl(), true);
                        model = newComposer.transformModel(loadedModel.network());

                        if (queryResult != null && queryResult.value1() != null) {
                            tapnTrace = parseTrace(!errorOutput.contains("Trace:") ? errorOutput : (errorOutput.split("Trace:")[1]), options, model, exportedModel, query, queryResult.value1());
                        }

                        if (tapnTrace != null) {
                            newTab = new PetriNetTab(loadedModel.network(), loadedModel.templates(), loadedModel.queries(), new TAPNLens(lens.isTimed(), lens.isGame(), false));

                            //The query being verified should be the only query
                            for (net.tapaal.gui.petrinet.verification.TAPNQuery loadedQuery : UnfoldNet.getQueries(queriesOut, loadedModel.network(), query.getCategory())) {
                                newTab.setInitialName(loadedQuery.getName() + " - unfolded");
                                loadedQuery.copyOptions(dataLayerQuery);
                                newTab.addQuery(loadedQuery);
                            }
                        }

                    } catch (ThreadDeath | Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                if (tapnTrace == null) {
                    tapnTrace = parseTrace(!errorOutput.contains("Trace:") ? errorOutput : (errorOutput.split("Trace:")[1]), options, model, exportedModel, query, queryResult.value1());
                }
				//return new VerificationResult<TimedArcPetriNetTrace>(queryResult.value1(), tapnTrace, runner.getRunningTime(), queryResult.value2(), standardOutput);
                return new VerificationResult<TimedArcPetriNetTrace>(queryResult.value1(), tapnTrace, null, runner.getRunningTime(), queryResult.value2(), false, standardOutput + "\n\n" + errorOutput, model, newTab);
			}
		}
	}

	private TimedArcPetriNetTrace parseTrace(String output, VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, ExportedVerifyTAPNModel exportedModel, TAPNQuery query, QueryResult queryResult) {
		if (((VerifyTAPNOptions) options).trace() == TraceOption.NONE) return null;
		
		VerifyTAPNTraceParser traceParser = new VerifyTAPNTraceParser(model.value1());
		TimedArcPetriNetTrace trace = traceParser.parseTrace(new BufferedReader(new StringReader(output)));

        if (trace == null) {
            if (((VerifyTAPNOptions) options).trace() != TraceOption.NONE) {
                if ((query.getProperty() instanceof TCTLEFNode && !queryResult.isQuerySatisfied()) ||
                    (query.getProperty() instanceof TCTLAGNode && queryResult.isQuerySatisfied()) ||
                    (query.getProperty() instanceof TCTLEGNode && !queryResult.isQuerySatisfied()) ||
                    (query.getProperty() instanceof TCTLAFNode && queryResult.isQuerySatisfied())) {
                    return null;
                }
            }
        }
		return trace;
	}

	private String createArgumentString(String modelFile, String queryFile, VerificationOptions options) {
		StringBuilder buffer = new StringBuilder(options.toString());
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
		StringBuilder buffer = new StringBuilder();
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
		return outputParser.parseOutput(output);
	}
	
	public boolean supportsModel(TimedArcPetriNet model, VerificationOptions options) {
        return !model.hasWeights() && !model.hasUrgentTransitions();
    }
	
	public boolean supportsQuery(TimedArcPetriNet model, TAPNQuery query, VerificationOptions options) {
        return !(query.getProperty() instanceof TCTLEGNode) &&
            !(query.getProperty() instanceof TCTLAFNode) &&
            !query.hasDeadlock();
    }
	
	public static void reset() {
		//Clear value
		verifytapnpath = "";
		Preferences.getInstance().setVerifytapnLocation(null);
		//Set the detault
		trySetup();
	}

    public String getHelpOptions() {
        runner = new ProcessRunner(verifytapnpath, "--help");
        runner.run();

        if (!runner.error()) {
            return readOutput(runner.standardOutput());
        }
        return null;
    }

	@Override
	public String toString() {
		return "verifytapn";
	}
	
	public boolean useDiscreteSemantics() {
		return false;
	}
}
