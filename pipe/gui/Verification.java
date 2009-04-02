package pipe.gui;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.management.Query;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.Place;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Transition;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.FileBrowser;
import pipe.gui.widgets.QueryDialogue;
import pipe.gui.widgets.QueryDialogue.QueryDialogueOption;
import dk.aau.cs.TA.DiscreetFiringAction;
import dk.aau.cs.TA.FiringAction;
import dk.aau.cs.TA.TimeDelayFiringAction;
import dk.aau.cs.TA.UppaalTrace;
import dk.aau.cs.TAPN.uppaaltransform.AdvancedUppaalSym;
import dk.aau.cs.TAPN.uppaaltransform.NaiveUppaalSym;
import dk.aau.cs.petrinet.PipeTapnToAauTapnTransformer;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNtoUppaalTransformer;

/**
 * Implementes af class for handling integrated Uppaal Verification
 * 
 * Copyright 2009
 * Author Kenneth Yrke Joergensen <kenneth@yrke.dk>
 * 
 * Licensed under the Open Software License version 3.0
 */


public class Verification {
	private static String verifytapath="";

	public static void runUppaalVerification(DataLayer appModel, TAPNQuery input, boolean saveUppaal) {
		//Setup

		String verifyta = System.getenv("verifyta");

		if (verifytapath.equals("")){
			verifytapath = verifyta;
		}

		verifyta = verifytapath; 

		File xmlfile=null, qfile=null;
		try {
			xmlfile = File.createTempFile("verifyta", ".xml");
			qfile = File.createTempFile("verifyta", ".q");
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		xmlfile.deleteOnExit();qfile.deleteOnExit();

		// if not set
		if (verifyta == null || verifyta.equals("")){
			try {
				File verifytaf = new FileBrowser("Uppaal Verifyta","",verifyta).openFile();
				verifyta=verifytaf.getAbsolutePath();
				verifytapath=verifyta;
			} catch (Exception e) {
				// There was some problem with the action
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"There were errors performing the requested action:\n" + e,
						"Error", JOptionPane.ERROR_MESSAGE
				);
			}
		}

		// Save the model
		// Get export file
		
		if (saveUppaal){
			String filename = null;
			try {
				filename = new FileBrowser("Uppaal XML","xml",filename).saveFile();
				xmlfile=new File(filename);
				String[] a = filename.split(".xml");
				qfile=new File(a[0]+".q");

			} catch (Exception e) {
				// There was some problem with the action
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"There were errors performing the requested action:\n" + e,
						"Error", JOptionPane.ERROR_MESSAGE
				);
			}
		}
		
