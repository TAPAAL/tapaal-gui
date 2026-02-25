package dk.aau.cs.verification.observations.expressions;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.TCTL.visitors.BooleanResult;

public class ObsPlace extends ObsLeaf {
    private final Object template;
    private final TimedPlace place;

    public ObsPlace(Object template, TimedPlace place) {
        this.template = template;
        this.place = place;
    }

    public ObsPlace(String templateName, String placeName, TimedArcPetriNetNetwork network) {
        if (templateName.equals("Shared")) {
            template = templateName;
            place = network.getSharedPlaceByName(placeName);
        } else {
            TimedArcPetriNet net = network.getTAPNByName(templateName);
            template = net;
            place = net.getPlaceByName(placeName);
        }
    }

    @Override
    public boolean containsPlace(TimedPlace place) {
        return this.place.equals(place);
    }

    @Override
    public ObsExpression replacePlace(TimedPlace toReplace, TimedPlace replacement, TimedArcPetriNet tapn, BooleanResult affected) {
        if (place.equals(toReplace)) {
            affected.setResult(true);
            if (replacement.isShared()) {
                return new ObsPlace("Shared", replacement);
            }
            
            return new ObsPlace(tapn, replacement);
        }

        return this;
    }

    @Override
    public String toString() {
        return template + "." + place.name();
    }

    @Override
    public String toXml(boolean legacy) {
        if (legacy) {
            return "<place>" + template + "_" + place.name() + "</place>";
        }
        
        return "<place component=\"" + template + "\" id=\"" + place.name() + "\"/>";
    }

    @Override
    public ObsExpression deepCopy() {
        ObsExpression copy = new ObsPlace(template, place);
        copy.setParent(parent);
        return copy;
    }

    @Override
    public boolean isPlace() {
        return true;
    }

    public TimedPlace getPlace() {
        return place;
    }

    public Object getTemplate() {
        return template;
    }
}
