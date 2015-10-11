package dk.aau.cs.io.queries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.HashTableSize;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.CreateGui;
import pipe.gui.widgets.InclusionPlaces;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.XMLParsing.XMLQueryParser;
import dk.aau.cs.TCTL.XMLParsing.XMLQueryParseException;
import dk.aau.cs.TCTL.visitors.RenameTemplateVisitor;
import dk.aau.cs.debug.Logger;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.translations.ReductionOption;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException; 
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class XMLQueryLoader extends QueryLoader{

    private File file;

    public XMLQueryLoader(File file, TimedArcPetriNetNetwork network){
        super(network);
        this.file = file;
    }

    @Override
    protected ArrayList<TAPNQuery> getQueries(){
        ArrayList<TAPNQuery> queries = new ArrayList<TAPNQuery>();
        TCTLAbstractProperty property = null;
        TAPNQuery query = null;
        NodeList propList = null;
        Node idNode = null;
        String propertyName = null;

        // Instantiate DOM builder components and build DOM Document object
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        Document doc = null;

        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(file);
        } catch(ParserConfigurationException e){
            Logger.log(e);
            queries.add(null);
            return queries;
        } catch (SAXException e){
            Logger.log(e);
            queries.add(null);
            return queries;
        } catch (IOException e){
            Logger.log(e);
            queries.add(null);
            return queries;
        }

        // Get all properties from DOM Document
        propList = doc.getElementsByTagName("property");

        for(int i = 0; i < propList.getLength(); i++){
            Node prop = propList.item(i);
            
            try{
                property = XMLQueryParser.parse(prop);
            } catch (XMLQueryParseException e){
                Logger.log(e);
                queries.add(null); 
                continue; 
            }

            // Find <id> tag and get property name
            if(((idNode = XMLQueryParser.findSubNode("id", prop)) == null) ||
                ((propertyName = XMLQueryParser.getText(idNode)) == null)){

                propertyName = "Query-" + i;
            }
                                
            // The number 9999 is the number of extra tokens allowed, 
            // this is set high s.t. we don't have to change it manually
            query = new TAPNQuery(propertyName, 9999, property, 
                TraceOption.NONE, SearchOption.HEURISTIC, ReductionOption.VerifyPN, 
                true, false, true, true, true, true, HashTableSize.MB_16, 
                ExtrapolationOption.AUTOMATIC, new InclusionPlaces());

            RenameTemplateVisitor rt = new RenameTemplateVisitor("", 
                network.activeTemplates().get(0).name());

            query.getProperty().accept(rt, null);
                    
            queries.add(query);
        }

        return queries;
    }

    public static void importQueries(File file, TimedArcPetriNetNetwork network){
        XMLQueryLoader loader = new XMLQueryLoader(file, network);
        Collection<TAPNQuery> queries = loader.parseQueries();

        for(TAPNQuery query : queries){
            CreateGui.getCurrentTab().addQuery(query);
        }
    }
}
