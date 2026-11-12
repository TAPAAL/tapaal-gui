package net.tapaal.verification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificationEngineDownloaderTest {
    @TempDir
    Path tempDirectory;

    @Test
    void extractsOnlyTheSupportedEngineExecutables() throws IOException {
        Path archive = tempDirectory.resolve("engines.zip");
        Path destination = tempDirectory.resolve("cache");
        byte[] verifytapn = {1, 2, 3};
        byte[] verifydtapn = {4, 5, 6};
        byte[] verifypn = {7, 8, 9};

        try (OutputStream output = Files.newOutputStream(archive);
             ZipOutputStream zip = new ZipOutputStream(output)) {
            add(zip, "tapaal/bin/verifytapn64", verifytapn);
            add(zip, "tapaal/bin/verifydtapn.exe", verifydtapn);
            add(zip, "tapaal/bin/verifypn", verifypn);
            add(zip, "tapaal/bin/not-an-engine", new byte[]{0});
        }

        VerificationEngineDownloader.extractEngines(archive, destination);

        assertArrayEquals(verifytapn, Files.readAllBytes(destination.resolve("verifytapn")));
        assertArrayEquals(verifydtapn, Files.readAllBytes(destination.resolve("verifydtapn")));
        assertArrayEquals(verifypn, Files.readAllBytes(destination.resolve("verifypn")));
        assertFalse(Files.exists(destination.resolve("not-an-engine")));
    }

    @Test
    void ignoresFilesThatOnlyContainAnEngineName() throws IOException {
        Path archive = tempDirectory.resolve("engines.zip");
        Path destination = tempDirectory.resolve("cache");

        try (OutputStream output = Files.newOutputStream(archive);
             ZipOutputStream zip = new ZipOutputStream(output)) {
            add(zip, "docs/verifytapn-manual", new byte[]{1});
            add(zip, "docs/myverifydtapn", new byte[]{2});
        }

        VerificationEngineDownloader.extractEngines(archive, destination);

        assertTrue(Files.exists(destination));
        assertFalse(Files.exists(destination.resolve("verifytapn")));
        assertFalse(Files.exists(destination.resolve("verifydtapn")));
        assertFalse(Files.exists(destination.resolve("verifypn")));
    }

    private static void add(ZipOutputStream zip, String name, byte[] contents) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(contents);
        zip.closeEntry();
    }
}
