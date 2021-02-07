package net.tapaal;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.components.BatchProcessingResultsTableModel;
import dk.aau.cs.io.batchProcessing.BatchProcessingResultsExporter;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.MemoryMonitor;
import dk.aau.cs.util.VerificationCallback;
import dk.aau.cs.verification.*;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationResult;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import pipe.dataLayer.TAPNQuery;
import pipe.gui.CreateGui;
import dk.aau.cs.debug.Logger;
import pipe.gui.Verifier;

/**
 * Main class for lunching TAPAAL
 *
 * @author Kenneth Yrke Joergensen (kenneth@yrke.dk)
 */
public class TAPAAL {

	public static final String TOOLNAME = "TAPAAL";
	public static final String VERSION = "DEV";

	public static String getProgramName(){
		return "" + TAPAAL.TOOLNAME + " " + TAPAAL.VERSION;
	}

	
	public static void main(String[] args) throws Exception {
		// Create a CommandLineParser using Posix Style
		CommandLineParser parser = new PosixParser();

		// Create possible commandline options
		Options options = new Options();
		options.addOption("d", "debug", false, "enable debug output .");

		CommandLine commandline = null;

		// Parse command line arguments
		try {
			commandline = parser.parse(options, args);
		} catch (ParseException exp) {
			System.err.println("There where an error parsing the specified arguments");
			System.err.println("Unexpected exception:" + exp.getMessage());
		}

		// Enable debug
		if (commandline.hasOption("debug")) {
			Logger.enableLogging(true);
		}

		if (TAPAAL.VERSION.equals("DEV")){
			Logger.enableLogging(true);
			Logger.log("Debug logging is enabled by default in DEV branch");
		}

		if (commandline.hasOption("batch")) {

			String[] files = commandline.getArgs();
			//String [] files = new String[] {"C:\\kyrke\\tapaal\\3.6\\src\\resources\\Example nets"};

			//String [] files = new String[] {"C:\\kyrke\\tapaal\\testmodels-tapaal"};
			//String [] files = new String[] {"C:\\tmp\\subset2"};
			File batchFolder = new File(files[0]);

			batchProcessing(batchFolder);


			return;
		}

		// Create the TAPAAL GUI
		CreateGui.init();

		// Open files
		String[] files = commandline.getArgs();
		Logger.log("Opening #files: " + files.length);
		for (String f : files) {
			File file = new File(f);

			if (file.exists()) { // Open the file
				if (file.canRead()) {
					try {
                        CreateGui.getAppGuiController().openTab(TabContent.createNewTabFromFile(file));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (file.exists()) {
					System.err.println("Can not read file " + file.toString());
				}
			} else {
				// XXX: Can we create the file? what would the default file type
				// be?
				// XXX: Can we check if we can write to the directory?
				System.err.println("Can not find file " + file.toString());

			}
		}

	}

	private static void batchProcessing(File batchFolder) throws Exception {
		//Sadly needs to create the gui
		CreateGui.init();
		CreateGui.getApp().setVisible(false);

		System.out.println("=============================================================");
		System.out.println("Batch Porcessing");
		System.out.println("=============================================================");

		System.out.println("Running in batch mode for " + batchFolder.getAbsolutePath());

		BatchProcessingResultsTableModel results = new BatchProcessingResultsTableModel();

		for (File f : batchFolder.listFiles()) {
			if (f.getName().toLowerCase().endsWith(".tapn") || f.getName().toLowerCase().endsWith(".xml")) {
				System.out.println("Processing File: " + f);

				TabContent tab = TabContent.createNewTabFromInputStream(new FileInputStream(f), f.getName());
				TimedArcPetriNetNetwork network = tab.network();
				List<TAPNQuery> queries = StreamSupport
						.stream(tab.queries().spliterator(), false)
						.collect(Collectors.toList());

				for (TAPNQuery query : queries) {

					System.out.println("    | Running query: " + query.getName());

					if(query.getReductionOption() == ReductionOption.VerifyTAPN || query.getReductionOption() == ReductionOption.VerifyTAPNdiscreteVerification || query.getReductionOption() == ReductionOption.VerifyPN) {
						Verifier.runVerifyTAPNVerification(network, query, false, new VerificationCallback() {
							@Override
							public void run(VerificationResult<TAPNNetworkTrace> result) {

								String resultString = result.getQueryResult().isQuerySatisfied() ? "Satisfied" : "Not Satisfied";
								System.out.println("    | Result: " + resultString);

								results.addResult(new BatchProcessingVerificationResult(
										f.toString(), query,resultString ,result.verificationTime(), MemoryMonitor.getPeakMemory(),result.stats()
								));
							}

						});
					} else {
						System.out.println("    | Skipped");
						//Verifier.runUppaalVerification(network, query);
					}

				}

			}
		}

		System.out.println("===========================================");
		System.out.println("===========================================");

		BatchProcessingResultsExporter exporter = new BatchProcessingResultsExporter();
		exporter.exportToCSV(results.getResults(), System.out);
		System.out.println("Done" + results.getRowCount());
	}

	public static File getInstallDir() {
		
		String str = ClassLoader.getSystemResource("TAPAAL.class").getPath();
		
		int placeOfJarSeperator = str.lastIndexOf('!');
		
		if (placeOfJarSeperator != -1){
			// Its a jar file, lets strip the jar ending.
			str = str.substring(0, placeOfJarSeperator);
			
			//Remove the name of the jar file
			str = str.substring(0, str.lastIndexOf("/")); //Keep the last /
			
		} else {
			// Its a class file, stip the name
			str = str.replace("TAPAAL.class", "");
		}
		
		try {
			
			//Fix as ubuntu (at least) does not set file:// from ClassLoader
			if (!str.contains("file:/")) {
				str = "file://" + str;
			}
			
			
			//Some magic to remove file:// and get the right seperators
			URL url = new URL(str);
			URI uri = url.toURI();
			
			// Workaround for the following bug: 
		    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5086147
		    // Remove extra slashes after the scheme part.
			
		    if ( uri.getAuthority() != null ){
		        try {
		            uri = new URI( uri.toString().replace("file://", "file:////" ) );
		        } catch ( URISyntaxException e ) {
		            throw new IllegalArgumentException( "The specified " +
		                "URI contains an authority, but could not be " +
		                "normalized.", e );
		        }
		    }
			
		    File f = new File(uri);
			str = f.getAbsolutePath();
			
			// Stip to base dir (exit bin dir)
			File installdir = new File(str);
			installdir = installdir.getParentFile();
			
			return installdir;
			
		} catch (MalformedURLException | URISyntaxException e) {
			e.printStackTrace();
		}
		return null;

	}
	

}
