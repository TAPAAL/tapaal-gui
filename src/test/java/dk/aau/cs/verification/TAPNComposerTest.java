package dk.aau.cs.verification;

import dk.aau.cs.Messenger;
import dk.aau.cs.DeduplicatingMessenger;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedTransition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TAPNComposerTest {
    private static final String ORPHAN_TRANSITION_MESSAGE =
        "There are orphan transitions (no incoming and no outgoing arcs) in the model.";

    @Test
    void oneOrphanTransitionProducesOneWarningPerComposition() {
        TimedArcPetriNetNetwork network = networkWithOneOrphanTransition();
        CountingMessenger messenger = new CountingMessenger();

        new TAPNComposer(messenger, false).transformModel(network);

        assertEquals(1, messenger.orphanTransitionWarnings);
    }

    @Test
    void sharedMessengerShowsOrphanTransitionWarningOnlyOnceAcrossCompositions() {
        CountingMessenger messenger = new CountingMessenger();
        Messenger queryDialogMessenger = new DeduplicatingMessenger(messenger);
        for (int i = 0; i < 3; i++) {
            new TAPNComposer(queryDialogMessenger, false).transformModel(networkWithOneOrphanTransition());
        }

        assertEquals(1, messenger.orphanTransitionWarnings);
    }

    private static TimedArcPetriNetNetwork networkWithOneOrphanTransition() {
        TimedArcPetriNet net = new TimedArcPetriNet("net");
        net.add(new TimedTransition("orphan"));
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        network.add(net);
        return network;
    }

    private static class CountingMessenger implements Messenger {
        private int orphanTransitionWarnings;

        @Override
        public void displayInfoMessage(String message) {
            if (ORPHAN_TRANSITION_MESSAGE.equals(message)) orphanTransitionWarnings++;
        }

        @Override
        public void displayInfoMessage(String message, String title) {
            displayInfoMessage(message);
        }

        @Override
        public void displayErrorMessage(String message) { }

        @Override
        public void displayErrorMessage(String message, String title) { }

        @Override
        public void displayWrappedErrorMessage(String message, String title) { }
    }
}
