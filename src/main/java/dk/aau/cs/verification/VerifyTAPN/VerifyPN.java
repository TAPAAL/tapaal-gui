package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.*;
import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.PNMLoader;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.util.FormatException;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;
import dk.aau.cs.verification.*;
import net.tapaal.Preferences;
import net.tapaal.TAPAAL;
import net.tapaal.gui.petrinet.TAPNLens;
import net.tapaal.gui.petrinet.verification.InclusionPlaces;
import net.tapaal.gui.petrinet.verification.InclusionPlaces.InclusionPlacesOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory;
import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;
import net.tapaal.gui.petrinet.verification.UnfoldNet;
import org.jetbrains.annotations.Nullable;
import pipe.gui.Constants;
import pipe.gui.FileFinder;
import pipe.gui.MessengerImpl;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.dataLayer.DataLayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class VerifyPN implements ModelChecker {

    private static final String NEED_TO_LOCATE_verifypn_MSG = "TAPAAL needs to know the location of the file verifypn.\n\n"
        + "Verifypn is a part of the TAPAAL distribution and it is\n"
        + "normally located in the directory lib.";

    public static final String VERIFYPN_EXE_PATTERN = "^verifypn.*(?:\\.exe)?$";
    private static final String VERIFYPN_VERSION_PATTERN = "^VerifyPN.*(\\d+\\.\\d+\\.\\d+).*$";

    protected static String verifypnpath = "";

    private final FileFinder fileFinder;
    private final Messenger messenger;

    private ProcessRunner runner;
    private boolean ctlOutput = false;

    public VerifyPN(FileFinder fileFinder, Messenger messenger) {
        this.fileFinder = fileFinder;
        this.messenger = messenger;
    }

    public boolean supportsStats() {
        return true;
    }

    public String[] getStatsExplanations() {
        String[] explanations = new String[3];
        if (ctlOutput) {
            explanations[0] = "The number of configurations explored during the on-the-fly generation of the dependency graph for the given net and query before a conclusive answer was reached.";
            explanations[1] = "The number of markings explored during the on-the-fly generation of the dependency graph for the given net and query before a conclusive answer was reached.";
            explanations[2] = "The number of hyper-edges explored during the on-the-fly generation of the dependency graph for the given net and query before a conclusive answer was reached.";
        } else {
            explanations[0] = "The number of found markings (each time a successor is calculated, this number is incremented)";
            explanations[1] = "The number of markings taken out of the waiting list during the search.";
            explanations[2] = "The number of markings found in the passed/waiting list at the end of verification.";
        }
        return explanations;
    }

    public String getPath() {
        return verifypnpath;
    }

    public String getVersion() {
        return EngineHelperFunctions.getVersion(new String[]{verifypnpath, "-v"}, VERIFYPN_VERSION_PATTERN);
    }
    public String getVersion(String path) {
        return EngineHelperFunctions.getVersion(new String[]{path, "-v"}, VERIFYPN_VERSION_PATTERN);
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
            messenger.displayErrorMessage("The engine verifypn is not executable.\n"
                + "The verifypn path will be reset. Please try again, "
                + "to manually set the verifypn path.", "VerifyPN Error");
            return false;
        }

        String version = getVersion(path);

        if (version != null) {
            return EngineHelperFunctions.versionIsEqualOrGreater(version, Constants.VERIFYPN_MIN_REV);
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
        if (isCorrectVersion(path)) {
            verifypnpath = path;
            Preferences.getInstance().setVerifypnLocation(path);
        } else {
            messenger.displayErrorMessage(
                    "The specified version of the file verifypn is too old, or not recognized as verifypn", "Verifypn Error"
            );
        }
    }

    public boolean setup() {
        if (isNotSetup()) {
            messenger.displayInfoMessage(NEED_TO_LOCATE_verifypn_MSG, "Locate verifypn");

            try {
                File file = fileFinder.ShowFileBrowserDialog("Verifypn", "", System.getProperty("user.home"));
                if (file != null) {
                    if (file.getName().matches(VERIFYPN_EXE_PATTERN)) {
                        setPath(file.getAbsolutePath());
                    } else {
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
            if (new File(verifypn).exists()) {
                verifypnpath = verifypn;
                VerifyPN v = new VerifyPN(new FileFinder(), new MessengerImpl());
                if (v.isCorrectVersion()) {
                    return true;
                } else {
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
            if (v.isCorrectVersion()) {
                return true;
            } else {
                verifypn = null;
                verifypnpath = null;
            }
        }

        //Search the installdir for verifytapn
        File installdir = TAPAAL.getInstallDir();

        String[] paths = {"/bin/verifypn", "/bin/verifypn64", "/bin/verifypn.exe", "/bin/verifypn64.exe"};
        for (String s : paths) {
            File verifypnfile = new File(installdir + s);

            if (verifypnfile.exists()) {

                verifypnpath = verifypnfile.getAbsolutePath();
                VerifyPN v = new VerifyPN(new FileFinder(), new MessengerImpl());
                if (v.isCorrectVersion()) {
                    return true;
                } else {
                    verifypn = null;
                    verifypnpath = null;
                }

            }
        }
        return false;
    }

    @Override
    public VerificationResult<TimedArcPetriNetTrace> verifyManually(String options, Tuple<TimedArcPetriNet, NameMapping> model, TAPNQuery query, net.tapaal.gui.petrinet.verification.TAPNQuery dataLayerQuery, TAPNLens lens) throws Exception {
        VerifyTAPNExporter exporter;
        if ((lens != null && lens.isColored() || model.value1().parentNetwork().isColored())) {
            exporter = new VerifyCPNExporter();
        } else {
            exporter = new VerifyPNExporter();
        }
        ExportedVerifyTAPNModel exportedModel = exporter.export(model.value1(), query, lens, model.value2(), null, dataLayerQuery);

        if (exportedModel == null) {
            messenger.displayErrorMessage("There was an error exporting the model");
        }

        return verifyManually(options, model, exportedModel, query);
    }

    private VerificationResult<TimedArcPetriNetTrace> verifyManually(String options, Tuple<TimedArcPetriNet, NameMapping> model, ExportedVerifyTAPNModel exportedModel, TAPNQuery query) {
        if (exportedModel == null) return null;

        runner = new ProcessRunner(verifypnpath, createArgumentString(exportedModel.modelFile(), exportedModel.queryFile(), options));
        runner.run();

        if (runner.error()) {
            return null;
        } else {
            String errorOutput = readOutput(runner.errorOutput());
            String standardOutput = readOutput(runner.standardOutput());

            Tuple<QueryResult, Stats> queryResult = parseQueryResult(standardOutput, model.value1().marking().size() + query.getExtraTokens(), query.getExtraTokens(), query);

            if (queryResult == null || queryResult.value1() == null) {
                return new VerificationResult<>(errorOutput + System.getProperty("line.separator") + standardOutput, runner.getRunningTime());
            } else {
                ctlOutput = queryResult.value1().isCTL;
                return new VerificationResult<>(queryResult.value1(), null, null, runner.getRunningTime(), queryResult.value2(), false, standardOutput + "\n\n" + errorOutput, model);
            }
        }
    }

    public VerificationResult<TimedArcPetriNetTrace> verify(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, TAPNQuery query, DataLayer guiModel, net.tapaal.gui.petrinet.verification.TAPNQuery dataLayerQuery, TAPNLens lens) throws Exception {
        if (!supportsModel(model.value1(), options)) {
            throw new UnsupportedModelException("Verifypn does not support the given model.");
        }

        if (!supportsQuery(model.value1(), query, options)) {
            throw new UnsupportedQueryException("Verifypn does not support the given query.");
        }

        if (((VerifyTAPNOptions) options).discreteInclusion()) mapDiscreteInclusionPlacesToNewNames(options, model);

        VerifyTAPNExporter exporter;
        if ((lens != null && lens.isColored() || model.value1().parentNetwork().isColored())) {
            exporter = new VerifyCPNExporter();
        } else {
            exporter = new VerifyPNExporter();
        }
        ExportedVerifyTAPNModel exportedModel = exporter.export(model.value1(), query, lens, model.value2(), guiModel, dataLayerQuery);

        if (exportedModel == null) {
            messenger.displayErrorMessage("There was an error exporting the model");
        }

        return verify(options, model, exportedModel, query, dataLayerQuery, lens);
    }

    private void mapDiscreteInclusionPlacesToNewNames(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model) {
        VerifyTAPNOptions verificationOptions = (VerifyTAPNOptions) options;

        if (verificationOptions.inclusionPlaces().inclusionOption() == InclusionPlacesOption.AllPlaces) {
            return;
        }

        List<TimedPlace> inclusionPlaces = new ArrayList<>();
        for (TimedPlace p : verificationOptions.inclusionPlaces().inclusionPlaces()) {
            if (p instanceof LocalTimedPlace) {
                LocalTimedPlace local = (LocalTimedPlace) p;
                if (local.model().isActive()) {
                    inclusionPlaces.add(model.value1().getPlaceByName(model.value2().map(local.model().name(), local.name())));
                }
            } else { // shared place
                inclusionPlaces.add(model.value1().getPlaceByName(model.value2().map("", p.name())));
            }
        }

        ((VerifyTAPNOptions) options).setInclusionPlaces(new InclusionPlaces(InclusionPlacesOption.UserSpecified, inclusionPlaces));
    }

    private VerificationResult<TimedArcPetriNetTrace> verify(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, ExportedVerifyTAPNModel exportedModel, TAPNQuery query, net.tapaal.gui.petrinet.verification.TAPNQuery dataLayerQuery, TAPNLens lens) throws IOException {
        ((VerifyTAPNOptions) options).setTokensInModel(model.value1().getNumberOfTokensInNet()); // TODO: get rid of me

        runner = new ProcessRunner(verifypnpath, createArgumentString(exportedModel.modelFile(), exportedModel.queryFile(), options));
        runner.run();

        if (runner.error()) {
            return null;
        } else {
            PetriNetTab newTab = null;
            TimedArcPetriNetTrace tapnTrace = null;
            String errorOutput = readOutput(runner.errorOutput());
            String standardOutput = readOutput(runner.standardOutput());

            Tuple<QueryResult, Stats> queryResult = parseQueryResult(standardOutput, model.value1().getNumberOfTokensInNet() + query.getExtraTokens(), query.getExtraTokens(), query);

            if (queryResult == null || queryResult.value1() == null) {
                return new VerificationResult<>(errorOutput + System.getProperty("line.separator") + standardOutput, runner.getRunningTime());
            } else {
                boolean isColored = (lens != null && lens.isColored() || model.value1().parentNetwork().isColored());
                boolean showTrace = ((query.getProperty() instanceof TCTLEFNode && queryResult.value1().isQuerySatisfied()) ||
                    (query.getProperty() instanceof TCTLAGNode && !queryResult.value1().isQuerySatisfied()) ||
                    (query.getProperty() instanceof TCTLEGNode && queryResult.value1().isQuerySatisfied()) ||
                    (query.getProperty() instanceof TCTLAFNode && !queryResult.value1().isQuerySatisfied())) ||
                    (query.getProperty() instanceof LTLENode && queryResult.value1().isQuerySatisfied()) ||
                    (query.getProperty() instanceof LTLANode && !queryResult.value1().isQuerySatisfied());

                if (options.traceOption() != TraceOption.NONE && isColored && showTrace) {
                    PNMLoader tapnLoader = new PNMLoader();
                    File fileOut = new File(options.unfoldedModelPath());
                    File queriesOut = new File(options.unfoldedQueriesPath());
                    try {
                        LoadedModel loadedModel = tapnLoader.load(fileOut);
                        TAPNComposer newComposer = new TAPNComposer(new MessengerImpl(), true);
                        model = newComposer.transformModel(loadedModel.network());

                        if (queryResult.value1() != null) {
                            tapnTrace = trace(errorOutput, standardOutput, options, model, exportedModel, query, queryResult);
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
                    } catch (FormatException e) {
                        e.printStackTrace();
                    } catch (NullPointerException | ThreadDeath n) {
                        return null;
                    }
                }

                ctlOutput = queryResult.value1().isCTL;

                if (tapnTrace == null) {
                    tapnTrace = trace(errorOutput, standardOutput, options, model, exportedModel, query, queryResult);
                }

                var result = new VerificationResult<>(queryResult.value1(), tapnTrace, runner.getRunningTime(), queryResult.value2(), false, standardOutput + "\n\n" + errorOutput, model, newTab);

                return result;
            }
        }
    }

    @Nullable
    private TimedArcPetriNetTrace trace(String errorOutput, String standardOutput, VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, ExportedVerifyTAPNModel exportedModel, TAPNQuery query, Tuple<QueryResult, Stats> queryResult) {
        TimedArcPetriNetTrace tapnTrace;

        if(!errorOutput.toLowerCase().contains("trace") && !standardOutput.contains("<trace>")) return null;
        if (!errorOutput.contains("Trace") && standardOutput.contains("<trace>")) {
            // Trace is on stdout
            String trace = "Trace:\n";
            trace += (standardOutput.split("(?=<trace>)")[1]);
            trace = trace.split("(?<=</trace>)")[0];
            tapnTrace = parseTrace(trace, options, model, exportedModel, query, queryResult.value1());
        } else {
            // Trace is on stderr
            // Verifypn will with some options output (at lest TAR) some extra information in the error output, therefor we need to find the trace tag
            if (errorOutput.contains("<trace>")) {
                var split = errorOutput.split("(?=<trace>)");
                if (split.length > 1 ) {
                    String trace = "Trace\n";
                    trace += split[1];
                    trace = trace.split("(?<=</trace>)")[0];
                    tapnTrace = parseTrace(trace, options, model, exportedModel, query, queryResult.value1());
                } else {
                    String trace = "Trace\n";
                    trace += errorOutput;
                    tapnTrace = parseTrace(trace, options, model, exportedModel, query, queryResult.value1());
                }
            } else {
                tapnTrace = parseTrace(errorOutput, options, model, exportedModel, query, queryResult.value1());
            }
        }

        return tapnTrace;
    }

    private TimedArcPetriNetTrace parseTrace(String output, VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, ExportedVerifyTAPNModel exportedModel, TAPNQuery query, QueryResult queryResult) {
        if (((VerifyTAPNOptions) options).trace() == TraceOption.NONE) return null;
        if ((query.getProperty() instanceof TCTLEFNode && !queryResult.isQuerySatisfied()) ||
            (query.getProperty() instanceof TCTLAGNode && queryResult.isQuerySatisfied()) ||
            (query.getProperty() instanceof TCTLEGNode && !queryResult.isQuerySatisfied()) ||
            (query.getProperty() instanceof TCTLAFNode && queryResult.isQuerySatisfied())) {
            return null;
        }

        VerifyTAPNTraceParser traceParser = new VerifyTAPNTraceParser(model.value1());

        return traceParser.parseTrace(new BufferedReader(new StringReader(output)));
    }

    private String createArgumentString(String modelFile, String queryFile, VerificationOptions options) {
        return options.toString() + ' ' + modelFile + ' ' + queryFile;
    }

    private String createArgumentString(String modelFile, String queryFile, String options) {
        return options + ' ' + modelFile + ' ' + queryFile;
    }

    private String readOutput(BufferedReader reader) {
        try {
            if (!reader.ready()) {
                return "";
            }
        } catch (IOException e1) {
            return "";
        }
        StringBuilder buffer = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append(System.getProperty("line.separator"));
            }
        } catch (IOException ignored) { }

        return buffer.toString();
    }

    private Tuple<QueryResult, Stats> parseQueryResult(String output, int totalTokens, int extraTokens, TAPNQuery query) {
        Tuple<QueryResult, Stats> result;
        if (output.contains("Processed N. Edges:")) {
            VerifyPNCTLOutputParser outputParser = new VerifyPNCTLOutputParser(totalTokens, extraTokens, query);
            result = outputParser.parseOutput(output);
            result.value1().isCTL = true;
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
        if (query.getCategory() == QueryCategory.CTL || query.getCategory() == QueryCategory.LTL) {
            return true;
        }
        return !(query.getProperty() instanceof TCTLEGNode) && !(query.getProperty() instanceof TCTLAFNode);
    }

    public static void reset() {
        //Clear value
        verifypnpath = "";
        Preferences.getInstance().setVerifypnLocation(null);
        //Set the default
        trySetup();
    }

    public String getHelpOptions() {
        runner = new ProcessRunner(verifypnpath, "--help");
        runner.run();

        if (!runner.error()) {
            return readOutput(runner.standardOutput());
        }
        return null;
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
