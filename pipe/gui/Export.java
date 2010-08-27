/*
 * Export class
 *
 * Created on 27-Feb-2004
 *
 */
package pipe.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.StreamPrintServiceFactory;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TNTransformer;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.widgets.FileBrowser;
import pipe.gui.widgets.QueryDialogue;
import dk.aau.cs.TAPN.Degree2BroadcastTransformer;
import dk.aau.cs.TAPN.TAPNToNTABroadcastTransformer;
import dk.aau.cs.TAPN.TAPNToNTATransformer;
import dk.aau.cs.TAPN.uppaaltransform.AdvancedUppaalNoSym;
import dk.aau.cs.TAPN.uppaaltransform.AdvancedUppaalSym;
import dk.aau.cs.TAPN.uppaaltransform.NaiveUppaalSym;
import dk.aau.cs.debug.Logger;
import dk.aau.cs.petrinet.PipeTapnToAauTapnTransformer;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNtoUppaalTransformer;
import dk.aau.cs.petrinet.colors.ColoredPipeTapnToColoredAauTapnTransformer;
import dk.aau.cs.petrinet.degree2converters.KyrketestUppaalSym;



/**
 * Class for exporting things to other formats, as well as printing.
 * @author Maxim
 */
public class Export {

	public static final int PNG = 1;
	public static final int POSTSCRIPT = 2;
	public static final int PRINTER = 3;
	public static final int TN = 4;
	public static final int TIKZ = 5;
	

	public static void toPostScript(Object g,String filename) 
	throws PrintException, IOException {
		// Input document type
		DocFlavor flavour = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		// Output stream MIME type
		String psMimeType = DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType();

		// Look up a print service factory that can handle this job
		StreamPrintServiceFactory[] factories = 
			StreamPrintServiceFactory.lookupStreamPrintServiceFactories(
					flavour, psMimeType);
		if (factories.length == 0) {
			throw new RuntimeException("No suitable factory found for export to PS");
		}

		FileOutputStream f = new FileOutputStream(filename);
		// Get a print service from the factory, create a print job and print
		factories[0].getPrintService(f).createPrintJob().print(
				new SimpleDoc(g, flavour, null),
				new HashPrintRequestAttributeSet());
		f.close();
	}


	public static void toPNG(JComponent g,String filename) throws IOException {
		Iterator i = ImageIO.getImageWritersBySuffix("png");
		if (!i.hasNext()) {
			throw new RuntimeException("No ImageIO exporters can handle PNG");
		}

		File f = new File(filename);
		BufferedImage img = new BufferedImage(g.getPreferredSize().width,
				g.getPreferredSize().height,
				BufferedImage.TYPE_3BYTE_BGR);
		g.print(img.getGraphics());
		ImageIO.write(img, "png", f);
	}


	public static void toTN(DataLayer net, String filename) throws IOException{
		TNTransformer tnt = new TNTransformer();
		try{
			tnt.saveTN(new File(filename), net);
		}catch(javax.xml.parsers.ParserConfigurationException e){
			System.out.println(e);
		}catch(javax.xml.transform.TransformerConfigurationException e){
			System.out.println(e);
		}
		catch(javax.xml.transform.TransformerException e){
			System.out.println(e);
		}
	}


	private static void toPrinter(Object g) throws PrintException {
		///* The Swing way
		PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
		DocFlavor flavour = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		PrintService printService[] = PrintServiceLookup.lookupPrintServices(flavour, pras);

		if (printService.length == 0) {
			throw new PrintException("\nUnable to locate a compatible printer service." +
			"\nTry exporting to PostScript.");
		}
		PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
		PrintService service = 
			ServiceUI.printDialog(null, 200, 200, printService, defaultService, flavour, pras);
		if (service != null) {
			DocPrintJob job = service.createPrintJob();
			DocAttributeSet das = new HashDocAttributeSet();
			Doc doc = new SimpleDoc(g, flavour, das);
			job.print(doc, pras);
		}
		//*/
		/* The AWT way:
    PrinterJob pjob = PrinterJob.getPrinterJob();
    PageFormat pf = pjob.defaultPage();
    pjob.setPrintable(g, pf);
    try {
      if (pjob.printDialog()) pjob.print();
    } catch (PrinterException e) {
      error=e.toString();
    }
    //*/
	}


