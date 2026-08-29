package dk.aau.cs.util;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import pipe.gui.TAPAALGUI;

public class NameTransformer {
    private boolean hasConformed = false;

    /**
     * Transforms a string to the following regex pattern:
     * {@code [a-zA-Z][_a-zA-Z0-9]*}
     */
    public String transform(String name) {
        String transformedName = name;

        transformedName = transformedName.replaceAll("\\s+|-", "_");
        transformedName = transformedName.replace("*", "star");
        transformedName = transformedName.replace("/", "slash");
        transformedName = transformedName.replace("+", "plus");
        transformedName = transformedName.replace("-", "minus");
    
        Require.that(transformedName.matches("[a-zA-Z][_a-zA-Z0-9]*"), "Name: " + name + " does not obey the regex pattern [a-zA-Z][_a-zA-Z0-9]*, and could not be transformed automatically");

        // Displays warning only once for a instance
        if (!hasConformed && !transformedName.equals(name)) {
            hasConformed = true;
            SwingUtilities.invokeLater(() -> {
                StringBuilder message = new StringBuilder();
                    message.append("Some names were incompatible with TAPAAL, and have\n");
                    message.append("been transformed to the pattern [a-zA-Z][_a-zA-Z0-9]*");
                    JOptionPane.showMessageDialog(
                        TAPAALGUI.getApp(),
                        message.toString(),
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            });
        }
        
        return transformedName;
    }
}
