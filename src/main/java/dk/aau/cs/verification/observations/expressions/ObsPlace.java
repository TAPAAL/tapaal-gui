package dk.aau.cs.verification.observations.expressions;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
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
    public String toXml() {
        return "<obs-place name=\"" + template + "_" + place.name() + "\"/>";
    }

    @Override
    public ObsExpression deepCopy() {
        return new ObsPlace(template, place);
    }
}
