package pipe.gui;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JSpinner;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.Place;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Transition;
import pipe.dataLayer.TAPNQuery.ReductionOption;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.FileBrowser;
import dk.aau.cs.TA.AbstractMarking;
import dk.aau.cs.TA.DiscreetFiringAction;
import dk.aau.cs.TA.FiringAction;
import dk.aau.cs.TA.SymbolicUppaalTrace;
import dk.aau.cs.TA.TimeDelayFiringAction;
import dk.aau.cs.TA.UppaalTrace;

/**
 * Implementes af class for handling integrated Uppaal Verification
 * 
 * Copyright 2009
 * Author Kenneth Yrke Joergensen <kenneth@yrke.dk>
 * 
 * Licensed under the Open Software License version 3.0
 */


public class Verification {
	static String verifytapath="";

	public static boolean setupVerifyta(){
		// if not set
		if (verifytapath == null || verifytapath.equals("")){
			
			
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"TAPAAL needs to know the location of the file verifyta.\n\n"+
					"Verifyta is a part of the UPPAAL distribution and it is\n" +
					"normally located in uppaal/bin-Linux or uppaal/bin-Win32,\n" +
					"depending on the operating system used.", 
					"Locate UPPAAL Verifyta",
					JOptionPane.INFORMATION_MESSAGE);
			
			try {
				File verifytaf = new FileBrowser("Uppaal Verifyta","",verifytapath).openFile();
				verifytapath=verifytaf.getAbsolutePath();
			} catch (Exception e) {
				// There was some problem with the action
				if (verifytapath == null){
					//JOptionPane.showMessageDialog(CreateGui.getApp(), "No verifyta specified: The verification is cancelled");
					verifytapath = "";
					return false;
				}else{
					JOptionPane.showMessageDialog(CreateGui.getApp(),
							"There were errors performing the requested action:\n" + e,
							"Error", JOptionPane.INFORMATION_MESSAGE
					);	
					verifytapath = "";
					return false;
				}
			}
		}
		
