package net.tapaal.gui.petrinet.verification;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import net.tapaal.gui.petrinet.TAPNLens;
import net.tapaal.gui.petrinet.smartdraw.SmartDrawDialog;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.verification.VerifyTAPN.*;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.TAPAALGUI;
import pipe.gui.FileFinder;
import pipe.gui.MessengerImpl;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.VerificationCallback;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.UPPAAL.UppaalIconSelector;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.UPPAAL.VerifytaOptions;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;

/**
 * Implements of class for handling integrated Uppaal Verification
 * 
 * Copyright 2009 Author Kenneth Yrke Joergensen <kenneth@yrke.dk>
 * 
 * Licensed under the Open Software License version 3.0
 */

public class Verifier {
    private static File reducedNetTempFile = null;

    private static Verifyta getVerifyta() {
        Verifyta verifyta = new Verifyta(new FileFinder(), new MessengerImpl());
        verifyta.setup();
        return verifyta;
    }

    private static VerifyTAPN getVerifyTAPN() {
        VerifyTAPN verifytapn = new VerifyTAPN(new FileFinder(), new MessengerImpl());
        verifytapn.setup();
        return verifytapn;
    }

    private static VerifyDTAPN getVerifydTAPN() {
        VerifyDTAPN verifydtapn = new VerifyDTAPN(new FileFinder(), new MessengerImpl());
        verifydtapn.setup();
        return verifydtapn;
    }

    private static VerifyPN getVerifyPN() {
        VerifyPN verifypn = new VerifyPN(new FileFinder(), new MessengerImpl());
        verifypn.setup();
        return verifypn;
    }

    private static TAPNQuery convertQuery(TAPNQuery query, TAPNLens lens) {
        if (lens == null) return query;

        TAPNQuery newQuery = query;
        if (!lens.isTimed() && query.getReductionOption() != ReductionOption.VerifyPN) {
            newQuery = new TAPNQuery(
                query.getName(),
                query.getCapacity(),
                query.getProperty().copy(),
                query.getTraceOption(),
                query.getSearchOption(),
                ReductionOption.VerifyPN,
                false,
                false,
                false,
                false,
                false,
                !lens.isGame() && query.useReduction(),
                null,
                null,
                query.inclusionPlaces(),
                false,
                false,
                0,
                lens.isColored() && query.usePartitioning(),
                lens.isColored() && query.useColorFixpoint(),
                lens.isColored() && query.useSymmetricVars(),
                lens.isColored(),
                query.useColoredReduction());
            newQuery.setCategory(TAPNQuery.QueryCategory.CTL);
            newQuery.setUseSiphontrap(query.isSiphontrapEnabled());
            newQuery.setUseQueryReduction(query.isQueryReductionEnabled());
            newQuery.setUseStubbornReduction(query.isStubbornReductionEnabled());
            newQuery.setUseTarOption(query.isTarOptionEnabled());
            newQuery.setUseTarjan(query.isTarjan());
        } else if (lens.isTimed() && query.getReductionOption() == ReductionOption.VerifyPN) {
            newQuery = new TAPNQuery(
                query.getName(),
                query.getCapacity(),
                query.getProperty().copy(),
                query.getTraceOption(),
                query.getSearchOption(),
                ReductionOption.VerifyDTAPN,
                query.useSymmetry(),
                query.useGCD(),
                query.useTimeDarts(),
                query.usePTrie(),
                query.useOverApproximation(),
                query.useReduction(),
                null,
                null,
                query.inclusionPlaces(),
                query.isOverApproximationEnabled(),
                query.isUnderApproximationEnabled(),
                query.approximationDenominator(),
                false,
                false,
                false,
                lens.isColored(),
                false);
            newQuery.setUseStubbornReduction(query.isStubbornReductionEnabled());
        }

        return newQuery;
    }

    public static ModelChecker getModelChecker(TAPNQuery query) {
        if (query.getReductionOption() == ReductionOption.VerifyTAPN) {
            return getVerifyTAPN();
        } else if (query.getReductionOption() == ReductionOption.VerifyDTAPN) {
            return getVerifydTAPN();
        } else if (query.getReductionOption() == ReductionOption.VerifyPN) {
            return getVerifyPN();
        } else {
            throw new RuntimeException("Verification method: " + query.getReductionOption() + ", should not be send here");
        }
    }

