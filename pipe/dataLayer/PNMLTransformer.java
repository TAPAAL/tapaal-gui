package pipe.dataLayer;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * Create pnmlTransformer object, which deals with getting information from the 
 * XML file, then passes this back to DataLayer constructor for construction
 * @author Ben Kirby, 10 Feb 2007
 */
public class PNMLTransformer {
   
   
   /** Create a Transformer*/
   public PNMLTransformer() {
      ;
   }
   
   
   /** 
    * Transform a PNML file into a Document which is returned and used to
    * construct the DataLayer
    * @returns Document from which DataLayer can be built
    * @param filename URI location of PNML
    * @throws IOException
    * @throws SAXException
    * @throws TransformerException
    * @throws ParserConfigurationException
    */
   public Document transformPNML(String filename) {
      // System.out.println("========================================================");
      // System.out.println("dataLayer Loading filename=\"" + filename + "\"");
      // System.out.println("========================================================");
      File outputObjectArrayList = null;
      Document document = null;
      

      try {
         // Create Transformer with XSL Source File
         StreamSource xsltSource = new StreamSource(
                 Thread.currentThread().getContextClassLoader().getResourceAsStream(
                 "xslt" + System.getProperty("file.separator")
                 + "GenerateObjectList.xsl"));
         Transformer transformer = TransformerFactory.newInstance().newTransformer(xsltSource);
         
         // TRY TO DO ALL IN MEMORT TO REDUCE READ-WRITE DELAYS
         
         // Output for XSLT Transformation
         outputObjectArrayList = new File(System.getProperty("java.io.tmpdir") + 
                 System.getProperty("file.separator") + "ObjectList.xml"); 
         outputObjectArrayList.deleteOnExit();
         StreamSource source = new StreamSource(filename);
         StreamResult result = new StreamResult(outputObjectArrayList);
         transformer.transform(source, result);
         
         // Get DOM for transformed document
         document = getDOM(outputObjectArrayList);    
      } catch (IOException e) {
         System.out.println("IOException thrown in loadPNML(String filename)" +
                 " : dataLayer Class : dataLayer Package");
         e.printStackTrace(System.err);
      } catch (SAXException e) {
         System.out.println("SAXException thrown in loadPNML(String filename)" +
                 " : dataLayer Class : dataLayer Package");
         e.printStackTrace(System.err);
      } catch (TransformerException e) {
         System.out.println("TransformerException thrown in " +
                 "loadPNML(String filename) : dataLayer Class : dataLayer Package");
         e.printStackTrace(System.err);
      } catch (ParserConfigurationException e) {
         System.out.println("ParserConfigurationException thrown in " +
                 "loadPNML(String filename) : dataLayer Class : dataLayer Package");
         e.printStackTrace(System.err);
      }
      
//		Delete transformed file
      if (outputObjectArrayList != null) {
         outputObjectArrayList.delete();
      }
      return document;
      
      //BK - surely I want to make everything exact?? - ignore below in favour of catches above
      //	 Maxim - make it throw out any exception it gets to the caller. Debugging message left in for now.
      //} catch (Exception e) {
      //  throw new RuntimeException(e);
      //}	    
   }

   
   /**
    * Return a DOM for the PNML file at URI pnmlFileName
    * @param pnmlFileName URI of PNML file
    * @return A DOM for the PNML file pnmlFileName
    * @throws ParserConfigurationException
    * @throws IOException
    * @throws SAXException
    */
   public Document getDOM(String pnmlFileName)
           throws ParserConfigurationException, IOException, SAXException{
      Document document = null;
      
      try {
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         documentBuilderFactory.setIgnoringElementContentWhitespace(true);
         // POSSIBLY ADD VALIDATING
         // documentBuilderFactory.setValidating(true);
         DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
         document = documentBuilder.parse(pnmlFileName);
      } catch (ParserConfigurationException e) {
         System.err.println("javax.xml.parsers.ParserConfigurationException " +
                 "thrown in getDom(String pnmlFileName) : dataLayer Class : " +
                 "dataLayer Package");
      } catch (IOException e) {
         System.err.println("ERROR: File may not be present or have the" +
                 " correct attributes");
         System.err.println("java.io.IOException thrown in " +
                 "getDom(String pnmlFileName) : dataLayer Class : dataLayer Package");
      } catch (SAXException e) {
         System.err.println("org.xml.sax.SAXException thrown in " +
                 "getDom(String pnmlFileName) : dataLayer Class : dataLayer Package");
      }
      return document;
   }

   
   /**
    * Return a DOM for the PNML File pnmlFile
    * @param pnmlFile File Object for PNML of Petri-Net
    * @return A DOM for the File Object for PNML of Petri-Net
    * @throws ParserConfigurationException
    * @throws IOException
    * @throws SAXException
    */
   public Document getDOM(File pnmlFile)
           throws ParserConfigurationException, IOException, SAXException{
      Document document = null;
      
      try {
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         documentBuilderFactory.setIgnoringElementContentWhitespace(true);
// POSSIBLY ADD VALIDATING
// documentBuilderFactory.setValidating(true);
         DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
         document = documentBuilder.parse(pnmlFile);
      } catch (ParserConfigurationException e) {
         System.err.println("javax.xml.parsers.ParserConfigurationException thrown in getDom(String pnmlFileName) : dataLayer Class : dataLayer Package");
      } catch (IOException e) {
         System.err.println("ERROR: File may not be present or have the correct attributes");
         System.err.println("java.io.IOException thrown in getDom(String pnmlFileName) : dataLayer Class : dataLayer Package" + e);
      } catch (SAXException e) { 
         System.err.println("org.xml.sax.SAXException thrown in getDom(String pnmlFileName) : dataLayer Class : dataLayer Package" + e);
         System.err.println("Workaround: delete the xmlns attribute from the PNML root node.  Probably not ideal, to be fixed when time allows.");
      }
      return document;
   }

}
