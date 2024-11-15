package dk.aau.cs.verification.observations.expressions;

import dk.aau.cs.model.tapn.TimedPlace;

public class ObsPlace extends ObsLeaf {
    private final Object template;
    private final TimedPlace place;

    public ObsPlace(Object template, TimedPlace place) {
        this.template = template;
        this.place = place;
    }

    @Override
    public String toString() {
        return template + "." + place.name();
    }

    @Override
    public ObsExpression copy() {
        return new ObsPlace(template, place);
    }
}
