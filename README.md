# TAPAAL GUI

This repo container the source files for the TAPAAL GUI.

## Run source

To run TAPAAL from sources make sure you have a java runtime installed. You can start tapaal by running: 

``` bash 
./gradlew run
```

On windows use `gradlew.bat`. Gradle will automatically download and configure all development dependencies. 

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
To run gradle from commandline use the gradle bootstrap scripts. (gradlew for unix, gradlew.bat for Windows).
You might need to set JAVA_HOME to point to the location of your JDK.

List of commands (see gradlew tasks --all for all)
  * run - build and run the source
  * build - build the source
  * test - run unit tests
  * jar - make a jar file
  * assemble - make a release build
