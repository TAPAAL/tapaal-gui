package pipe.gui;

import dk.aau.cs.gui.TabContent
import dk.aau.cs.model.tapn.TimeInterval
import dk.aau.cs.model.tapn.TimeInvariant
import dk.aau.cs.model.tapn.Weight
import org.jetbrains.annotations.NotNull
import pipe.gui.graphicElements.*
import pipe.gui.graphicElements.tapn.*
import java.awt.Point
import java.util.*
import kotlin.collections.HashMap

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.lang.Exception


class CopyPastImportExport {

    @Serializable
    data class TAPAALCopyPastModel(
        val places: List<PlaceModel>,
        val transitions: List<TransitionModel>,
        val inputArcs: List<InputArcModel>,
        val outputArc: List<OutputArcModel>,
        val inhibitorArc: List<InhibitorArcModel>,
        val transportArcs: List<TransportArcModel>,
    )

    @Serializable
    data class PlaceModel(val name: String, val x: Int, val y: Int, val tokens: Int, val invariant: String)
    @Serializable
    data class TransitionModel(
        val name: String,
        val x: Int,
        val y: Int,
        val uncontrollable: Boolean,
        val urgent: Boolean,
        val rotation: Int,
    )
    @Serializable
    data class InputArcModel(val source: String, val target: String, val defaultGuard: String, val weight: String)
    @Serializable
    data class OutputArcModel(val source: String, val target: String, val weight: String)
    @Serializable
    data class InhibitorArcModel(val source: String, val target: String, val weight: String)
    @Serializable
    data class TransportArcModel(val source: String, val mid: String, val target: String, val defaultGuard: String, val weight: String)


