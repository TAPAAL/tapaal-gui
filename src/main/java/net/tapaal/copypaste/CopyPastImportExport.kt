<<<<<<< HEAD
package net.tapaal.copypaste

import dk.aau.cs.model.tapn.IntWeight
import dk.aau.cs.model.tapn.TimeInterval
import dk.aau.cs.model.tapn.TimeInvariant
import dk.aau.cs.model.tapn.Weight
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.NotNull
import pipe.gui.Constants
import pipe.gui.petrinet.PetriNetTab
import pipe.gui.petrinet.graphicElements.Arc
import pipe.gui.petrinet.graphicElements.PetriNetObject
import pipe.gui.petrinet.graphicElements.PlaceTransitionObject
import pipe.gui.petrinet.graphicElements.tapn.*
import java.awt.Point


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
    data class PathPointModel(val x: Int, val y: Int, val curve: Boolean = false)

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
    data class InputArcModel(val source: String, val target: String, val guard: String, val weight: String, val path: List<PathPointModel> = listOf())
    @Serializable
    data class OutputArcModel(val source: String, val target: String, val weight: String, val path: List<PathPointModel> = listOf())
    @Serializable
    data class InhibitorArcModel(val source: String, val target: String, val weight: String, val path: List<PathPointModel> = listOf())
    @Serializable
    data class TransportArcModel(val source: String, val mid: String, val target: String, val guard: String, val weight: String, val path1: List<PathPointModel> = listOf(), val path2: List<PathPointModel> = listOf())


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
                        val path = o.arcPath.arcPathPoints.map {
                            PathPointModel(it.originalX, it.originalY, it.pointType)
                        }
                        inhibitorArcs.add(InhibitorArcModel(o.source.name, o.target.name, defaultWeight, path))
                    }
                    is TimedTransportArcComponent -> {
                        if (foundTransportArcs.contains(o.connectedTo)) {

                            val arc1: TimedTransportArcComponent
                            val arc2: TimedTransportArcComponent

                            if (o.source is TimedPlaceComponent) {
                                arc1 = o
                                arc2 = o.connectedTo
                            } else {
                                arc1 = o.connectedTo
                                arc2 = o
                            }

                            val start = arc1.source as TimedPlaceComponent
                            val mid = arc1.target as TimedTransitionComponent
                            val end = arc2.target as TimedPlaceComponent

                            val path1 = arc1.arcPath.arcPathPoints.map {
                                PathPointModel(it.originalX, it.originalY, it.pointType)
                            }
                            val path2 = arc2.arcPath.arcPathPoints.map {
                                PathPointModel(it.originalX, it.originalY, it.pointType)
                            }

                            val defaultGuard = o.guard.toString(false)
                            val defaultWeight = o.weight.nameForSaving(false).ifBlank {"1"}

                            transportArcs.add(TransportArcModel(start.name, mid.name, end.name, defaultGuard, defaultWeight, path1, path2))
                        } else  {
                            foundTransportArcs.add(o)
                        }
                    }
                    is TimedInputArcComponent -> {
                        val defaultGuard = o.guard.toString(false)
                        val defaultWeight = o.weight.nameForSaving(false).ifBlank {"1"}

                        val path = o.arcPath.arcPathPoints.map {
                            PathPointModel(it.originalX, it.originalY, it.pointType)
                        }

                        inputArcs.add(InputArcModel(o.source.name, o.target.name, defaultGuard, defaultWeight, path))
                    }
                    is TimedOutputArcComponent -> {
                        val defaultWeight = o.weight.nameForSaving(false).ifBlank {"1"}

                        val path = o.arcPath.arcPathPoints.map {
                            PathPointModel(it.originalX, it.originalY, it.pointType)
                        }
                        outputArcs.add(OutputArcModel(o.source.name, o.target.name, defaultWeight, path))
                    }
                }
            }

            return Json.encodeToString(
                TAPAALCopyPastModel(
                    places,
                    transitions,
                    inputArcs,
                    outputArcs,
                    inhibitorArcs,
                    transportArcs,
                )
            )
        }

        @JvmStatic
        fun past(s: String, tab: PetriNetTab) {

            val model: TAPAALCopyPastModel
            try {
                model = Json.decodeFromString(s)
            } catch (e: Exception) { // It seems the throw exception is internal to the library
                return
            }

            tab.drawingSurface().selectionObject.clearSelection()
            tab.guiModelManager.startTransaction()
            val nameToElementMap = HashMap<String, PlaceTransitionObject>()

            for ( p in model.places) {
                val tokens = p.tokens
                val invariant = if (tab.lens.isTimed) {
                    TimeInvariant.parse(p.invariant, tab.network().constantStore)
                } else {
                    TimeInvariant.LESS_THAN_INFINITY
                }

                val r = tab.guiModelManager.addNewTimedPlace(tab.model, Point(p.x + Constants.PLACE_TRANSITION_HEIGHT, p.y + Constants.PLACE_TRANSITION_HEIGHT))
                if (!r.hasErrors) {
                    r.result.underlyingPlace().addTokens(tokens)
                    r.result.invariant = invariant
                    nameToElementMap[p.name] = r.result
                }
            }

            for (t in model.transitions) {
                val r = tab.guiModelManager.addNewTimedTransitions(tab.model, Point(t.x + Constants.PLACE_TRANSITION_HEIGHT, t.y + Constants.PLACE_TRANSITION_HEIGHT), t.urgent && tab.lens.isTimed, t.uncontrollable && tab.lens.isGame)
                if (!r.hasErrors) {
                    nameToElementMap[t.name] = r.result
                    r.result.rotate(t.rotation)
                }
            }

            for (a in model.inputArcs) {

                val from = nameToElementMap[a.source] as? TimedPlaceComponent
                val to = nameToElementMap[a.target] as? TimedTransitionComponent
                val guard = findGuard(a.guard, tab)

                val weight = findWeight(a.weight, tab)

                if (from != null && to != null && guard != null) {
                    val r = tab.guiModelManager.addTimedInputArc(tab.model, from, to, null)
                    if (!r.hasErrors) {
                        r.result.setGuardAndWeight(guard, weight)
                        addArcPath(a.path, r.result)
                    }
                }

            }

            for (a in model.outputArc) {

                val to = nameToElementMap[a.source] as? TimedTransitionComponent
                val from = nameToElementMap[a.target] as? TimedPlaceComponent

                val weight = findWeight(a.weight, tab)

                if (from != null && to != null) {
                    val r = tab.guiModelManager.addTimedOutputArc(tab.model, to, from, null)
                    if (!r.hasErrors) {
                        r.result.weight = weight
                        addArcPath(a.path, r.result)
                    }
                }

            }

            for (a in model.inhibitorArc) {
                val from = nameToElementMap[a.source] as? TimedPlaceComponent
                val to = nameToElementMap[a.target] as? TimedTransitionComponent

                val weight = findWeight(a.weight, tab)

                if (from != null && to != null) {
                    val r = tab.guiModelManager.addInhibitorArc(tab.model, from, to, null)
                    if (!r.hasErrors) {
                        r.result.setGuardAndWeight(TimeInterval.ZERO_INF, weight)
                        addArcPath(a.path, r.result)
                    }
                }
            }

            for (a in model.transportArcs) {
                val from = nameToElementMap[a.source] as? TimedPlaceComponent
                val mid = nameToElementMap[a.mid] as? TimedTransitionComponent
                val to = nameToElementMap[a.target] as? TimedPlaceComponent

                val guard = findGuard(a.guard, tab)

                val weight = findWeight(a.weight, tab)

                if (from != null && mid != null && to != null && guard != null && weight != null) {
                    if (tab.lens.isTimed) {
                        val r = tab.guiModelManager.addTimedTransportArc(tab.model, from, mid, to, null, null)
                        if (!r.hasErrors) {
                            r.result.setGuardAndWeight(guard, weight)
                            addArcPath(a.path1, r.result)
                            addArcPath(a.path2, r.result.connectedTo)
                        }
                    } else {
                        val r = tab.guiModelManager.addTimedInputArc(tab.model, from, mid, null)
                        val r2 = tab.guiModelManager.addTimedOutputArc(tab.model, mid, to, null)

                        if (!r.hasErrors && !r2.hasErrors) {
                            r.result.setGuardAndWeight(guard, weight)
                            r2.result.weight = weight
                            addArcPath(a.path1, r.result)
                            addArcPath(a.path2, r2.result)
                        }
                    }

                }
            }
            tab.guiModelManager.commit()
            nameToElementMap.values.forEach { it.select() }
        }

        private fun findWeight(weight: String, tab: PetriNetTab): Weight? {
            return if (tab.lens.isColored) {
                IntWeight(1)
            } else {
                try {
                    Weight.parseWeight(weight, tab.network().constantStore)
                } catch (e: Exception) {
                    null
                }
            }
        }
        private fun findGuard(guard: String, tab: PetriNetTab): TimeInterval? {
            return if (tab.lens.isTimed) {
                try {
                    TimeInterval.parse(guard, tab.network().constantStore)
                } catch (e: Exception) {
                    null
                }
            } else {
                TimeInterval.ZERO_INF
            }
        }
        private fun addArcPath(path: List<PathPointModel>, arc: Arc) {
            arc.arcPath.purgePathPoints()
            path.forEach {
                arc.arcPath.addPoint(it.x.toDouble()+ Constants.PLACE_TRANSITION_HEIGHT, it.y.toDouble()+ Constants.PLACE_TRANSITION_HEIGHT, it.curve)
            }
            arc.arcPath.updateArc()
            arc.select()
        }


    }

}


