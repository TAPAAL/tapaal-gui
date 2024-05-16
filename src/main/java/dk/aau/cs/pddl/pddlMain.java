package dk.aau.cs.pddl;

import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.LoadedQueries;
import dk.aau.cs.io.ModelLoader;
import dk.aau.cs.io.queries.XMLQueryLoader;
import net.tapaal.gui.petrinet.TAPNLens;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import org.apache.commons.cli.CommandLine;
import pipe.gui.swingcomponents.filebrowser.FileBrowser;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class pddlMain {

    public static void main(String modelFilePath, String queriesFilePath, String outPath) throws Exception {
        main(
            new File(modelFilePath),
            new File(queriesFilePath),
            outPath
        );
    }

    public static void main(File modelFile, File queriesFile, String outPath) throws Exception {

        // Load Petri net
        ModelLoader loader = new ModelLoader();
        LoadedModel loadedModel = loader.load(modelFile);

        XMLQueryLoader queryLoader = new XMLQueryLoader(queriesFile, loadedModel.network());
        ArrayList<TAPNQuery> loadedQueries = queryLoader.getQueries(loadedModel.getLens(), 0);

        // Translate to PDDL
        var planningTask = new Model(loadedModel, loadedQueries);

        var stringifier = new PddlStringifier(planningTask);

        // Stringify and Output

        String pddlDomainString = stringifier.buildDomain().toString();
        HashMap<String, StringBuilder> pddlTaskStrings = stringifier.buildTasks();

        Files.createDirectories(Paths.get(outPath));

        var pddlDomainFile = new File(outPath + "/model.pddl");
        pddlDomainFile.createNewFile();
        var pddlDomainWriter = new FileWriter(pddlDomainFile);

        pddlDomainWriter.write(pddlDomainString);
        pddlDomainWriter.close();

        for(var e: pddlTaskStrings.entrySet()) {
            String taskId = e.getKey();
            String taskStr = e.getValue().toString();

            var pddlTaskFile = new File(outPath + "/" + taskId + ".pddl");
            pddlTaskFile.createNewFile();
            var pddlTaskWriter = new FileWriter(pddlTaskFile);
            pddlTaskWriter.write(taskStr);
            pddlTaskWriter.close();
        }

    }

}
