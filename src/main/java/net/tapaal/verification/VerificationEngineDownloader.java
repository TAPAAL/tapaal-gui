package net.tapaal.verification;

import com.sun.jna.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Downloads and caches the open-source verification engines distributed with
 * TAPAAL. UPPAAL's verifyta is deliberately not included because it is a
 * separately licensed component and must still be configured by the user.
 */
public final class VerificationEngineDownloader {
    public enum Engine {
        VERIFYTAPN("verifytapn"),
        VERIFYDTAPN("verifydtapn"),
        VERIFYPN("verifypn");

        private final String executableName;

        Engine(String executableName) {
            this.executableName = executableName;
        }
    }

    private static final String DEFAULT_BUNDLE_VERSION = "4.0.4";
    private static final String DEFAULT_DOWNLOAD_BASE = "https://download.tapaal.net/tapaal/tapaal-4.0/";
    private static final String BUNDLE_VERSION_PROPERTY = "tapaal.engine.bundle.version";
    private static final String DOWNLOAD_BASE_PROPERTY = "tapaal.engine.download.base";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final Object DOWNLOAD_LOCK = new Object();

    private static volatile IOException lastFailure;
    private static volatile boolean downloadAttempted;

    private VerificationEngineDownloader() {
    }

    /**
     * Returns a cached executable, downloading the platform bundle if needed.
     * The returned path is stable between application launches.
     */
    public static Path ensureEngine(Engine engine) throws IOException {
        Path cacheDirectory = cacheDirectory();
        Path executable = cacheDirectory.resolve(executableFileName(engine));

        synchronized (DOWNLOAD_LOCK) {
            if (Files.isRegularFile(executable) && Files.size(executable) > 0) {
                return executable;
            }

            if (downloadAttempted && lastFailure != null) {
                throw lastFailure;
            }

            try {
                downloadAttempted = true;
                Files.createDirectories(cacheDirectory);
                Path bundle = downloadBundle(cacheDirectory);
                if (bundle.getFileName().toString().endsWith(".zip")) {
                    extractEngines(bundle, cacheDirectory);
                } else {
                    extractEnginesFromDmg(bundle, cacheDirectory);
                }

                if (!Files.isRegularFile(executable) || Files.size(executable) == 0) {
                    throw new IOException("The downloaded TAPAAL bundle did not contain " + engine.executableName + ".");
                }
                return executable;
            } catch (IOException e) {
                lastFailure = e;
                throw e;
            } catch (RuntimeException e) {
                IOException failure = new IOException("Could not prepare the downloaded TAPAAL verification engines.", e);
                lastFailure = failure;
                throw failure;
            }
        }
    }

    /**
     * The last download error, useful for logging without showing three
     * separate dialogs when startup checks all three engines.
     */
    public static Optional<IOException> lastFailure() {
        return Optional.ofNullable(lastFailure);
    }

