package dk.aau.cs.io

import dk.aau.cs.model.CPN.ColorType
import dk.aau.cs.model.CPN.Expressions.AddExpression
import dk.aau.cs.model.CPN.Expressions.NumberOfExpression
import dk.aau.cs.model.CPN.Expressions.TupleExpression
import dk.aau.cs.model.CPN.Expressions.UserOperatorExpression
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.util.Vector

internal class PNMLoaderTest {
    @Test
    fun `cleaning add expressions does not copy color types`() {
        val colorType = ColorType("E").apply { addColor("a") }
        val color = UserOperatorExpression(colorType.firstColor)
        val tuple = TupleExpression(Vector(listOf(color)))
        val numberOf = NumberOfExpression(1, Vector(listOf(tuple)))
        val expression = AddExpression(Vector(listOf(numberOf)))

        val cleaned = LoadTACPN().constructCleanAddExpression(expression)
        val cleanedNumberOf = cleaned.addExpression.first() as NumberOfExpression
        val cleanedTuple = cleanedNumberOf.color.first() as TupleExpression

        assertSame(color, cleanedTuple.colors.first())
    }

    @Test
    fun `partition elements in tuple markings are expanded`() {
        val model = PNMLoader().load(partitionedTupleNet().byteInputStream())
        val tokens = model.templates().first().model().getPlaceByName("P").tokens()

        assertEquals(3, tokens.size)
        assertEquals(setOf("(a, b1)", "(a, b2)", "(a, b3)"), tokens.map { it.color().toString() }.toSet())

        val saved = TimedArcPetriNetNetworkWriter(
            model.network(), model.templates(), model.queries(), model.network().constants(), model.getLens()
        ).savePNML().toString()
        val reloaded = TapnXmlLoader().load(saved.byteInputStream())

        assertEquals(3, reloaded.templates().first().model().getPlaceByName("P").tokens().size)
    }

    private fun partitionedTupleNet() = """
        <?xml version="1.0"?>
        <pnml xmlns="http://www.pnml.org/version-2009/grammar/pnml">
          <net id="partitioned" type="http://www.pnml.org/version-2009/grammar/symmetricnet">
            <name><text>partitioned</text></name>
            <declaration>
              <structure>
                <declarations>
                  <namedsort id="A" name="A">
                    <cyclicenumeration><feconstant id="a" name="a"/></cyclicenumeration>
                  </namedsort>
                  <namedsort id="B" name="B">
                    <cyclicenumeration>
                      <feconstant id="b1" name="b1"/>
                      <feconstant id="b2" name="b2"/>
                      <feconstant id="b3" name="b3"/>
                    </cyclicenumeration>
                  </namedsort>
                  <namedsort id="AB" name="AB">
                    <productsort><usersort declaration="A"/><usersort declaration="B"/></productsort>
                  </namedsort>
                  <partition id="BPartition" name="BPartition">
                    <usersort declaration="B"/>
                    <partitionelement id="bs1" name="bs1">
                      <useroperator declaration="b1"/><useroperator declaration="b2"/>
                    </partitionelement>
                    <partitionelement id="bs2" name="bs2">
                      <useroperator declaration="b3"/>
                    </partitionelement>
                  </partition>
                </declarations>
              </structure>
            </declaration>
            <page id="page0">
              <place id="P">
                <name><text>P</text></name>
                <type><text>AB</text><structure><usersort declaration="AB"/></structure></type>
                <hlinitialMarking>
                  <text>&lt;a,bs1+bs2&gt;</text>
                  <structure>
                    <tuple>
                      <subterm><useroperator declaration="a"/></subterm>
                      <subterm><add>
                        <subterm><useroperator declaration="bs1"/></subterm>
                        <subterm><useroperator declaration="bs2"/></subterm>
                      </add></subterm>
                    </tuple>
                  </structure>
                </hlinitialMarking>
              </place>
            </page>
          </net>
        </pnml>
    """.trimIndent()
}
