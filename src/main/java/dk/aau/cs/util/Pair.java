package dk.aau.cs.util;

public class Pair<K, V> {
    private final K first;
    private final V second;

    public Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;

        Pair<?, ?> pair = (Pair<?, ?>)o;

        if (!first.equals(pair.first)) return false;
        return second.equals(pair.second);
    }

    @Override
    public int hashCode() {
        return first.hashCode() + second.hashCode();
    }
}