		//Create transformer
		PipeTapnToAauTapnTransformer transformer = new PipeTapnToAauTapnTransformer(appModel, 0);
		TAPN model=null;
		try {
			model = transformer.getAAUTAPN();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (CreateGui.getApp().getComponentCount() == 0) {
			return;
		}


		int capacity;
		String currentQuery = "";
		capacity = input.capacity;
		String inputQuery = input.query;
		TraceOption traceOption = input.traceOption;
		SearchOption searchOption = input.searchOption;
		String verifytaOptions = "";

		if (traceOption == TraceOption.SOME){
			verifytaOptions = "-t0";
		}else if (traceOption == TraceOption.FASTEST){
			verifytaOptions = "-t2";
		}else if (traceOption == TraceOption.NONE){
			verifytaOptions = "";
		}

		if (searchOption == SearchOption.BFS){
			verifytaOptions += "-o0";
		}else if (searchOption == SearchOption.DFS ){
			verifytaOptions += "-o1";
		}else if (searchOption == SearchOption.RDFS ){
			verifytaOptions += "-o2";
		}else if (searchOption == SearchOption.CLOSE_TO_TARGET_FIRST){
			verifytaOptions += "-o6";
		}

		if (inputQuery == null) {return;}




		// Select the model based on selected export option.

		if (input.reductionOption == TAPNQuery.ReductionOption.NAIVE_UPPAAL_SYM){
			
			NaiveUppaalSym t = new NaiveUppaalSym();
			try {
				t.autoTransform(model, new PrintStream(xmlfile), new PrintStream(qfile), inputQuery, capacity);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (input.reductionOption == TAPNQuery.ReductionOption.ADV_UPPAAL_SYM){
			
			AdvancedUppaalSym t = new AdvancedUppaalSym();
			try {
				t.autoTransform(model, new PrintStream(xmlfile), new PrintStream(qfile), inputQuery, capacity);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {

			try {
				model.convertToConservative();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				model = model.convertToDegree2();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}



			//Create uppaal xml file
			try {
				TAPNtoUppaalTransformer t2 = new TAPNtoUppaalTransformer(model, new PrintStream(xmlfile), capacity);
				t2.transform();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				model.transformQueriesToUppaal(capacity, inputQuery, new PrintStream(qfile));
			} catch (Exception e) {
				System.err.println("We had an error translating the query");
			}

		}


		

		
		// Do verifta 
		PetriNetObject.ignoreSelection(false);
		CreateGui.getApp().repaint();

		
		RunningVerificationWidgets t = (new Verification()).new RunningVerificationWidgets();
		t.createDialog();
		
		//Run the verifucation thread 	
		RunUppaalVerification a = (new Verification()).new RunUppaalVerification(verifyta, verifytaOptions, xmlfile, qfile, t); //Wtf?
		a.start();
		
		t.show();
		
		if (t.interrupted){
			a.verifyStop();
			a.interrupt();
			a.stop();
			a.destroy();
			
			try {
				a.wait();
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
		
		
		
		
		
		boolean property=false; 
		boolean error=false;
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

					//Print trace,
					ArrayList<String> tmp=null;
					
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
		resultmessage = property ? "Property Satisfied" : "Property Not Satisfied"; 
		resultmessage+= "\n Verification Time is estimated to: " + (a.verificationtime/1000.0) + "s";
		
		JOptionPane.showMessageDialog(CreateGui.getApp(),
				resultmessage,
				"Verification Result",
				JOptionPane.INFORMATION_MESSAGE);


		if (input.traceOption != TAPNQuery.TraceOption.NONE && property && input.reductionOption == TAPNQuery.ReductionOption.NAIVE){
			
			
			//Show the trace
			CreateGui.getApp().setAnimationMode(true);
			CreateGui.getAnimator().resethistory();
			
			try {
				ArrayList<FiringAction> tmp2 = null;
				tmp2 = UppaalTrace.parseUppaalTraceAdvanced(bufferedReaderStderr);

				for (FiringAction f : tmp2){

					if (f instanceof TimeDelayFiringAction){

						float time = ((TimeDelayFiringAction)f).getDealy();
						CreateGui.getAnimator().manipulatehistory(time);

					} else if (f instanceof DiscreetFiringAction){
						DiscreetFiringAction stringdfa = (DiscreetFiringAction) f;

						Transition trans = CreateGui.currentPNMLData().getTransitionByName(stringdfa.getTransition());
						pipe.dataLayer.DiscreetFiringAction realdfa = new pipe.dataLayer.DiscreetFiringAction(trans);

						for (String s : stringdfa.getConsumedTokensList().keySet()){

							Place p = CreateGui.currentPNMLData().getPlaceByName(s);
							Float token = stringdfa.getConsumedTokensList().get(s).get(0); // XXX - just getting the first, guess that we dont support more tokens from smae place any wway :( (for now)

							realdfa.addConsumedToken(p, token);
						}
						CreateGui.getAnimator().manipulatehistory(realdfa);
					}

				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}
		
		
		return;


	}

	
	public class RunningVerificationWidgets extends javax.swing.JPanel {
		private JRootPane myRootPane;
		Thread verification=null;
		boolean finished=false;
		boolean interrupted=false;
		
		public RunningVerificationWidgets() {
			  
			this.verification = verification;
				
			setLayout(new GridLayout(2,1));
			add(new Label("Verification is running \n " +
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
			// TODO Auto-generated method stub
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
				child.waitFor();
				endTimeMs  = System.currentTimeMillis();

				bufferedReaderStderr = new BufferedReader(new InputStreamReader(child.getErrorStream()));
				bufferedReaderStdout = new BufferedReader(new InputStreamReader(child.getInputStream()));

				/*for (String s : commands){
					System.out.print(s + " ");
				}*/ 
				
				String line=null;
				
				verificationtime = endTimeMs-startTimeMs;
				
				dialog.finished();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		
	
	
	}
	
}
