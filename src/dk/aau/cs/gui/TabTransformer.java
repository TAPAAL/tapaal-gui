package dk.aau.cs.gui;

import dk.aau.cs.model.tapn.*;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Template;
import pipe.gui.graphicElements.*;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;

import java.util.ArrayList;

public class TabTransformer {
    static public void removeTimingInformation(TabContent tab){
        for(Template template : tab.allTemplates()){
            ArrayList<TimedTransportArcComponent> transportArcComponents = new ArrayList<TimedTransportArcComponent>();
            // Make place token age invariant infinite
            for(TimedPlace place : template.model().places()){
                place.setInvariant(TimeInvariant.LESS_THAN_INFINITY);
            }
            // Make transitions non-urgent
            for(TimedTransition transition : template.model().transitions()){
                transition.setUrgent(false);
            }

            for(Arc arc : template.guiModel().getArcs()){
                // Make output arc guards infinite
                if(arc instanceof TimedOutputArcComponent) {
                    TimedOutputArcComponent arcComp = (TimedOutputArcComponent) arc;
                    arcComp.setGuardAndWeight(TimeInterval.ZERO_INF, arcComp.getWeight());
                }

                // Add and process transport arcs in separate list to avoid delete errors
                if(arc instanceof TimedTransportArcComponent){
                    TimedTransportArcComponent arcComp = (TimedTransportArcComponent) arc;
                    transportArcComponents.add(arcComp);
                }
            }

            // Replace transport arcs with regular arcs
            for(TimedTransportArcComponent arc : transportArcComponents){
                // Input arc
                if(arc.getSource() instanceof Place) {
                    TimedPlace source = template.model().getPlaceByName(arc.getSource().getName());
                    TimedTransition destination = template.model().getTransitionByName(arc.getTarget().getName());

                    TimedInputArc addedArc = new TimedInputArc(source, destination, TimeInterval.ZERO_INF, arc.getWeight());
                    template.model().add(addedArc);

                    // GUI
                    DataLayer guiModel = template.guiModel();
                    Place guiSource = guiModel.getPlaceByName(arc.getSource().getName());
                    Transition guiTarget = guiModel.getTransitionByName(arc.getTarget().getName());
                    Arc newArc = new TimedInputArcComponent(new TimedOutputArcComponent(
                            0d,
                            0d,
                            0d,
                            0d,
                            guiSource,
                            guiTarget,
                            arc.getWeight().value(),
                            arc.getSource().getName() + "_to_" + arc.getTarget().getName(),
                            false
                    ));

                    // Build ArcPath
                    Place oldGuiSource = guiModel.getPlaceByName(arc.getSource().getName());
                    Transition oldGuiTarget = guiModel.getTransitionByName(arc.getTarget().getName());
                    ArcPath newArcPath = createArcPath(guiModel, oldGuiSource, oldGuiTarget, newArc);

                    // Set arcPath, guiModel and connectors
                    ((TimedInputArcComponent) newArc).setUnderlyingArc(addedArc);
                    newArc.setArcPath(newArcPath);
                    newArc.updateArcPosition();
                    guiModel.addPetriNetObject(newArc);
                    guiSource.addConnectFrom(newArc);
                    guiTarget.addConnectTo(newArc);

                }
                // Output arc
                else if(arc.getSource() instanceof Transition){
                    TimedPlace destination = template.model().getPlaceByName(arc.getTarget().getName());
                    TimedTransition source = template.model().getTransitionByName(arc.getSource().getName());

                    TimedOutputArc addedArc = new TimedOutputArc(source, destination, arc.getWeight());
                    template.model().add(addedArc);

                    // GUI
                    DataLayer guiModel = template.guiModel();
                    Place guiTarget = guiModel.getPlaceByName(arc.getTarget().getName());
                    Transition guiSource = guiModel.getTransitionByName(arc.getSource().getName());
                    Arc newArc = new TimedOutputArcComponent(
                            0d,
                            0d,
                            0d,
                            0d,
                            guiSource,
                            guiTarget,
                            arc.getWeight().value(),
                            arc.getSource().getName() + "_to_" + arc.getTarget().getName(),
                            false
                    );

                    // Build ArcPath
                    Place oldGuiTarget = guiModel.getPlaceByName(arc.getTarget().getName());
                    Transition oldGuiSource = guiModel.getTransitionByName(arc.getSource().getName());
                    ArcPath newArcPath = createArcPath(guiModel, oldGuiSource, oldGuiTarget, newArc);

                    // Set arcPath, guiModel and connectors
                    ((TimedOutputArcComponent) newArc).setUnderlyingArc(addedArc);
                    newArc.setArcPath(newArcPath);
                    newArc.updateArcPosition();
                    guiModel.addPetriNetObject(newArc);
                    guiSource.addConnectFrom(newArc);
                    guiTarget.addConnectTo(newArc);

                    // Delete the transport arc
                    arc.delete();
                }
            }
        }
    }

    private static ArcPath createArcPath(DataLayer currentGuiModel, PlaceTransitionObject source, PlaceTransitionObject target, Arc arc) {
        Arc guiArc = currentGuiModel.getArcByEndpoints(source, target);
        ArcPath arcPath = guiArc.getArcPath();
        int arcPathPointsNum = arcPath.getNumPoints();

        // Build ArcPath
        ArcPath newArcPath = new ArcPath(arc);
        for(int k = 0; k < arcPathPointsNum; k++) {
            ArcPathPoint point = arcPath.getArcPathPoint(k);
            newArcPath.addPoint(
                    point.getPoint().x,
                    point.getPoint().y,
                    point.getPointType()
            );
        }

        return newArcPath;
    }
}
