# TAPAAL GUI

This repo container the source files for the TAPAAL GUI.

## Run source

TAPAAL is built and tested with Java 25, the current Java LTS release. The
Gradle build declares a Java 25 toolchain and can provision it automatically
through Foojay when Gradle is already running with a JVM.

For a developer machine without a globally installed Java, use the local
bootstrap script. It downloads the Temurin JDK into the ignored `.tools`
directory and uses it only for this checkout:

``` bash 
./tools/run-with-java.sh run
```

The script accepts any Gradle task, for example:

``` bash
./tools/run-with-java.sh test
./tools/run-with-java.sh build
```

On Windows, use `tools/run-with-java.ps1 run`. If Java 25 is already installed,
the normal `./gradlew` or `gradlew.bat` commands continue to work.

## IDE
Please feel free to use the IDE or editor of your choice to develop TAPAAL. Please be careful not to include any project or configuration files to source control, feel free to add files to .gitignore.
The TAPAAL development team encourages to use JetBrains IntelliJ IDEA. The IDE can be freely downloaded on https://www.jetbrains.com/idea/download/

To setup IntelliJ for TAPAAL development:

  * Check out the source locally
  * Start IntelliJ and select "Import Project"
  * Navigate to the source location, and select OK
  * Select "Import project from external model" and select "Gradle" (will be the default"), press next
  * Select "Use auto-import" and "Use default gradle wrapper (recommended)
  * Select Finish

To run TAPAAL from IntelliJ select the Gradle tab (normally in the right pane), and click on application - run.
From now on you can run and debug using the play/debug button in the tool menu.

## Gradle
To run Gradle from the command line, use the Gradle bootstrap scripts
(`gradlew` for Unix, `gradlew.bat` for Windows), or the local Java bootstrap
scripts above when no global Java installation is available. Gradle 9.1 is
already compatible with Java 25, so no Gradle wrapper upgrade is required.

List of commands (see gradlew tasks --all for all)
  * run - build and run the source
  * build - build the source
  * test - run unit tests
  * jar - make a jar file
  * assemble - make a release build
  * distZip - make a distribution zip file
