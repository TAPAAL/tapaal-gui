package dk.aau.cs.util;

import dk.aau.cs.debug.Logger;

public class JavaUtil {
    public static int getJREMajorVersion(){
        String version = System.getProperty("java.version");
        String[] versionSplit = version.split("\\.");

        try {
            if (Integer.parseInt(versionSplit[0]) >= 9) {
                //Version format (9.X.Y)
                return Integer.parseInt(versionSplit[0]);
            } else {
                //Before java 9 version in format (1.X.Y)
                return Integer.parseInt(versionSplit[1]);
            }
        } catch (NumberFormatException e) {
            Logger.log("Error parsing java version, failing silent (0): " + e.getMessage());
            return 0; // Unknown version
        }
    }
}
