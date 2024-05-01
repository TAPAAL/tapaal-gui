package dk.aau.cs.pddl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class util {

    public static<T> ArrayList<ArrayList<T>> cartesian(ArrayList<ArrayList<T>> setOfSets) {

        List<Iterable<T>> iterables = setOfSets.stream().map(set ->
            (Iterable<T>)set
        ).collect(Collectors.toList());

        return cartesian(iterables);
    }

    public static<T> ArrayList<ArrayList<T>> cartesian(Iterable<Iterable<T>> setOfSets) {

        var setOfSetsIterator = setOfSets.iterator();
        if(!setOfSetsIterator.hasNext())
            return new ArrayList<>();


        ArrayList<ArrayList<T>> out = new ArrayList<>();

        // Explode first set into a set of singles
        var first_set = setOfSetsIterator.next();
        for(var x: first_set) {
            out.add(new ArrayList<>() {{
                add(x);
            }});
        }

        while(setOfSetsIterator.hasNext()) {
            var next = setOfSetsIterator.next();
            out = cartesian(out, next);
        }


        return out;
    }

    private static<T> ArrayList<ArrayList<T>> cartesian(Iterable<ArrayList<T>> tupleSet, Iterable<T> set) {
        ArrayList<ArrayList<T>> out = new ArrayList<>();

        for(ArrayList<T> t: tupleSet) {
            for (T s : set) {
                out.add(new ArrayList<T>() {{
                    addAll(t);
                    add(s);
                }});
            }
        }

        return out;
    }

}
