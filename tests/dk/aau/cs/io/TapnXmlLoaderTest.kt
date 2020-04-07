package dk.aau.cs.io

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.ThrowingSupplier



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