package dk.aau.cs.io

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.ThrowingSupplier
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent
import java.math.BigDecimal


internal class TapnXmlLoaderTest {

    internal class MalformedXML {
        @Test @Disabled
        fun `Malformed XML should throw an exception`() {
            val tapnXmlLoader = TapnXmlLoader();
            Assertions.assertThrows(Exception::class.java) {
                tapnXmlLoader.load("hello".asInpurtStream())
            }
        }
    }

    class Place {

        @Test
        fun `Parse place`() {
            val net = xmlNet(
                """
                    <place displayName="true" id="Start" initialMarking="1" invariant="&lt; inf" name="Start" nameOffsetX="-5" nameOffsetY="35" positionX="135" positionY="30"/>
                """
            ).asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader();

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })

            Assertions.assertEquals(1, r.templates().first().guiModel().places.size)

            val place = r.templates().first().guiModel().places[0]
            Assertions.assertEquals("Start", place.name)
            Assertions.assertEquals(135, place.positionX)
            Assertions.assertEquals(30, place.positionY)

            Assertions.assertEquals(-5, place.nameOffsetX)
            Assertions.assertEquals(35, place.nameOffsetY)

        }

        @Test
        //Older version of TAPAAL saved the positionX/Y and nameOffsetX/Y in double format eg. 35.0
        fun `Place positions can be double formatted`(){

            val net = xmlNet(
                """
                    <place displayName="true" id="Start" initialMarking="1" invariant="&lt; inf" name="Start" nameOffsetX="-5.0" nameOffsetY="35.0" positionX="135.0" positionY="30.0"/>
                """
            ).asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader();

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })

            val place = r.templates().first().guiModel().places[0]

            Assertions.assertEquals(135, place.positionX)
            Assertions.assertEquals(30, place.positionY)

            Assertions.assertEquals(-5, place.nameOffsetX)
            Assertions.assertEquals(35, place.nameOffsetY)

        }

        @Test
        fun `Empty place`() {
            val net = xmlNet("<place></place>").asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader()

            Assertions.assertThrows(Exception::class.java){
                tapnXmlLoader.load(net)
            }
        }

        @Test
        fun `Place has no initial tokesn`() {
            val net = xmlNet("""
                    <place displayName="true" id="Start" initialMarking="0" invariant="&lt; inf" name="Start" nameOffsetX="-5.0" nameOffsetY="35.0" positionX="135.0" positionY="30.0"/>
                """).asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader()

            val model = tapnXmlLoader.load(net)
            val place = model.templates().first().guiModel().getPlaceByName("Start") as TimedPlaceComponent

            Assertions.assertEquals(0, place.numberOfTokens)
        }

        @Test
        fun `Place has one token`() {
            val net = xmlNet("""
                    <place displayName="true" id="Start" initialMarking="1" invariant="&lt; inf" name="Start" nameOffsetX="-5.0" nameOffsetY="35.0" positionX="135.0" positionY="30.0"/>
                """).asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader()

            val model = tapnXmlLoader.load(net)
            val place = model.templates().first().guiModel().getPlaceByName("Start") as TimedPlaceComponent

            Assertions.assertEquals(1, place.numberOfTokens)
            Assertions.assertEquals(BigDecimal.ZERO, place.underlyingPlace().tokens().first().age())
        }

        @Test
        fun `SharedPlace has a token bug#1887512`() {
            val net = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <pnml xmlns="http://www.informatik.hu-berlin.de/top/pnml/ptNetb">
                  <shared-place initialMarking="1" invariant="&lt; inf" name="P0"/>
                  <net active="true" id="IntroExample" type="P/T net">
                    <place displayName="true" id="P0" initialMarking="1" invariant="&lt; inf" name="P0" nameOffsetX="0" nameOffsetY="0" positionX="105" positionY="30"/>
                  </net>
                 </pnml>
            """.trimIndent().asInpurtStream()

            val tapnXmlLoader = TapnXmlLoader()

            val model = tapnXmlLoader.load(net)
            val place = model.templates().first().guiModel().getPlaceByName("P0") as TimedPlaceComponent

            Assertions.assertEquals(1, place.numberOfTokens)
            Assertions.assertEquals(BigDecimal.ZERO, place.underlyingPlace().tokens().first().age())
        }

        @Test
        fun `Place has 5 token`() {
            val net = xmlNet("""
                    <place displayName="true" id="Start" initialMarking="5" invariant="&lt; inf" name="Start" nameOffsetX="-5.0" nameOffsetY="35.0" positionX="135.0" positionY="30.0"/>
                """).asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader()

            val model = tapnXmlLoader.load(net)
            val place = model.templates().first().guiModel().getPlaceByName("Start") as TimedPlaceComponent

            Assertions.assertEquals(5, place.numberOfTokens)
            place.underlyingPlace().tokens().forEach {
                Assertions.assertEquals(BigDecimal.ZERO, it.age())
            }
        }

        @Test
        fun `Initial token ages survive save and load`() {
            val loader = TapnXmlLoader()
            val loaded = loader.load(xmlNet("""
                    <place displayName="true" id="Start" initialMarking="2" invariant="&lt; inf" name="Start" nameOffsetX="0" nameOffsetY="0" positionX="0" positionY="0">
                        <initialMarkingAge><token age="1.5"/></initialMarkingAge>
                    </place>
                """).asInpurtStream())

            val writer = TimedArcPetriNetNetworkWriter(
                loaded.network(), loaded.templates(), loaded.queries(), loaded.network().constants(), loaded.getLens()
            )
            val saved = writer.savePNML().toString()
            val reloaded = TapnXmlLoader().load(saved.asInpurtStream())
            val ages = reloaded.templates().first().model().getPlaceByName("Start").tokens().map { it.age() }

            Assertions.assertTrue(saved.contains("<initialMarkingAge>"))
            Assertions.assertTrue(saved.contains("<token age=\"1.5\"/>"))
            Assertions.assertFalse(saved.contains("color=\"dot\""))
            Assertions.assertFalse(saved.contains("age=\"0\""))
            Assertions.assertFalse(saved.contains("initialMarkingAge=\""))
            Assertions.assertEquals(listOf(BigDecimal("1.5"), BigDecimal.ZERO), ages)
        }

        @Test
        fun `Structured initial token ages are matched to colors`() {
            val xml = javaClass.getResource("/Example nets/fireflies.tapn")!!.readText()
            val tokenAges = (6 downTo 1).joinToString("") {
                "<token color=\"$it\" age=\"${it + 10}\"/>"
            }
            val model = TapnXmlLoader().load(xml.replaceFirst(
                "</hlinitialMarking>",
                "</hlinitialMarking><initialMarkingAge>$tokenAges</initialMarkingAge>"
            ).asInpurtStream())
            val tokens = model.templates().first().model().getPlaceByName("waiting").tokens()
            val saved = TimedArcPetriNetNetworkWriter(
                model.network(), model.templates(), model.queries(), model.network().constants(), model.getLens()
            ).savePNML().toString()

            Assertions.assertEquals(
                (1..6).associate { it.toString() to BigDecimal(it + 10) },
                tokens.associate { it.color().toString() to it.age() }
            )
            Assertions.assertTrue(saved.contains("color=\"1\""))
        }

        @Test
        fun `Native tapn round trip preserves model diagram and query data`() {
            val loaded = TapnXmlLoader().load(xmlNet(
                """
                    <place displayName="true" id="Start" initialMarking="2" invariant="&lt; inf" name="Start" nameOffsetX="-5" nameOffsetY="35" positionX="60" positionY="60">
                        <initialMarkingAge><token age="1.5"/></initialMarkingAge>
                    </place>
                    <transition angle="90" displayName="true" id="Finish" infiniteServer="false" name="Finish" nameOffsetX="10" nameOffsetY="20" positionX="240" positionY="60" priority="2" urgent="true"/>
                    <arc id="Start to Finish" inscription="[2,5]" nameOffsetX="3" nameOffsetY="4" source="Start" target="Finish" type="timed" weight="1">
                        <arcpath arcPointType="false" id="0" xCoord="87" yCoord="72"/>
                        <arcpath arcPointType="true" id="1" xCoord="180" yCoord="120"/>
                    </arc>
                    <query active="false" approximationDenominator="3" capacity="7" discreteInclusion="false" enableOverApproximation="false" enableUnderApproximation="false" extrapolationOption="AUTOMATIC" gcd="false" hashTableSize="MB_16" inclusionPlaces="*NONE*" name="Reach start" overApproximation="false" pTrie="true" query="EF IntroExample.Start = 1" reduction="true" reductionOption="VerifyDTAPN" searchOption="DFS" symmetry="false" timeDarts="true" traceOption="SOME" useStubbornReduction="false" useTarOption="true"/>
                """
            ).asInpurtStream())

            val saved = TimedArcPetriNetNetworkWriter(
                loaded.network(), loaded.templates(), loaded.queries(), loaded.network().constants(), loaded.getLens()
            ).savePNML().toString()
            val reloaded = TapnXmlLoader().load(saved.asInpurtStream())

            val originalTemplate = loaded.templates().first()
            val roundTrippedTemplate = reloaded.templates().first()
            Assertions.assertEquals(originalTemplate.model().name(), roundTrippedTemplate.model().name())
            Assertions.assertEquals(originalTemplate.model().places().size, roundTrippedTemplate.model().places().size)
            Assertions.assertEquals(originalTemplate.model().transitions().size, roundTrippedTemplate.model().transitions().size)
            Assertions.assertEquals(originalTemplate.model().inputArcs().count(), roundTrippedTemplate.model().inputArcs().count())
            Assertions.assertEquals(
                originalTemplate.model().getPlaceByName("Start").tokens().map { it.age() },
                roundTrippedTemplate.model().getPlaceByName("Start").tokens().map { it.age() }
            )

            val originalPlace = originalTemplate.guiModel().getPlaceByName("Start")
            val roundTrippedPlace = roundTrippedTemplate.guiModel().getPlaceByName("Start")
            Assertions.assertEquals(originalPlace.positionX, roundTrippedPlace.positionX)
            Assertions.assertEquals(originalPlace.positionY, roundTrippedPlace.positionY)
            Assertions.assertEquals(originalPlace.nameOffsetX, roundTrippedPlace.nameOffsetX)
            Assertions.assertEquals(originalPlace.nameOffsetY, roundTrippedPlace.nameOffsetY)

            val originalTransition = originalTemplate.guiModel().transitions.first()
            val roundTrippedTransition = roundTrippedTemplate.guiModel().transitions.first()
            Assertions.assertEquals(originalTransition.positionX, roundTrippedTransition.positionX)
            Assertions.assertEquals(originalTransition.positionY, roundTrippedTransition.positionY)
            Assertions.assertEquals(originalTransition.nameOffsetX, roundTrippedTransition.nameOffsetX)
            Assertions.assertEquals(originalTransition.nameOffsetY, roundTrippedTransition.nameOffsetY)

            Assertions.assertEquals(originalTemplate.guiModel().getArcs().size, roundTrippedTemplate.guiModel().getArcs().size)
            Assertions.assertEquals(
                originalTemplate.guiModel().getArcs().first().getArcPath().getArcPathDetails().contentDeepToString(),
                roundTrippedTemplate.guiModel().getArcs().first().getArcPath().getArcPathDetails().contentDeepToString()
            )

            Assertions.assertEquals(loaded.queries().size, reloaded.queries().size)
            val originalQuery = loaded.queries().first()
            val roundTrippedQuery = reloaded.queries().first()
            Assertions.assertEquals(originalQuery.getName(), roundTrippedQuery.getName())
            Assertions.assertEquals(originalQuery.getQuery(), roundTrippedQuery.getQuery())
            Assertions.assertEquals(originalQuery.getCapacity(), roundTrippedQuery.getCapacity())
            Assertions.assertEquals(originalQuery.getTraceOption(), roundTrippedQuery.getTraceOption())
            Assertions.assertEquals(originalQuery.getSearchOption(), roundTrippedQuery.getSearchOption())
            Assertions.assertEquals(originalQuery.getReductionOption(), roundTrippedQuery.getReductionOption())
            Assertions.assertEquals(originalQuery.isActive(), roundTrippedQuery.isActive())
        }
    }

    class Transition {
        @Test
        fun `Parse Transition`() {
            val net = xmlNet(
                """
                     <transition angle="90" displayName="true" id="T1" infiniteServer="false" name="T1" nameOffsetX="-5" nameOffsetY="35" positionX="360" positionY="300" priority="0" urgent="false"/>
                """
            ).asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader();

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })

            Assertions.assertEquals(1, r.templates().first().guiModel().transitions.size)

            val transition = r.templates().first().guiModel().transitions[0]
            Assertions.assertEquals("T1", transition.name)
            Assertions.assertEquals(360, transition.positionX)
            Assertions.assertEquals(300, transition.positionY)

            Assertions.assertEquals(-5, transition.nameOffsetX)
            Assertions.assertEquals(35, transition.nameOffsetY)

        }

        @Test
        //Older version of TAPAAL saved the positionX/Y and nameOffsetX/Y in double format eg. 35.0
        fun `Transiton positions can be double formatted`(){
            val net = xmlNet(
                """
                     <transition angle="90" displayName="true" id="T1" infiniteServer="false" name="T1" nameOffsetX="-5" nameOffsetY="35" positionX="360" positionY="300" priority="0" urgent="false"/>
                """
            ).asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader();

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })

            val transition = r.templates().first().guiModel().transitions[0]
            Assertions.assertEquals(360, transition.positionX)
            Assertions.assertEquals(300, transition.positionY)

            Assertions.assertEquals(-5, transition.nameOffsetX)
            Assertions.assertEquals(35, transition.nameOffsetY)

        }

        @Test
        fun `Empty Transition`() {
            val net = xmlNet("<transition></transition>").asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader()

            Assertions.assertThrows(Exception::class.java){
                tapnXmlLoader.load(net)
            }
        }

        @Test
        fun `if urgent not defined, default value is false`() {
            val net = xmlNet("""
                     <transition angle="0" displayName="true" id="T1" infiniteServer="false" name="T1" nameOffsetX="-5" nameOffsetY="35" positionX="360" positionY="300" priority="0"/>
                """).asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader()

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })

            Assertions.assertFalse( r.network().allTemplates().first().getTransitionByName("T1").isUrgent )


        }
    }

    class NetworkModel {
        @Test
        fun `Native tapn round trip preserves network model data`() {
            val loaded = TapnXmlLoader().load(
                javaClass.getResourceAsStream("/Example nets/workflow-medical.tapn")!!
            )
            val saved = TimedArcPetriNetNetworkWriter(
                loaded.network(), loaded.templates(), loaded.queries(), loaded.network().constants(), loaded.getLens()
            ).savePNML().toString()
            val reloaded = TapnXmlLoader().load(saved.asInpurtStream())

            Assertions.assertEquals(
                loaded.network().sharedPlaces().map { it.name() },
                reloaded.network().sharedPlaces().map { it.name() }
            )
            Assertions.assertEquals(
                loaded.network().sharedTransitions().map { it.name() to it.isUrgent() },
                reloaded.network().sharedTransitions().map { it.name() to it.isUrgent() }
            )
            Assertions.assertEquals(
                loaded.network().constants().map { it.name() },
                reloaded.network().constants().map { it.name() }
            )
        }
    }

    class InputArc {
        @Test
        fun `Parse Input Arc`() {
            val net = xmlNet(
                """
                        <place displayName="true" id="P0" initialMarking="0" invariant="&lt; inf" name="P0" nameOffsetX="0" nameOffsetY="0" positionX="60" positionY="60"/>
                        <transition angle="0" displayName="true" id="T0" infiniteServer="false" name="T0" nameOffsetX="0" nameOffsetY="0" positionX="240" positionY="60" priority="0" urgent="false"/>
                        <arc id="P0 to T0" inscription="[0,inf)" nameOffsetX="0" nameOffsetY="0" source="P0" target="T0" type="timed" weight="1"></arc>
                """
            ).asInpurtStream()

            val tapnXmlLoader = TapnXmlLoader()

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })
        }
    }

    class OutputArc {
        @Test
        fun `Parse Output Arc`() {
            val net = xmlNet(
                """
                        <place displayName="true" id="P0" initialMarking="0" invariant="&lt; inf" name="P0" nameOffsetX="0" nameOffsetY="0" positionX="60" positionY="60"/>
                        <transition angle="0" displayName="true" id="T0" infiniteServer="false" name="T0" nameOffsetX="0" nameOffsetY="0" positionX="240" positionY="60" priority="0" urgent="false"/>
                        <arc id="T0 to P0" inscription="1" nameOffsetX="0" nameOffsetY="0" source="T0" target="P0" type="normal" weight="1"></arc>
                """
            ).asInpurtStream()

            val tapnXmlLoader = TapnXmlLoader()

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })
        }
    }

    class InhibitorArc {
        @Test
        fun `Inhibitor Arc`() {
            val net = xmlNet(
                """
                        <place displayName="true" id="P0" initialMarking="0" invariant="&lt; inf" name="P0" nameOffsetX="0" nameOffsetY="0" positionX="60" positionY="60"/>
                        <transition angle="0" displayName="true" id="T0" infiniteServer="false" name="T0" nameOffsetX="0" nameOffsetY="0" positionX="240" positionY="60" priority="0" urgent="false"/>
                        <arc id="P0 to T0" inscription="[0,inf)" nameOffsetX="0" nameOffsetY="0" source="P0" target="T0" type="tapnInhibitor" weight="1"></arc>
                """
            ).asInpurtStream()

            val tapnXmlLoader = TapnXmlLoader()

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })
        }
    }

    class TransportArc {
        @Test
        fun `Transport Arc`() {
            val net = xmlNet(
                """
                        <place displayName="true" id="P0" initialMarking="0" invariant="&lt; inf" name="P0" nameOffsetX="0" nameOffsetY="0" positionX="60" positionY="60"/>
                        <place displayName="true" id="P1" initialMarking="0" invariant="&lt; inf" name="P1" nameOffsetX="0" nameOffsetY="0" positionX="60" positionY="60"/>
                        <transition angle="0" displayName="true" id="T0" infiniteServer="false" name="T0" nameOffsetX="0" nameOffsetY="0" positionX="240" positionY="60" priority="0" urgent="false"/>
                        <arc id="T0 to P1" inscription="[0,inf):1" nameOffsetX="0" nameOffsetY="0" source="T0" target="P1" type="transport" weight="1"></arc>
                        <arc id="P0 to T0" inscription="[0,inf):1" nameOffsetX="0" nameOffsetY="0" source="P0" target="T0" type="transport" weight="1"></arc>
                """
            ).asInpurtStream()

            val tapnXmlLoader = TapnXmlLoader()

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })
        }

        @Test @Disabled
        fun `Transport missing parter, gives error`() {
            val net = xmlNet(
                """
                        <place displayName="true" id="P0" initialMarking="0" invariant="&lt; inf" name="P0" nameOffsetX="0" nameOffsetY="0" positionX="60" positionY="60"/>
                        <place displayName="true" id="P1" initialMarking="0" invariant="&lt; inf" name="P1" nameOffsetX="0" nameOffsetY="0" positionX="60" positionY="60"/>
                        <transition angle="0" displayName="true" id="T0" infiniteServer="false" name="T0" nameOffsetX="0" nameOffsetY="0" positionX="240" positionY="60" priority="0" urgent="false"/>
                        <arc id="P0 to T0" inscription="[0,inf):1" nameOffsetX="0" nameOffsetY="0" source="P0" target="T0" type="transport" weight="1"></arc>
                """
            ).asInpurtStream()

            val tapnXmlLoader = TapnXmlLoader()

            Assertions.assertThrows(java.lang.Exception::class.java) {
                tapnXmlLoader.load(net)
            }
        }
    }

    class ArcPathPoints {
        @Test
        fun `Parse Arc without arcpathpoints`() {
            val net = xmlNet(
                """
                        <place displayName="true" id="P0" initialMarking="0" invariant="&lt; inf" name="P0" nameOffsetX="0" nameOffsetY="0" positionX="60" positionY="60"/>
                        <transition angle="0" displayName="true" id="T0" infiniteServer="false" name="T0" nameOffsetX="0" nameOffsetY="0" positionX="240" positionY="60" priority="0" urgent="false"/>
                        <arc id="P0 to T0" inscription="[0,inf)" nameOffsetX="0" nameOffsetY="0" source="P0" target="T0" type="timed" weight="1">
                        </arc>
                """
            ).asInpurtStream()

            val tapnXmlLoader = TapnXmlLoader()

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })
        }

        @Test @Disabled
        fun `Parse Arc with only one arcpathpoint should fail`() {
            val net = xmlNet(
                """
                        <place displayName="true" id="P0" initialMarking="0" invariant="&lt; inf" name="P0" nameOffsetX="0" nameOffsetY="0" positionX="60" positionY="60"/>
                        <transition angle="0" displayName="true" id="T0" infiniteServer="false" name="T0" nameOffsetX="0" nameOffsetY="0" positionX="240" positionY="60" priority="0" urgent="false"/>
                        <arc id="P0 to T0" inscription="[0,inf)" nameOffsetX="0" nameOffsetY="0" source="P0" target="T0" type="timed" weight="1">
                            <arcpath arcPointType="false" id="0" xCoord="87" yCoord="72"/>
                        </arc>
                """
            ).asInpurtStream()

            val tapnXmlLoader = TapnXmlLoader()

            Assertions.assertThrows(java.lang.Exception::class.java) {
                tapnXmlLoader.load(net)
            }
        }
    }
}


fun  String.asInpurtStream() : java.io.InputStream {
    return java.io.StringBufferInputStream(this)
}

fun xmlNet(s:String) : String {
    return """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <pnml xmlns="http://www.informatik.hu-berlin.de/top/pnml/ptNetb">
                  <net active="true" id="IntroExample" type="P/T net">
                    $s
                  </net>
                 </pnml>
            """.trimIndent()
}
