package net.tapaal.gui;

import dk.aau.cs.model.tapn.TimedPlace;
import net.tapaal.TAPAAL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Tag("gui-smoke")
class GuiWorkflowSmokeTest {
    @Test
    @Timeout(45)
    void editUndoRedoSaveAndReopen(@TempDir Path directory) throws Throwable {
        var file = directory.resolve("smoke.tapn");
        try (var fixture = getClass().getResourceAsStream("/validation/one-place.tapn")) {
            Files.copy(fixture, file);
        }
        var robot = new Robot();
        try {
            var original = onEdt(() -> {
                TAPAAL.main(new String[]{file.toString()});
                assertTrue(TAPAALGUI.getAppGui().isShowing());
                return TAPAALGUI.getCurrentTab();
            });
            assertEquals(Map.of("Start", 1), marking(original));

            Point point = onEdt(() -> {
                var canvas = original.drawingSurface();
                var place = original.getAvailableDrawActions().stream()
                    .filter(action -> "Place".equals(action.getValue(Action.NAME)))
                    .findFirst().orElseThrow();
                assertTrue(place.isEnabled());
                place.actionPerformed(new ActionEvent(canvas, ActionEvent.ACTION_PERFORMED, "Place"));
                Rectangle visible = canvas.getVisibleRect();
                assertFalse(visible.isEmpty(), "The editor canvas must be visible");
                var center = new Point(visible.x + visible.width / 2, visible.y + visible.height / 2);
                SwingUtilities.convertPointToScreen(center, canvas);
                return center;
            });
            robot.mouseMove(point.x, point.y);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            robot.waitForIdle();

            var editedMarking = marking(original);
            assertEquals(2, editedMarking.size());
            assertEquals(1, editedMarking.get("Start"));
            assertTrue(editedMarking.containsValue(0));

            clickMenu("Edit", "Undo");
            assertEquals(Map.of("Start", 1), marking(original));
            clickMenu("Edit", "Redo");
            assertEquals(editedMarking, marking(original));
            clickMenu("File", "Save");
            assertFalse(onEdt(original::getNetChanged), "Saving should clear the dirty state");
            clickMenu("File", "Close");

            onEdt(() -> {
                var controller = TAPAALGUI.getAppGuiController();
                assertTrue(controller.getTabs().isEmpty());
                controller.openFiles(List.of(file.toFile()));
                return null;
            });
            var reopened = awaitReopenedTab();
            assertNotSame(original, reopened);
            assertEquals(editedMarking, marking(reopened));
            System.out.println("Opened, edited, undid, redid, saved and reopened marking " + editedMarking);
        } catch (Throwable failure) {
            try {
                var artifacts = Path.of(System.getProperty("tapaal.smoke.artifacts"));
                Files.createDirectories(artifacts);
                var screenshot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                ImageIO.write(screenshot, "png", artifacts.resolve("workflow-failure.png").toFile());
                Files.copy(file, artifacts.resolve("workflow-failure.tapn"));
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

    private static Map<String, Integer> marking(PetriNetTab tab) throws Exception {
        return onEdt(() -> {
            var modelMarking = tab.currentTemplate().model().places().stream()
                .collect(Collectors.toMap(TimedPlace::name, TimedPlace::numberOfTokens));
            var canvasMarking = Arrays.stream(tab.drawingSurface().getGuiModel().getPlaces())
                .map(TimedPlaceComponent.class::cast)
                .collect(Collectors.toMap(TimedPlaceComponent::getName, TimedPlaceComponent::getNumberOfTokens));
            assertEquals(modelMarking, canvasMarking, "Canvas and model markings must agree");
            return modelMarking;
        });
    }

    static void clickMenu(String menuName, String actionName) throws Exception {
        onEdt(() -> {
            var bar = TAPAALGUI.getAppGui().getJMenuBar();
            for (int index = 0; index < bar.getMenuCount(); index++) {
                JMenu menu = bar.getMenu(index);
                if (menu == null || !menuName.equals(menu.getText())) {
                    continue;
                }
                for (int itemIndex = 0; itemIndex < menu.getItemCount(); itemIndex++) {
                    JMenuItem item = menu.getItem(itemIndex);
                    if (item != null && actionName.equals(item.getText())) {
                        assertTrue(item.isEnabled(), actionName + " must be enabled");
                        item.doClick(0);
                        return null;
                    }
                }
            }
            return fail("Missing menu action " + menuName + " / " + actionName);
        });
    }

    private static PetriNetTab awaitReopenedTab() throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        do {
            var tab = onEdt(() -> TAPAALGUI.getAppGuiController().getTabs().stream().findFirst().orElse(null));
            if (tab != null) {
                return tab;
            }
            Thread.sleep(25);
        } while (System.nanoTime() < deadline);
        return fail("Open did not attach a tab within ten seconds");
    }

    static <T> T onEdt(Callable<T> action) throws Exception {
        var task = new FutureTask<>(action);
        SwingUtilities.invokeLater(task);
        return task.get(15, TimeUnit.SECONDS);
    }
}
