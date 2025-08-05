package dk.aau.cs.verification.VerifyTAPN;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import net.tapaal.gui.petrinet.verification.Verifier;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class VerifyPNInteractiveHandle {
    private Process verifypnProcess;
    private BufferedWriter writer;
    private BufferedReader reader;
    private BufferedReader errorReader;

    public boolean startInteractiveMode(String modelPath) {
        try {
            VerifyPN verifyPn = Verifier.getVerifyPN();
            List<String> initCommand = List.of(verifyPn.getPath(), modelPath, "-C", "--interactive-mode");
    
            ProcessBuilder pb = new ProcessBuilder(initCommand);
            verifypnProcess = pb.start();
    
            writer = new BufferedWriter(new OutputStreamWriter(verifypnProcess.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(verifypnProcess.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(verifypnProcess.getErrorStream()));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String sendMessage(String message) throws IOException {
        writer.write(message);
        final int numNewlines = 3;
        for (int i = 0; i < numNewlines; ++i) {
            writer.newLine();
        }

        writer.flush();

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
            if (line.trim().isEmpty()) {
                break;
            }
        }

        return response.toString();
    }

    public void stopInteractiveMode() {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (errorReader != null) errorReader.close();
            if (verifypnProcess != null) verifypnProcess.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}