		return true;
		
	}
	public static String setupVerifytaPath() {
		String verifyta = System.getenv("verifyta");

		if (verifytapath.equals("")){			
			verifytapath = verifyta == null ? "" : verifyta;
		}
		return verifyta;
	}

	public static String getVerifytaVersion(){
		
		if (verifytapath.equals("")){
			return "N/A";
		}
		
		String[] commands;

		commands = new String[]{verifytapath, "-v"};

		Process child=null;
		
		try {
			child = Runtime.getRuntime().exec(commands);
			child.waitFor();
		} catch (IOException e) {
			return "N/A";
		} catch (InterruptedException e) {
			return "N/A";
		}
		
		BufferedReader bufferedReaderStdout = new BufferedReader(new InputStreamReader(child.getInputStream()));
		
		String versioninfo = null;
		try {
			versioninfo = bufferedReaderStdout.readLine();
		} catch (IOException e) {
			return "Error";
		}
		
		Pattern pattern = Pattern.compile("\\(rev. (\\d+)\\)");
		Matcher m =  pattern.matcher(versioninfo);
		m.find();
		return m.group(1);
	}
	//Check if verifyta is present and if it is the right version
	public static boolean checkVerifyta(){
		
		if (verifytapath == ""){
			
			
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"No verifyta specified: The verification is cancelled",
					"Verification Error",
					JOptionPane.ERROR_MESSAGE);
			verifytapath = "";
			
			return false;
		}
		
		String[] commands;

		commands = new String[]{verifytapath, "-v"};

		Process child=null;
		try {
		
			child = Runtime.getRuntime().exec(commands);
			child.waitFor();
		
		int version;
		
		//Try to see if this program is recognised as verifyta
		
		BufferedReader bufferedReaderStdout = new BufferedReader(new InputStreamReader(child.getInputStream()));
		
		String versioninfo = null;
		
		versioninfo = bufferedReaderStdout.readLine();
		
		String[] stringarray = null;
		stringarray = versioninfo.split("\\(rev\\.");
		String versiontmp = stringarray[1];
		
		stringarray = versiontmp.split("\\)");
		
		versiontmp = stringarray[0];
		
		version = Integer.parseInt(versiontmp.trim());
		
				if (version < Pipe.verifytaMinRev){
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"The specified version of the file verifyta is too old.\n\n" +
					"Get the latest development version of UPPAAL from \n" +
					"www.uppaal.com.",
					"Verifyta Error",
					JOptionPane.ERROR_MESSAGE);
			verifytapath="";
			return false;
		}
				
		} catch (Exception e) {
		
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"The program can not be verified as being verifyta. \nThe verifyta path will be reset. Please try again, to manually set the verifyta path.",
					"Verifyta Error",
					JOptionPane.ERROR_MESSAGE);
			verifytapath="";
			return false;
		}
		

				
		
		return true;
	}
	
	public static void analyzeAndOptimizeKBound(DataLayer appModel, int k, JSpinner tokensControl)
	{
		KBoundOptimizer optimizer = new KBoundOptimizer(appModel, k);
		optimizer.analyze();
		
		if(optimizer.isBounded())
		{
			tokensControl.setValue(optimizer.getMinBound());
		}
	}
	
	public static void analyseKBounded(DataLayer appModel, int k){
		KBoundAnalyzer analyzer = new KBoundAnalyzer(appModel, k);
		analyzer.analyze();
	}
	
	public static void runUppaalVerification(DataLayer appModel, TAPNQuery input) {
		runUppaalVerification(appModel, input, false);
	}
	public static void runUppaalVerification(DataLayer appModel, TAPNQuery input, boolean untimedTrace) {
		//Setup

		setupVerifyta();
		if (verifytapath == ""){
			return;
		}
		if (!checkVerifyta()){
			System.err.println("Verifyta not found, or you are running an old version of Verifyta.\n" +
			"Update to the latest development version.");
			return;
		}

		String verifyta = verifytapath;
		
		File xmlfile=null, qfile=null;
		try {
			xmlfile = File.createTempFile( "verifyta", "test.xml");
			qfile = File.createTempFile("verifyta", ".q");
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		xmlfile.deleteOnExit();qfile.deleteOnExit();

				
		String inputQuery = input.query;
		
		VerifytaOptions verifytaOptions = new VerifytaOptions(input.traceOption, input.searchOption, untimedTrace);
				
		if (inputQuery == null) {return;}

		//Handle problems with liveness checking 
		if (inputQuery.contains("E[]") || inputQuery.contains("A<>") ) {
			
			//If selected wrong method for checking
			if (!(input.reductionOption == ReductionOption.ADV_NOSYM || input.reductionOption == ReductionOption.ADV_UPPAAL_SYM 
					|| input.reductionOption == ReductionOption.BROADCAST_STANDARD 
					|| input.reductionOption == ReductionOption.BROADCAST_SYM
					|| input.reductionOption == ReductionOption.BROADCAST_DEG2
					|| input.reductionOption == ReductionOption.BROADCAST_DEG2_SYM
					|| input.reductionOption == ReductionOption.ADV_BROADCAST_SYM
					|| input.reductionOption == ReductionOption.OPT_BROADCAST
					|| input.reductionOption == ReductionOption.OPT_BROADCAST_SYM
					|| input.reductionOption == ReductionOption.SUPER_BROADCAST
					|| input.reductionOption == ReductionOption.SUPER_BROADCAST_SYM)){
				//Error
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"Verification of liveness properties (EG,AF) is not possible with the selected reduction option.",
						"Verification Error",
						JOptionPane.ERROR_MESSAGE);
				// XXX - Srba
				return;
			}
			
			//Check if degree-2 or give an error
			if (!appModel.isDegree2() && !(input.reductionOption == ReductionOption.BROADCAST_STANDARD 
					|| input.reductionOption == ReductionOption.BROADCAST_SYM
					|| input.reductionOption == ReductionOption.BROADCAST_DEG2
					|| input.reductionOption == ReductionOption.BROADCAST_DEG2_SYM
					|| input.reductionOption == ReductionOption.ADV_BROADCAST_SYM
					|| input.reductionOption == ReductionOption.OPT_BROADCAST
					|| input.reductionOption == ReductionOption.OPT_BROADCAST_SYM
					|| input.reductionOption == ReductionOption.SUPER_BROADCAST
					|| input.reductionOption == ReductionOption.SUPER_BROADCAST_SYM)){
				//Error
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"The net cannot be verified for liveness properties (EG,AF) because there is\n"+
						"a transition with either more that two input places or more than two output places.\n"+
                        "You may try to modify the model so that the net does not contain any such transition\n"+
                        "and then run the verification process again.",
						"Liveness Verification Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
		}

	
		Export.exportUppaalXMLFromQuery(appModel, input, xmlfile.getAbsolutePath(), qfile.getAbsolutePath());

		
		// Do verifta 
//		PetriNetObject.ignoreSelection(false);
//		CreateGui.getApp().repaint();

		
		RunningVerificationWidgets t = (new Verification()).new RunningVerificationWidgets();
		t.createDialog();
		
		//Run the verifucation thread 	
		RunUppaalVerification a = (new Verification()).new RunUppaalVerification(verifyta, verifytaOptions.toString(), xmlfile, qfile, t); //Wtf?
		a.start();
		
		t.show();
		
		if (t.interrupted){
			a.verifyStop();
			a.interrupt();
			a.stop();
			a.destroy();
			
			try {
				a.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			a=null;
			//Stop ther verification!
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"Verification was interupted by the user. No result found!",
					"Verification Result",
					JOptionPane.INFORMATION_MESSAGE);
			return;
			
		}
		
		//Close ther verification running dialog, (bug for possible racecondition)
		t.close();
		
		
		
		
		boolean property=false; 
		boolean error=true;
		BufferedReader bufferedReaderStderr = a.bufferedReaderStderr;
		BufferedReader bufferedReaderStdout = a.bufferedReaderStdout;
		
		// Show the verification result dialog
		
		String resultmessage = "";
		
		
		//Parse result
		String line=null;

		try {
			while ( (line = bufferedReaderStdout.readLine()) != null){
				if (line.contains("Property is satisfied")) {
					property = true;

					error=false;
					//break;

				}else if (line.contains("Property is NOT satisfied.")){
					property=false;
					error=false;
				}

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			error=true;
		}
		
		if (error){
			System.err.println("There was an error verifying the model.");
			return;
		}

		
	
		//Display Answer
		resultmessage = property ? "Property Satisfied.": "Property Not Satisfied."; 
		resultmessage+= "\nVerification time is estimated to: " + (a.verificationtime/1000.0) + "s";
		
		JOptionPane.showMessageDialog(CreateGui.getApp(),
				resultmessage,
				"Verification Result",
				JOptionPane.INFORMATION_MESSAGE);


		
		// Show simulator is selected and reduction is the right method
		if ((input.traceOption != TAPNQuery.TraceOption.NONE && (input.reductionOption == TAPNQuery.ReductionOption.NAIVE || input.reductionOption == ReductionOption.ADV_NOSYM) )&&
				//and Only view the trace, if a trace is generated based on property
				((inputQuery.contains("E<>") && property) || (inputQuery.contains("A[]") && !property) ||
						(inputQuery.contains("E[]") && property) || (inputQuery.contains("A<>") && !property))){
			
			
			

			
			//Select to display concreet trace
			if ((inputQuery.contains("E<>") || inputQuery.contains("A[]")) && !untimedTrace){
				
			

		    //Set to animation mode   
			CreateGui.getApp().setAnimationMode(true);
			CreateGui.getApp().setMode(Pipe.START);
            PetriNetObject.ignoreSelection(true);
			CreateGui.getView().getSelectionObject().clearSelection();
			
			CreateGui.getAnimator().resethistory();
			
			
			try {
				ArrayList<FiringAction> tmp2 = null;
				tmp2 = UppaalTrace.parseUppaalTraceAdvanced(bufferedReaderStderr);

				// Handeling of the UPPAAL verifyta error in generating traces
				if (tmp2 == null){
					JOptionPane.showMessageDialog(CreateGui.getApp(),
							"Generation of a concrete trace in UPPAAL failed.\n\n" +
							"TAPAAL will re-run the verification process\n" +
							"in order to obtain at least an untimed trace.",				
							"Concrete Trace Generation Error",
							JOptionPane.INFORMATION_MESSAGE);
					//XXX - Srba
					
					runUppaalVerification(appModel, input, true);
					return;
					
				}
				
				CreateGui.getAnimator().resethistory();
				
				for (FiringAction f : tmp2){

					if (f instanceof TimeDelayFiringAction){

						BigDecimal time = new BigDecimal(""+((TimeDelayFiringAction)f).getDealy());
						CreateGui.getAnimator().manipulatehistory(time);

					} else if (f instanceof DiscreetFiringAction){
						DiscreetFiringAction stringdfa = (DiscreetFiringAction) f;

						Transition trans = CreateGui.currentPNMLData().getTransitionByName(stringdfa.getTransition());
						pipe.dataLayer.DiscreetFiringAction realdfa = new pipe.dataLayer.DiscreetFiringAction(trans);

						for (String s : stringdfa.getConsumedTokensList().keySet()){

							Place p = CreateGui.currentPNMLData().getPlaceByName(s);
							BigDecimal token = new BigDecimal(""+stringdfa.getConsumedTokensList().get(s).get(0)); // XXX - just getting the first, guess that we dont support more tokens from smae place any wway :( (for now)

							realdfa.addConsumedToken(p, token);
						}
						CreateGui.getAnimator().manipulatehistory(realdfa);
					}
					
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			} else {
				// Display abstract trace
				try {
					ArrayList<AbstractMarking> tmp2 = null;
					tmp2 = SymbolicUppaalTrace.parseUppaalAbstractTrace(bufferedReaderStderr);

					JOptionPane.showMessageDialog(CreateGui.getApp(),
							"The verification process returned an untimed trace.\n\n"+
							"This means that with appropriate time delays the displayed\n"+
							"sequence of discrete transitions can become a concrete trace.\n"+
							"In case of liveness properties (EG, AF) the untimed trace\n"+
							"either ends in a deadlock, or time divergent computation without\n" +
							"any discrete transitions, or it loops back to some earlier configuration.\n"+
							"The user may experiment in the simulator with different time delays\n"+
							"in order to realize the suggested untimed trace in the model.",
							"Verification Information",
							JOptionPane.INFORMATION_MESSAGE);
					
					//Set to animation mode   
					CreateGui.getApp().setAnimationMode(true);
					CreateGui.getApp().setMode(Pipe.START);
		            PetriNetObject.ignoreSelection(true);
					CreateGui.getView().getSelectionObject().clearSelection();
					
			
					CreateGui.getAnimator().resethistory();
					
					CreateGui.addAbstractAnimationPane();
					
					AnimationHistory untimedAnimationHistory = CreateGui.getAbstractAnimationPane();

					
					for (AbstractMarking am : tmp2){
						untimedAnimationHistory.addHistoryItemDontChange(am.getFiredTranstiion().trim());
					}
					
					

					
					
				} catch (Exception e){
//					 TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

			
		}
		
		
		return;


	}

	
	public class RunningVerificationWidgets extends javax.swing.JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7324730866458670919L;
		private JRootPane myRootPane;
		Thread verification=null;
		boolean finished=false;
		boolean interrupted=false;
		
		public RunningVerificationWidgets() {
			setLayout(new GridLayout(2,1));
			add(new Label("Verification is running ...\n" +
					"Please wait!")
			);
			
			JButton okButton = new JButton("Interupt Verification");
			

			okButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							interrupted=true;
							close();
						}
					}
			);
			
			add(okButton);
		    
			
		}

		public void close() {
			myRootPane.getParent().setVisible(false);
		}
		
		public void show() {
			if (!finished){
				myRootPane.getParent().setVisible(true);
			}
		}
		
		public void createDialog(){
			EscapableDialog guiDialog = 
				new EscapableDialog(CreateGui.getApp(), "Verification running : " + Pipe.getProgramName(), true);

			myRootPane = guiDialog.getRootPane();
			Container contentPane = guiDialog.getContentPane();
			
			// 1 Set layout
			contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));      

			// 2 Add query editor
			contentPane.add(this);
			
			guiDialog.setResizable(true);     

			// Make window fit contents' preferred size
			guiDialog.pack();

			// Move window to the middle of the screen
			guiDialog.setLocationRelativeTo(null);
			guiDialog.setVisible(false);
			
			return;
		}

		public void finished() {
			finished = true;
			this.close();
		}
		
		
	}
	
	public class RunUppaalVerification extends Thread{
		
		String verifyta=null;
		String verifytaOptions=null;
		File xmlfile=null, qfile=null;
		BufferedReader bufferedReaderStderr=null;
		BufferedReader bufferedReaderStdout=null;
		boolean error=true;
		RunningVerificationWidgets dialog = null;
		long verificationtime=0;
		
		
		public RunUppaalVerification(String verifyta, String verifytaOptions, File xmlfile, File qfile, RunningVerificationWidgets dialog) {
		
			this.verifyta = verifyta;
			this.verifytaOptions = verifytaOptions;
			
			this.xmlfile = xmlfile;
			this.qfile = qfile;
			this.dialog = dialog;
			
		}

		public void verifyStop() {
			
			child.destroy();
		}
		Process child=null;
		public void run() {

			try {
				// Execute a command with an argument that contains a space
				//String[] commands = new String[]{"/usr/bin/time", "-p", verifyta, verifytaOptions, xmlfile.getAbsolutePath(), qfile.getAbsolutePath()/*, " 2> ", tracefile.getAbsolutePath()*/};
				String[] commands;

				commands = new String[]{verifyta, verifytaOptions, xmlfile.getAbsolutePath(), qfile.getAbsolutePath()/*, " 2> ", tracefile.getAbsolutePath()*/};

				long startTimeMs=0, endTimeMs=0;
				
				startTimeMs = System.currentTimeMillis();
				child = Runtime.getRuntime().exec(commands);
				
				//Start drain for buffers
				
				BufferDrain stdout = new BufferDrain(new BufferedReader(new InputStreamReader(child.getInputStream())));
				BufferDrain stderr = new BufferDrain(new BufferedReader(new InputStreamReader(child.getErrorStream())));
				
				stdout.start();
				stderr.start();
				
				child.waitFor();
				endTimeMs  = System.currentTimeMillis();
				
				//Wait for the buffers to be drained3
				// XXX - kyrke - are thise subprocess killed right when 
				// mother process is killed?, or do we have to handle them better?
				stdout.join();
				stderr.join();

				bufferedReaderStdout = new BufferedReader(new StringReader(stdout.getString().toString()));
				bufferedReaderStderr = new BufferedReader(new StringReader(stderr.getString().toString()));

				/*for (String s : commands){
					System.out.print(s + " ");
				}*/ 
				
				verificationtime = endTimeMs-startTimeMs;
				
				dialog.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		
	

	}

	public class BufferDrain extends Thread{

		BufferedReader drain=null;
		StringBuffer string=null;
		boolean running;

		public BufferDrain(BufferedReader drain) {
			this.drain = drain;
			string = new StringBuffer();
		}

		public void run() {

			try {
				running = true;

				int c;
				while (running){


					c=drain.read();

					if (c!=-1){
						string.append((char)c);
					} else {
						running = false;
					}
				}

			} catch (IOException e) {

				e.printStackTrace();
				running=false;
			}


		}
		
		public StringBuffer getString(){
			return string;
		}

	}
	
}
