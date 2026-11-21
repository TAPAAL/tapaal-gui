/**
 * GUI scenarios that drive the real TAPAAL window on a display.
 *
 * <p>To add one:
 * <ol>
 *   <li>Put the input model in {@code src/test/resources/validation/<case>/}.</li>
 *   <li>Add a class named {@code <Area><Behaviour>Test} to this package with a
 *       {@code @RegisterExtension final GuiHarness gui = new GuiHarness();} field and one {@code @Test}.
 *       Copy the demonstration closest to your scenario.</li>
 *   <li>Drive the editor with the harness's mouse, keyboard, menu and dialog methods. Assert on editor state after
 *       each step, and compare a saved file or snapshot with a reviewed expected document.</li>
 *   <li>Run it with {@code xvfb-run -a -s '-screen 0 1280x900x24' ./gradlew guiSmokeTest --tests '*Something*'}.</li>
 * </ol>
 *
 * <p>{@code tools/validation/gui-harness.md} lists the verified harness methods and the production route each one
 * uses. It also covers isolation and speed and the scenarios the harness does not support.
 *
 * <p>Nothing needs registering: {@code guiSmokeTest} runs every {@code *Test} class in this package,
 * each in a fresh JVM, and the ordinary {@code test} task skips this package. Give helpers here a
 * name that does not end in {@code Test}, so the task does not start a JVM for them.
 *
 * <p>When a scenario needs a production hook, add the smallest one that exposes the behavior to the
 * test, such as a {@code CaughtExceptions.report} call in a catch block, and add it with the scenario.
 */
package net.tapaal.gui.smoke;
