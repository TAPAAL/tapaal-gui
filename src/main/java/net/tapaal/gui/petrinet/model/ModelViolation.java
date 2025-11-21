package net.tapaal.gui.petrinet.model;

public enum ModelViolation {

    //PlaceNotNull("Place can't be null"),
    //TransitionNotNull("Transion can't be null"),
    //ModelNotNull("Model can't be null"),
    MaxOneArcBetweenPlaceAndTransition("There is already an arc between the selected place and transition"),
    MaxOneArcBetweenTransitionAndPlace("There is already an arc between the selected transition and place");

    private final String errorMessage;

    ModelViolation(String s) {
        this.errorMessage = s;
    }

    public String getErrorMessage() { return this.errorMessage;}
}