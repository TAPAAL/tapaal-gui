package net.tapaal.gui.petrinet.smartdraw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import net.tapaal.gui.GuiFrameController;
import pipe.gui.GuiFrame;
import pipe.gui.petrinet.PetriNetTab;

@Tag("gui")
class SmartDrawCompletionTest {
    @Test
    void completionOffersLayoutForTheResultAfterSelectionChanges() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);
        List<PetriNetTab> offered = new ArrayList<>();
        AtomicReference<GuiFrame> frame = new AtomicReference<>();
        AtomicReference<GuiFrameController> controller = new AtomicReference<>();
        AtomicReference<PetriNetTab> target = new AtomicReference<>();
        AtomicReference<PetriNetTab> unrelated = new AtomicReference<>();

        try {
            SwingUtilities.invokeAndWait(() -> {
                frame.set(new GuiFrame("layout test"));
                controller.set(new GuiFrameController(frame.get()));
                PetriNetTab source = tabWithPlace("source.tapn");
                source.currentTemplate().setHasPositionalInfo(true);
                target.set(tabWithPlace("unfolded.tapn"));
                unrelated.set(tabWithPlace("unrelated.tapn"));
                controller.get().openTab(source);
                controller.get().openTab(target.get());
                controller.get().openTab(unrelated.get());
                controller.get().changeToTab(source);

                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override protected Void doInBackground() throws Exception {
                        started.countDown();
                        assertTrue(release.await(10, TimeUnit.SECONDS));
                        return null;
                    }
                    @Override protected void done() {
                        firePropertyChange("unfolding", null, target.get());
                        finished.countDown();
                    }
                };
                SmartDrawDialog.setupWorkerListener(worker, tab -> {
                    assertTrue(SwingUtilities.isEventDispatchThread());
                    offered.add(tab);
                });
                worker.execute();
            });

            assertTrue(started.await(10, TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(() -> controller.get().changeToTab(unrelated.get()));
            release.countDown();
            assertTrue(finished.await(10, TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(() -> {
                assertEquals(List.of(target.get()), offered);
                assertEquals(unrelated.get(), controller.get().getCurrentTab().orElseThrow());
            });
        } finally {
            release.countDown();
            SwingUtilities.invokeAndWait(() -> {
                if (frame.get() != null) frame.get().dispose();
            });
        }
    }

    @Test
    void positionedEmptyAndUndrawableResultsDoNotOfferLayout() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<PetriNetTab> offered = new ArrayList<>();
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override protected Void doInBackground() { return null; }
            };
            SmartDrawDialog.setupWorkerListener(worker, offered::add);
            PetriNetTab positioned = tabWithPlace("positioned.tapn");
            positioned.currentTemplate().setHasPositionalInfo(true);
            PetriNetTab empty = PetriNetTab.createNewEmptyTab("empty.tapn", true, false, false, false);
            PetriNetTab undrawable = tabWithPlace("large.tapn");
            undrawable.network().setPaintNet(false);

            worker.firePropertyChange("unfolding", null, positioned);
            worker.firePropertyChange("unfolding", null, empty);
            worker.firePropertyChange("unfolding", null, undrawable);
            worker.firePropertyChange("unfolding", null, null);

            assertTrue(offered.isEmpty());
        });
    }

    private static PetriNetTab tabWithPlace(String name) {
        PetriNetTab tab = PetriNetTab.createNewEmptyTab(name, true, false, false, false);
        tab.guiModelManager.addNewTimedPlace(tab.currentTemplate().guiModel(), new Point(100, 100));
        return tab;
    }
}