	public static void exportGuiView(GuiView g,int format, DataLayer model) {
		boolean gridEnabled = Grid.isEnabled();
		String filename = null;

		if (g.getComponentCount() == 0) {
			return;
		}

		if (CreateGui.getFile() != null) {
			filename=CreateGui.getFile().getAbsolutePath();
			// change file extension
			int dotpos = filename.lastIndexOf('.');
			if (dotpos > filename.lastIndexOf(System.getProperty("file.separator"))) {
				// dot is for extension
				filename = filename.substring(0,dotpos+1);
				switch (format) {
				case PNG:        
					filename += "png"; 
					break;
				case POSTSCRIPT: 
					filename += "ps";
					break;
				case TN:
					filename += "xml";
					break;             
				case TIKZ:
					filename += "tex";
				}
			}
		}

		// Stuff to make it export properly
		g.updatePreferredSize();
		PetriNetObject.ignoreSelection(true);
		if (gridEnabled) {
			Grid.disableGrid();
		}

		try {
			switch (format) {
			case PNG:
				filename = new FileBrowser("PNG image","png",filename).saveFile();
				if (filename != null) {
					toPNG(g, filename);
				}
				break;
			case POSTSCRIPT:
				filename = new FileBrowser("PostScript file","ps",filename).saveFile();
				if (filename != null) {
					toPostScript(g, filename);
				}
				break;
			case PRINTER:
				toPrinter(g);
				break;
			case TN:
				filename=new FileBrowser("TN net","xml",filename).saveFile();
				if (filename!=null) {
					toTN(model,filename);
				}
				break;  
			case TIKZ:
				Object[] possibilities = {"Only the TikZ figure", "Full compilable LaTex including your figure"};
				String figureOptions = (String)JOptionPane.showInputDialog(
									CreateGui.getApp(),
				                    "Choose how you would like your TikZ figure outputted: \n",
				                    "Customized Dialog",
				                    JOptionPane.PLAIN_MESSAGE,
				                    null,
				                    possibilities,
				                    "Only the TikZ figure");
				TikZExporter.TikZOutputOption tikZOption  = TikZExporter.TikZOutputOption.FIGURE_ONLY;
				if(figureOptions == null)
					return;
				
				if(figureOptions == possibilities[0])
					tikZOption = TikZExporter.TikZOutputOption.FIGURE_ONLY;
				if(figureOptions == possibilities[1])
					tikZOption = TikZExporter.TikZOutputOption.FULL_LATEX;
				
				filename=new FileBrowser("TikZ figure","tex",filename).saveFile();
				if (filename!=null) {
					TikZExporter output = new TikZExporter(model,filename,tikZOption);
					output.ExportToTikZ();
				}
			}
		} catch (Exception e) {
			// There was some problem with the action
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There were errors performing the requested action:\n" + e,
					"Error", JOptionPane.ERROR_MESSAGE
			);
		}

		if (gridEnabled) {
			Grid.enableGrid();
		}
		PetriNetObject.ignoreSelection(false);
		g.repaint();

