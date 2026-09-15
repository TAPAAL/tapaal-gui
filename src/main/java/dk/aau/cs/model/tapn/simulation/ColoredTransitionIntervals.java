package dk.aau.cs.model.tapn.simulation;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.Expressions.ExpressionContext;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.util.IntervalOperations;

import java.math.BigDecimal;
import java.util.*;

public final class ColoredTransitionIntervals {
    public static TimeInterval calculate(TimedTransition transition, List<Map<Variable, Color>> bindings, boolean urgentTransitionEnabled) {
        Map<String, ColorType> types = new HashMap<>();
        for (var type : transition.model().parentNetwork().colorTypes()) {
            types.put(type.getName(), type);
        }

        List<TimeInterval> result = new ArrayList<>();
        for (var binding : bindings) {
            Map<String, Color> values = new HashMap<>();
            binding.forEach((variable, color) -> values.put(variable.getName(), color));
            var context = new ExpressionContext(values, types);
            List<TimeInterval> intervals = List.of(TimeInterval.ZERO_INF);
            var transitions = transition.isShared()
                ? transition.sharedTransition().transitions() : List.of(transition);
            for (var member : transitions) {
                for (var arc : member.getInputArcs()) {
                    for (var requirement : arc.getArcExpression().eval(context).entrySet()) {
                        intervals = IntervalOperations.intersectingInterval(intervals,
                            tokenIntervals(arc.source(), requirement.getKey(), requirement.getValue(),
                                intervalFor(arc.interval(), arc.getColorTimeIntervals(), requirement.getKey())));
                    }
                }

                for (var arc : member.getTransportArcsGoingThrough()) {
                    for (var requirement : arc.getInputExpression().eval(context).entrySet()) {
                        var color = requirement.getKey();
                        var interval = IntervalOperations.intersection(
                            intervalFor(arc.interval(), arc.getColorTimeIntervals(), color),
                            arc.destination().invariantFor(color).asIterval());
                        intervals = IntervalOperations.intersectingInterval(intervals,
                            tokenIntervals(arc.source(), color, requirement.getValue(), interval));
                    }
                }
            }

            result = IntervalOperations.unionIntervalSequences(result, intervals);
        }

        for (var template : transition.model().parentNetwork().activeTemplates()) {
            for (var place : template.places()) {
                for (var token : place.tokens()) {
                    var invariant = place.invariantFor(token.color());
                    if (!(invariant.upperBound() instanceof Bound.InfBound)) {
                        result = IntervalOperations.intersectingInterval(result,
                            List.of(invariant.subtractToken(token.age())));
                    }
                }
            }
        }

        if (urgentTransitionEnabled) {
            result = IntervalOperations.intersectingInterval(result,
                List.of(new TimeInterval(true, new IntBound(0), new IntBound(0), true)));
        }

        return result.isEmpty() ? null : result.get(0);
    }

    private static TimeInterval intervalFor(TimeInterval fallback, List<ColoredTimeInterval> intervals, Color color) {
        for (var interval : intervals) {
            if (interval.getColor().equals(color)) return interval;
            if (interval.getColor().equals(Color.STAR_COLOR)) fallback = interval;
        }

        return fallback;
    }

    private static List<TimeInterval> tokenIntervals(TimedPlace place, Color color, int count, TimeInterval interval) {
        if (count <= 0) return List.of(TimeInterval.ZERO_INF);
        if (interval == null) return List.of();
        var tokens = new ArrayList<TimedToken>();
        for (var token : place.tokens()) {
            if (token.color().equals(color)) tokens.add(token);
        }

        tokens.sort(Comparator.comparing(TimedToken::age).reversed());
        List<TimeInterval> result = new ArrayList<>();
        for (int i = 0; i + count <= tokens.size(); i++) {
            BigDecimal lower = IntervalOperations.getRatBound(interval.lowerBound()).getBound()
                .subtract(tokens.get(i + count - 1).age());
            boolean lowerIncluded = lower.signum() < 0 || interval.isLowerBoundNonStrict();
            lower = lower.max(BigDecimal.ZERO);
            Bound upper = interval.upperBound();
            if (!(upper instanceof Bound.InfBound)) {
                BigDecimal value = IntervalOperations.getRatBound(upper).getBound().subtract(tokens.get(i).age());
                int comparison = value.compareTo(lower);
                if (comparison < 0 || comparison == 0 && !(lowerIncluded && interval.isUpperBoundNonStrict())) continue;
                upper = new RatBound(value);
            }

            var window = new TimeInterval(lowerIncluded, new RatBound(lower), upper, interval.isUpperBoundNonStrict());
            result = IntervalOperations.unionIntervalSequences(result, List.of(window));
        }
        
        return result;
    }
}
