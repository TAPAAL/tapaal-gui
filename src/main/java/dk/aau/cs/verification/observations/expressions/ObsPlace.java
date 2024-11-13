package dk.aau.cs.verification.observations.expressions;

import dk.aau.cs.model.tapn.TimedPlace;

public class ObsPlace extends ObsLeaf {
    private final TimedPlace place;

    public ObsPlace(TimedPlace place) {
        this.place = place;
    }

    @Override
    public String toString() {
        return place.name();
    }

    @Override
    public ObsExpression copy() {
        return new ObsPlace(place);
    }
}