=======
package net.tapaal.copypaste

import dk.aau.cs.model.tapn.IntWeight
import dk.aau.cs.model.tapn.TimeInterval
import dk.aau.cs.model.tapn.TimeInvariant
import dk.aau.cs.model.tapn.Weight
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.NotNull
import pipe.gui.Constants
import pipe.gui.petrinet.PetriNetTab
import pipe.gui.petrinet.graphicElements.Arc
import pipe.gui.petrinet.graphicElements.PetriNetObject
import pipe.gui.petrinet.graphicElements.PlaceTransitionObject
import pipe.gui.petrinet.graphicElements.tapn.*
import java.awt.Point


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
    data class PathPointModel(val x: Int, val y: Int, val curve: Boolean = false)

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
    data class InputArcModel(val source: String, val target: String, val guard: String, val weight: String, val path: List<PathPointModel> = listOf())
    @Serializable
    data class OutputArcModel(val source: String, val target: String, val weight: String, val path: List<PathPointModel> = listOf())
    @Serializable
    data class InhibitorArcModel(val source: String, val target: String, val weight: String, val path: List<PathPointModel> = listOf())
    @Serializable
    data class TransportArcModel(val source: String, val mid: String, val target: String, val guard: String, val weight: String, val path1: List<PathPointModel> = listOf(), val path2: List<PathPointModel> = listOf())


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
                        val path = o.arcPath.arcPathPoints.map {
                            PathPointModel(it.originalX, it.originalY, it.pointType)
                        }
                        inhibitorArcs.add(InhibitorArcModel(o.source.name, o.target.name, defaultWeight, path))
                    }
                    is TimedTransportArcComponent -> {
                        if (foundTransportArcs.contains(o.connectedTo)) {

                            val arc1: TimedTransportArcComponent
                            val arc2: TimedTransportArcComponent

                            if (o.source is TimedPlaceComponent) {
                                arc1 = o
                                arc2 = o.connectedTo
                            } else {
                                arc1 = o.connectedTo
                                arc2 = o
                            }

                            val start = arc1.source as TimedPlaceComponent
                            val mid = arc1.target as TimedTransitionComponent
                            val end = arc2.target as TimedPlaceComponent

                            val path1 = arc1.arcPath.arcPathPoints.map {
                                PathPointModel(it.originalX, it.originalY, it.pointType)
                            }
                            val path2 = arc2.arcPath.arcPathPoints.map {
                                PathPointModel(it.originalX, it.originalY, it.pointType)
                            }

                            val defaultGuard = o.guard.toString(false)
                            val defaultWeight = o.weight.nameForSaving(false).ifBlank {"1"}

                            transportArcs.add(TransportArcModel(start.name, mid.name, end.name, defaultGuard, defaultWeight, path1, path2))
                        } else  {
                            foundTransportArcs.add(o)
                        }
                    }
                    is TimedInputArcComponent -> {
                        val defaultGuard = o.guard.toString(false)
                        val defaultWeight = o.weight.nameForSaving(false).ifBlank {"1"}

                        val path = o.arcPath.arcPathPoints.map {
                            PathPointModel(it.originalX, it.originalY, it.pointType)
                        }

                        inputArcs.add(InputArcModel(o.source.name, o.target.name, defaultGuard, defaultWeight, path))
                    }
                    is TimedOutputArcComponent -> {
                        val defaultWeight = o.weight.nameForSaving(false).ifBlank {"1"}

                        val path = o.arcPath.arcPathPoints.map {
                            PathPointModel(it.originalX, it.originalY, it.pointType)
                        }
                        outputArcs.add(OutputArcModel(o.source.name, o.target.name, defaultWeight, path))
                    }
                }
            }

            return Json.encodeToString(
                TAPAALCopyPastModel(
                    places,
                    transitions,
                    inputArcs,
                    outputArcs,
                    inhibitorArcs,
                    transportArcs,
                )
            )
        }

        @JvmStatic
        fun past(s: String, tab: PetriNetTab) {

            val model: TAPAALCopyPastModel
            try {
                model = Json.decodeFromString(s)
            } catch (e: Exception) { // It seems the throw exception is internal to the library
                return
            }

            tab.drawingSurface().selectionObject.clearSelection()
            tab.guiModelManager.startTransaction()
            val nameToElementMap = HashMap<String, PlaceTransitionObject>()

            for ( p in model.places) {
                val tokens = p.tokens
                val invariant = if (tab.lens.isTimed) {
                    TimeInvariant.parse(p.invariant, tab.network().constantStore)
                } else {
                    TimeInvariant.LESS_THAN_INFINITY
                }

                val r = tab.guiModelManager.addNewTimedPlace(tab.model, Point(p.x + Constants.PLACE_TRANSITION_HEIGHT, p.y + Constants.PLACE_TRANSITION_HEIGHT))
                if (!r.hasErrors) {
                    r.result.underlyingPlace().addTokens(tokens)
                    r.result.invariant = invariant
                    nameToElementMap[p.name] = r.result
                }
            }

            for (t in model.transitions) {
                val r = tab.guiModelManager.addNewTimedTransitions(tab.model, Point(t.x + Constants.PLACE_TRANSITION_HEIGHT, t.y + Constants.PLACE_TRANSITION_HEIGHT), t.urgent && tab.lens.isTimed, t.uncontrollable && tab.lens.isGame)
                if (!r.hasErrors) {
                    nameToElementMap[t.name] = r.result
                    r.result.rotate(t.rotation)
                }
            }

            for (a in model.inputArcs) {

                val from = nameToElementMap[a.source] as? TimedPlaceComponent
                val to = nameToElementMap[a.target] as? TimedTransitionComponent
                val guard = findGuard(a.guard, tab)

                val weight = findWeight(a.weight, tab)

                if (from != null && to != null && guard != null) {
                    val r = tab.guiModelManager.addTimedInputArc(tab.model, from, to, null)
                    if (!r.hasErrors) {
                        r.result.setGuardAndWeight(guard, weight)
                        addArcPath(a.path, r.result)
                    }
                }

            }

            for (a in model.outputArc) {

                val to = nameToElementMap[a.source] as? TimedTransitionComponent
                val from = nameToElementMap[a.target] as? TimedPlaceComponent

                val weight = findWeight(a.weight, tab)

                if (from != null && to != null) {
                    val r = tab.guiModelManager.addTimedOutputArc(tab.model, to, from, null)
                    if (!r.hasErrors) {
                        r.result.weight = weight
                        addArcPath(a.path, r.result)
                    }
                }

            }

            for (a in model.inhibitorArc) {
                val from = nameToElementMap[a.source] as? TimedPlaceComponent
                val to = nameToElementMap[a.target] as? TimedTransitionComponent

                val weight = findWeight(a.weight, tab)

                if (from != null && to != null) {
                    val r = tab.guiModelManager.addInhibitorArc(tab.model, from, to, null)
                    if (!r.hasErrors) {
                        r.result.setGuardAndWeight(TimeInterval.ZERO_INF, weight)
                        addArcPath(a.path, r.result)
                    }
                }
            }

            for (a in model.transportArcs) {
                val from = nameToElementMap[a.source] as? TimedPlaceComponent
                val mid = nameToElementMap[a.mid] as? TimedTransitionComponent
                val to = nameToElementMap[a.target] as? TimedPlaceComponent

                val guard = findGuard(a.guard, tab)

                val weight = findWeight(a.weight, tab)

                if (from != null && mid != null && to != null && guard != null && weight != null) {
                    if (tab.lens.isTimed) {
                        val r = tab.guiModelManager.addTimedTransportArc(tab.model, from, mid, to, null, null)
                        if (!r.hasErrors) {
                            r.result.setGuardAndWeight(guard, weight)
                            addArcPath(a.path1, r.result)
                            addArcPath(a.path2, r.result.connectedTo)
                        }
                    } else {
                        val r = tab.guiModelManager.addTimedInputArc(tab.model, from, mid, null)
                        val r2 = tab.guiModelManager.addTimedOutputArc(tab.model, mid, to, null)

                        if (!r.hasErrors && !r2.hasErrors) {
                            r.result.setGuardAndWeight(guard, weight)
                            r2.result.weight = weight
                            addArcPath(a.path1, r.result)
                            addArcPath(a.path2, r2.result)
                        }
                    }

                }
            }
            tab.guiModelManager.commit()
            nameToElementMap.values.forEach { it.select() }
        }

        private fun findWeight(weight: String, tab: PetriNetTab): Weight? {
            return if (tab.lens.isColored) {
                IntWeight(1)
            } else {
                try {
                    Weight.parseWeight(weight, tab.network().constantStore)
                } catch (e: Exception) {
                    null
                }
            }
        }
        private fun findGuard(guard: String, tab: PetriNetTab): TimeInterval? {
            return if (tab.lens.isTimed) {
                try {
                    TimeInterval.parse(guard, tab.network().constantStore)
                } catch (e: Exception) {
                    null
                }
            } else {
                TimeInterval.ZERO_INF
            }
        }
        private fun addArcPath(path: List<PathPointModel>, arc: Arc) {
            arc.arcPath.purgePathPoints()
            path.forEach {
                arc.arcPath.addPoint(it.x.toDouble()+ Constants.PLACE_TRANSITION_HEIGHT, it.y.toDouble()+ Constants.PLACE_TRANSITION_HEIGHT, it.curve)
            }
            arc.arcPath.updateArc()
            arc.select()
        }


    }

}


>>>>>>> origin/cpn
