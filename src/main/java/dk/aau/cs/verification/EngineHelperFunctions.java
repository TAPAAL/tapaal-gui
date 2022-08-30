package dk.aau.cs.verification;

import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EngineHelperFunctions {

    /**
     * Returns the version number as a string based on the Pattern
     * @param stream The stream to read from
     * @param strPattern The pattern to match against first line of the stream
     * @return The version number as a string or null if no match
     */
    public static String readVersionNumberFrom(InputStream stream, String strPattern) {
        String result = null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));

        StringBuilder sb = new StringBuilder();
        String versioninfo = null;
        try {
            int c;
            while(bufferedReader.ready() && (c = bufferedReader.read()) != -1){ // Buffer read line will block until newline is reached
                if (c == '\n'){
                    break;
                }
                sb.append((char)c);
            }
        } catch (IOException e) {
            return null;
        }
        versioninfo = sb.toString();

        Pattern pattern = Pattern.compile(strPattern);
        Matcher m = pattern.matcher(versioninfo);

        if (m.find()) {
            result = m.group(1);
            return result;
        } else {
            return null;
        }
    }

    public static String getVersion(String[] command, String strPattern) {
        String result = null;

        if (!(command[0] == null || command[0].equals("") || !(new File(command[0]).exists()))) {

            InputStream stream = null;
            Process child = null;
            try {
                child = Runtime.getRuntime().exec(command);
                stream = child.getInputStream();

                child.waitFor(2, TimeUnit.SECONDS);


                if (stream != null) {
                    result = EngineHelperFunctions.readVersionNumberFrom(stream, strPattern);
                }

            } catch (IOException | InterruptedException e) {
                return null;
            } finally {
                if (stream != null) {
                    child.destroyForcibly();
                }
            }
        }

        return result;
    }

    public static boolean versionIsEqualOrGreater(String versionStr, String versionToCompareStr) {
        String[] version = versionStr.split("-")[0].split("\\.");
        String[] targetVersion = versionToCompareStr.split("\\."); // Pattern is X.Y.Z-something

        for (int i = 0; i < targetVersion.length; i++) {
            if (version.length < i + 1) version[i] = "0";
            int diff = Integer.parseInt(version[i]) - Integer.parseInt(targetVersion[i]);
            if (diff > 0) {
                return true;
            } else if (diff < 0) {
                return false;
            }
        }

        return true;
    }
}
