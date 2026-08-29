package dk.aau.cs.verification;

import java.util.HashSet;

import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.Tuple;

/**
 * Composes the domain model without constructing or copying a diagram.
 *
 * <p>The GUI-aware {@link TAPNComposer} still exists for export and trace
 * presentation. Verification and model analysis can use this composer so the
 * model path has no dependency on Swing graphics or {@code DataLayer}.</p>
 */
public final class TAPNModelComposer implements ITAPNComposer {
    private final Messenger messenger;
    private final boolean singleComponentNoPrefix;
    private final boolean inlineConstants;
    private HashSet<String> processedSharedObjects;
    private boolean hasShownMessage;

    public TAPNModelComposer(boolean singleComponentNoPrefix) {
        this(null, singleComponentNoPrefix, true);
    }

    public TAPNModelComposer(Messenger messenger, boolean singleComponentNoPrefix) {
        this(messenger, singleComponentNoPrefix, true);
    }

    public TAPNModelComposer(
        Messenger messenger,
        boolean singleComponentNoPrefix,
        boolean inlineConstants
    ) {
        this.messenger = messenger;
        this.singleComponentNoPrefix = singleComponentNoPrefix;
        this.inlineConstants = inlineConstants;
    }

    @Override
    public Tuple<TimedArcPetriNet, NameMapping> transformModel(TimedArcPetriNetNetwork model) {
        processedSharedObjects = new HashSet<>();
        hasShownMessage = false;

        TimedArcPetriNet composedModel = new TimedArcPetriNet("ComposedModel");
        NameMapping mapping = new NameMapping();
        composedModel.setParentNetwork(model);

        createSharedPlaces(model, composedModel, mapping);
        createPlaces(model, composedModel, mapping);
        createTransitions(model, composedModel, mapping);
        createInputArcs(model, composedModel, mapping);
        createOutputArcs(model, composedModel, mapping);
        createTransportArcs(model, composedModel, mapping);
        createInhibitorArcs(model, composedModel, mapping);

        return new Tuple<>(composedModel, mapping);
    }

    private void createSharedPlaces(
        TimedArcPetriNetNetwork model,
        TimedArcPetriNet composedModel,
        NameMapping mapping
    ) {
        for (SharedPlace place : model.sharedPlaces()) {
            if (!model.isSharedPlaceUsedInTemplates(place)) {
                continue;
            }

            String uniquePlaceName = composedPlaceName(place);
            LocalTimedPlace composedPlace = copyPlace(uniquePlaceName, place);
            composedModel.add(composedPlace);
            mapping.addMappingForShared(place.name(), uniquePlaceName);
            copyTokens(place, composedPlace);
        }
    }

    private void createPlaces(
        TimedArcPetriNetNetwork model,
        TimedArcPetriNet composedModel,
        NameMapping mapping
    ) {
        for (TimedArcPetriNet template : model.activeTemplates()) {
            for (TimedPlace timedPlace : template.places()) {
                if (timedPlace.isShared()) {
                    continue;
                }

                String uniquePlaceName = (!singleComponentNoPrefix || model.activeTemplates().size() > 1)
                    ? template.name() + "__" + timedPlace.name()
                    : timedPlace.name();
                LocalTimedPlace composedPlace = copyPlace(uniquePlaceName, timedPlace);
                composedModel.add(composedPlace);
                mapping.addMapping(template.name(), timedPlace.name(), uniquePlaceName);
                copyTokens(timedPlace, composedPlace);
            }
        }
    }

    private LocalTimedPlace copyPlace(String name, TimedPlace source) {
        TimeInvariant invariant = source.invariant();
        if (!(invariant.upperBound() instanceof Bound.InfBound)) {
            invariant = new TimeInvariant(
                invariant.isUpperNonstrict(),
                new IntBound(invariant.upperBound().value())
            );
        }

        LocalTimedPlace copy = new LocalTimedPlace(name, invariant, source.getColorType());
        copy.setCtiList(source.getCtiList());
        copy.setTokenExpression(source.getTokensAsExpression());
        return copy;
    }

    private void copyTokens(TimedPlace source, LocalTimedPlace destination) {
        for (TimedToken token : source.tokens()) {
            destination.addToken(new TimedToken(destination, token.age(), token.color()));
        }
    }

    private void createTransitions(
        TimedArcPetriNetNetwork model,
        TimedArcPetriNet composedModel,
        NameMapping mapping
    ) {
        for (TimedArcPetriNet template : model.activeTemplates()) {
            for (TimedTransition timedTransition : template.transitions()) {
                if (processedSharedObjects.contains(timedTransition.name())) {
                    continue;
                }

                if (timedTransition.isOrphan()) {
                    reportOrphanTransition();
                    continue;
                }

                String uniqueTransitionName = composedTransitionName(timedTransition, model);
                TimedTransition transition = new TimedTransition(
                    uniqueTransitionName,
                    timedTransition.isUrgent(),
                    timedTransition.getGuard(),
                    timedTransition.getDistribution(),
                    timedTransition.getWeight(),
                    timedTransition.getFiringMode()
                );
                transition.setUncontrollable(timedTransition.isUncontrollable());
                composedModel.add(transition);

                if (timedTransition.isShared()) {
                    String name = timedTransition.sharedTransition().name();
                    processedSharedObjects.add(name);
                    mapping.addMappingForShared(name, uniqueTransitionName);
                } else {
                    mapping.addMapping(template.name(), timedTransition.name(), uniqueTransitionName);
                }
            }
        }
    }

