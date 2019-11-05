package net.tapaal.swinghelpers;

import javax.swing.*;

/**
 * A JToggleButton that without any text
 */
public class ToggleButtonWithoutText extends JToggleButton {

    public ToggleButtonWithoutText(Action a) {
        super(a);
        if (a.getValue(Action.SMALL_ICON) != null) {
            // toggle buttons like to have images *and* text, nasty
            setText(null);
        }
        this.setRequestFocusEnabled(false);
    }

}
