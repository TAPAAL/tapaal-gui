package dk.aau.cs.model.CPN;

import dk.aau.cs.model.CPN.Expressions.AllExpression;
import dk.aau.cs.model.CPN.Expressions.ColorExpression;
import dk.aau.cs.model.CPN.Expressions.TupleExpression;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;

import java.util.*;

public class ColorMultiset implements Map<Color, Integer> {

    private final HashMap<Color, Integer> map = new HashMap<>();
    private final ColorType colorType;
    private int numberOfTokens = 0;

    public ColorMultiset(ColorType colorType, int numberOf, Iterable<Color> colors, Vector<ColorExpression> colorExpression) {
        this.colorType = colorType;
        if (numberOf > 0) {
            for (Color c : colors) {
                //Direct put is fine here, since constructor ensures colorType and positive number
                map.put(c, numberOf);
            }
            updateTokenNumber(colorExpression, numberOf);
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return map.containsValue(o);
    }

    @Override
    public Integer get(Object o) {
        //Default to 0 when key not in set
        if (!containsKey(o)) {
            return 0;
        } else {
            return map.get(o);
        }
    }

    @Override
    public Integer put(Color color, Integer integer) {
        if (!(color.getColorType().equals(colorType))) {
            throw new IllegalArgumentException(String.format("Type mismatch: Attempted to put %s in multiset of type %s", color.getName(), colorType.getName()));
        }
        //If at most 0, clean key from set
        if (integer <= 0) {
            if (containsKey(color)) {
                Integer previous = get(color);
                remove(color);
                return previous;
            } else {
                return null;
            }
        } else {
            return map.put(color, integer);
        }
    }

    @Override
    public Integer remove(Object o) {
        return map.remove(o);
    }

    @Override
    public void putAll(Map<? extends Color, ? extends Integer> map) {
        for (Entry<? extends Color, ? extends Integer> kv : map.entrySet()) {
            put(kv.getKey(), kv.getValue());
        }
    }

    @Override
    public void clear() {
        map.clear();
        numberOfTokens = 0;
    }

    @Override
    public Set<Color> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Integer> values() {
        return map.values();
    }

    @Override
    public Set<Entry<Color, Integer>> entrySet() {
        return map.entrySet();
    }

    public void add(Color color, int count) {
        Integer result = get(color) + count;
        put(color, result);
    }

    public void sub(Color color, int count) {
        Integer result = get(color) - count;
        put(color, result);
    }

    public void addAll(Map<? extends Color, ? extends Integer> map, Vector<ColorExpression> colorExpressions, int numberOf) {
        updateTokenNumber(colorExpressions, numberOf);
        for (Entry<? extends Color, ? extends Integer> kv : map.entrySet()) {
            add(kv.getKey(), kv.getValue());
        }
    }

    public void subAll(Map<? extends Color, ? extends Integer> map) {
        for (Entry<? extends Color, ? extends Integer> kv : map.entrySet()) {
            sub(kv.getKey(), kv.getValue());
        }
    }

    public void scale(int scalar) {
        for (Entry<? extends Color, ? extends Integer> kv : entrySet()) {
            Color color = kv.getKey();
            Integer result = get(color) * scalar;
            put(color, result);
        }
    }

    private void updateTokenNumber(Vector<ColorExpression> colorExpression, int numberOf) {
        for (ColorExpression ce : colorExpression) {
            if (ce instanceof TupleExpression) {
                updateTupleTokenNumber(((TupleExpression) ce).getColors(), numberOf);
            } else if (ce instanceof AllExpression) {
                numberOfTokens += ((AllExpression) ce).size() * numberOf;
            } else {
                numberOfTokens += numberOf;
            }
        }
    }

    private void updateTupleTokenNumber(Vector<ColorExpression> colorExpression, int numberOf) {
        int numberOfTupleTokens = 1;
        for (ColorExpression ce : colorExpression) {
            if (ce instanceof AllExpression) {
                if (numberOfTupleTokens < numberOf)
                    numberOfTupleTokens *= ((AllExpression) ce).size() * numberOf;
                else
                    numberOfTupleTokens *= ((AllExpression) ce).size();
            }
        }
        numberOfTokens += Math.max(numberOfTupleTokens, numberOf);
    }

    public Vector<TimedToken> getTokens(TimedPlace place) {
        Vector<TimedToken> result = new Vector<>();
        place.setNumberOfTokens(numberOfTokens);

        for (Entry<Color, Integer> entry : entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                result.add(new TimedToken(place, entry.getKey()));
            }
        }
        return result;
    }
}