    private String composedTransitionName(TimedTransition transition, TimedArcPetriNetNetwork model) {
        if (transition.isShared()) {
            return "Shared__" + transition.name();
        }
        if (singleComponentNoPrefix && model.activeTemplates().size() == 1) {
            return transition.name();
        }
        return transition.model().name() + "__" + transition.name();
    }

    private void createInputArcs(
        TimedArcPetriNetNetwork model,
        TimedArcPetriNet composedModel,
        NameMapping mapping
    ) {
        for (TimedArcPetriNet template : model.activeTemplates()) {
            for (TimedInputArc arc : template.inputArcs()) {
                String sourceTemplate = arc.source().isShared() ? "" : template.name();
                String targetTemplate = arc.destination().isShared() ? "" : template.name();
                TimedPlace source = composedModel.getPlaceByName(
                    mapping.map(sourceTemplate, arc.source().name())
                );
                TimedTransition target = composedModel.getTransitionByName(
                    mapping.map(targetTemplate, arc.destination().name())
                );

                TimeInterval interval = copyInterval(arc.interval());
                TimedInputArc composedArc = new TimedInputArc(
                    source,
                    target,
                    interval,
                    arc.getWeightValue(),
                    arc.getArcExpression()
                );
                composedArc.setColorTimeIntervals(arc.getColorTimeIntervals());
                composedModel.add(composedArc);
            }
        }
    }

    private void createOutputArcs(
        TimedArcPetriNetNetwork model,
        TimedArcPetriNet composedModel,
        NameMapping mapping
    ) {
        for (TimedArcPetriNet template : model.activeTemplates()) {
            for (TimedOutputArc arc : template.outputArcs()) {
                String sourceTemplate = arc.source().isShared() ? "" : template.name();
                String destinationTemplate = arc.destination().isShared() ? "" : template.name();
                TimedTransition source = composedModel.getTransitionByName(
                    mapping.map(sourceTemplate, arc.source().name())
                );
                TimedPlace destination = composedModel.getPlaceByName(
                    mapping.map(destinationTemplate, arc.destination().name())
                );

                composedModel.add(new TimedOutputArc(
                    source,
                    destination,
                    arc.getWeightValue(),
                    arc.getExpression()
                ));
            }
        }
    }

    private void createTransportArcs(
        TimedArcPetriNetNetwork model,
        TimedArcPetriNet composedModel,
        NameMapping mapping
    ) {
        for (TimedArcPetriNet template : model.activeTemplates()) {
            for (TransportArc arc : template.transportArcs()) {
                String sourceTemplate = arc.source().isShared() ? "" : template.name();
                String transitionTemplate = arc.transition().isShared() ? "" : template.name();
                String destinationTemplate = arc.destination().isShared() ? "" : template.name();

                TimedPlace source = composedModel.getPlaceByName(
                    mapping.map(sourceTemplate, arc.source().name())
                );
                TimedTransition transition = composedModel.getTransitionByName(
                    mapping.map(transitionTemplate, arc.transition().name())
                );
                TimedPlace destination = composedModel.getPlaceByName(
                    mapping.map(destinationTemplate, arc.destination().name())
                );

                TransportArc composedArc = new TransportArc(
                    source,
                    transition,
                    destination,
                    copyInterval(arc.interval()),
                    arc.getWeightValue(),
                    arc.getInputExpression().deepCopy(),
                    arc.getOutputExpression().deepCopy()
                );
                composedArc.setColorTimeIntervals(arc.getColorTimeIntervals());
                composedModel.add(composedArc);
            }
        }
    }

    private void createInhibitorArcs(
        TimedArcPetriNetNetwork model,
        TimedArcPetriNet composedModel,
        NameMapping mapping
    ) {
        for (TimedArcPetriNet template : model.activeTemplates()) {
            for (TimedInhibitorArc arc : template.inhibitorArcs()) {
                String sourceTemplate = arc.source().isShared() ? "" : template.name();
                String destinationTemplate = arc.destination().isShared() ? "" : template.name();
                TimedPlace source = composedModel.getPlaceByName(
                    mapping.map(sourceTemplate, arc.source().name())
                );
                TimedTransition target = composedModel.getTransitionByName(
                    mapping.map(destinationTemplate, arc.destination().name())
                );

                composedModel.add(new TimedInhibitorArc(
                    source,
                    target,
                    copyInterval(arc.interval()),
                    arc.getWeightValue(),
                    arc.getArcExpression()
                ));
            }
        }
    }

    private TimeInterval copyInterval(TimeInterval interval) {
        TimeInterval copy = new TimeInterval(interval);
        if (inlineConstants) {
            copy.setLowerBound(new IntBound(copy.lowerBound().value()));
            if (copy.upperBound() instanceof Bound.InfBound) {
                copy.setUpperBound(copy.upperBound());
            } else {
                copy.setUpperBound(new IntBound(copy.upperBound().value()));
            }
        }
        return copy;
    }

    private void reportOrphanTransition() {
        if (!hasShownMessage && messenger != null) {
            messenger.displayInfoMessage(
                "There are orphan transitions (no incoming and no outgoing arcs) in the model."
            );
            hasShownMessage = true;
        }
    }

    @Override
    public String composedTransitionName(TimedTransition transition) {
        if (transition.isShared()) {
            return "Shared__" + transition.name();
        }
        if (singleComponentNoPrefix) {
            return transition.name();
        }
        return transition.model().name() + "__" + transition.name();
    }

    @Override
    public String composedPlaceName(TimedPlace place) {
        if (place.isShared()) {
            return "Shared__" + place.name();
        }
        if (singleComponentNoPrefix) {
            return place.name();
        }
        return ((LocalTimedPlace) place).model().name() + "__" + place.name();
    }
}
