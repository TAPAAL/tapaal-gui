package dk.aau.cs.model.tapn

import dk.aau.cs.model.CPN.Color
import dk.aau.cs.model.CPN.ColorType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TimedPlaceTest {

    @Test
    fun `A new Place has default ColorType`() {

        val place = LocalTimedPlace("test")

        assertEquals(ColorType.COLORTYPE_DOT, place.colorType)
        place.colorType


    }

    @Test
    fun `Can add token to Place`() {
        val place = LocalTimedPlace("test")
        with(place) {
            setCurrentMarking(LocalTimedMarking())
            addTokens(5)
            assertEquals(5, tokens().size)
        }
    }

    @Test
    fun `A token added has correct Color Type`() {

        val place = LocalTimedPlace("test")
        with(place) {
            setCurrentMarking(LocalTimedMarking())
            addTokens(1)
            assertEquals(ColorType.COLORTYPE_DOT.firstColor, place.tokens().first().color)
        }

    }

    @Test
    fun `Adding tokens for non-default ColorType`() {

        val place = LocalTimedPlace("test")
        with(place) {
            setCurrentMarking(LocalTimedMarking())

            val ct = ColorType("new").apply {
                val c = Color(this, 0, "fisk")
                addColor(c)
            }
            colorType = ct

            addTokens(1)
            assertEquals(1, tokens().size)

            assertEquals(ct.firstColor, tokens().first().color)
        }

    }

    @Test
    fun `Changing colorType on place with tokens`() {

        val place = LocalTimedPlace("test")
        with(place) {
            setCurrentMarking(LocalTimedMarking())

            addTokens(1)

            val ct = ColorType("new").apply {
                val c = Color(this, 0, "fisk")
                addColor(c)
            }
            colorType = ct

            addTokens(1)
            assertEquals(1, tokens().size)

            assertEquals(ct.firstColor, tokens().first().color)
        }

    }

}