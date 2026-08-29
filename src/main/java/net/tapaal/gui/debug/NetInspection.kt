package net.tapaal.gui.debug

import dk.aau.cs.model.CPN.Color
import dk.aau.cs.model.CPN.ColorType
import dk.aau.cs.model.CPN.ProductType
import dk.aau.cs.model.tapn.Constant
import dk.aau.cs.model.tapn.RealConstant
import dk.aau.cs.model.tapn.SharedPlace
import dk.aau.cs.model.tapn.SharedTransition
import dk.aau.cs.model.tapn.TimedArcPetriNet
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork
import dk.aau.cs.model.tapn.TimedInhibitorArc
import dk.aau.cs.model.tapn.TimedInputArc
import dk.aau.cs.model.tapn.TimedOutputArc
import dk.aau.cs.model.tapn.TimedPlace
import dk.aau.cs.model.tapn.TimedToken
import dk.aau.cs.model.tapn.TimedTransition
import dk.aau.cs.model.tapn.TransportArc
import dk.aau.cs.model.CPN.Variable
import net.tapaal.gui.petrinet.TAPNLens
import net.tapaal.gui.petrinet.verification.TAPNQuery
import pipe.gui.petrinet.PetriNetTab

/** A read-only, Swing-independent representation of one inspection tree. */
data class InspectionNode(
    val label: String,
    val value: String? = null,
    val children: List<InspectionNode> = emptyList(),
) {
    fun displayText(): String = if (value == null) label else "$label: $value"
}

/** Builds an inspection snapshot from the model, never from editor widgets. */
object NetInspectionSnapshot {
    fun capture(tab: PetriNetTab): InspectionNode =
        capture(tab.network(), tab.getLens(), tab.queries()).let { root ->
            root.copy(
                value = tab.getTabTitle(),
                children = listOf(simulationNode(tab)) + root.children,
            )
        }

    fun capture(
        network: TimedArcPetriNetNetwork,
        lens: TAPNLens,
        queries: Iterable<TAPNQuery>,
    ): InspectionNode {
        val children = mutableListOf<InspectionNode>()

        children += section(
            "Lens",
            listOf(
                item("Timed", lens.isTimed),
                item("Game", lens.isGame),
                item("Colored", lens.isColored),
                item("Stochastic", lens.isStochastic),
            ),
        )
        children += section(
            "Network",
            listOf(
                item("Default bound", network.getDefaultBound()),
                item("Paint net", network.paintNet()),
                item("Templates", network.allTemplates().size),
                item("Shared places", network.numberOfSharedPlaces()),
                item("Shared transitions", network.numberOfSharedTransitions()),
                item("Current tokens", network.marking().size()),
            ),
        )
        children += section("Constants", network.constants().map { guarded("Constant") { constantNode(it) } })
        children += section("Real constants", network.realConstants().map { guarded("Real constant") { realConstantNode(it) } })
        children += section("Variables", network.variables().map { guarded("Variable") { variableNode(it) } })
        children += section("Color types", network.colorTypes().map { guarded("Color type") { colorTypeNode(it) } })
        children += section(
            "User-defined distributions",
            network.userDefinedDistributions().map { guarded("Distribution") { item("Distribution", it) } },
        )
        children += section("Shared places", network.sharedPlaces().map { guarded("Shared place") { placeNode(it) } })
        children += section("Shared transitions", network.sharedTransitions().map { guarded("Shared transition") { sharedTransitionNode(it) } })
        children += section("Templates", network.allTemplates().map { guarded("Template") { templateNode(it) } })
        children += section("Queries", queries.map { guarded("Query") { queryNode(it) } }.toList())

        return InspectionNode("Net", children = children)
    }

    private fun templateNode(template: TimedArcPetriNet): InspectionNode {
        val children = listOf(
            item("Active", template.isActive),
            section("Places", template.places().map { guarded("Place") { placeNode(it) } }),
            section("Transitions", template.transitions().map { guarded("Transition") { transitionNode(it) } }),
            section("Input arcs", template.inputArcs().map { guarded("Input arc") { inputArcNode(it) } }.toList()),
            section("Output arcs", template.outputArcs().map { guarded("Output arc") { outputArcNode(it) } }.toList()),
            section("Inhibitor arcs", template.inhibitorArcs().map { guarded("Inhibitor arc") { inhibitorArcNode(it) } }.toList()),
            section("Transport arcs", template.transportArcs().map { guarded("Transport arc") { transportArcNode(it) } }.toList()),
        )
        return InspectionNode("Template", template.name(), children)
    }

    private fun placeNode(place: TimedPlace): InspectionNode {
        val details = mutableListOf(
            item("Name", place.name()),
            item("Shared", place.isShared()),
            item("Color type", place.getColorType()),
            item("Invariant", place.invariant()),
            item("Token count", place.tokens().size),
        )
        if (place.getCtiList().isNotEmpty()) {
            details += section("Colored invariants", place.getCtiList().map { item("Invariant", it) })
        }
        details += section("Tokens", place.tokens().map { guarded("Token") { tokenNode(it) } })
        return InspectionNode("Place", place.name(), details)
    }

    private fun transitionNode(transition: TimedTransition): InspectionNode {
        val details = listOf(
            item("Name", transition.name()),
            item("Shared", transition.isShared()),
            item("Urgent", transition.isUrgent()),
            item("Uncontrollable", transition.isUncontrollable()),
            item("Enabled", transition.isEnabled()),
            item("Guard", transition.getGuard()),
            item("Distribution", transition.getDistribution()),
            item("Weight", transition.getWeight()),
            item("Firing mode", transition.getFiringMode()),
        )
        return InspectionNode("Transition", transition.name(), details)
    }

