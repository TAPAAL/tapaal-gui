/**
 * GUI scenarios that drive the real TAPAAL window on a display.
 *
 * <p>To add one:
 * <ol>
 *   <li>Put the input model, and the expected saved model if the scenario saves, in
 *       {@code src/test/resources/validation/<case>/}.</li>
 *   <li>Add a class named {@code <Something>Test} to this package with a
 *       {@code @RegisterExtension final GuiHarness gui = new GuiHarness();} field and one {@code @Test}.
 *       {@code PlaceDragSaveRegressionTest} is the recipe for comparing a saved file and
 *       {@code GuiWorkflowSmokeTest} the recipe for checking editor state.</li>
 *   <li>Run it with {@code xvfb-run -a ./gradlew guiSmokeTest --tests '*Something*'}.</li>
 * </ol>
 *
 * <p>Nothing needs registering: {@code guiSmokeTest} runs every {@code *Test} class in this package,
 * each in a fresh JVM, and the ordinary {@code test} task skips this package. Give helpers here a
 * name that does not end in {@code Test}, so the task does not start a JVM for them.
 *
 * <p>When a scenario needs a production hook, add the smallest one that exposes the behavior to the
 * test, such as a {@code CaughtExceptions.report} call in a catch block, and add it with the scenario.
 */
package net.tapaal.gui.smoke;
