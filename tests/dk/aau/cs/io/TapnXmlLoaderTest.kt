package dk.aau.cs.io

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.ThrowingSupplier
import pipe.gui.graphicElements.tapn.TimedPlaceComponent
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