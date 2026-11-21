package dk.aau.cs.debug;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Makes failures that production code catches without rethrowing visible to tests.
 *
 * <p>Call {@link #report} from a catch block that deliberately continues. The label names the
 * call site and must stay stable, because tests refer to it. Production installs no observer, so
 * reporting does nothing and the catch block keeps its current behavior.
 */
public final class CaughtExceptions {

    @FunctionalInterface
    public interface Observer {
        /** Called on the thread that caught the failure. It must return quickly and must not throw. */
        void caught(String site, Throwable failure);
    }

    /** Removes the observer it was returned for. */
    public interface Installation extends AutoCloseable {
        @Override
        void close();
    }

    private static final Observer NONE = (site, failure) -> { };
    private static final AtomicReference<Observer> observer = new AtomicReference<>(NONE);

    private CaughtExceptions() { }

    public static void report(String site, Throwable failure) {
        observer.get().caught(site, failure);
    }

    /** Installs one observer at a time, so a leaked installation fails the next test. */
    public static Installation install(Observer newObserver) {
        Objects.requireNonNull(newObserver, "observer");
        if (!observer.compareAndSet(NONE, newObserver)) {
            throw new IllegalStateException("A caught-exception observer is already installed");
        }
        return () -> observer.compareAndSet(newObserver, NONE);
    }
}
