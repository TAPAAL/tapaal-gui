package net.tapaal.gui.petrinet.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.verification.VerificationResult;
import net.tapaal.gui.GuiFrameController;
import pipe.gui.GuiFrame;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.dataLayer.DataLayer;

@Tag("gui")
class RunVerificationOwnershipTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void completionKeepsItsOwnerAfterSelectionChanges(boolean explicitOwner) throws Exception {
        Completion completion = new Completion();
        AtomicReference<GuiFrame> frame = new AtomicReference<>();
        AtomicReference<GuiFrameController> controller = new AtomicReference<>();
        AtomicReference<PetriNetTab> owner = new AtomicReference<>();
        AtomicReference<PetriNetTab> other = new AtomicReference<>();

        try {
            SwingUtilities.invokeAndWait(() -> {
                frame.set(new GuiFrame("verification test"));
                controller.set(new GuiFrameController(frame.get()));
                owner.set(PetriNetTab.createNewEmptyTab("owner.tapn", true, false, false, false));
                other.set(PetriNetTab.createNewEmptyTab("other.tapn", true, false, false, false));
                controller.get().openTab(owner.get());
                controller.get().openTab(other.get());
                controller.get().changeToTab(owner.get());

                RunVerificationBase worker = explicitOwner
                    ? new CapturingVerification(owner.get(), completion)
                    : new CapturingVerification(owner.get().getGuiModels(), completion);
                worker.execute(null, owner.get().network(), new TAPNQuery(new TCTLEFNode(new TCTLTrueNode()), 0), null,
                    owner.get().getLens());
            });

            assertTrue(completion.started.await(10, TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(() -> controller.get().changeToTab(other.get()));
            completion.release.countDown();
            assertTrue(completion.shown.await(10, TimeUnit.SECONDS));
            assertEquals(owner.get(), completion.owner.get());
            assertEquals(Boolean.TRUE, completion.shownOnEdt.get());
            SwingUtilities.invokeAndWait(() -> assertEquals(other.get(), controller.get().getCurrentTab().orElseThrow()));
        } finally {
            completion.release.countDown();
            SwingUtilities.invokeAndWait(() -> {
                if (frame.get() != null) frame.get().dispose();
            });
        }
    }

    private static class Completion {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final CountDownLatch shown = new CountDownLatch(1);
        final AtomicReference<PetriNetTab> owner = new AtomicReference<>();
        final AtomicReference<Boolean> shownOnEdt = new AtomicReference<>();
    }

    private static class CapturingVerification extends RunVerificationBase {
        private final Completion completion;

        CapturingVerification(PetriNetTab owner, Completion completion) {
            super(null, null, owner.getGuiModels(), null, false, null, owner);
            this.completion = completion;
        }

        CapturingVerification(HashMap<TimedArcPetriNet, DataLayer> diagrams, Completion completion) {
            super(null, null, diagrams, null, false, null);
            this.completion = completion;
        }

        @Override protected VerificationResult<TAPNNetworkTrace> doInBackground() throws Exception {
            completion.started.countDown();
            assertTrue(completion.release.await(10, TimeUnit.SECONDS));
            return new VerificationResult<>(null, (TAPNNetworkTrace) null, 0, "");
        }

        @Override protected boolean showResult(VerificationResult<TAPNNetworkTrace> result) {
            completion.owner.set(getOwnerTab().orElse(null));
            completion.shownOnEdt.set(SwingUtilities.isEventDispatchThread());
            completion.shown.countDown();
            return false;
        }
    }
}
