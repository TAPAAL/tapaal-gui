package net.tapaal.gui;

import dk.aau.cs.debug.CaughtExceptions;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Records failures that production code reports through {@link CaughtExceptions} during one test.
 *
 * <p>A test fails after it finishes if any recorded failure was neither allowed nor taken.
 * Use {@link #allow} for catches that are expected in the test environment, and {@link #take}
 * when the test provokes a failure and asserts on it.
 */
final class CaughtExceptionRecorder implements BeforeEachCallback, AfterEachCallback {
    record Caught(String site, Throwable failure, String thread) {
        @Override
        public String toString() {
            return site + " on " + thread + ": " + failure;
        }
    }

    private record Allowance(String site, Class<? extends Throwable> type, String reason) {
        boolean matches(Caught caught) {
            return site.equals(caught.site()) && type.isInstance(caught.failure());
        }
    }

    private final List<Allowance> allowances = new ArrayList<>();
    private final List<Caught> recorded = new ArrayList<>();
    private CaughtExceptions.Installation installation;

    /** Accepts any number of failures of this type at this site, including none. */
    CaughtExceptionRecorder allow(String site, Class<? extends Throwable> type, String reason) {
        allowances.add(new Allowance(site, type, reason));
        return this;
    }

    /** Removes and returns the failures recorded at this site so far. */
    synchronized List<Caught> take(String site) {
        var taken = recorded.stream().filter(caught -> caught.site().equals(site)).collect(Collectors.toList());
        recorded.removeAll(taken);
        return taken;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        synchronized (this) {
            recorded.clear();
        }
        installation = CaughtExceptions.install((site, failure) -> {
            synchronized (this) {
                recorded.add(new Caught(site, failure, Thread.currentThread().getName()));
            }
        });
    }

    @Override
    public void afterEach(ExtensionContext context) {
        installation.close();
        List<Caught> unexpected;
        synchronized (this) {
            for (var caught : recorded) {
                allowances.stream().filter(allowance -> allowance.matches(caught)).findFirst()
                    .ifPresent(allowance -> System.out.println("Allowed caught exception (" + allowance.reason() + "): " + caught));
            }
            unexpected = recorded.stream()
                .filter(caught -> allowances.stream().noneMatch(allowance -> allowance.matches(caught)))
                .collect(Collectors.toList());
            recorded.clear();
        }
        if (unexpected.isEmpty()) {
            return;
        }
        var message = unexpected.stream().map(Caught::toString)
            .collect(Collectors.joining("\n  ", "Production code caught and continued after:\n  ", ""));
        var error = new AssertionFailedError(message, unexpected.get(0).failure());
        unexpected.stream().skip(1).forEach(caught -> error.addSuppressed(caught.failure()));
        throw error;
    }
}
