package net.tapaal.gui;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TapnXmlComparisonTest {
    private static final String SAVED = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<pnml xmlns=\"urn:test\">\n"
        + "  <net id=\"N\">\n"
        + "    <place id=\"P0\" positionX=\"60\" positionY=\"60\" invariant=\"&lt; inf\"/>\n"
        + "    <place id=\"P1\" positionX=\"90\" positionY=\"60\"/>\n"
        + "    <labels border=\"true\"> keep  this text </labels>\n"
        + "  </net>\n"
        + "</pnml>\n";

    @Test
    void ignoresFormattingAttributeOrderAndEscaping() throws Exception {
        String equivalent = "<pnml xmlns='urn:test'><!-- reformatted --><net id='N'>"
            + "<place positionY='60' invariant='&#60; inf' positionX='60' id='P0'></place>"
            + "\t<place positionY=\"60\" positionX=\"90\" id=\"P1\"/>"
            + "<labels border='true'><![CDATA[ keep  this text ]]></labels></net></pnml>";
        assertEquals(List.of(), differences(SAVED, equivalent));
    }

    @Test
    void reportsChangedGeometryAtItsPath() throws Exception {
        assertEquals(
            List.of("/pnml/net[@id='N']/place[@id='P0']/@positionX: expected '60' but was '75'"),
            differences(SAVED, SAVED.replace("positionX=\"60\"", "positionX=\"75\""))
        );
    }

    @Test
    void keepsTextAndChildOrderMeaningful() throws Exception {
        assertEquals(
            List.of("/pnml/net[@id='N']/labels[1]/text(): expected ' keep  this text ' but was 'keep this text'"),
            differences(SAVED, SAVED.replace(" keep  this text ", "keep this text"))
        );
        String swapped = SAVED.replace(
            "    <place id=\"P0\" positionX=\"60\" positionY=\"60\" invariant=\"&lt; inf\"/>\n"
                + "    <place id=\"P1\" positionX=\"90\" positionY=\"60\"/>\n",
            "    <place id=\"P1\" positionX=\"90\" positionY=\"60\"/>\n"
                + "    <place id=\"P0\" positionX=\"60\" positionY=\"60\" invariant=\"&lt; inf\"/>\n"
        );
        assertEquals(List.of(
            "/pnml/net[@id='N']/place[@id='P0']: element <place> is out of order: expected as child 1 but found as child 2"
        ), differences(SAVED, swapped));
    }

    @Test
    void reportsMissingAndExtraElements() throws Exception {
        String labels = "    <labels border=\"true\"> keep  this text </labels>\n";
        assertEquals(
            List.of("/pnml/net[@id='N']/labels[1]: missing element <labels>"),
            differences(SAVED, SAVED.replace(labels, ""))
        );
        assertEquals(
            List.of("/pnml/net[@id='N']/labels[2]: unexpected element <labels>"),
            differences(SAVED, SAVED.replace(labels, labels + labels))
        );
        assertEquals(
            List.of("/pnml/net[@id='N']/place[@id='P1']: missing element <place>"),
            differences(SAVED, SAVED.replace("    <place id=\"P1\" positionX=\"90\" positionY=\"60\"/>\n", ""))
        );
    }

    private static List<String> differences(String expected, String actual) throws Exception {
        return TapnXmlComparison.differences(
            new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8)),
            new ByteArrayInputStream(actual.getBytes(StandardCharsets.UTF_8))
        );
    }
}