    private fun sharedTransitionNode(transition: SharedTransition): InspectionNode {
        val details = listOf(
            item("Name", transition.name()),
            item("Urgent", transition.isUrgent()),
            item("Uncontrollable", transition.isUncontrollable()),
            item("Enabled", transition.isEnabled()),
            item("Guard", transition.getGuard()),
            item("Distribution", transition.getDistribution()),
            item("Weight", transition.getWeight()),
            item("Firing mode", transition.getFiringMode()),
            section("Template transitions", transition.transitions().map { guarded("Transition") { item("Transition", it) } }),
        )
        return InspectionNode("Shared transition", transition.name(), details)
    }

    private fun tokenNode(token: TimedToken): InspectionNode = InspectionNode(
        "Token",
        children = listOf(
            item("Age", token.age()),
            item("Color", token.color()),
        ),
    )

    private fun inputArcNode(arc: TimedInputArc): InspectionNode = InspectionNode(
        "Input arc",
        children = listOf(
            item("From", arc.source()),
            item("To", arc.destination()),
            item("Interval", arc.interval()),
            item("Weight", arc.getWeight()),
            item("Expression", arc.getArcExpression()),
            section("Colored intervals", arc.getColorTimeIntervals().map { item("Interval", it) }),
        ),
    )

    private fun inhibitorArcNode(arc: TimedInhibitorArc): InspectionNode = InspectionNode(
        "Inhibitor arc",
        children = listOf(
            item("From", arc.source()),
            item("To", arc.destination()),
            item("Interval", arc.interval()),
            item("Weight", arc.getWeight()),
            item("Expression", arc.getArcExpression()),
            section("Colored intervals", arc.getColorTimeIntervals().map { item("Interval", it) }),
        ),
    )

    private fun outputArcNode(arc: TimedOutputArc): InspectionNode = InspectionNode(
        "Output arc",
        children = listOf(
            item("From", arc.source()),
            item("To", arc.destination()),
            item("Weight", arc.getWeight()),
            item("Expression", arc.getExpression()),
        ),
    )

    private fun transportArcNode(arc: TransportArc): InspectionNode = InspectionNode(
        "Transport arc",
        children = listOf(
            item("From", arc.source()),
            item("Through", arc.transition()),
            item("To", arc.destination()),
            item("Interval", arc.interval()),
            item("Weight", arc.getWeight()),
            item("Input expression", arc.getInputExpression()),
            item("Output expression", arc.getOutputExpression()),
            section("Colored intervals", arc.getColorTimeIntervals().map { item("Interval", it) }),
        ),
    )

    private fun colorTypeNode(colorType: ColorType): InspectionNode {
        val children = mutableListOf(
            item("Id", colorType.getId()),
            item("Product type", colorType is ProductType),
        )

        if (colorType is ProductType) {
            children += section(
                "Constituents",
                colorType.getConstituents().map { item("Color type", it.getName()) },
            )
        } else {
            children += section("Colors", colorType.getColors().map { guarded("Color") { colorNode(it) } })
        }
        return InspectionNode("Color type", colorType.getName(), children)
    }

    private fun colorNode(color: Color): InspectionNode = InspectionNode(
        "Color",
        color.getColorName(),
        if (color.getTuple() == null) emptyList() else color.getTuple().map(::colorNode),
    )

    private fun constantNode(constant: Constant): InspectionNode = item("Constant", constant)

    private fun realConstantNode(constant: RealConstant): InspectionNode = item("Real constant", constant)

    private fun variableNode(variable: Variable): InspectionNode = InspectionNode(
        "Variable",
        variable.getName(),
        listOf(
            item("Id", variable.getId()),
            item("Color type", variable.getColorType()),
        ),
    )

    private fun queryNode(query: TAPNQuery): InspectionNode = InspectionNode(
        "Query",
        query.getName(),
        listOf(
            item("Active", query.isActive()),
            item("Category", query.getCategory()),
            item("Query", query.getQuery()),
            item("Property", query.getProperty()),
        ),
    )

    private fun section(label: String, children: List<InspectionNode>): InspectionNode =
        InspectionNode(label, children = children)

    private fun item(label: String, value: Any?): InspectionNode =
        InspectionNode(label, valueText(value))

    private fun valueText(value: Any?): String = try {
        value?.toString() ?: "null"
    } catch (exception: Exception) {
        "ERROR: ${exception.javaClass.simpleName}: ${exception.message ?: "no message"}"
    }

    private fun guarded(label: String, block: () -> InspectionNode): InspectionNode = try {
        block()
    } catch (exception: Exception) {
        InspectionNode(label, "ERROR: ${exception.javaClass.simpleName}: ${exception.message ?: "no message"}")
    }

    private fun simulationNode(tab: PetriNetTab): InspectionNode = guarded("Simulation") {
        val animator = tab.getAnimator()
        val history = tab.getAnimationHistorySidePanel()
        section(
            "Simulation",
            listOf(
                item("Animation mode", tab.isInAnimationMode),
                item("Firing mode", animator?.getFiringmode()),
                item("Action history entries", animator?.getActionHistory()?.size),
                item("Initial tokens", animator?.getInitialMarking()?.size()),
                item("Trace loaded", animator?.getTrace() != null),
                item("History selected index", history?.selectedIndex),
                item("History entries", history?.getListModel()?.size),
            ),
        )
    }
}