    public static String getReducedNetFilePath() {
        return reducedNetTempFile.getAbsolutePath();
    }

    public static void analyzeKBound(TimedArcPetriNetNetwork tapnNetwork, TAPNLens lens, HashMap<TimedArcPetriNet, DataLayer> guiModels, int k, JSpinner tokensControl, TAPNQuery query) {
        ModelChecker modelChecker;

        if ((lens == null && tapnNetwork.isUntimed()) || (lens != null && !lens.isTimed())) {
            modelChecker = getVerifyPN();
        } else if ((lens == null && tapnNetwork.isColored()) || (lens != null && lens.isColored()) || tapnNetwork.hasWeights() ||
                tapnNetwork.hasUrgentTransitions() || tapnNetwork.hasUncontrollableTransitions()) {
            modelChecker = getVerifydTAPN();
        } else {
            modelChecker = getVerifyTAPN();
        }

        if (!modelChecker.isCorrectVersion()) {
            new MessengerImpl().displayErrorMessage(
                "No " + modelChecker + " specified or you are running an old version of it:\nThe verification is cancelled",
                "Verification Error");
            return;
        }

        KBoundAnalyzer optimizer = new KBoundAnalyzer(tapnNetwork, lens, guiModels, k, modelChecker, new MessengerImpl(), tokensControl, query);
        optimizer.analyze();
    }


