package net.tapaal.gui.petrinet.verification;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Pure engine-eligibility policy.  The Swing dialog supplies facts about the
 * model and query; this module derives the required features and filters the
 * catalog without mutating dialog state.
 */
public final class EngineEligibility {
    private EngineEligibility() { }

    public record Context(
        boolean timed,
        boolean stochastic,
        boolean game,
        boolean colored,
        boolean strictNet,
        boolean weightedArcs,
        boolean inhibitorArcs,
        boolean coloredInhibitorArcs,
        boolean urgentTransitions,
        boolean degreeGreaterThanTwo,
        boolean nonzeroInitialTokenAges,
        boolean fastestTrace,
        boolean deadlock,
        boolean hasEfOrAg,
        boolean hasEgOrAf,
        boolean nestedQuantifications,
        boolean coloredPlaceQueries
    ) { }

    public enum SearchStrategy { BFS, DFS, RANDOM, HEURISTIC }

    public record SearchContext(
        boolean hyperLtl,
        boolean fastestTrace,
        boolean ltl,
        boolean game,
        boolean timed,
        boolean liveQuery,
        boolean continuousOrDiscreteEngine,
        boolean tarjan,
        boolean someTrace,
        boolean reachabilityQuery,
        boolean explicitSearch,
        SearchStrategy selected
    ) { }

    public record SearchDecision(
        EnumSet<SearchStrategy> enabled,
        SearchStrategy selected,
        String label
    ) {
        public SearchDecision {
            enabled = enabled.isEmpty()
                ? EnumSet.noneOf(SearchStrategy.class)
                : EnumSet.copyOf(enabled);
        }

        public boolean isEnabled(SearchStrategy strategy) {
            return enabled.contains(strategy);
        }
    }

    public record TraceContext(
        boolean game,
        boolean timed,
        boolean nonStrictNet,
        boolean deadlock,
        boolean livePathRoot,
        boolean reachability,
        boolean ltlOrHyperLtl,
        TAPNQuery.TraceOption selected
    ) { }

    public record TraceDecision(
        EnumSet<TAPNQuery.TraceOption> enabled,
        TAPNQuery.TraceOption selected
    ) {
        public TraceDecision {
            enabled = enabled.isEmpty()
                ? EnumSet.noneOf(TAPNQuery.TraceOption.class)
                : EnumSet.copyOf(enabled);
        }

        public boolean isEnabled(TAPNQuery.TraceOption option) {
            return enabled.contains(option);
        }
    }

    public record TraceRefinementContext(
        boolean ctl,
        boolean game,
        boolean verifyPn,
        boolean startsWithAgOrEf,
        boolean inhibitorArcs,
        boolean nestedQuantifications
    ) { }

    public static boolean traceRefinementEnabled(TraceRefinementContext context) {
        return context.ctl()
            && !context.game()
            && context.verifyPn()
            && context.startsWithAgOrEf()
            && !context.inhibitorArcs()
            && !context.nestedQuantifications();
    }

    public record TarjanDecision(boolean visible, boolean enabled, boolean clearSelection) { }

    public static TarjanDecision tarjanDecision(int queryTypeIndex) {
        return switch (queryTypeIndex) {
            case 1 -> new TarjanDecision(true, true, false);
            case 2 -> new TarjanDecision(true, false, true);
            default -> new TarjanDecision(false, false, false);
        };
    }

    public record ExplicitSearchDecision(boolean enabled, boolean selected, boolean rememberedState) { }

    public static ExplicitSearchDecision explicitSearchDecision(
        boolean canUse,
        boolean currentlyEnabled,
        boolean currentlySelected,
        boolean rememberedState
    ) {
        if (canUse) {
            return new ExplicitSearchDecision(true, rememberedState, rememberedState);
        }

        return new ExplicitSearchDecision(
            false,
            false,
            currentlyEnabled ? currentlySelected : rememberedState
        );
    }

    public static boolean coloredReductionEnabled(boolean someTrace, boolean game) {
        return !someTrace && !game;
    }

