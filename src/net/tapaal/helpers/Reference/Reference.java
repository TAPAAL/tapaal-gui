package net.tapaal.helpers.Reference;

import dk.aau.cs.util.Require;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Reference<T> {

    private T ref;

    public Reference(T ref) {
        setReference(ref);
    }
    public final T get() {
        return ref;
    }

    void setReference(T ref) {
        Require.notNull(ref, "Can't make a reference to null");
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
