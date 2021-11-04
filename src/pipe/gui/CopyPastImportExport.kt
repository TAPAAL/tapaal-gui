package pipe.gui;

import dk.aau.cs.gui.TabContent
import dk.aau.cs.model.tapn.TimedTransition
import org.jetbrains.annotations.NotNull
import org.w3c.dom.Element
import pipe.gui.graphicElements.*
import pipe.gui.graphicElements.tapn.*
import java.awt.Point
import java.io.StringBufferInputStream
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.HashMap


class CopyPastImportExport {


    companion object {
        @JvmStatic
        fun toXML(selection: ArrayList<PetriNetObject>): @NotNull String {

            val r = StringBuilder();

            r.append("<root>")

            for (o in selection) {
                when (o) {
                    is Place -> {
                        val s = """
                            <place name="%s" x="%d" y="%d" />
                        """.trimIndent()
                        r.append(s.format(o.name, o.originalX, o.originalY))
                    }
                    is TimedTransitionComponent -> {
                        val s = """
                            <transition name="%s" x="%d" y="%d" isUncontrollable="%b" isUrgent="%b" />
                        """.trimIndent()
                        r.append(s.format(o.name, o.originalX, o.originalY, o.isUncontrollable, o.isUrgent))
                    }
                    is TimedTransportArcComponent -> Unit
                    is TimedInputArcComponent -> {
                        val s = """
                            <inputarc from="%s" to="%s" />
                        """.trimIndent()
                        r.append(s.format(o.source.name, o.target.name))
                    }
                    is TimedOutputArcComponent -> {
                        val s = """
                            <outputarc from="%s" to="%s" />
                        """.trimIndent()
                        r.append(s.format(o.source.name, o.target.name))
                    }
                }
            }
            r.append("</root>")
            return r.toString();
        }

        @JvmStatic
        fun past(s: String, tab: TabContent) {

            val nameToElementMap = HashMap<String, PlaceTransitionObject>()

            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val xmlStringBuilder = StringBuilder(s);
            val input = StringBufferInputStream(xmlStringBuilder.toString())
            val document = builder.parse(input)

            val places = document.getElementsByTagName("place");
            for (i in 0 until places.length) {
                val node = places.item(i) as? Element
                val name = node?.getAttribute("name")!!
                val x = Integer.parseInt(node?.getAttribute("x"))
                val y = Integer.parseInt(node?.getAttribute("y"))

                if (node != null) {
                    val r = tab.guiModelManager.addNewTimedPlace(tab.model, Point(x + Pipe.PLACE_TRANSITION_HEIGHT, y + Pipe.PLACE_TRANSITION_HEIGHT))
                    if (!r.hasErrors) {
                        nameToElementMap[name] = r.result
                    }
                }
            }

            val transition = document.getElementsByTagName("transition");
            for (i in 0 until transition.length) {
                val node = transition.item(i) as? Element
                val name = node?.getAttribute("name")!!
                val isUncontrollable:Boolean = (node?.getAttribute("isUncontrollable")).toBoolean()
                val isUrgent = (node?.getAttribute("isUrgent")).toBoolean()
                val x = Integer.parseInt(node?.getAttribute("x"))
                val y = Integer.parseInt(node?.getAttribute("y"))

                if (node != null) {
                    val r = tab.guiModelManager.addNewTimedTransitions(tab.model, Point(x + Pipe.PLACE_TRANSITION_HEIGHT, y + Pipe.PLACE_TRANSITION_HEIGHT), isUrgent, isUncontrollable)
                    if (!r.hasErrors) {
                        nameToElementMap[name] = r.result
                    }
                }

            }


            val inputArc = document.getElementsByTagName("inputarc");
            for (i in 0 until inputArc.length) {
                val node = inputArc.item(i) as? Element
                val from = node?.getAttribute("from")!!
                val to = node?.getAttribute("to")!!

                if (node != null) {
                    val from = nameToElementMap[from] as? TimedPlaceComponent
                    val to = nameToElementMap[to] as? TimedTransitionComponent

                    if (from != null && to != null) {
                        tab.guiModelManager.addTimedInputArc(tab.model, from, to, null)
                    }
                }
            }

            val outputArc = document.getElementsByTagName("outputarc");
            for (i in 0 until outputArc.length) {
                val node = outputArc.item(i) as? Element
                val from = node?.getAttribute("from")!!
                val to = node?.getAttribute("to")!!

                if (node != null) {
                    val from = nameToElementMap[from] as? TimedTransitionComponent
                    val to = nameToElementMap[to] as? TimedPlaceComponent

                    if (from != null && to != null) {
                        tab.guiModelManager.addTimedOutputArc(tab.model, from, to, null)
                    }
                }
            }

            tab.drawingSurface().selectionObject.clearSelection()
            nameToElementMap.values.forEach { it.select() }


        }
    }

}
