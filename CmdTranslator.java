import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.TAPN.Degree2BroadcastTransformer;
import dk.aau.cs.TAPN.TAPNToNTABroadcastTransformer;
import dk.aau.cs.TAPN.uppaaltransform.AdvancedUppaalNoSym;
import dk.aau.cs.TAPN.uppaaltransform.AdvancedUppaalSym;
import dk.aau.cs.TAPN.uppaaltransform.NaiveUppaalSym;
import dk.aau.cs.petrinet.PipeTapnToAauTapnTransformer;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNtoUppaalTransformer;


public class CmdTranslator {

	private static final String STANDARD = "-r1";
	private static final String OPT_STANDARD = "-r2";
	private static final String BROADCAST_STANDARD = "-r3";
	private static final String DEG2_BROADCAST = "-r4";

	private static final String BROADCAST_STANDARD_NAME = "Broadcast Reduction";
	private static final String DEG2_BROADCAST_NAME = "Degree2 Broadcast Reduction";
	private static final String STANDARD_NAME = "Standard Reduction";
	private static final String OPT_STANDARD_NAME = "Optimized Standard Reduction";

	private static String reductionOption = null;
	private static boolean symmetry = false;
	private static String inputfile = null;
	private static String outputfile = null;
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		if(args == null || args.length < 3 || args.length > 4){
			printHelp();
			return;
		}
		
		parseArguments(args);

		File input = new File(inputfile);
		if(!input.exists()){
			System.out.append("Input file not found!");
			return;
		}

		String filePrefix = outputfile.lastIndexOf(".") == -1 ? outputfile : outputfile.substring(0, outputfile.lastIndexOf("."));

		File xmlfile = new File(filePrefix + ".xml");
		File qfile = new File(filePrefix + ".q");

		try {
			if(!xmlfile.exists()){
				xmlfile.createNewFile();
			}

			if(!qfile.exists()){
				qfile.createNewFile();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}


		DataLayer dataLayer = new DataLayer(input);
		int capacity = dataLayer.getQueries().get(0).capacity;

		PipeTapnToAauTapnTransformer aauTrans = new PipeTapnToAauTapnTransformer(dataLayer, capacity);
		TAPN tapn = null;

		try{
			tapn = aauTrans.getAAUTAPN();
		}catch(Exception e){
			e.printStackTrace();
			return;
		}

		TAPNQuery tapnQuery = dataLayer.getQueries().get(0);

		if(reductionOption.equalsIgnoreCase(STANDARD) && !symmetry){
			try {
				tapn.convertToConservative();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				tapn = tapn.convertToDegree2();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}



			//Create uppaal xml file
			try {
				TAPNtoUppaalTransformer t2 = new TAPNtoUppaalTransformer(tapn, new PrintStream(xmlfile), capacity);
				t2.transform();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				tapn.transformQueriesToUppaal(capacity, tapnQuery.query, new PrintStream(qfile));
			} catch (Exception e) {
				System.err.println("We had an error translating the query");
			}
		}else if(reductionOption.equalsIgnoreCase(STANDARD) && symmetry){
			NaiveUppaalSym t = new NaiveUppaalSym();
			try {
				t.autoTransform(tapn, new PrintStream(xmlfile), new PrintStream(qfile), tapnQuery.query, capacity);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if (reductionOption.equalsIgnoreCase(OPT_STANDARD) && !symmetry){
			AdvancedUppaalNoSym t = new AdvancedUppaalNoSym();
			try {
				t.autoTransform(tapn, new PrintStream(xmlfile), new PrintStream(qfile), tapnQuery.query, capacity);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		} else if (reductionOption.equalsIgnoreCase(OPT_STANDARD) && symmetry){
			AdvancedUppaalSym t = new AdvancedUppaalSym();
			try {
				t.autoTransform(tapn, new PrintStream(xmlfile), new PrintStream(qfile), tapnQuery.query, capacity);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(reductionOption.equalsIgnoreCase(BROADCAST_STANDARD)){
			TAPNToNTABroadcastTransformer broadcastTransformer = 
				new dk.aau.cs.TAPN.TAPNToNTABroadcastTransformer(capacity, symmetry);
			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(tapn);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = broadcastTransformer.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(tapnQuery.query, capacity + 1 + tapn.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(reductionOption.equalsIgnoreCase(DEG2_BROADCAST)){
			Degree2BroadcastTransformer broadcastTransformer = 
				new dk.aau.cs.TAPN.Degree2BroadcastTransformer(capacity, symmetry);
			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(tapn);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = broadcastTransformer.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(tapnQuery.query, capacity + 1 + tapn.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else{
			throw new Exception("Wrong reduction method");
		}
	}

	private static void parseArguments(String[] args) {
		reductionOption = args[0];
		if(args.length == 4 && args[1].equals("-s")){
			symmetry = true;
		}
		int offset = args.length == 4 ? 0 : -1;
		
		inputfile = args[2+offset];
		outputfile = args[3+offset];		
	}

	private static void printHelp() {
		System.out.println("CmdTranslator -rx [-s] inputfile outputfile");
		System.out.println();
		System.out.println("Options:");
		
		System.out.println(" -s  use symmetry reduction");
		System.out.println();
		System.out.println("Reductions:");
		
		System.out.print(" ");
		System.out.print(STANDARD);
		System.out.print(" - ");
		System.out.println(STANDARD_NAME);

		System.out.print(" ");
		System.out.print(OPT_STANDARD);
		System.out.print(" - ");
		System.out.println(OPT_STANDARD_NAME);
		
		System.out.print(" ");
		System.out.print(BROADCAST_STANDARD);
		System.out.print(" - ");
		System.out.println(BROADCAST_STANDARD_NAME);

		System.out.print(" ");
		System.out.print(DEG2_BROADCAST);
		System.out.print(" - ");
		System.out.println(DEG2_BROADCAST_NAME);
	}

}
