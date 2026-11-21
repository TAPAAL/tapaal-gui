package net.tapaal.gui;

import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import net.tapaal.TAPAAL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.action.GuiAction;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.xml.transform.TransformerConfigurationException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import static net.tapaal.gui.GuiWorkflowSmokeTest.clickMenu;
import static net.tapaal.gui.GuiWorkflowSmokeTest.onEdt;
import static org.junit.jupiter.api.Assertions.*;

/** Drags a place with the mouse, saves through the File menu and compares the saved document. */
@Tag("gui-smoke")
class PlaceDragSaveRegressionTest {
    private static final String FIXTURES = "/validation/move-place/";
    // Multiples of the 15-pixel editor grid at 100% zoom, so grid snapping cannot change the result.
    private static final int DRAG_X = 90;
    private static final int DRAG_Y = 60;

    @RegisterExtension
    final CaughtExceptionRecorder caught = new CaughtExceptionRecorder()
        .allow("TAPAALGUI.init/native-about-handler", UnsupportedOperationException.class, "no native about menu on this desktop")
        .allow("TAPAALGUI.init/native-quit-handler", UnsupportedOperationException.class, "no native quit menu on this desktop")
        .allow("TAPAALGUI.init/taskbar-icon", UnsupportedOperationException.class, "no taskbar on this desktop");

    @Test
    @Timeout(60)
    void draggedPlaceIsSavedAtItsNewPosition(@TempDir Path directory) throws Throwable {
        var file = directory.resolve("move-place.tapn");
        copyFixture("before.tapn", file);
        var artifacts = Path.of(System.getProperty("tapaal.smoke.artifacts", "build/reports/gui-smoke"))
            .resolve("place-drag-save");
        var robot = new Robot();
        try {
            var tab = onEdt(() -> {
                TAPAAL.main(new String[]{file.toString()});
                return TAPAALGUI.getCurrentTab();
            });
            assertTrue(onEdt(() -> selectTool(tab).isSelected()), "The editor should start with the Select tool");

            drag(robot, tab, "P0");
            assertEquals(new Point(60 + DRAG_X, 60 + DRAG_Y), onEdt(() -> position(tab, "P0")),
                "The mouse drag should move P0 on the canvas");
            assertTrue(onEdt(tab::getNetChanged), "The drag should mark the net as changed");

            clickMenu("File", "Save");
            assertFalse(onEdt(tab::getNetChanged), "Saving should clear the dirty state");
            assertSavedDocumentMatches("expected.tapn", file, artifacts);

            // Make the writer fail inside its own catch block, then save a second move.
            drag(robot, tab, "P0");
            try (var ignored = UnavailableTransformerFactory.select()) {
                clickMenu("File", "Save");
            }
            var saveFailures = caught.take(TimedArcPetriNetNetworkWriter.SAVE_FILE_SITE);
            assertEquals(1, saveFailures.size(), "The writer should report the failure it swallows: " + saveFailures);
            var failure = saveFailures.get(0).failure();
            assertInstanceOf(TransformerConfigurationException.class, failure);
            assertEquals(UnavailableTransformerFactory.MESSAGE, failure.getMessage());
            assertEquals(List.of(), TapnXmlComparison.differences(fixture("expected.tapn"), file),
                "The failed save should leave the previous file in place");
            System.out.println("Observed swallowed save failure " + saveFailures.get(0)
                + "; editor dirty after failed save: " + onEdt(tab::getNetChanged));
        } catch (Throwable failure) {
            try {
                Files.createDirectories(artifacts);
                var screenshot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                ImageIO.write(screenshot, "png", artifacts.resolve("failure.png").toFile());
                if (Files.exists(file)) {
                    Files.copy(file, artifacts.resolve("saved.tapn"), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception artifactError) {
                failure.addSuppressed(artifactError);
            }
            throw failure;
        }
    }

    @AfterEach
    void closeWindows() throws Exception {
        onEdt(() -> {
            for (var window : Window.getWindows()) {
                window.dispose();
            }
            return null;
        });
    }

    /** Presses on the place, moves the real mouse in steps and releases it. */
    private static void drag(Robot robot, PetriNetTab tab, String placeName) throws Exception {
        Point from = onEdt(() -> {
            var place = place(tab, placeName);
            assertTrue(place.isShowing(), placeName + " must be visible on the canvas");
            var center = new Point(place.getWidth() / 2, place.getHeight() / 2);
            SwingUtilities.convertPointToScreen(center, place);
            return center;
        });
        robot.mouseMove(from.x, from.y);
        robot.waitForIdle();
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.waitForIdle();
        int steps = 10;
        for (int step = 1; step <= steps; step++) {
            robot.mouseMove(from.x + DRAG_X * step / steps, from.y + DRAG_Y * step / steps);
            robot.waitForIdle();
        }
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.waitForIdle();
    }

    private static void assertSavedDocumentMatches(String expectedName, Path saved, Path artifacts) throws Exception {
        var expected = fixture(expectedName);
        var differences = TapnXmlComparison.differences(expected, saved);
        if (differences.isEmpty()) {
            return;
        }
        Files.createDirectories(artifacts);
        Files.copy(expected, artifacts.resolve("expected.tapn"), StandardCopyOption.REPLACE_EXISTING);
        Files.write(artifacts.resolve("differences.txt"), differences);
        fail("Saved document differs from " + FIXTURES + expectedName + " (artifacts in " + artifacts + "):\n  "
            + String.join("\n  ", differences));
    }

    private static GuiAction selectTool(PetriNetTab tab) {
        return tab.getAvailableDrawActions().stream()
            .filter(action -> "Select".equals(action.getValue(GuiAction.NAME)))
            .findFirst().orElseThrow();
    }

    private static TimedPlaceComponent place(PetriNetTab tab, String name) {
        return Arrays.stream(tab.drawingSurface().getGuiModel().getPlaces())
            .map(TimedPlaceComponent.class::cast)
            .filter(place -> name.equals(place.getName()))
            .findFirst().orElseThrow(() -> new AssertionError("No place named " + name));
    }

    private static Point position(PetriNetTab tab, String name) {
        var place = place(tab, name);
        return new Point(place.getOriginalX(), place.getOriginalY());
    }

    private static void copyFixture(String name, Path target) throws Exception {
        Files.copy(fixture(name), target);
    }

    private static Path fixture(String name) throws Exception {
        var resource = PlaceDragSaveRegressionTest.class.getResource(FIXTURES + name);
        assertNotNull(resource, "Missing fixture " + FIXTURES + name);
        return Path.of(resource.toURI());
    }
}
