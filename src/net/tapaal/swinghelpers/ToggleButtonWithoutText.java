package net.tapaal.swinghelpers;

import javax.swing.*;
import java.awt.*;

/**
 * A JToggleButton that without any text
 */
public class ToggleButtonWithoutText extends JToggleButton {

    public ToggleButtonWithoutText(Action a) {
        super(a);
        if (a.getValue(Action.SMALL_ICON) == null) {
            setVisible(false);
        }
        setText(null);

        this.setRequestFocusEnabled(false);
    }

}
