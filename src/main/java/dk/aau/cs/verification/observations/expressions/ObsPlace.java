package dk.aau.cs.verification.observations.expressions;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.TCTL.visitors.BooleanResult;
import dk.aau.cs.TCTL.visitors.XMLQueryVisitorUtils;

public class ObsPlace extends ObsLeaf {
    private final Object template;
    private final TimedPlace place;
    private final String color;

    public ObsPlace(Object template, TimedPlace place) {
        this(template, place, null);
    }

    public ObsPlace(Object template, TimedPlace place, String color) {
        this.template = template;
        this.place = place;
        this.color = color == null || color.isBlank() ? null : color.trim();
    }

    public ObsPlace(String templateName, String placeName, TimedArcPetriNetNetwork network) {
        this(templateName, placeName, null, network);
    }

    public ObsPlace(String templateName, String placeName, String color, TimedArcPetriNetNetwork network) {
        if (templateName.equals("Shared")) {
            template = templateName;
            place = network.getSharedPlaceByName(placeName);
        } else {
            TimedArcPetriNet net = network.getTAPNByName(templateName);
            template = net;
            place = net != null ? net.getPlaceByName(placeName) : null;
        }
        
        this.color = color == null || color.isBlank() ? null : color.trim();
    }

    @Override
    public boolean containsPlace(TimedPlace place) {
        return this.place != null && this.place.equals(place);
    }

    @Override
    public ObsExpression replacePlace(TimedPlace toReplace, TimedPlace replacement, TimedArcPetriNet tapn, BooleanResult affected) {
        if (place != null && place.equals(toReplace)) {
            affected.setResult(true);
            if (replacement.isShared()) {
                return new ObsPlace("Shared", replacement, color);
            }
            
            return new ObsPlace(tapn, replacement, color);
        }

        return this;
    }

    @Override
    public String toString() {
        return template + "." + (place != null ? place.name() : "") + (color == null ? "" : "." + color);
    }

    @Override
    public String toXml() {
        var placeName = place != null ? place.name() : "";
        var colorExpr = color == null ? "" : "<color-expression>" + XMLQueryVisitorUtils.colorToXML(color) + "</color-expression>";
        return "<place>" + template + "__" + placeName + colorExpr + "</place>";
    }

    @Override
    public ObsExpression deepCopy() {
        ObsExpression copy = new ObsPlace(template, place, color);
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

    public String getColor() {
        return color;
    }
}
