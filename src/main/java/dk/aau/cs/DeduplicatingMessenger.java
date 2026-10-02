package dk.aau.cs;

import java.util.HashSet;
import java.util.Set;

/** A messenger that displays each distinct informational message at most once. */
public class DeduplicatingMessenger implements Messenger {
    private final Messenger delegate;
    private final Set<String> displayedInfoMessages = new HashSet<>();

    public DeduplicatingMessenger(Messenger delegate) {
        this.delegate = delegate;
    }

    @Override
    public void displayInfoMessage(String message) {
        if (markAsDisplayed(message)) delegate.displayInfoMessage(message);
    }

    @Override
    public void displayInfoMessage(String message, String title) {
        if (markAsDisplayed(message)) delegate.displayInfoMessage(message, title);
    }

    private synchronized boolean markAsDisplayed(String message) {
        return displayedInfoMessages.add(message);
    }

    @Override
    public void displayErrorMessage(String message) {
        delegate.displayErrorMessage(message);
    }

    @Override
    public void displayErrorMessage(String message, String title) {
        delegate.displayErrorMessage(message, title);
    }

    @Override
    public void displayWrappedErrorMessage(String message, String title) {
        delegate.displayWrappedErrorMessage(message, title);
    }
}