    public record DiscreteInclusionDecision(boolean visible, boolean selectPlacesVisible) { }

    public static DiscreteInclusionDecision discreteInclusionDecision(boolean verifyTapn) {
        return new DiscreteInclusionDecision(verifyTapn, verifyTapn);
    }

    public record SymmetryDecision(boolean visible, boolean enabled, boolean selected) { }

    public static SymmetryDecision symmetryDecision(
        boolean reductionPresent,
        boolean disabledEngine,
        boolean approximationOrSomeTrace,
        boolean currentlyEnabled,
        boolean currentlySelected
    ) {
        if (!reductionPresent) {
            return new SymmetryDecision(false, currentlyEnabled, currentlySelected);
        }
        if (disabledEngine) {
            return new SymmetryDecision(true, false, currentlySelected);
        }
        if (approximationOrSomeTrace) {
            return new SymmetryDecision(true, false, false);
        }
        return new SymmetryDecision(true, true, currentlyEnabled ? currentlySelected : true);
    }

    public record ApproximationDecision(
        boolean skeletonEnabled,
        boolean skeletonSelected,
        boolean noApproximationEnabled,
        boolean noApproximationSelected,
        boolean overApproximationEnabled,
        boolean underApproximationEnabled,
        boolean denominatorEnabled
    ) { }

    public static ApproximationDecision approximationDecision(
        boolean deadlockOrLiveness,
        boolean game,
        boolean fastestTrace,
        boolean skeletonCurrentlyEnabled,
        boolean skeletonCurrentlySelected,
        boolean noApproximationCurrentlySelected
    ) {
        boolean skeletonEnabled = !deadlockOrLiveness;
        boolean skeletonSelected = skeletonEnabled
            ? (skeletonCurrentlyEnabled ? skeletonCurrentlySelected : true)
            : false;
        if (game || fastestTrace) {
            return new ApproximationDecision(
                skeletonEnabled,
                skeletonSelected,
                true,
                fastestTrace ? true : noApproximationCurrentlySelected,
                false,
                false,
                false
            );
        }
        return new ApproximationDecision(
            skeletonEnabled,
            skeletonSelected,
            true,
            noApproximationCurrentlySelected,
            true,
            true,
            true
        );
    }

    public static boolean stubbornReductionEnabled(boolean hyperLtl, boolean timeDarts) {
        return !hyperLtl && !timeDarts;
    }

    public record DiscreteDecision(
        boolean timeDartsForcedOff,
        boolean gcdForcedOff,
        boolean stubbornForcedOff,
        boolean symmetryForcedOff
    ) { }

    public static DiscreteDecision discreteDecision(
        boolean discreteEngine,
        boolean nonStrictNet,
        boolean urgentTransitions,
        boolean fastestTrace,
        boolean game,
        boolean deadlock,
        boolean liveQuery
    ) {
        if (!discreteEngine) {
            return new DiscreteDecision(false, false, false, false);
        }

        boolean liveness = liveQuery;
        boolean timeDartsForcedOff = urgentTransitions || fastestTrace || game
            || (nonStrictNet && liveness)
            || (deadlock && liveness);
        boolean gcdForcedOff = deadlock || liveness || game;
        boolean stubbornForcedOff = liveness;
        boolean symmetryForcedOff = deadlock && liveness;
        return new DiscreteDecision(timeDartsForcedOff, gcdForcedOff, stubbornForcedOff, symmetryForcedOff);
    }

