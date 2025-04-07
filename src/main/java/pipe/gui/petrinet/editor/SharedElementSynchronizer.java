package pipe.gui.petrinet.editor;

import javax.swing.JOptionPane;

import dk.aau.cs.model.tapn.*;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.graphicElements.ArcPath;
import pipe.gui.petrinet.graphicElements.PlaceTransitionObject;
import pipe.gui.petrinet.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransportArcComponent;
import net.tapaal.gui.petrinet.Template;

public class SharedElementSynchronizer {
    /**
     * @param obj the place or transition to syncrhonize
     * @return true if the arcs were synchronized successfully, false otherwise
     */
    public static boolean updateSharedArcs(PlaceTransitionObject obj) {
        PetriNetTab currentTab = TAPAALGUI.getCurrentTab();
        try {
            currentTab.guiModelManager.startTransaction();
            if (obj instanceof TimedTransitionComponent) {
                updateSharedTransitionArcs((TimedTransitionComponent)obj);
            } else if (obj instanceof TimedPlaceComponent) {
                updateSharedPlaceArcs((TimedPlaceComponent)obj);
            }

            currentTab.guiModelManager.commit();
            return true;
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(
                TAPAALGUI.getApp(),
                "Arc conflicts with an existing arc in one of the other components.\n Delete the arc in all but one of the components to resolve the conflict.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            currentTab.guiModelManager.abort();
        }
    }

    /**
     * Update and synchronize arcs for a shared transition across templates
     */
    private static void updateSharedTransitionArcs(TimedTransitionComponent transition) {
        PetriNetTab currentTab = TAPAALGUI.getCurrentTab();
        Template currentTemplate = currentTab.currentTemplate();
        TimedTransition sourceTransition = transition.underlyingTransition();
        
        for (Template template : currentTab.allTemplates()) {
            if (template.equals(currentTemplate)) continue;
    
            TimedTransition templateTransition = template.model().getTransitionByName(sourceTransition.name());
            if (templateTransition == null) continue;

            syncTransitionArcsFromSourceToTarget(currentTab, currentTemplate, template, 
                sourceTransition, templateTransition, false, transition);
            syncTransitionArcsFromSourceToTarget(currentTab, template, currentTemplate, 
                templateTransition, sourceTransition, true, transition);
        }
    }
    
    /**
     * Update and synchronize arcs for a shared place across templates
     */
    private static void updateSharedPlaceArcs(TimedPlaceComponent place) {
        PetriNetTab currentTab = TAPAALGUI.getCurrentTab();
        Template currentTemplate = currentTab.currentTemplate();
        TimedPlace sourcePlace = place.underlyingPlace();

        for (Template template : currentTab.allTemplates()) {
            if (template.equals(currentTemplate)) continue;
    
            TimedPlace templatePlace = template.model().getPlaceByName(sourcePlace.name());
            if (templatePlace == null) continue;
    
            syncPlaceArcsFromSourceToTarget(currentTab, currentTemplate, template, 
                sourcePlace, templatePlace, false, place);
            syncPlaceArcsFromSourceToTarget(currentTab, template, currentTemplate, 
                templatePlace, sourcePlace, true, place);
        }
    }
    
    private static void syncTransitionArcsFromSourceToTarget(
        PetriNetTab currentTab,
        Template sourceTemplate, 
        Template targetTemplate, 
        TimedTransition sourceTransition, 
        TimedTransition targetTransition,
        boolean isCurrentTemplateTarget,
        TimedTransitionComponent currentTransition
    ) {
        for (TimedPlace sourcePlace : sourceTemplate.model().places()) {
            if (!sourcePlace.isShared()) continue;
            
            TimedPlace targetPlace = targetTemplate.model().getPlaceByName(sourcePlace.name());
            if (targetPlace == null) continue;
            
            throwIfConflict(sourceTemplate, targetTemplate, sourcePlace, targetPlace, sourceTransition, targetTransition);

            syncInputArc(currentTab, sourceTemplate, targetTemplate, sourcePlace, targetPlace, 
                        sourceTransition, targetTransition, isCurrentTemplateTarget, currentTransition, null);
            
            syncOutputArc(currentTab, sourceTemplate, targetTemplate, sourcePlace, targetPlace,
                        sourceTransition, targetTransition, isCurrentTemplateTarget, currentTransition, null);
            
            syncInhibitorArc(currentTab, sourceTemplate, targetTemplate, sourcePlace, targetPlace,
                        sourceTransition, targetTransition, isCurrentTemplateTarget, currentTransition, null);
            
            syncTransportArcs(currentTab, sourceTemplate, targetTemplate, sourcePlace, targetPlace,
                        sourceTransition, targetTransition, isCurrentTemplateTarget, currentTransition, null);
        }
    }
    
    private static void syncPlaceArcsFromSourceToTarget(
        PetriNetTab currentTab,
        Template sourceTemplate, 
        Template targetTemplate, 
        TimedPlace sourcePlace, 
        TimedPlace targetPlace,
        boolean isCurrentTemplateTarget,
        TimedPlaceComponent currentPlace
    ) {
        for (TimedTransition sourceTransition : sourceTemplate.model().transitions()) {
            if (!sourceTransition.isShared()) continue;
            
            TimedTransition targetTransition = targetTemplate.model().getTransitionByName(sourceTransition.name());
            if (targetTransition == null) continue;

            throwIfConflict(sourceTemplate, targetTemplate, sourcePlace, targetPlace, sourceTransition, targetTransition);

            syncInputArc(currentTab, sourceTemplate, targetTemplate, sourcePlace, targetPlace, 
                    sourceTransition, targetTransition, isCurrentTemplateTarget, null, currentPlace);
            
            syncOutputArc(currentTab, sourceTemplate, targetTemplate, sourcePlace, targetPlace,
                    sourceTransition, targetTransition, isCurrentTemplateTarget, null, currentPlace);
            
            syncInhibitorArc(currentTab, sourceTemplate, targetTemplate, sourcePlace, targetPlace,
                    sourceTransition, targetTransition, isCurrentTemplateTarget, null, currentPlace);
            
            syncTransportArcs(currentTab, sourceTemplate, targetTemplate, sourcePlace, targetPlace,
                    sourceTransition, targetTransition, isCurrentTemplateTarget, null, currentPlace);
        }
    }

    private static void throwIfConflict(
        Template sourceTemplate, 
        Template targetTemplate,
        TimedPlace sourcePlace, 
        TimedPlace targetPlace,
        TimedTransition sourceTransition, 
        TimedTransition targetTransition
    ) {
        boolean inputArcConflict = 
            targetTemplate.model().hasArcFromPlaceToTransition(targetPlace, targetTransition) &&
            sourceTemplate.model().hasArcFromPlaceToTransition(sourcePlace, sourceTransition);
        
        boolean outputArcConflict = 
            targetTemplate.model().hasArcFromTransitionToPlace(targetTransition, targetPlace) &&
            sourceTemplate.model().hasArcFromTransitionToPlace(sourceTransition, sourcePlace);
        
        if (inputArcConflict || outputArcConflict) {
            throw new IllegalStateException("Arc already exists");
        }
    }
    
    private static void syncInputArc(
        PetriNetTab currentTab,
        Template sourceTemplate, Template targetTemplate,
        TimedPlace sourcePlace, TimedPlace targetPlace,
        TimedTransition sourceTransition, TimedTransition targetTransition,
        boolean isCurrentTemplateTarget,
        TimedTransitionComponent currentTransition,
        TimedPlaceComponent currentPlace
    ) {
        TimedInputArc inputArc = sourceTemplate.model().getInputArcFromPlaceToTransition(sourcePlace, sourceTransition);
        if (inputArc == null || targetTemplate.model().getInputArcFromPlaceToTransition(targetPlace, targetTransition) != null) {
            return;
        }
        
        TimedTransitionComponent guiTransition = (TimedTransitionComponent)targetTemplate.guiModel().getTransitionByName(sourceTransition.name());
        TimedPlaceComponent guiPlace = (TimedPlaceComponent)targetTemplate.guiModel().getPlaceByName(sourcePlace.name());

        if (guiPlace == null || guiTransition == null) return;

        ArcPath arcPath = new ArcPath();
        
        int startX, startY;
        if (isCurrentTemplateTarget && currentPlace != null) {
            startX = currentPlace.getPositionX();
            startY = currentPlace.getPositionY();
        } else {
            startX = guiPlace.getPositionX();
            startY = guiPlace.getPositionY();
        }

        arcPath.addPoint(startX, startY, false);
        
        int endX, endY;
        if (isCurrentTemplateTarget && currentTransition != null) {
            endX = currentTransition.getPositionX();
            endY = currentTransition.getPositionY();
        } else {
            endX = guiTransition.getPositionX();
            endY = guiTransition.getPositionY();
        }
        arcPath.addPoint(endX, endY, false);
        
        TimedInputArcComponent newArc = currentTab.guiModelManager.addTimedInputArc(targetTemplate.guiModel(), guiPlace, guiTransition, arcPath).getResult();
        TimedInputArc newInputArc = newArc.underlyingTimedInputArc();
        newInputArc.setWeight(inputArc.getWeight());
        newInputArc.setTimeInterval(inputArc.interval());
        newInputArc.setExpression(inputArc.getArcExpression());
        newInputArc.setColorTimeIntervals(inputArc.getColorTimeIntervals());
        newArc.updateLabel(true);
    }
    
    private static void syncOutputArc(
        PetriNetTab currentTab,
        Template sourceTemplate, Template targetTemplate,
        TimedPlace sourcePlace, TimedPlace targetPlace,
        TimedTransition sourceTransition, TimedTransition targetTransition,
        boolean isCurrentTemplateTarget,
        TimedTransitionComponent currentTransition,
        TimedPlaceComponent currentPlace
    ) {
        TimedOutputArc outputArc = sourceTemplate.model().getOutputArcFromTransitionAndPlace(sourceTransition, sourcePlace);
        if (outputArc == null || targetTemplate.model().getOutputArcFromTransitionAndPlace(targetTransition, targetPlace) != null) {
            return;
        }
        
        TimedTransitionComponent guiTransition = (TimedTransitionComponent)targetTemplate.guiModel().getTransitionByName(sourceTransition.name());
        TimedPlaceComponent guiPlace = (TimedPlaceComponent)targetTemplate.guiModel().getPlaceByName(sourcePlace.name());

        if (guiPlace == null || guiTransition == null) return;

        ArcPath arcPath = new ArcPath();
        
        int startX, startY;
        if (isCurrentTemplateTarget && currentTransition != null) {
            startX = currentTransition.getPositionX();
            startY = currentTransition.getPositionY();
        } else {
            startX = guiTransition.getPositionX();
            startY = guiTransition.getPositionY();
        }

        arcPath.addPoint(startX, startY, false);
        
        int endX, endY;
        if (isCurrentTemplateTarget && currentPlace != null) {
            endX = currentPlace.getPositionX();
            endY = currentPlace.getPositionY();
        } else {
            endX = guiPlace.getPositionX();
            endY = guiPlace.getPositionY();
        }
        arcPath.addPoint(endX, endY, false);
        
        TimedOutputArcComponent newArc = currentTab.guiModelManager.addTimedOutputArc(targetTemplate.guiModel(), guiTransition, guiPlace, arcPath).getResult();
        TimedOutputArc newOutputArc = newArc.underlyingArc();
        newOutputArc.setWeight(outputArc.getWeight());
        newOutputArc.setExpression(outputArc.getExpression());
        newArc.updateLabel(true);
    }
    
    private static void syncInhibitorArc(
        PetriNetTab currentTab,
        Template sourceTemplate, Template targetTemplate,
        TimedPlace sourcePlace, TimedPlace targetPlace,
        TimedTransition sourceTransition, TimedTransition targetTransition,
        boolean isCurrentTemplateTarget,
        TimedTransitionComponent currentTransition,
        TimedPlaceComponent currentPlace
    ) {
        TimedInhibitorArc inhibitorArc = sourceTemplate.model().getInhibitorArcFromPlaceAndTransition(sourcePlace, sourceTransition);
        if (inhibitorArc == null || targetTemplate.model().getInhibitorArcFromPlaceAndTransition(targetPlace, targetTransition) != null) {
            return;
        }
        
        TimedTransitionComponent guiTransition = (TimedTransitionComponent)targetTemplate.guiModel().getTransitionByName(sourceTransition.name());
        TimedPlaceComponent guiPlace = (TimedPlaceComponent)targetTemplate.guiModel().getPlaceByName(sourcePlace.name());

        if (guiPlace == null || guiTransition == null) return;

        ArcPath arcPath = new ArcPath();
        
        int startX, startY;
        if (isCurrentTemplateTarget && currentPlace != null) {
            startX = currentPlace.getPositionX();
            startY = currentPlace.getPositionY();
        } else {
            startX = guiPlace.getPositionX();
            startY = guiPlace.getPositionY();
        }
        arcPath.addPoint(startX, startY, false);
        
        int endX, endY;
        if (isCurrentTemplateTarget && currentTransition != null) {
            endX = currentTransition.getPositionX();
            endY = currentTransition.getPositionY();
        } else {
            endX = guiTransition.getPositionX();
            endY = guiTransition.getPositionY();
        }
        arcPath.addPoint(endX, endY, false);
        
        currentTab.guiModelManager.addInhibitorArc(targetTemplate.guiModel(), guiPlace, guiTransition, arcPath);
    }
    
    private static void syncTransportArcs(
        PetriNetTab currentTab,
        Template sourceTemplate, Template targetTemplate,
        TimedPlace sourcePlace, TimedPlace targetPlace,
        TimedTransition sourceTransition, TimedTransition targetTransition,
        boolean isCurrentTemplateTarget,
        TimedTransitionComponent currentTransition,
        TimedPlaceComponent currentPlace
    ) {
        for (TimedPlace sourceDestPlace : sourceTemplate.model().places()) {
            if (!sourceDestPlace.isShared()) continue;
            
            TimedPlace targetDestPlace = targetTemplate.model().getPlaceByName(sourceDestPlace.name());
            if (targetDestPlace == null) continue;
            
            TransportArc transportArc = sourceTemplate.model().getTransportArcFromPlaceTransitionAndPlace(
                sourcePlace, sourceTransition, sourceDestPlace);
                
            if (transportArc == null || targetTemplate.model().getTransportArcFromPlaceTransitionAndPlace(
                targetPlace, targetTransition, targetDestPlace) != null) {
                continue;
            }
            
            TimedTransitionComponent guiTransition = (TimedTransitionComponent)targetTemplate.guiModel().getTransitionByName(sourceTransition.name());
            TimedPlaceComponent guiSourcePlace = (TimedPlaceComponent)targetTemplate.guiModel().getPlaceByName(sourcePlace.name());
            TimedPlaceComponent guiDestPlace = (TimedPlaceComponent)targetTemplate.guiModel().getPlaceByName(sourceDestPlace.name());

            if (guiSourcePlace == null || guiDestPlace == null || guiTransition == null) continue;

            ArcPath sourceArcPath = new ArcPath();
            
            int sourceX, sourceY;
            if (isCurrentTemplateTarget && currentPlace != null && sourcePlace.name().equals(currentPlace.underlyingPlace().name())) {
                sourceX = currentPlace.getPositionX();
                sourceY = currentPlace.getPositionY();
            } else {
                sourceX = guiSourcePlace.getPositionX();
                sourceY = guiSourcePlace.getPositionY();
            }
            sourceArcPath.addPoint(sourceX, sourceY, false);
            
            int transX, transY;
            if (isCurrentTemplateTarget && currentTransition != null) {
                transX = currentTransition.getPositionX();
                transY = currentTransition.getPositionY();
            } else {
                transX = guiTransition.getPositionX();
                transY = guiTransition.getPositionY();
            }
            sourceArcPath.addPoint(transX, transY, false);
            
            ArcPath destArcPath = new ArcPath();
            destArcPath.addPoint(transX, transY, false);

            int destX, destY;
            if (isCurrentTemplateTarget && currentPlace != null && sourceDestPlace.name().equals(currentPlace.underlyingPlace().name())) {
                destX = currentPlace.getPositionX();
                destY = currentPlace.getPositionY();
            } else {
                destX = guiDestPlace.getPositionX();
                destY = guiDestPlace.getPositionY();
            }
            destArcPath.addPoint(destX, destY, false);
            
            TimedTransportArcComponent newArc = currentTab.guiModelManager.addTimedTransportArc(
                targetTemplate.guiModel(), guiSourcePlace, guiTransition, guiDestPlace, sourceArcPath, destArcPath).getResult();
            TransportArc newTransportArc = newArc.underlyingTransportArc();
            newTransportArc.setWeight(transportArc.getWeight());
            newTransportArc.setTimeInterval(transportArc.interval());
            newTransportArc.setInputExpression(transportArc.getInputExpression());
            newTransportArc.setOutputExpression(transportArc.getOutputExpression());
            newTransportArc.setColorTimeIntervals(transportArc.getColorTimeIntervals());
            newArc.updateLabel(true);
        }
    }
}