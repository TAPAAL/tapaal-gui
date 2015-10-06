package dk.aau.cs.io.queries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
import dk.aau.cs.TCTL.SUMOParsing.TokenMgrError;
import dk.aau.cs.TCTL.SUMOParsing.ParseException;
import dk.aau.cs.TCTL.SUMOParsing.SUMOQueryParser;
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
import org.w3c.dom.Element;

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
        String propertyName = null;

        // Instantiate DOM builder components and build DOM Document object
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        Document doc = null;
        NodeList propList;
        NodeList propChildren;

        try {
            db = dbf.newDocumentBuilder();
        } catch(ParserConfigurationException e){
            // TODO do something meaningful with exception
            Logger.log(e);
            queries.add(null);
            return queries;
        }

        try {
            doc = db.parse(file);
        } catch (SAXException e){
            // TODO add dialog box: could not parse XML file
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
                // TODO Add dialog box: syntax error while parsing properties
                Logger.log(e);
                queries.add(null); 
                continue; 
            }

            // Find <id> tag and get property name
            propChildren = prop.getChildNodes();

            for(int j = 0; j < propChildren.getLength(); j++){
                Node child = propChildren.item(j);
                if(child.getNodeType() == Node.ELEMENT_NODE){
                    if(child.getNodeName().equals("id")){
                        // Traverse children and get TEXT
                        NodeList subNodes = child.getChildNodes();
                        for(int k = 0; k < subNodes.getLength(); k++){
                            Node n = subNodes.item(k);
                            if(n.getNodeType() == Node.TEXT_NODE){
                                propertyName = n.getNodeValue();
                                break;
                            }
                        }
                        break;
                    }
                }
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
