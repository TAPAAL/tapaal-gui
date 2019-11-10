package net.tapaal.helpers.Reference;

import dk.aau.cs.util.Require;

import java.util.Objects;
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

    public<U> U map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return mapper.apply(ref);
    }

}