    companion object {
        @JvmStatic
        fun toXML(selection: ArrayList<PetriNetObject>): @NotNull String {

            val foundTransportArcs = mutableSetOf<TimedTransportArcComponent>()

            val places = mutableListOf<PlaceModel>()
            val transitions = mutableListOf<TransitionModel>()
            val inputArcs = mutableListOf<InputArcModel>()
            val outputArcs = mutableListOf<OutputArcModel>()
            val transportArcs = mutableListOf<TransportArcModel>()
            val inhibitorArcs = mutableListOf<InhibitorArcModel>()

            for (o in selection) {
                when (o) {
                    is TimedPlaceComponent -> {
                        val tokens = o.numberOfTokens
                        val invariant = o.invariant.toString(false)
                        places.add(PlaceModel(o.name, o.originalX, o.originalY, tokens, invariant))
                    }
                    is TimedTransitionComponent -> {
                        transitions.add(TransitionModel(o.name, o.originalX, o.originalY, uncontrollable = o.isUncontrollable, urgent = o.isUrgent, rotation = o.angle))
                    }
                    is TimedInhibitorArcComponent -> {
                        val defaultWeight = o.weight.nameForSaving(false).ifBlank {"1"}
                        inhibitorArcs.add(InhibitorArcModel(o.source.name, o.target.name, defaultWeight))
                    }
                    is TimedTransportArcComponent -> {
                        if (foundTransportArcs.contains(o.connectedTo)) {
                            val source1 = o.source
                            val target1 = o.target
                            val source2 = o.connectedTo.source
                            val target2 = o.connectedTo.target

                            val start: TimedPlaceComponent;
                            val mid: TimedTransitionComponent
                            val end: TimedPlaceComponent
                            if (source1 is TimedPlaceComponent) {
                                start = source1 as TimedPlaceComponent
                                mid = target1 as TimedTransitionComponent
                                end = target2 as TimedPlaceComponent
                            } else if (source1 is TimedTransitionComponent) {
                                start = source2 as TimedPlaceComponent
                                mid =  source1 as TimedTransitionComponent
                                end = target1 as TimedPlaceComponent
                            } else {
                                //Ignored not a valid arc
                                break;
                            }

                            val defaultGuard = o.guard.toString(false)
                            val defaultWeight = o.weight.nameForSaving(false).ifBlank {"1"}

                            transportArcs.add(TransportArcModel(start.name, mid.name, end.name, defaultGuard, defaultWeight))
                        } else  {
                            foundTransportArcs.add(o);
                        }
                    }
                    is TimedInputArcComponent -> {
                        val defaultGuard = o.guard.toString(false)
                        val defaultWeight = o.weight.nameForSaving(false).ifBlank {"1"}
                        inputArcs.add(InputArcModel(o.source.name, o.target.name, defaultGuard, defaultWeight))
                    }
                    is TimedOutputArcComponent -> {
                        val defaultWeight = o.weight.nameForSaving(false).ifBlank {"1"}
                        outputArcs.add(OutputArcModel(o.source.name, o.target.name, defaultWeight))
                    }
                }
            }

            return Json.encodeToString(TAPAALCopyPastModel(
                places,
                transitions,
                inputArcs,
                outputArcs,
                inhibitorArcs,
                transportArcs,
            ))
        }

        @JvmStatic
        fun past(s: String, tab: TabContent) {

            val model: TAPAALCopyPastModel
            try {
                model = Json.decodeFromString<TAPAALCopyPastModel>(s)
            } catch (e: Exception) { // It seems the throw exception is internal to the library
                return;
            }

            val nameToElementMap = HashMap<String, PlaceTransitionObject>()

            for ( p in model.places) {
                val tokens = p.tokens
                val invariant = TimeInvariant.parse(p.invariant, tab.network().constantStore)

                val r = tab.guiModelManager.addNewTimedPlace(tab.model, Point(p.x + Pipe.PLACE_TRANSITION_HEIGHT, p.y + Pipe.PLACE_TRANSITION_HEIGHT))
                if (!r.hasErrors) {
                    r.result.underlyingPlace().addTokens(tokens)
                    r.result.invariant = invariant
                    nameToElementMap[p.name] = r.result
                }
            }

            for (t in model.transitions) {
                val r = tab.guiModelManager.addNewTimedTransitions(tab.model, Point(t.x + Pipe.PLACE_TRANSITION_HEIGHT, t.y + Pipe.PLACE_TRANSITION_HEIGHT), t.urgent, t.uncontrollable)
                if (!r.hasErrors) {
                    nameToElementMap[t.name] = r.result
                    r.result.rotate(t.rotation)
                }
            }

            for (a in model.inputArcs) {

                val from = nameToElementMap[a.source] as? TimedPlaceComponent
                val to = nameToElementMap[a.target] as? TimedTransitionComponent
                val guard = try {
                    TimeInterval.parse(a.defaultGuard, tab.network().constantStore);
                } catch (e: Exception) {
                    null
                }

                val weight = try {
                    Weight.parseWeight(a.weight, tab.network().constantStore)
                } catch (e: Exception) {
                    null
                }

                if (from != null && to != null && guard != null) {
                    val r = tab.guiModelManager.addTimedInputArc(tab.model, from, to, null)
                    if (!r.hasErrors) {
                        r.result.setGuardAndWeight(guard, weight)
                    }
                }

            }

            for (a in model.outputArc) {

                val to = nameToElementMap[a.source] as? TimedTransitionComponent
                val from = nameToElementMap[a.target] as? TimedPlaceComponent

                val weight = try {
                    Weight.parseWeight(a.weight, tab.network().constantStore)
                } catch (e: Exception) {
                    null
                }

                if (from != null && to != null) {
                    val r = tab.guiModelManager.addTimedOutputArc(tab.model, to, from, null)
                    if (!r.hasErrors) {
                        r.result.weight = weight
                    }
                }

            }

            for (a in model.inhibitorArc) {
                val from = nameToElementMap[a.source] as? TimedPlaceComponent
                val to = nameToElementMap[a.target] as? TimedTransitionComponent

                val weight = try {
                    Weight.parseWeight(a.weight, tab.network().constantStore)
                } catch (e: Exception) {
                    null
                }

                if (from != null && to != null) {
                    val r = tab.guiModelManager.addInhibitorArc(tab.model, from, to, null)
                    if (!r.hasErrors) {
                        r.result.setGuardAndWeight(TimeInterval.ZERO_INF, weight)
                    }
                }
            }

            for (a in model.transportArcs) {
                val from = nameToElementMap[a.source] as? TimedPlaceComponent
                val mid = nameToElementMap[a.mid] as? TimedTransitionComponent
                val to = nameToElementMap[a.target] as? TimedPlaceComponent

                val guard = try {
                    TimeInterval.parse(a.defaultGuard, tab.network().constantStore);
                } catch (e: Exception) {
                    null
                }

                val weight = try {
                    Weight.parseWeight(a.weight, tab.network().constantStore)
                } catch (e: Exception) {
                    null
                }

                if (from != null && mid != null && to != null && guard != null && weight != null) {
                    val r = tab.guiModelManager.addTimedTransportArc(tab.model, from, mid, to, null, null);
                    if (!r.hasErrors) {
                        r.result.setGuardAndWeight(guard, weight);
                    }
                }
            }

            tab.drawingSurface().selectionObject.clearSelection()
            nameToElementMap.values.forEach { it.select() }


        }
    }

}


