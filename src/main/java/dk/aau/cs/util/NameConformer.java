package dk.aau.cs.util;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import pipe.gui.TAPAALGUI;

public class NameConformer {
    private boolean hasConformed = false;

    /**
     * Conforms a string to the following regex pattern:
     * {@code [a-zA-Z][_a-zA-Z0-9]*}
     */
    public String conform(String name) {
        String conformedName = name;

        conformedName = conformedName.replaceAll("\\s+|-", "_");
        conformedName = conformedName.replace("*", "star");
        conformedName = conformedName.replace("/", "slash");
        conformedName = conformedName.replace("+", "plus");
        conformedName = conformedName.replace("-", "minus");
    
        Require.that(conformedName.matches("[a-zA-Z][_a-zA-Z0-9]*"), "Name: " + name + " does not conform to the pattern [a-zA-Z][_a-zA-Z0-9]*, and could not be conformed automatically");

        // Displays warning only once for a instance
        if (!hasConformed && !conformedName.equals(name)) {
            hasConformed = true;
            SwingUtilities.invokeLater(() -> {
                StringBuilder message = new StringBuilder();
                    message.append("Some names were incompatible with TAPAAL, and have\n");
                    message.append("been conformed to the pattern [a-zA-Z][_a-zA-Z0-9]*");
                    JOptionPane.showMessageDialog(
                        TAPAALGUI.getApp(),
                        message.toString(),
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            });
        }
        
        return conformedName;
    }
}
