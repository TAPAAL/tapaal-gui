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
import javax.imageio.ImageWriter;
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
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.widgets.FileBrowser;
import dk.aau.cs.debug.Logger;
import dk.aau.cs.petrinet.PipeTapnToAauTapnTransformer;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.colors.ColoredPipeTapnToColoredAauTapnTransformer;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.translations.tapn.BroadcastTranslation;
import dk.aau.cs.translations.tapn.Degree2BroadcastTranslation;
import dk.aau.cs.translations.tapn.OptimizedStandardSymmetryTranslation;
import dk.aau.cs.translations.tapn.OptimizedStandardTranslation;
import dk.aau.cs.translations.tapn.StandardSymmetryTranslation;
import dk.aau.cs.translations.tapn.StandardTranslation;



/**
 * Class for exporting things to other formats, as well as printing.
 * @author Maxim
 */
public class Export {

	public static final int PNG = 1;
	public static final int POSTSCRIPT = 2;
	public static final int PRINTER = 3;
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
		Iterator<ImageWriter> i = ImageIO.getImageWritersBySuffix("png");
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
		if (g.getComponentCount() == 0) {
			return;
		}

		String filename = null;
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
				case TIKZ:
					filename += "tex";
				}
			}
		}

		boolean gridEnabled = Grid.isEnabled();
		setupViewForExport(g, gridEnabled);

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
			case TIKZ:
				Object[] possibilities = {"Only the TikZ figure", "Full compilable LaTex including your figure"};
				String figureOptions = (String)JOptionPane.showInputDialog(
						CreateGui.getApp(),
						"Choose how you would like your TikZ figure outputted: \n",
						"Export to TikZ",
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
					TikZExporter output;
					if(!model.isUsingColors()){
						output = new TikZExporter(model,filename,tikZOption);
					}else{
						output = new TikZExporterForColoredTAPN(model, filename, tikZOption);
					}
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

		resetViewAfterExport(g, gridEnabled);

		return;
	}


	private static void resetViewAfterExport(GuiView g, boolean gridEnabled) {
		if (gridEnabled) {
			Grid.enableGrid();
		}
		PetriNetObject.ignoreSelection(false);
		g.repaint();
	}


	private static void setupViewForExport(GuiView g, boolean gridEnabled) {
		// Stuff to make it export properly
		g.updatePreferredSize();
		PetriNetObject.ignoreSelection(true);
		if (gridEnabled) {
			Grid.disableGrid();
		}
	}

	
	public static void exportUppaalXMLFromQuery(DataLayer appModel, TAPNQuery input, String modelFile, String queryFile) {
		File xmlfile=null, qfile=null;
		try {
			xmlfile = new File(modelFile);
			qfile = new File(queryFile);
		} catch (NullPointerException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return;
		}
	
		//Create transformer
		PipeTapnToAauTapnTransformer transformer = null;

		if(appModel.isUsingColors()){
			transformer = new ColoredPipeTapnToColoredAauTapnTransformer();
		}else{
			transformer = new PipeTapnToAauTapnTransformer();
		}

		TAPN model=null;
		try {
			model = transformer.getAAUTAPN(appModel, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (CreateGui.getApp().getComponentCount() == 0) {
			return;
		}


		int capacity;
		capacity = input.getCapacity();
		String inputQuery = input.getQuery();
		TraceOption traceOption = input.getTraceOption();
		SearchOption searchOption = input.getSearchOption();
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

		// TODO: Refactor so translation to dk.aau.cs.petrinet.TAPNQuery happens exactly once
		
		// Select the model based on selected export option.
		if (input.getReductionOption() == ReductionOption.STANDARDSYMMETRY){

			StandardSymmetryTranslation t = new StandardSymmetryTranslation();
			try {
				t.autoTransform(model, new PrintStream(xmlfile), new PrintStream(qfile), new dk.aau.cs.petrinet.TAPNQuery(input.getProperty(), 0), capacity);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (input.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARDSYMMETRY){

			OptimizedStandardSymmetryTranslation t = new OptimizedStandardSymmetryTranslation();
			try {
				t.autoTransform(model, new PrintStream(xmlfile), new PrintStream(qfile), new dk.aau.cs.petrinet.TAPNQuery(input.getProperty(), 0), capacity);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if (input.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARD){
			Logger.log("Using ADV_NOSYMQ");
			OptimizedStandardTranslation t = new OptimizedStandardTranslation();
			try {
				t.autoTransform(model, new PrintStream(xmlfile), new PrintStream(qfile), new dk.aau.cs.petrinet.TAPNQuery(input.getProperty(), 0), capacity);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	


		} else if(input.getReductionOption() == ReductionOption.BROADCAST || input.getReductionOption() == ReductionOption.BROADCASTSYMMETRY){
			BroadcastTranslation broadcastTransformer = null;
			if(appModel.isUsingColors()){
				broadcastTransformer = new dk.aau.cs.translations.coloredtapn.ColoredBroadcastTranslation(capacity, input.getReductionOption() == ReductionOption.BROADCASTSYMMETRY);
			}else{
				broadcastTransformer = new dk.aau.cs.translations.tapn.BroadcastTranslation(capacity, input.getReductionOption() == ReductionOption.BROADCASTSYMMETRY);
			}
			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(model);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = broadcastTransformer.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(input.getProperty(), capacity + 1 + model.getTokens().size()));
				query.output(new PrintStream(qfile));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(input.getReductionOption() == ReductionOption.DEGREE2BROADCASTSYMMETRY || input.getReductionOption() == ReductionOption.DEGREE2BROADCAST){
			Degree2BroadcastTranslation broadcastTransformer = null;
			if(appModel.isUsingColors()){
				broadcastTransformer = new dk.aau.cs.translations.coloredtapn.ColoredDegree2BroadcastTranslation(capacity, input.getReductionOption() == ReductionOption.DEGREE2BROADCASTSYMMETRY);
			}else{
				broadcastTransformer = new dk.aau.cs.translations.tapn.Degree2BroadcastTranslation(capacity, input.getReductionOption() == ReductionOption.DEGREE2BROADCASTSYMMETRY);
			}
			try{
				dk.aau.cs.TA.NTA nta = broadcastTransformer.transformModel(model);
				nta.outputToUPPAALXML(new PrintStream(xmlfile));
				dk.aau.cs.TA.UPPAALQuery query = broadcastTransformer.transformQuery(new dk.aau.cs.petrinet.TAPNQuery(input.getProperty(), capacity + 1 + model.getTokens().size()));
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
				StandardTranslation t2 = new StandardTranslation(model, new PrintStream(xmlfile), capacity);
				t2.transform();
				t2.transformQueriesToUppaal(capacity, new dk.aau.cs.petrinet.TAPNQuery(input.getProperty(), 0), new PrintStream(qfile));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
}
