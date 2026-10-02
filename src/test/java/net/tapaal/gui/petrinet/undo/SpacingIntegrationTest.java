package net.tapaal.gui.petrinet.undo;

import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.TapnXmlLoader;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import net.tapaal.gui.petrinet.Template;
import org.junit.jupiter.api.Test;
import pipe.gui.canvas.Grid;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.graphicElements.AnnotationNote;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.ArcPathPoint;
import pipe.gui.petrinet.graphicElements.PetriNetObject;

import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.geom.PathIterator;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpacingIntegrationTest {
    @Test
    void spacingUndoPreservesPreviouslySavedBundledModels() throws Exception {
        onEdt(() -> {
            for (String name : List.of("producer-consumer.tapn", "alternating-bit-protocol-components.tapn", "cpn-packet.tapn")) {
                PetriNetTab tab = loadTab(Files.readString(Path.of("src/main/resources/Example nets", name)));
                byte[] before = save(tab);
                tab.increaseSpacing();
                tab.getUndoManager().undo();
                assertEquals(new String(before, StandardCharsets.UTF_8), new String(save(tab), StandardCharsets.UTF_8), name);
            }
        });
    }
    @Test
    void undoAndRedoRestoreNodesAnnotationAndBentArcAcrossZoomAndGridChanges() throws Exception {
        onEdt(() -> {
            PetriNetTab tab = loadTab(modelXml());
            Template template = tab.currentTemplate();
            List<Point> before = positions(template);
            Point noteBefore = annotation(template).getLocation();
            List<Double> pathBefore = path(template);
            byte[] beforeSave = save(tab);

            tab.increaseSpacing();
            List<Point> after = positions(template);
            Point noteAfter = annotation(template).getLocation();
            List<Double> pathAfter = path(template);
            assertNotEquals(before, after);
            assertNotEquals(noteBefore, noteAfter);
            assertEquals(new Point(annotation(template).getOriginalX(), annotation(template).getOriginalY()), noteAfter,
                "Annotation coordinates written to disk must match its visible location");
            assertNotEquals(pathBefore, pathAfter);
            byte[] afterSave = save(tab);

            tab.zoomTo(200);
            Grid.enableGrid();
            tab.getUndoManager().undo();
            assertTrue(Grid.isEnabled(), "Restoring positions must preserve the grid setting");
            assertEquals(before, positions(template));
            Grid.disableGrid();
            tab.zoomTo(100);
            assertEquals(noteBefore, annotation(template).getLocation());
            assertEquals(pathBefore, path(template));
            assertRoundTripEquals(beforeSave, save(tab));

            tab.zoomTo(50);
            tab.getUndoManager().redo();
            assertEquals(after, positions(template));
            tab.zoomTo(100);
            assertEquals(noteAfter, annotation(template).getLocation());
            assertEquals(pathAfter, path(template));
            assertRoundTripEquals(afterSave, save(tab));
        });
    }

    @Test
    void undoAfterChangingTemplateRestoresOnlyTheOriginatingTemplate() throws Exception {
        onEdt(() -> {
            PetriNetTab tab = loadTab(modelXml());
            Template first = tab.currentTemplate();
            Template second = null;
            for (Template template : tab.allTemplates()) {
                if (template != first) second = template;
            }
            assertNotNull(second);
            List<Point> firstBefore = positions(first);
            List<Double> pathBefore = path(first);
            List<Point> secondBefore = positions(second);
            tab.increaseSpacing();
            List<Point> firstAfter = positions(first);
            tab.getTemplateExplorer().selectTemplate(second);
            tab.getUndoManager().undo();
            assertSame(second.model(), tab.currentTemplate().model());
            assertEquals(firstBefore, positions(first));
            assertEquals(pathBefore, path(first));
            assertEquals(secondBefore, positions(second));
            tab.getUndoManager().redo();
            assertEquals(firstAfter, positions(first));
            assertEquals(secondBefore, positions(second));
        });
    }

    @Test
    void rejectedDecreaseDoesNotConsumeThePreviousUndoEntry() throws Exception {
        onEdt(() -> {
            PetriNetTab tab = loadTab(modelXml());
            List<Point> beforeLastChange = positions(tab.currentTemplate());
            for (int i = 0; i < 30; i++) {
                List<Point> before = positions(tab.currentTemplate());
                tab.decreaseSpacing();
                if (before.equals(positions(tab.currentTemplate()))) {
                    tab.getUndoManager().undo();
                    assertEquals(beforeLastChange, positions(tab.currentTemplate()));
                    return;
                }
                beforeLastChange = before;
            }
            fail("Expected decreasing spacing to reach the minimum-distance guard");
        });
    }

    private static void assertRoundTripEquals(byte[] expected, byte[] actual) throws Exception {
        // The legacy annotation loader adds one to stored coordinates and dimensions.
        // Compare equivalent reloads so this test detects drift caused by spacing undo,
        // without silently changing the existing file-format interpretation.
        LoadedModel baseline = new TapnXmlLoader().load(new ByteArrayInputStream(expected));
        LoadedModel restored = new TapnXmlLoader().load(new ByteArrayInputStream(actual));
        var baselineTemplates = baseline.templates().iterator();
        var restoredTemplates = restored.templates().iterator();
        while (baselineTemplates.hasNext()) {
            Template a = baselineTemplates.next();
            Template b = restoredTemplates.next();
            assertEquals(positions(a), positions(b));
            assertEquals(annotation(a).getBounds(), annotation(b).getBounds());
            assertEquals(path(a), path(b));
        }
        assertFalse(restoredTemplates.hasNext());
    }

    private static byte[] save(PetriNetTab tab) throws Exception {
        return new TimedArcPetriNetNetworkWriter(tab.network(), tab.allTemplates(), tab.queries(),
            tab.network().constants()).savePNML().toByteArray();
    }

    private static PetriNetTab loadTab(String xml) throws Exception {
        LoadedModel model = new TapnXmlLoader().load(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        return new PetriNetTab(model.network(), model.templates(), model.queries(), model.getLens());
    }

    private static List<Point> positions(Template template) {
        List<Point> points = new ArrayList<>();
        for (PetriNetObject object : template.guiModel().getPetriNetObjects()) {
            if (!(object instanceof Arc)) points.add(new Point(object.getOriginalX(), object.getOriginalY()));
        }
        for (Arc arc : template.guiModel().getArcs()) {
            for (ArcPathPoint point : arc.getArcPath().getArcPathPoints()) {
                if (!point.isEndPoint()) points.add(new Point(point.getOriginalX(), point.getOriginalY()));
            }
        }
        return points;
    }

    private static AnnotationNote annotation(Template template) {
        for (PetriNetObject object : template.guiModel().getPetriNetObjects()) {
            if (object instanceof AnnotationNote) return (AnnotationNote) object;
        }
        throw new AssertionError("Missing annotation");
    }

    private static List<Double> path(Template template) {
        List<Double> segments = new ArrayList<>();
        for (Arc arc : template.guiModel().getArcs()) {
            PathIterator iterator = arc.getArcPath().getPathIterator(null);
            double[] coordinates = new double[6];
            while (!iterator.isDone()) {
                int type = iterator.currentSegment(coordinates);
                segments.add((double) type);
                int count = type == PathIterator.SEG_CUBICTO ? 6 : type == PathIterator.SEG_QUADTO ? 4 : type == PathIterator.SEG_CLOSE ? 0 : 2;
                for (int i = 0; i < count; i++) segments.add(coordinates[i]);
                iterator.next();
            }
        }
        return segments;
    }

    private static String modelXml() {
        String net = """
            <net active="true" id="%s" type="P/T net">
              <labels border="true" height="40" positionX="101" positionY="233" width="180">Saved annotation</labels>
              <place displayName="true" id="P0" initialMarking="0" invariant="&lt; inf" name="P0" nameOffsetX="0" nameOffsetY="35" positionX="101" positionY="103"/>
              <transition angle="0" displayName="true" id="T0" infiniteServer="false" name="T0" nameOffsetX="0" nameOffsetY="35" positionX="341" positionY="103" priority="0" urgent="false"/>
              <arc id="P0 to T0" inscription="[0,inf)" nameOffsetX="0" nameOffsetY="0" source="P0" target="T0" type="timed" weight="1">
                <arcpath arcPointType="false" id="0" xCoord="116" yCoord="118"/>
                <arcpath arcPointType="false" id="1" xCoord="221" yCoord="191"/>
                <arcpath arcPointType="false" id="2" xCoord="356" yCoord="118"/>
              </arc>
            </net>
            """;
        return "<pnml xmlns=\"http://www.informatik.hu-berlin.de/top/pnml/ptNetb\">"
            + String.format(net, "First") + String.format(net, "Second") + "</pnml>";
    }

    private static void onEdt(CheckedRunnable action) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Grid.disableGrid();
            try {
                action.run();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            } finally {
                Grid.disableGrid();
            }
        });
    }

    private interface CheckedRunnable {
        void run() throws Exception;
    }
}
