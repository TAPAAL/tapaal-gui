package net.tapaal.gui.petrinet.verification;

import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineEligibilityTest {
    private static final List<EngineSupportOptions> CATALOG = List.of(
        new VerifyDTAPNEngineOptions(),
        new VerifyTAPNEngineOptions(),
        new UPPAALCombiOptions(),
        new UPPAALOptimizedStandardOptions(),
        new UPPAALStandardOptions(),
        new UPPAALBroadcastOptions(),
        new UPPAALBroadcastDegree2Options(),
        new VerifyPNEngineOptions()
    );

    @Test
    void derivesAllRequiredFeaturesFromEligibilityContext() {
        var context = new EngineEligibility.Context(
            true, true, true, true, true, true, true, true, true, true,
            true, true, true, true, true, true, true
        );

        var expected = EnumSet.allOf(EngineFeature.class);
        expected.remove(EngineFeature.DEADLOCK_NET_DEGREE_2_EXP);
        expected.remove(EngineFeature.ONLY_UNTIMED);
        assertEquals(expected, EngineEligibility.requiredFeatures(context));
    }

    @Test
    void untimedModelsOfferOnlyUntimedEngine() {
        var engines = EngineEligibility.compatibleEngines(
            false,
            false,
            EnumSet.noneOf(EngineFeature.class),
            CATALOG
        );

        assertIterableEquals(List.of("TAPAAL: Untimed Engine (verifypn)"), names(engines));
    }

    @Test
    void stochasticModelsExcludeContinuousEngine() {
        var engines = EngineEligibility.compatibleEngines(
            true,
            true,
            EnumSet.of(EngineFeature.TIMED_NETS),
            CATALOG
        );

        assertFalse(names(engines).contains("TAPAAL: Continuous Engine (verifytapn)"));
        assertTrue(names(engines).contains("TAPAAL: Discrete Engine (verifydtapn)"));
    }

    @Test
    void catalogFilteringMatchesEveryFeatureSet() {
        EngineFeature[] features = EngineFeature.values();
        int comparisons = 0;

        for (int mask = 0; mask < (1 << features.length); mask++) {
            int currentMask = mask;
            EnumSet<EngineFeature> required = EnumSet.noneOf(EngineFeature.class);
            for (int bit = 0; bit < features.length; bit++) {
                if ((mask & (1 << bit)) != 0) required.add(features[bit]);
            }

            var actual = EngineEligibility.compatibleEngines(true, false, required, CATALOG);
            for (EngineSupportOptions engine : CATALOG) {
                boolean expected = engine.areOptionsSupported(required);
                assertEquals(expected, actual.contains(engine),
                    () -> "catalog mismatch for mask " + currentMask + " and " + engine.getNameString());
                comparisons++;
            }
        }

        assertEquals(8 * (1 << 20), comparisons);
    }

    @Test
    void requiredFeatureDerivationMatchesLegacyRulesForEveryBooleanCombination() {
        int combinations = 1 << 17;
        for (int mask = 0; mask < combinations; mask++) {
            int currentMask = mask;
            var context = new EngineEligibility.Context(
                bit(mask, 0), bit(mask, 1), bit(mask, 2), bit(mask, 3),
                bit(mask, 4), bit(mask, 5), bit(mask, 6), bit(mask, 7),
                bit(mask, 8), bit(mask, 9), bit(mask, 10), bit(mask, 11),
                bit(mask, 12), bit(mask, 13), bit(mask, 14), bit(mask, 15),
                bit(mask, 16)
            );
            assertEquals(legacyRequiredFeatures(context), EngineEligibility.requiredFeatures(context),
                () -> "feature derivation mismatch for mask " + currentMask);
        }
    }

    @Test
    void preservesCatalogOrder() {
        var engines = EngineEligibility.compatibleEngines(
            true,
            false,
            EnumSet.of(EngineFeature.TIMED_NETS),
            CATALOG
        );

        assertIterableEquals(names(CATALOG.subList(0, 7)), names(engines));
    }

    @Test
    void hyperLtlLeavesOnlyDepthFirstSearch() {
        var decision = EngineEligibility.searchDecision(new EngineEligibility.SearchContext(
            true, false, false, false, false, false, false, false, false, false, false,
            EngineEligibility.SearchStrategy.BFS
        ));

        assertEquals(EnumSet.of(EngineEligibility.SearchStrategy.DFS), decision.enabled());
        assertEquals(EngineEligibility.SearchStrategy.DFS, decision.selected());
    }

    @Test
    void fastestTraceDisablesAllSearchStrategiesWithoutChangingSelection() {
        var decision = EngineEligibility.searchDecision(new EngineEligibility.SearchContext(
            false, true, false, false, true, false, true, false, false, false, false,
            EngineEligibility.SearchStrategy.RANDOM
        ));

        assertTrue(decision.enabled().isEmpty());
        assertEquals(EngineEligibility.SearchStrategy.RANDOM, decision.selected());
    }

    @Test
    void timedLivenessDisablesBreadthFirstAndUnsupportedHeuristic() {
        var decision = EngineEligibility.searchDecision(new EngineEligibility.SearchContext(
            false, false, false, false, true, true, false, false, false, false, false,
            EngineEligibility.SearchStrategy.BFS
        ));

        assertFalse(decision.isEnabled(EngineEligibility.SearchStrategy.BFS));
        assertFalse(decision.isEnabled(EngineEligibility.SearchStrategy.HEURISTIC));
        assertEquals(EngineEligibility.SearchStrategy.DFS, decision.selected());
    }

    @Test
    void untimedReachabilityUsesRandomHeuristicLabel() {
        var decision = EngineEligibility.searchDecision(new EngineEligibility.SearchContext(
            false, false, false, false, false, false, false, false, false, true, false,
            EngineEligibility.SearchStrategy.HEURISTIC
        ));

        assertEquals("Random heuristic    ", decision.label());
    }

    @Test
    void gameQueriesAllowOnlyNoTrace() {
        var decision = EngineEligibility.traceDecision(new EngineEligibility.TraceContext(
            true, false, false, false, false, true, false, TAPNQuery.TraceOption.FASTEST
        ));

        assertEquals(EnumSet.of(TAPNQuery.TraceOption.NONE), decision.enabled());
        assertEquals(TAPNQuery.TraceOption.NONE, decision.selected());
    }

    @Test
    void timedStrictQueriesDoNotAllowFastestTrace() {
        var decision = EngineEligibility.traceDecision(new EngineEligibility.TraceContext(
            false, true, false, false, false, false, false, TAPNQuery.TraceOption.FASTEST
        ));

        assertFalse(decision.isEnabled(TAPNQuery.TraceOption.FASTEST));
        assertTrue(decision.isEnabled(TAPNQuery.TraceOption.SOME));
        assertEquals(TAPNQuery.TraceOption.SOME, decision.selected());
    }

    @Test
    void untimedNonReachabilityQueriesDisableTraceOptions() {
        var decision = EngineEligibility.traceDecision(new EngineEligibility.TraceContext(
            false, false, false, false, false, false, false, TAPNQuery.TraceOption.SOME
        ));

        assertTrue(decision.enabled().isEmpty());
        assertEquals(TAPNQuery.TraceOption.NONE, decision.selected());
    }

    @Test
    void timedNonstrictReachabilityAllowsFastestTrace() {
        var decision = EngineEligibility.traceDecision(new EngineEligibility.TraceContext(
            false, true, true, false, false, true, false, TAPNQuery.TraceOption.FASTEST
        ));

        assertTrue(decision.isEnabled(TAPNQuery.TraceOption.FASTEST));
        assertEquals(TAPNQuery.TraceOption.FASTEST, decision.selected());
    }

    @Test
    void traceRefinementRequiresPlainUntimedReachabilityShape() {
        assertTrue(EngineEligibility.traceRefinementEnabled(
            new EngineEligibility.TraceRefinementContext(true, false, true, true, false, false)));
        assertFalse(EngineEligibility.traceRefinementEnabled(
            new EngineEligibility.TraceRefinementContext(true, true, true, true, false, false)));
        assertFalse(EngineEligibility.traceRefinementEnabled(
            new EngineEligibility.TraceRefinementContext(true, false, true, true, true, false)));
    }

    @Test
    void tarjanIsVisibleForLtlAndForcedOffForHyperLtl() {
        assertEquals(new EngineEligibility.TarjanDecision(true, true, false),
            EngineEligibility.tarjanDecision(1));
        assertEquals(new EngineEligibility.TarjanDecision(true, false, true),
            EngineEligibility.tarjanDecision(2));
        assertEquals(new EngineEligibility.TarjanDecision(false, false, false),
            EngineEligibility.tarjanDecision(0));
    }

    @Test
    void explicitSearchRemembersSelectionWhileUnavailable() {
        var disabled = EngineEligibility.explicitSearchDecision(false, true, true, false);
        assertFalse(disabled.enabled());
        assertFalse(disabled.selected());
        assertTrue(disabled.rememberedState());

        var restored = EngineEligibility.explicitSearchDecision(true, false, false, disabled.rememberedState());
        assertTrue(restored.enabled());
        assertTrue(restored.selected());
    }

    @Test
    void coloredReductionAndInclusionVisibilityFollowEngineTrace() {
        assertTrue(EngineEligibility.coloredReductionEnabled(false, false));
        assertFalse(EngineEligibility.coloredReductionEnabled(true, false));
        assertFalse(EngineEligibility.coloredReductionEnabled(false, true));
        assertEquals(new EngineEligibility.DiscreteInclusionDecision(true, true),
            EngineEligibility.discreteInclusionDecision(true));
        assertEquals(new EngineEligibility.DiscreteInclusionDecision(false, false),
            EngineEligibility.discreteInclusionDecision(false));
    }

    @Test
    void symmetryReductionIsForcedOffOnlyByKnownIncompatibleChoices() {
        assertEquals(new EngineEligibility.SymmetryDecision(true, false, true),
            EngineEligibility.symmetryDecision(true, true, false, true, true));
        assertEquals(new EngineEligibility.SymmetryDecision(true, false, false),
            EngineEligibility.symmetryDecision(true, false, true, true, true));
        assertEquals(new EngineEligibility.SymmetryDecision(true, true, true),
            EngineEligibility.symmetryDecision(true, false, false, false, false));
    }

    @Test
    void approximationDisablesApproximationForGamesAndFastestTraces() {
        var game = EngineEligibility.approximationDecision(false, true, false, true, true, false);
        assertTrue(game.noApproximationEnabled());
        assertFalse(game.overApproximationEnabled());
        assertFalse(game.underApproximationEnabled());

        var fastest = EngineEligibility.approximationDecision(false, false, true, true, false, false);
        assertTrue(fastest.noApproximationSelected());
        assertFalse(fastest.denominatorEnabled());
    }

    @Test
    void skeletonAnalysisIsForcedOffForDeadlockAndLiveness() {
        var decision = EngineEligibility.approximationDecision(true, false, false, true, true, true);
        assertFalse(decision.skeletonEnabled());
        assertFalse(decision.skeletonSelected());
    }

    @Test
    void stubbornReductionRequiresNeitherHyperLtlNorTimeDarts() {
        assertTrue(EngineEligibility.stubbornReductionEnabled(false, false));
        assertFalse(EngineEligibility.stubbornReductionEnabled(true, false));
        assertFalse(EngineEligibility.stubbornReductionEnabled(false, true));
    }

    @Test
    void discreteReductionDecisionCapturesForcedOptionRules() {
        var decision = EngineEligibility.discreteDecision(
            true, true, false, false, false, true, true);
        assertTrue(decision.timeDartsForcedOff());
        assertTrue(decision.gcdForcedOff());
        assertTrue(decision.stubbornForcedOff());
        assertTrue(decision.symmetryForcedOff());

        var nonDiscrete = EngineEligibility.discreteDecision(
            false, true, true, true, true, true, true);
        assertFalse(nonDiscrete.timeDartsForcedOff());
        assertFalse(nonDiscrete.gcdForcedOff());
        assertFalse(nonDiscrete.stubbornForcedOff());
        assertFalse(nonDiscrete.symmetryForcedOff());
    }

    private static List<String> names(List<EngineSupportOptions> engines) {
        return engines.stream().map(EngineSupportOptions::getNameString).toList();
    }

    private static boolean bit(int mask, int index) {
        return (mask & (1 << index)) != 0;
    }

    private static EnumSet<EngineFeature> legacyRequiredFeatures(EngineEligibility.Context context) {
        EnumSet<EngineFeature> required = EnumSet.noneOf(EngineFeature.class);
        if (context.fastestTrace()) required.add(EngineFeature.FASTEST_TRACE);
        if (context.deadlock() && context.hasEfOrAg() && !context.degreeGreaterThanTwo()) {
            required.add(EngineFeature.DEADLOCK_NET_DEGREE_2_EXP);
        }
        if (context.deadlock() && context.hasEgOrAf()) required.add(EngineFeature.DEADLOCK_EG_OR_AF);
        if (context.deadlock() && context.inhibitorArcs()) required.add(EngineFeature.DEADLOCK_WITH_INHIB);
        if (context.weightedArcs()) required.add(EngineFeature.WEIGHTS);
        if (context.inhibitorArcs()) required.add(EngineFeature.INHIBITOR_ARCS);
        if (context.coloredInhibitorArcs()) required.add(EngineFeature.COLORED_INHIBITOR_ARCS);
        if (context.urgentTransitions()) required.add(EngineFeature.URGENT_TRANSITIONS);
        if (context.hasEgOrAf()) required.add(EngineFeature.EG_OR_AF);
        if (context.strictNet()) required.add(EngineFeature.STRICT_NETS);
        if (context.timed()) required.add(EngineFeature.TIMED_NETS);
        if (context.deadlock() && context.degreeGreaterThanTwo()) {
            required.add(EngineFeature.DEADLOCK_NET_DEGREE_GREATER_THAN_2);
        }
        if (context.game()) required.add(EngineFeature.GAMES);
        if (context.hasEgOrAf() && context.degreeGreaterThanTwo()) {
            required.add(EngineFeature.EG_OR_AF_WITH_NET_DEGREE_GREATER_THAN_2);
        }
        if (context.nestedQuantifications()) required.add(EngineFeature.NESTED_QUANTIFICATIONS);
        if (context.colored()) required.add(EngineFeature.COLORED);
        if (context.colored() && !context.timed()) required.add(EngineFeature.ONLY_UNTIMED);
        if (context.stochastic()) required.add(EngineFeature.SMC);
        if (context.coloredPlaceQueries()) required.add(EngineFeature.COLORED_PLACE_QUERIES);
        if (context.nonzeroInitialTokenAges()) required.add(EngineFeature.NONZERO_INITIAL_TOKEN_AGES);
        return required;
    }
}