		return;
	}

	public static void exportUppaalAdvancedGuiView(GuiView appView, DataLayer AppModel) {
		// TODO Auto-generated method stub
		boolean gridEnabled = Grid.isEnabled();
		String filename = null;
		
		//Create transformer
		PipeTapnToAauTapnTransformer transformer = new PipeTapnToAauTapnTransformer(AppModel, 0);
		TAPN model=null;
		try {
			model = transformer.getAAUTAPN();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (appView.getComponentCount() == 0) {
			return;
		}

		if (CreateGui.getFile() != null) {
			filename=CreateGui.getFile().getAbsolutePath();
			// change file extension
			int dotpos = filename.lastIndexOf('.');
			if (dotpos > filename.lastIndexOf(System.getProperty("file.separator"))) {
				// dot is for extension
				filename = filename.substring(0,dotpos+1);

			}
		}

		// Stuff to make it export proper
		appView.updatePreferredSize();
		PetriNetObject.ignoreSelection(true);
		if (gridEnabled) {
			Grid.disableGrid();
		}

		 //int currentCapacity = this.getCapacity();

		//String input = JOptionPane.showInputDialog("Maximum number of extra capacity tokens:", String.valueOf(currentCapacity));
		String input = JOptionPane.showInputDialog("Number of ekstra token capacity : (use x - y to generate from x to ty)", 0);
		
		int capacity;
		int capacitymax=0;
		
		if (input.contains("-")) {
			//Generate many files
			
			String[] t =  input.split("-");
			capacity = Integer.parseInt(t[0]);
			capacitymax = Integer.parseInt(t[1]);
			

		}else {
			try {
				capacity = Integer.parseInt(input);
				if (capacity < 0)
					JOptionPane.showMessageDialog(CreateGui.getApp(),"Please enter a positive number. ");
				else{
					// this.setCapacity(newCapacity);
				}

			} catch (Exception exc) {
				if (input != null)
					JOptionPane.showMessageDialog(CreateGui.getApp(),"Please enter a number.","Invalid entry",JOptionPane.ERROR_MESSAGE);
				System.err.println(exc.toString());
				return;
			}

		}

		
		TAPNQuery inputFromGUI = QueryDialogue.ShowUppaalQueryDialogue(QueryDialogue.QueryDialogueOption.Export, null);
		if (inputFromGUI == null) {return;}
		String inputQuery = inputFromGUI.query;
		
		
		
		//Selete export type
		input = JOptionPane.showInputDialog(CreateGui.getApp(),"Please enter the export tecqnique: \n" +
				"(enter nothing): naive export\n" +
				"-s for naive + symetry reduction \n" +
				/*"-m for minimal model optimisation \n" +*/
				"-c for using capacity optimisation\n" +
				/*"-m -s -d minimal model with symetry reduction and nice draw, \n" +*/
				"-c -s -d capacity with symetry reduction and nice draw, \n" +
				"-o order preset" +
				"-y kyrke testing" + 
				"-s -d -r for remove unsused");
				
		
		boolean drawnice=false, symred = false;
		boolean minialmodel = false;
		boolean capacityminial = false;
		boolean capacityreset = false;
		boolean removeun = false;
		boolean orderpreset = false;
		boolean kyrketesting = false;
		
		if (input.contains("-r")) {
			System.out.println("removeun selected");
			removeun = true;
		} 
		
		if (input.contains("-s")) {
			symred = true;
		} 
		
		if (input.contains("-z")) {
		}
		if (input.contains("-o")) {
			orderpreset = true;
		} 
		
		if (input.contains("-y")) {
			kyrketesting = true;
		} 
		
		if (input.contains("-d")) {
			System.out.println("drawnice selected");
			drawnice = true;
		} 
		
		if (input.contains("-n") && input.contains("-d")) {
			try {
				throw new Exception("Export method not implemented");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (input.contains("-m")){
			minialmodel = true;
		}
		
		if (input.contains("-c")){
			capacityminial = true;
		}
		
//		Get export file
		try {
			filename = new FileBrowser("Uppaal XML","xml",filename).saveFile();
		} catch (Exception e) {
			// There was some problem with the action
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There were errors performing the requested action:\n" + e,
					"Error", JOptionPane.ERROR_MESSAGE
			);
		}

		
		
		
		
		
		// Do transform
		try {
			model.convertToConservative();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (orderpreset){
			
			model.orderPresetRescrition();
			
			for (dk.aau.cs.petrinet.Arc a : model.getTransitions().get(0).getPreset()){
				System.out.println(a);
			}
		}
		KyrketestUppaalSym test = null;
		try {
			
			if (minialmodel){
				model = model.convertToDegree2("minimal");
			} else if (capacityminial)  {
				model = model.convertToDegree2("capacity");
			} else if (kyrketesting) {
				
				test = new KyrketestUppaalSym(model);
				model = test.transform(model);
				
			}else {
			
				model = model.convertToDegree2();
			}
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int i = capacity;
		String[] a = filename.split(".xml");
		
		do {
			//Create uppaal query file
			//Remove the .xml part
			
			filename=a[0];	

			if (capacitymax != 0) {
				// Append number to name
				filename+="-"+i;
			}
			
			//Create uppaal xml file
			try {
				
				if (kyrketesting){
					test = new KyrketestUppaalSym(model);
					test.transformToUppaal(new PrintStream(new File(filename+".xml")), i);
				}else {
					TAPNtoUppaalTransformer t2 = new TAPNtoUppaalTransformer(model, new PrintStream(new File(filename+".xml")), i, symred, drawnice, capacityreset, removeun);
					t2.transform();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			try {
				
				if (kyrketesting){
					test.transformQueriesToUppaal(i, inputQuery, new PrintStream(new File(filename+".q")));
				}else {
					model.transformQueriesToUppaal(i, inputQuery, new PrintStream(new File(filename+".q")));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.err.println("We had an error translating the query");
			}

			i++;
		} while (i <= capacitymax);

		JOptionPane.showMessageDialog(CreateGui.getApp(),
				"The uppaal files .xml and .q have been generated.",
				"Translation into UPPAAL finished",
				JOptionPane.INFORMATION_MESSAGE);
	
	


	if (gridEnabled) {
		Grid.enableGrid();
	}
	PetriNetObject.ignoreSelection(false);
	appView.repaint();

	return;
}

	public static void exportUppaalGuiView(GuiView appView, DataLayer AppModel) {
		// TODO Auto-generated method stub
		boolean gridEnabled = Grid.isEnabled();
		String filename = null;
		
		//Create transformer
		PipeTapnToAauTapnTransformer transformer = new PipeTapnToAauTapnTransformer(AppModel, 0);
		TAPN model=null;
		try {
			model = transformer.getAAUTAPN();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (appView.getComponentCount() == 0) {
			return;
		}

		if (CreateGui.getFile() != null) {
			filename=CreateGui.getFile().getAbsolutePath();
			// change file extension
			int dotpos = filename.lastIndexOf('.');
			if (dotpos > filename.lastIndexOf(System.getProperty("file.separator"))) {
				// dot is for extension
				filename = filename.substring(0,dotpos+1);

			}
		}

		// Stuff to make it export properly
		appView.updatePreferredSize();
		PetriNetObject.ignoreSelection(true);
		if (gridEnabled) {
			Grid.disableGrid();
		}

		 //int currentCapacity = this.getCapacity();

		//String input = JOptionPane.showInputDialog("Maximum number of extra capacity tokens:", String.valueOf(currentCapacity));
//		String input = JOptionPane.showInputDialog("Number of ekstra token capacity :", 0);
		int capacity;
/*		try {
			capacity = Integer.parseInt(input);
			if (capacity < 0)
				JOptionPane.showMessageDialog(CreateGui.getApp(),"Please enter a positive number.");
			else{
				// this.setCapacity(newCapacity);
			}

		} catch (Exception exc) {
			if (input != null)
				JOptionPane.showMessageDialog(CreateGui.getApp(),"Please enter a number.","Invalid entry",JOptionPane.ERROR_MESSAGE);
			System.err.println(exc.toString());
			return;
		}
*/		


		TAPNQuery inputFromGUI = QueryDialogue.ShowUppaalQueryDialogue(QueryDialogue.QueryDialogueOption.Export, null);
		if (inputFromGUI == null) {return;}
		capacity = inputFromGUI.capacity;
		String inputQuery = inputFromGUI.query;
		
		
//		Get export file
		try {
			filename = new FileBrowser("Uppaal XML","xml",filename).saveFile();
		} catch (Exception e) {
			// There was some problem with the action
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There were errors performing the requested action:\n" + e,
					"Error", JOptionPane.ERROR_MESSAGE
			);
		}

		

		
		// Do transform
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
			TAPNtoUppaalTransformer t2 = new TAPNtoUppaalTransformer(model, new PrintStream(new File(filename)), capacity, false, false, false, false);
			t2.transform();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Create uppaal query file
		//Remove the .xml part
		String[] a = filename.split(".xml");
		filename=a[0];
		
		try {
			model.transformQueriesToUppaal(capacity, inputQuery, new PrintStream(new File(filename+".q")));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("We had an error translating the query");
		}
		    
		JOptionPane.showMessageDialog(CreateGui.getApp(),
				"The uppaal files .xml and .q have been generated.",
				"Translation into UPPAAL finished",
				JOptionPane.INFORMATION_MESSAGE);
	
	


	if (gridEnabled) {
		Grid.enableGrid();
	}
	PetriNetObject.ignoreSelection(false);
	appView.repaint();

	return;
}
	
	
	public static void exportUppaalSymetricGuiView(GuiView appView, DataLayer AppModel) {
		// TODO Auto-generated method stub
		boolean gridEnabled = Grid.isEnabled();
		String filename = null;
		
		//Create transformer
		PipeTapnToAauTapnTransformer transformer = new PipeTapnToAauTapnTransformer(AppModel, 0);
		TAPN model=null;
		try {
			model = transformer.getAAUTAPN();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (appView.getComponentCount() == 0) {
			return;
		}

		if (CreateGui.getFile() != null) {
			filename=CreateGui.getFile().getAbsolutePath();
			// change file extension
			int dotpos = filename.lastIndexOf('.');
			if (dotpos > filename.lastIndexOf(System.getProperty("file.separator"))) {
				// dot is for extension
				filename = filename.substring(0,dotpos+1);

			}
		}

		// Stuff to make it export properly
		appView.updatePreferredSize();
		PetriNetObject.ignoreSelection(true);
		if (gridEnabled) {
			Grid.disableGrid();
		}

		 //int currentCapacity = this.getCapacity();

		//String input = JOptionPane.showInputDialog("Maximum number of extra capacity tokens:", String.valueOf(currentCapacity));
//		String input = JOptionPane.showInputDialog("Number of ekstra token capacity :", 0);
		int capacity;
/*		try {
			capacity = Integer.parseInt(input);
			if (capacity < 0)
				JOptionPane.showMessageDialog(CreateGui.getApp(),"Please enter a positive number.");
			else{
				// this.setCapacity(newCapacity);
			}

		} catch (Exception exc) {
			if (input != null)
				JOptionPane.showMessageDialog(CreateGui.getApp(),"Please enter a number.","Invalid entry",JOptionPane.ERROR_MESSAGE);
			System.err.println(exc.toString());
			return;
		}
*/		


		TAPNQuery inputFromGUI = QueryDialogue.ShowUppaalQueryDialogue(QueryDialogue.QueryDialogueOption.Export, null);
		if (inputFromGUI == null) {return;}
		capacity = inputFromGUI.capacity;
		String inputQuery = inputFromGUI.query;
		
//		Get export file
		try {
			filename = new FileBrowser("Uppaal XML","xml",filename).saveFile();
		} catch (Exception e) {
			// There was some problem with the action
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There were errors performing the requested action:\n" + e,
					"Error", JOptionPane.ERROR_MESSAGE
			);
		}

		

		
		// Do transform
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
			TAPNtoUppaalTransformer t2 = new TAPNtoUppaalTransformer(model, new PrintStream(new File(filename)), capacity);
			t2.transform();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Create uppaal query file
		//Remove the .xml part
		String[] a = filename.split(".xml");
		filename=a[0];
		
		try {
			model.transformQueriesToUppaal(capacity, inputQuery, new PrintStream(new File(filename+".q")));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("We had an error translating the query");
		}
		    
		JOptionPane.showMessageDialog(CreateGui.getApp(),
				"The uppaal files .xml and .q have been generated.",
				"Translation into UPPAAL finished",
				JOptionPane.INFORMATION_MESSAGE);
	
	


	if (gridEnabled) {
		Grid.enableGrid();
	}
	PetriNetObject.ignoreSelection(false);
	appView.repaint();

	return;
}

	public static void exportDegree2(GuiView appView, DataLayer appModel) {
		
		PipeTapnToAauTapnTransformer transformer = new PipeTapnToAauTapnTransformer(appModel, 0);
		TAPN model=null;
		try {
			model = transformer.getAAUTAPN();
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		
		//CreateGui.getApp().createNewTab(model);
		
		try {
			model.convertToConservative();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		TAPN model1=null, model2=null, model3 = null;
		try {
			model1 = model.convertToDegree2();
			model2 = model.convertToDegree2capacity();
			model3 = model.convertToDegree2("minimal");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		/*
		
		try {
			model.exportToDOT(new PrintStream(new File("/tmp/test1")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String command = "/tmp/script.sh";
	    try {
			Process child = Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		String filename="";
		try {
			filename = new FileBrowser("TAPN/PIPE XML","xml",filename).saveFile();
		} catch (Exception e) {
			// There was some problem with the action
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There were errors performing the requested action:\n" + e,
					"Error", JOptionPane.ERROR_MESSAGE
			);
		}
		
		String[] a = filename.split(".xml");
		
		try {
			model1.exportToPIPExml(new PrintStream(new File(filename)));
			model2.exportToPIPExml(new PrintStream(new File(a[0]+"1.xml")));
			model3.exportToPIPExml(new PrintStream(new File(a[0]+"2.xml")));

			
			CreateGui.getApp().createNewTabFromFile(new File(filename),false);
			CreateGui.getApp().createNewTabFromFile(new File(a[0]+"1.xml"),false);
			CreateGui.getApp().createNewTabFromFile(new File(a[0]+"2.xml"),false);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}


	public static void exportUppaalXMLFromQuery(DataLayer appModel, TAPNQuery input) {
		File xmlfile=null, qfile=null;
		try {
			xmlfile = File.createTempFile("verifyta", ".xml");
			qfile = File.createTempFile("verifyta", ".q");
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		xmlfile.deleteOnExit();qfile.deleteOnExit();
		String filename = null;
		try {
			filename = new FileBrowser("Uppaal XML","xml",filename).saveFile();
			xmlfile=new File(filename);
			String[] a = filename.split(".xml");
			qfile=new File(a[0]+".q");

		} catch (Exception e) {
			// There was some problem with the action
			if (filename == null){
				JOptionPane.showMessageDialog(CreateGui.getApp(), "No Uppaal XML file saved.");
				return;
			}else{
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"There were errors performing the requested action:\n" + e,
						"Error", JOptionPane.ERROR_MESSAGE
				);				
			}
		}
		
		//Create transformer
		PipeTapnToAauTapnTransformer transformer = null;
		
		if(appModel.isUsingColors()){
			transformer = new ColoredPipeTapnToColoredAauTapnTransformer(appModel, 0);
		}else{
			transformer = new PipeTapnToAauTapnTransformer(appModel, 0);
		}
		
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
		}else if (input.reductionOption == TAPNQuery.ReductionOption.ADV_NOSYM){
			Logger.log("Using ADV_NOSYMQ");
			AdvancedUppaalNoSym t = new AdvancedUppaalNoSym();
			try {
				t.autoTransform(model, new PrintStream(xmlfile), new PrintStream(qfile), inputQuery, capacity);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	

			
		} else if(input.reductionOption == TAPNQuery.ReductionOption.INHIB_TO_PRIO_STANDARD){
			TAPNToNTATransformer trans = 
				new dk.aau.cs.TAPN.TAPNToNTAStandardTransformer(capacity);
			
			try{
				dk.aau.cs.TA.NTA nta = trans.transformModel(model);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = trans.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(inputQuery, capacity + 1 + model.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else if(input.reductionOption == TAPNQuery.ReductionOption.INHIB_TO_PRIO_SYM){
			TAPNToNTATransformer trans = 
				new dk.aau.cs.TAPN.TAPNToNTASymmetryTransformer(capacity);
			
			try{
				dk.aau.cs.TA.NTA nta = trans.transformModel(model);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = trans.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(inputQuery, capacity + 1 + model.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(input.reductionOption == TAPNQuery.ReductionOption.BROADCAST_STANDARD || input.reductionOption == TAPNQuery.ReductionOption.BROADCAST_SYM){
			TAPNToNTABroadcastTransformer broadcastTransformer = null;
			if(appModel.isUsingColors()){
				broadcastTransformer = new dk.aau.cs.TAPN.colorTranslations.ColoredBroadcastTransformer(capacity, input.reductionOption == TAPNQuery.ReductionOption.BROADCAST_SYM);
			}else{
				broadcastTransformer = new dk.aau.cs.TAPN.TAPNToNTABroadcastTransformer(capacity, input.reductionOption == TAPNQuery.ReductionOption.BROADCAST_SYM);
			}
			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(model);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = broadcastTransformer.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(inputQuery, capacity + 1 + model.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(input.reductionOption == TAPNQuery.ReductionOption.BROADCAST_DEG2_SYM || input.reductionOption == TAPNQuery.ReductionOption.BROADCAST_DEG2){
			Degree2BroadcastTransformer broadcastTransformer = null;
			if(appModel.isUsingColors()){
				broadcastTransformer = new dk.aau.cs.TAPN.colorTranslations.ColoredDegree2BroadcastTransformer(capacity, input.reductionOption == TAPNQuery.ReductionOption.BROADCAST_DEG2_SYM);
			}else{
				broadcastTransformer = new dk.aau.cs.TAPN.Degree2BroadcastTransformer(capacity, input.reductionOption == TAPNQuery.ReductionOption.BROADCAST_DEG2_SYM);
			}
			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(model);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = broadcastTransformer.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(inputQuery, capacity + 1 + model.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(input.reductionOption == TAPNQuery.ReductionOption.ADV_BROADCAST_SYM){
			TAPNToNTABroadcastTransformer broadcastTransformer = 
				new dk.aau.cs.TAPN.AdvancedBroadcastTransformer(capacity, true);
			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(model);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = broadcastTransformer.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(inputQuery, capacity + 1 + model.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(input.reductionOption == TAPNQuery.ReductionOption.OPT_BROADCAST_SYM || input.reductionOption == TAPNQuery.ReductionOption.OPT_BROADCAST){
			TAPNToNTABroadcastTransformer broadcastTransformer = 
				new dk.aau.cs.TAPN.OptimizedBroadcastTransformer(capacity, input.reductionOption == TAPNQuery.ReductionOption.OPT_BROADCAST_SYM);
			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(model);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = broadcastTransformer.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(inputQuery, capacity + 1 + model.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(input.reductionOption == TAPNQuery.ReductionOption.SUPER_BROADCAST_SYM || input.reductionOption == TAPNQuery.ReductionOption.SUPER_BROADCAST){
			TAPNToNTABroadcastTransformer broadcastTransformer = 
				new dk.aau.cs.TAPN.SuperBroadcastTransformer(capacity, input.reductionOption == TAPNQuery.ReductionOption.SUPER_BROADCAST_SYM);
			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(model);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = broadcastTransformer.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(inputQuery, capacity + 1 + model.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {

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
		
	}	
}