    public static TraceDecision traceDecision(TraceContext context) {
        EnumSet<TAPNQuery.TraceOption> enabled = EnumSet.allOf(TAPNQuery.TraceOption.class);
        if (context.game()) {
            enabled.remove(TAPNQuery.TraceOption.SOME);
            enabled.remove(TAPNQuery.TraceOption.FASTEST);
        } else if (context.timed()) {
            if (!context.nonStrictNet() || context.deadlock() || context.livePathRoot()) {
                enabled.remove(TAPNQuery.TraceOption.FASTEST);
            }
        } else if (context.reachability() || context.ltlOrHyperLtl()) {
            enabled.remove(TAPNQuery.TraceOption.FASTEST);
        } else {
            enabled.clear();
        }

        TAPNQuery.TraceOption selected = context.selected();
        if (enabled.isEmpty()) {
            selected = TAPNQuery.TraceOption.NONE;
        } else if (selected == TAPNQuery.TraceOption.FASTEST && !enabled.contains(selected)) {
            selected = enabled.contains(TAPNQuery.TraceOption.SOME)
                ? TAPNQuery.TraceOption.SOME
                : TAPNQuery.TraceOption.NONE;
        }
        return new TraceDecision(enabled, selected);
    }

    public static SearchDecision searchDecision(SearchContext context) {
        EnumSet<SearchStrategy> enabled = EnumSet.allOf(SearchStrategy.class);

        if (context.hyperLtl()) {
            enabled.remove(SearchStrategy.BFS);
            enabled.remove(SearchStrategy.RANDOM);
            enabled.remove(SearchStrategy.HEURISTIC);
        } else if (context.fastestTrace()) {
            enabled.clear();
        } else if (context.ltl()) {
            enabled.remove(SearchStrategy.BFS);
            if (!context.tarjan() && context.someTrace()) enabled.remove(SearchStrategy.RANDOM);
        }

        if (context.game()) {
            enabled.remove(SearchStrategy.HEURISTIC);
        } else if (context.timed() && context.liveQuery()) {
            enabled.remove(SearchStrategy.BFS);
            if (!context.continuousOrDiscreteEngine()) enabled.remove(SearchStrategy.HEURISTIC);
        }

        SearchStrategy selected = context.selected();
        if (!context.fastestTrace() && !enabled.contains(selected)) {
            selected = enabled.contains(SearchStrategy.HEURISTIC)
                ? SearchStrategy.HEURISTIC
                : SearchStrategy.DFS;
        }

        String label = !context.timed() && !context.game() && context.reachabilityQuery()
            && !context.explicitSearch()
            ? "Random heuristic    "
            : "Heuristic    ";
        return new SearchDecision(enabled, selected, label);
    }

    public static EnumSet<EngineFeature> requiredFeatures(Context context) {
        EnumSet<EngineFeature> required = EnumSet.noneOf(EngineFeature.class);

        if (context.fastestTrace()) required.add(EngineFeature.FASTEST_TRACE);
        if (context.deadlock() && context.hasEfOrAg() && !context.degreeGreaterThanTwo()) {
            required.add(EngineFeature.DEADLOCK_NET_DEGREE_2_EXP);
        }
        if (context.deadlock() && context.hasEgOrAf()) {
            required.add(EngineFeature.DEADLOCK_EG_OR_AF);
        }
        if (context.deadlock() && context.inhibitorArcs()) {
            required.add(EngineFeature.DEADLOCK_WITH_INHIB);
        }
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

    public static List<EngineSupportOptions> compatibleEngines(
        Context context,
        Collection<EngineSupportOptions> catalog
    ) {
        return compatibleEngines(
            context.timed(),
            context.stochastic(),
            requiredFeatures(context),
            catalog
        );
    }

    public static List<EngineSupportOptions> compatibleEngines(
        boolean timed,
        boolean stochastic,
        Set<EngineFeature> requiredFeatures,
        Collection<EngineSupportOptions> catalog
    ) {
        if (!timed) {
            return catalog.stream()
                .filter(engine -> engine.getNameString().equals(EngineSupportOptions.UNTIMED_ENGINE_NAME))
                .toList();
        }

        return catalog.stream()
            .filter(engine -> engine.areOptionsSupported(requiredFeatures))
            .filter(engine -> !(stochastic && engine.getNameString().equals(EngineSupportOptions.CONTINUOUS_ENGINE_NAME)))
            .toList();
    }
}