    private static Path downloadBundle(Path cacheDirectory) throws IOException {
        String version = System.getProperty(BUNDLE_VERSION_PROPERTY, DEFAULT_BUNDLE_VERSION);
        PlatformInfo platform = PlatformInfo.current();
        String extension = platform.mac ? ".dmg" : ".zip";
        Path bundle = cacheDirectory.resolve("tapaal-" + version + "-" + platform.archiveSuffix + extension);

        if (Files.isRegularFile(bundle) && Files.size(bundle) > 0) {
            return bundle;
        }

        String base = System.getProperty(DOWNLOAD_BASE_PROPERTY, DEFAULT_DOWNLOAD_BASE);
        if (!base.endsWith("/")) {
            base += "/";
        }
        URI uri = URI.create(base + "tapaal-" + version + "-" + platform.archiveSuffix + extension);
        Path partial = bundle.resolveSibling(bundle.getFileName() + ".part");

        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
            HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(REQUEST_TIMEOUT)
                .header("User-Agent", "TAPAAL verification engine downloader")
                .GET()
                .build();
            HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(partial));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                Files.deleteIfExists(partial);
                throw new IOException("Could not download the TAPAAL verification engines (HTTP " + response.statusCode() + ").");
            }

            moveReplacing(partial, bundle);
            return bundle;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Files.deleteIfExists(partial);
            throw new IOException("Downloading the TAPAAL verification engines was interrupted.", e);
        } catch (IOException e) {
            Files.deleteIfExists(partial);
            throw e;
        }
    }

    /**
     * Extracts only known engine entries from a TAPAAL zip archive. This also
     * avoids extracting arbitrary archive paths into the user's cache.
     */
    static void extractEngines(Path archive, Path destination) throws IOException {
        Files.createDirectories(destination);
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            zip.stream()
                .filter(entry -> !entry.isDirectory())
                .forEach(entry -> {
                    Engine engine = engineForFileName(fileName(entry.getName()));
                    if (engine != null) {
                        try {
                            extractEntry(zip, entry, destination.resolve(executableFileName(engine)));
                        } catch (IOException e) {
                            throw new ExtractionException(e);
                        }
                    }
                });
        } catch (ExtractionException e) {
            throw e.cause;
        }
    }

    private static void extractEntry(ZipFile zip, ZipEntry entry, Path destination) throws IOException {
        Path partial = destination.resolveSibling(destination.getFileName() + ".part");
        try (InputStream input = zip.getInputStream(entry); OutputStream output = Files.newOutputStream(partial)) {
            input.transferTo(output);
        }
        moveReplacing(partial, destination);
        makeExecutable(destination);
    }

    private static void moveReplacing(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void extractEnginesFromDmg(Path dmg, Path destination) throws IOException {
        Path mountPoint = Files.createTempDirectory("tapaal-engine-bundle-");
        Process mount = null;
        try {
            mount = new ProcessBuilder("hdiutil", "attach", "-nobrowse", "-readonly", "-mountpoint", mountPoint.toString(), dmg.toString())
                .redirectErrorStream(true)
                .start();
            if (!mount.waitFor(60, TimeUnit.SECONDS) || mount.exitValue() != 0) {
                throw new IOException("Could not mount the downloaded TAPAAL engine bundle.");
            }
            extractEnginesFromDirectory(mountPoint, destination);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Mounting the TAPAAL engine bundle was interrupted.", e);
        } finally {
            if (mount != null && !mount.isAlive() && mount.exitValue() == 0) {
                Process detach = new ProcessBuilder("hdiutil", "detach", mountPoint.toString())
                    .redirectErrorStream(true)
                    .start();
                try {
                    detach.waitFor(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else if (mount != null && mount.isAlive()) {
                mount.destroyForcibly();
            }
            deleteRecursively(mountPoint);
        }
    }

    private static void extractEnginesFromDirectory(Path directory, Path destination) throws IOException {
        try (var paths = Files.walk(directory)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                Engine engine = engineForFileName(path.getFileName().toString());
                if (engine != null) {
                    try {
                        Path target = destination.resolve(executableFileName(engine));
                        Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                        makeExecutable(target);
                    } catch (IOException e) {
                        throw new ExtractionException(e);
                    }
                }
            });
        } catch (ExtractionException e) {
            throw e.cause;
        }
    }

    private static void makeExecutable(Path executable) throws IOException {
        if (!Platform.isWindows() && !executable.toFile().setExecutable(true, true)) {
            throw new IOException("Could not make the downloaded engine executable: " + executable);
        }
    }

    private static Path cacheDirectory() throws IOException {
        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isBlank()) {
            throw new IOException("The user's home directory is not available.");
        }
        PlatformInfo platform = PlatformInfo.current();
        return Path.of(userHome, ".tapaal", "engines", System.getProperty(BUNDLE_VERSION_PROPERTY, DEFAULT_BUNDLE_VERSION), platform.id);
    }

    private static String executableFileName(Engine engine) {
        return engine.executableName + (Platform.isWindows() ? ".exe" : "");
    }

    private static String fileName(String path) {
        int separator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return separator >= 0 ? path.substring(separator + 1) : path;
    }

    private static Engine engineForFileName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        if (normalized.endsWith(".exe")) {
            normalized = normalized.substring(0, normalized.length() - 4);
        }
        if (normalized.matches("verifytapn(?:64)?")) {
            return Engine.VERIFYTAPN;
        }
        if (normalized.matches("verifydtapn(?:64)?")) {
            return Engine.VERIFYDTAPN;
        }
        if (normalized.matches("verifypn(?:64)?")) {
            return Engine.VERIFYPN;
        }
        return null;
    }

    private static void deleteRecursively(Path directory) {
        try (var paths = Files.walk(directory)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // Temporary mount-point cleanup must not hide the original error.
                }
            });
        } catch (IOException ignored) {
            // Temporary mount-point cleanup must not hide the original error.
        }
    }

    private static final class ExtractionException extends RuntimeException {
        private final IOException cause;

        private ExtractionException(IOException cause) {
            this.cause = cause;
        }
    }

    private static final class PlatformInfo {
        private final String id;
        private final String archiveSuffix;
        private final boolean mac;

        private PlatformInfo(String id, String archiveSuffix, boolean mac) {
            this.id = id;
            this.archiveSuffix = archiveSuffix;
            this.mac = mac;
        }

        private static PlatformInfo current() throws IOException {
            if (Platform.isWindows() && Platform.is64Bit()) {
                return new PlatformInfo("windows-x64", "win64", false);
            }
            if (Platform.isLinux() && Platform.is64Bit()) {
                return new PlatformInfo("linux-x64", "linux64", false);
            }
            if (Platform.isMac()) {
                String architecture = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
                if (architecture.contains("aarch64") || architecture.contains("arm64")) {
                    return new PlatformInfo("mac-arm64", "mac-arm64", true);
                }
                if (Platform.is64Bit()) {
                    return new PlatformInfo("mac-x64", "mac-intel64", true);
                }
            }
            throw new IOException("Automatic verification engine downloads are not supported on this platform.");
        }
    }
}
