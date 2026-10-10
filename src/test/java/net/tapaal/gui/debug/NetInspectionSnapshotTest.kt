package net.tapaal.gui.debug

import dk.aau.cs.model.CPN.ColorType
import dk.aau.cs.model.CPN.ProductType
import dk.aau.cs.model.CPN.Variable
import dk.aau.cs.TCTL.TCTLConstNode
import dk.aau.cs.model.tapn.Constant
import dk.aau.cs.model.tapn.LocalTimedPlace
import dk.aau.cs.model.tapn.SharedPlace
import dk.aau.cs.model.tapn.SharedTransition
import dk.aau.cs.model.tapn.TimedArcPetriNet
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork
import dk.aau.cs.model.tapn.TimedInhibitorArc
import dk.aau.cs.model.tapn.TimedInputArc
import dk.aau.cs.model.tapn.TimedOutputArc
import dk.aau.cs.model.tapn.TimedToken
import dk.aau.cs.model.tapn.TimedTransition
import dk.aau.cs.model.tapn.TimeInterval
import dk.aau.cs.model.tapn.TransportArc
import net.tapaal.gui.petrinet.TAPNLens
import net.tapaal.gui.petrinet.verification.TAPNQuery
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class NetInspectionSnapshotTest {
    @Test
    fun `captures model state without constructing editor widgets`() {
        val colors = ColorType("Colors").apply {
            addColor("red")
            addColor("blue")
        }
        val product = ProductType("Pairs").apply {
            addType(colors)
            addType(ColorType.COLORTYPE_DOT)
        }
        val network = TimedArcPetriNetNetwork()
        network.add(colors)
        network.add(product)
        network.add(Variable("color", colors))
        network.setConstants(listOf(Constant("limit", 3)))

        val template = TimedArcPetriNet("Template")
        val place = LocalTimedPlace("input", colors)
        val output = LocalTimedPlace("output", colors)
        val blocker = LocalTimedPlace("blocker", colors)
        val transportInput = LocalTimedPlace("transportInput", colors)
        val transportOutput = LocalTimedPlace("transportOutput", colors)
        val transition = TimedTransition("fire")
        template.add(place)
        template.add(output)
        template.add(blocker)
        template.add(transportInput)
        template.add(transportOutput)
        template.add(transition)
        network.add(template)
        place.addToken(TimedToken(place, BigDecimal("1.5"), colors.getColorByName("red")))
        template.add(TimedInputArc(place, transition, TimeInterval.ZERO_INF))
        template.add(TimedOutputArc(transition, output))
        template.add(TimedInhibitorArc(blocker, transition))
        template.add(TransportArc(transportInput, transition, transportOutput, TimeInterval.ZERO_INF))

        val sharedPlace = SharedPlace("shared", colors)
        network.add(sharedPlace)
        template.add(sharedPlace)
        val sharedTransition = SharedTransition("sharedTransition")
        network.add(sharedTransition)
        val sharedMember = TimedTransition("sharedMember")
        template.add(sharedMember)
        sharedTransition.makeShared(sharedMember)

        val query = TAPNQuery(
            "reachability",
            0,
            TCTLConstNode(1),
            null,
            null,
            null,
            false,
            false,
            false,
            false,
            null,
            null,
            true,
        )

        val root = NetInspectionSnapshot.capture(
            network,
            TAPNLens(true, false, true, false),
            listOf(query),
        )
        val text = flatten(root)

        assertTrue(text.contains("Template"))
        assertTrue(text.contains("input"))
        assertTrue(text.contains("1.5"))
        assertTrue(text.contains("red"))
        assertTrue(text.contains("Pairs"))
        assertTrue(text.contains("Constituents"))
        assertTrue(text.contains("limit = 3"))
        assertTrue(text.contains("Input arc"))
        assertTrue(text.contains("Output arc"))
        assertTrue(text.contains("Inhibitor arc"))
        assertTrue(text.contains("Transport arc"))
        assertTrue(text.contains("Place: shared"))
        assertTrue(text.contains("Shared transition: sharedTransition"))
        assertTrue(text.contains("Query: reachability"))
    }

    private fun flatten(node: InspectionNode): String =
        (listOf(node.displayText()) + node.children.map(::flatten)).joinToString("\n")
}
