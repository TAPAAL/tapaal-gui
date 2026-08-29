package net.tapaal.helpers;

import java.awt.Component;
import java.awt.Container;

public class Enabler {
    /**
     * Set all components in a container to be enabled or disabled.
     * @param container The container to set the components of.
     * @param isEnabled Whether the components should be enabled or disabled.
     */
    public static void setAllEnabled(Container container, boolean isEnabled) {
        for (Component component : container.getComponents()) {
            component.setEnabled(isEnabled);
            if (component instanceof Container) {
                setAllEnabled((Container) component, isEnabled);
            }
        }

        container.setEnabled(isEnabled);
    }
}
