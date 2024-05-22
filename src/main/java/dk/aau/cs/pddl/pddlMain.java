package dk.aau.cs.pddl;

import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.PNMLoader;
import dk.aau.cs.io.queries.XMLQueryLoader;
import net.tapaal.gui.petrinet.verification.TAPNQuery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
        Model planningTask = parseModel(modelFile, queriesFile);

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


    public static void test(String modelFilePath, String queriesFilePath) throws FileNotFoundException {
        test(
            new File(modelFilePath),
            new File(queriesFilePath)
        );
    }

    public static void test(File modelFile, File queriesFile) throws FileNotFoundException {
        Model planningTask = parseModel(modelFile, queriesFile);

        System.out.println("Can parse pnml: " + true);
        System.out.println("Valid queries:");
        for(var entry: planningTask.getQueries().entrySet()) {
            String queryName = entry.getKey();
            int queryIndex = entry.getValue().getXmlIndex();
            System.out.println(queryIndex + "," + queryName);
        }
    }

    private static Model parseModel(File modelFile, File queriesFile) throws FileNotFoundException {
        // Load Petri net
        PNMLoader loader = new PNMLoader();
        LoadedModel loadedModel = loader.load(new FileInputStream(modelFile));

        XMLQueryLoader queryLoader = new XMLQueryLoader(queriesFile, loadedModel.network());
        ArrayList<TAPNQuery> loadedQueries = queryLoader.getQueries(loadedModel.getLens(), 0);

        // Translate to PDDL
        Model planningTask = new Model(loadedModel, loadedQueries);

        return planningTask;
    }

}
