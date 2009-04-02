/*
 * ExperimentValidator.java
 *
 * Created on 9 / agost / 2007, 12:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pipe.experiment.validation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import pipe.dataLayer.DataLayer;
import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.IOException;
import javax.xml.transform.stream.StreamSource;
import java.net.URI;

import javax.swing.JOptionPane;

 /**
  * This class handles the validation of the experiment file. This validation
  * includes schema validation and matching with the current net.
  * @author marc
  *
 */
public class ExperimentValidator {
    Document doc;
    DataLayer sourceDataLayer;
    
    /** Creates a new instance of ExperimentValidator
     @param doc Experiment document to be validated
     @param sourceDataLayer PN which has to match the Experiment to validate
     */
    public ExperimentValidator(Document doc, DataLayer sourceDataLayer) {
        this.doc = doc;
        this.sourceDataLayer = sourceDataLayer;
    }
    
    /** Validates the XML Document against the ExperimentSchema and checks whether it fits the
     * current net.
     * @return true if it is a valid Experiment file
     * @throws SAXException
     * @throws NotMatchingException if the experiment file does not match with the net.
     */
    public boolean validate() throws SAXException,NotMatchingException{
        //Load schema file
        File schemaFile = new File (Thread.currentThread().getContextClassLoader().getResource(
                 "schema" + System.getProperty("file.separator")
                 + "ExperimentSchema.xsd").getPath());
        
        //Build a Schema object
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = null;
        try {
            schema = factory.newSchema(schemaFile);
        } catch (SAXException e) {
            System.out.println(e);
          return false;
        }
        URI docURI=null;
        try{
            docURI = new URI(doc.getDocumentURI());
        }catch(java.net.URISyntaxException e){}
        StreamSource ssource = new StreamSource(new File(docURI));
        //Build a DOMSource from the document
        //DOMSource source = new DOMSource(doc);
        Validator validator = schema.newValidator();

        //Schema validation
        try {
          validator.validate(ssource);
          this.checkVectors();
          this.checkNetWorkConformance();
          return true;
        } catch (IOException e){
            System.out.println(e);
            return false;
        }
    }
    
    private void checkVectors() throws NotMatchingException{
         String number = "(([0-9]+))|([0-9])*\\.([0-9])*|([0-9])*\\.([0-9])*E([0-9])+";
         String vectorPattern = "("+number+")|("+number+")(,("+number+"))*";
         //System.out.println(vectorPattern);System.exit(0);
         NodeList nl=doc.getElementsByTagName("Range");
         for(int i=0; i<nl.getLength(); i++){
             Element range = (Element) nl.item(i);
             if(range.hasAttribute("Vector")){
                 String vector = range.getAttribute("Vector");
                 if (!vector.matches(vectorPattern)){
                     throw new NotMatchingException("Invalid vector \""+vector+"\"");
                 }
             }
         }
    }
    
    /** Checks whether the Experiment fits the current net. If it doesn't, a
     * NotMatchingException is thrown.
     * throws NotMatchingException
     */
    private void checkNetWorkConformance() throws NotMatchingException{
        //Variables
        //Canviar-ho, xq seran TransitionName i PlaceName enlloc de NodeName
        NodeList nl=doc.getElementsByTagName("Variable");
        for(int i=0; i<nl.getLength(); i++){
            Element v = (Element)nl.item(i);
            if(v.hasAttribute("TransitionName")){
                if(sourceDataLayer.getTransitionByName(v.getAttribute("TransitionName"))==null){
                    throw new NotMatchingException("Variable " + v.getAttribute("name") + 
                        " refers to a transition which does not exist ("+v.getAttribute("TransitionName")+").");
                }
            }else if (v.hasAttribute("PlaceName")){
                if(sourceDataLayer.getPlaceByName(v.getAttribute("PlaceName"))==null){
                    throw new NotMatchingException("Variable " + v.getAttribute("name") + 
                        " refers to a place which does not exist ("+v.getAttribute("PlaceName")+").");    
                }
            }/*
            if(sourceDataLayer.getPlace(v.getAttribute("NodeName"))==null && sourceDataLayer.getTransition(v.getAttribute("NodeName"))==null){
                throw new NotMatchingException("Variable " + v.getAttribute("Name") + 
                        " refers to a node which does not exist ("+v.getAttribute("NodeName")+").");
            }*/
        }
        
    }
    
    
}