    public static void runUppaalVerification(TimedArcPetriNetNetwork timedArcPetriNetNetwork, TAPNQuery input) {
        Verifyta verifyta = getVerifyta();
        if (!verifyta.isCorrectVersion()) {
            System.err.println("Verifyta not found, or you are running an old version of Verifyta.\n"
                + "Update to the latest development version.");
            return;
        }

        TCTLAbstractProperty inputQuery = input.getProperty();

        VerifytaOptions verifytaOptions = new VerifytaOptions(
            input.getTraceOption(),
            input.getSearchOption(),
            false,
            input.getReductionOption(),
            input.useSymmetry(),
            input.useOverApproximation(),
            input.isOverApproximationEnabled(),
            input.isUnderApproximationEnabled(),
            input.approximationDenominator()
        );

        if (inputQuery == null) {
            return;
        }

        if (timedArcPetriNetNetwork != null) {
            RunVerificationBase thread = new RunVerification(verifyta, new UppaalIconSelector(), new MessengerImpl());
            RunningVerificationDialog dialog = new RunningVerificationDialog(TAPAALGUI.getApp(), thread);
            if(timedArcPetriNetNetwork.isColored() && input.getTraceOption() != TAPNQuery.TraceOption.NONE){
                SmartDrawDialog.setupWorkerListener(thread);
            }
            thread.execute(
                verifytaOptions,
                timedArcPetriNetNetwork,
                new dk.aau.cs.model.tapn.TAPNQuery(input.getProperty(), input.getCapacity()),
                null
            );
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
                "There was an error converting the model.",
                "Conversion error", JOptionPane.ERROR_MESSAGE);
        }

    }

    public static void runVerifyTAPNVerification(TimedArcPetriNetNetwork tapnNetwork, TAPNQuery query, VerificationCallback callback) {
        runVerifyTAPNVerification(tapnNetwork, query, callback, null, false, null);
    }

    public static void runVerifyTAPNVerification(
        TimedArcPetriNetNetwork tapnNetwork,
        TAPNQuery query,
        VerificationCallback callback,
        HashMap<TimedArcPetriNet, DataLayer> guiModels,
        boolean onlyCreateReducedNet,
        TAPNLens lens) {
        query = convertQuery(query, lens);

        ModelChecker verifytapn = getModelChecker(query);


        try {
            reducedNetTempFile = File.createTempFile("reduced-", ".pnml");
        } catch (IOException e) {
            new MessengerImpl().displayErrorMessage(
                e.getMessage(),
                "Error");
            return;
        }

        if (!verifytapn.isCorrectVersion()) {
            new MessengerImpl().displayErrorMessage(
                "No " + verifytapn + " specified: The verification is cancelled",
                "Verification Error");
            return;
        }

        TCTLAbstractProperty inputQuery = query.getProperty();

        int bound = query.getCapacity();
        boolean isColored = (lens != null && lens.isColored() || tapnNetwork.isColored());
        VerifyTAPNOptions verifytapnOptions;
        if (query.getReductionOption() == ReductionOption.VerifyDTAPN) {
            verifytapnOptions = new VerifyDTAPNOptions(
                bound,
                query.getTraceOption(),
                query.getSearchOption(),
                query.useSymmetry(),
                query.useGCD(),
                query.useTimeDarts(),
                query.usePTrie(),
                query.useOverApproximation(),
                query.discreteInclusion(),
                query.inclusionPlaces(),
                query.getWorkflowMode(),
                query.getStrongSoundnessBound(),
                query.isOverApproximationEnabled(),
                query.isUnderApproximationEnabled(),
                query.approximationDenominator(),
                query.isStubbornReductionEnabled(),
                reducedNetTempFile.getAbsolutePath(),
                query.usePartitioning(),
                query.useColorFixpoint(),
                isColored // Unfold net
            );
        } else if (query.getReductionOption() == ReductionOption.VerifyPN) {
            verifytapnOptions = new VerifyPNOptions(
                bound,
                query.getTraceOption(),
                query.getSearchOption(),
                query.useOverApproximation(),
                query.useReduction() ? ModelReduction.AGGRESSIVE : ModelReduction.NO_REDUCTION,
                query.isOverApproximationEnabled(),
                query.isUnderApproximationEnabled(),
                query.approximationDenominator(),
                query.getCategory(),
                query.getAlgorithmOption(),
                query.isSiphontrapEnabled(),
                query.isQueryReductionEnabled() ? TAPNQuery.QueryReductionTime.UnlimitedTime : TAPNQuery.QueryReductionTime.NoTime,
                query.isStubbornReductionEnabled(),
                reducedNetTempFile.getAbsolutePath(),
                query.isTarOptionEnabled(),
                query.isTarjan(),
                isColored,
                isColored && query.getTraceOption() != TAPNQuery.TraceOption.NONE,
                query.usePartitioning(),
                query.useColorFixpoint(),
                query.useSymmetricVars(),
                query.useColoredReduction()
            );
        } else {
            verifytapnOptions = new VerifyTAPNOptions(
                bound,
                query.getTraceOption(),
                query.getSearchOption(),
                query.useSymmetry(),
                query.useOverApproximation(),
                query.discreteInclusion(),
                query.inclusionPlaces(),
                query.isOverApproximationEnabled(),
                query.isUnderApproximationEnabled(),
                query.approximationDenominator(),
                false,
                isColored
            );
        }

        if (inputQuery == null) {
            return;
        }

        if (tapnNetwork != null) {
            RunVerificationBase thread;
            if (reducedNetTempFile != null) {
                thread = new RunVerification(verifytapn, new VerifyTAPNIconSelector(), new MessengerImpl(), callback, guiModels, reducedNetTempFile.getAbsolutePath(), onlyCreateReducedNet);
            } else {
                thread = new RunVerification(verifytapn, new VerifyTAPNIconSelector(), new MessengerImpl(), callback, guiModels);
            }

            RunningVerificationDialog dialog = new RunningVerificationDialog(TAPAALGUI.getApp(), thread);
            if (tapnNetwork.isColored() && query.getTraceOption() != TAPNQuery.TraceOption.NONE) {
                SmartDrawDialog.setupWorkerListener(thread);
            }
            thread.execute(verifytapnOptions, tapnNetwork, new dk.aau.cs.model.tapn.TAPNQuery(query.getProperty(), bound), query, lens);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
                "There was an error converting the model.",
                "Conversion error", JOptionPane.ERROR_MESSAGE);
        }

        return;
    }
}
