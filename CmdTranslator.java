import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.TAPN.AdvancedBroadcastTransformer;
import dk.aau.cs.TAPN.Degree2BroadcastTransformer;
import dk.aau.cs.TAPN.TAPNToNTABroadcastTransformer;
import dk.aau.cs.TAPN.TAPNToNTATransformer;
import dk.aau.cs.petrinet.PipeTapnToAauTapnTransformer;
import dk.aau.cs.petrinet.TAPN;


public class CmdTranslator {

	private static final String INHIB_PRIO_STANDARD = "-r0";
	private static final String INHIB_PRIO_SYM = "-r1";
	private static final String BROADCAST_STANDARD = "-r2";
	private static final String BROADCAST_SYM = "-r3";
	private static final String DEG2_BROADCAST_SYM = "-r4";
	private static final String ADV_BROADCAST_SYM = "-r5";
	private static final String OPT_BROADCAST_SYM = "-r6";
	private static final String SUPER_BROADCAST_SYM = "-r7";
	
	private static final String INHIB_PRIO_STANDARD_NAME = "Standard Reduction (inhib. using priorities)";
	private static final String INHIB_PRIO_SYM_NAME = "Symmetry Reduction (inhib. using priorities)";
	private static final String BROADCAST_STANDARD_NAME = "Standard Broadcast Reduction";
	private static final String BROADCAST_SYM_NAME = "Symmetric Broadcast Reduction";
	private static final String DEG2_BROADCAST_SYM_NAME = "Symmetric Degree2 Broadcast Reduction";
	private static final String ADV_BROADCAST_SYM_NAME = "Symmetric Advanced Broadcast Reduction";
	private static final String OPT_BROADCAST_SYM_NAME = "Optimized Symmetric Broadcast Reduction";
	private static final String SUPER_BROADCAST_SYM_NAME = "Symmetric Super Broadcast Reduction";
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		if(args == null || args.length != 3){
			printHelp();
			return;
		}

		File input = new File(args[1]);
		if(!input.exists()){
			System.out.append("Input file not found!");
			return;
		}

		String filePrefix = args[2].lastIndexOf(".") == -1 ? args[2] : args[2].substring(0, args[2].lastIndexOf("."));

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

		if(args[0].equalsIgnoreCase(INHIB_PRIO_STANDARD)){
			TAPNToNTATransformer trans = 
				new dk.aau.cs.TAPN.TAPNToNTAStandardTransformer(capacity);
			
			try{
				dk.aau.cs.TA.NTA nta = trans.transformModel(tapn);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = trans.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(tapnQuery.query, capacity + tapn.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else if(args[0].equalsIgnoreCase(INHIB_PRIO_SYM)){
			TAPNToNTATransformer trans = 
				new dk.aau.cs.TAPN.TAPNToNTASymmetryTransformer(capacity);
			
			try{
				dk.aau.cs.TA.NTA nta = trans.transformModel(tapn);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = trans.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(tapnQuery.query, capacity + tapn.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(args[0].equalsIgnoreCase(BROADCAST_STANDARD) || args[0].equalsIgnoreCase(BROADCAST_SYM)){
			TAPNToNTABroadcastTransformer broadcastTransformer = 
				new dk.aau.cs.TAPN.TAPNToNTABroadcastTransformer(capacity, args[0].equalsIgnoreCase(BROADCAST_SYM));
			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(tapn);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = broadcastTransformer.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(tapnQuery.query, capacity + tapn.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(args[0].equalsIgnoreCase(DEG2_BROADCAST_SYM)){
			Degree2BroadcastTransformer broadcastTransformer = 
				new dk.aau.cs.TAPN.Degree2BroadcastTransformer(capacity);
			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(tapn);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = broadcastTransformer.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(tapnQuery.query, capacity + tapn.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(args[0].equalsIgnoreCase(ADV_BROADCAST_SYM)){
			AdvancedBroadcastTransformer broadcastTransformer = 
				new dk.aau.cs.TAPN.AdvancedBroadcastTransformer(capacity, true);
			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(tapn);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = broadcastTransformer.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(tapnQuery.query, capacity + tapn.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(args[0].equalsIgnoreCase(OPT_BROADCAST_SYM)){
			TAPNToNTABroadcastTransformer broadcastTransformer = 
				new dk.aau.cs.TAPN.OptimizedBroadcastTransformer(capacity, true);
			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(tapn);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = broadcastTransformer.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(tapnQuery.query, capacity + tapn.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(args[0].equalsIgnoreCase(SUPER_BROADCAST_SYM)){
			TAPNToNTABroadcastTransformer broadcastTransformer = 
				new dk.aau.cs.TAPN.SuperBroadcastTransformer(capacity, true);
			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(tapn);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = broadcastTransformer.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(tapnQuery.query, capacity + tapn.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			throw new Exception("Wrong reduction method");
		}
	}

	private static void printHelp() {
		System.out.println("CmdTranslator -rx inputfile outputfile");
		System.out.println();
		System.out.println("Argument:");

		System.out.print(INHIB_PRIO_STANDARD);
		System.out.print(" - ");
		System.out.println(INHIB_PRIO_STANDARD_NAME);

		System.out.print(INHIB_PRIO_SYM);
		System.out.print(" - ");
		System.out.println(INHIB_PRIO_SYM_NAME);

		System.out.print(BROADCAST_STANDARD);
		System.out.print(" - ");
		System.out.println(BROADCAST_STANDARD_NAME);

		System.out.print(BROADCAST_SYM);
		System.out.print(" - ");
		System.out.println(BROADCAST_SYM_NAME);

		System.out.print(DEG2_BROADCAST_SYM);
		System.out.print(" - ");
		System.out.println(DEG2_BROADCAST_SYM_NAME);
		
		System.out.print(ADV_BROADCAST_SYM);
		System.out.print(" - ");
		System.out.println(ADV_BROADCAST_SYM_NAME);
		
		System.out.print(OPT_BROADCAST_SYM);
		System.out.print(" - ");
		System.out.println(OPT_BROADCAST_SYM_NAME);
		
		System.out.print(SUPER_BROADCAST_SYM);
		System.out.print(" - ");
		System.out.println(SUPER_BROADCAST_SYM_NAME);
	}

}
