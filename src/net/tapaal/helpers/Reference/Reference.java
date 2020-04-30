package net.tapaal.helpers.Reference;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class Reference<T> {

    private T ref;

    // You properly want to create a MutableReference
    Reference(T ref) {
        super();

        setReference(ref);
    }

    public final T get() {
        return ref;
    }

    void setReference(T ref) {
        this.ref = ref;
    }

    public void ifPresent(Consumer<? super T> consumer) {
        if (ref != null)
            consumer.accept(ref);
    }

    public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
        if (this.ref != null) {
            action.accept(this.ref);
        } else {
            emptyAction.run();
        }

    }

    public<U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return ref==null ? Optional.empty() : Optional.of(mapper.apply(ref));
    }

}
