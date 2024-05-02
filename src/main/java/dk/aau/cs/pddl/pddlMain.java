package dk.aau.cs.pddl;

import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.ModelLoader;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class pddlMain {
    public static void main(CommandLine commandline) throws Exception {
        String[] args = commandline.getArgs();
        String petriNetPath = args[0];
        String pddlDomainPath = args[1];
        String pddlTaskPath = args[2];


        File petriNetFile = new File(petriNetPath);
        ModelLoader loader = new ModelLoader();
        LoadedModel loadedModel = loader.load(petriNetFile);

        var planningTask = new Model();
        planningTask.parse(
            loadedModel.network(),
            loadedModel.templates(),
            loadedModel.queries(),
            loadedModel.network().constants(),
            loadedModel.getLens()
        );

        var stringifier = new PddlStringifier(planningTask);
        String pddlDomainString = stringifier.buildDomain().toString();
        String pddlTaskString = stringifier.buildTask().toString();


        Files.createDirectories(Paths.get(pddlDomainPath + "/.."));
        Files.createDirectories(Paths.get(pddlTaskPath + "/.."));

        var pddlDomainFile = new File(pddlDomainPath);
        pddlDomainFile.createNewFile();
        var pddlDomainWriter = new FileWriter(pddlDomainFile);

        var pddlTaskFile = new File(pddlTaskPath);
        pddlTaskFile.createNewFile();
        var pddlTaskWriter = new FileWriter(pddlTaskFile);

        pddlDomainWriter.write(pddlDomainString);
        pddlTaskWriter.write(pddlTaskString);

        pddlDomainWriter.close();
        pddlTaskWriter.close();
    }
}
