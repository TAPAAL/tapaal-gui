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
import dk.aau.cs.TCTL.visitors.RenameTemplateVisitor;
import dk.aau.cs.debug.Logger;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.translations.ReductionOption;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.DocumentBuilder;
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

        // Instantiate DOM builder components and build DOM Document object
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder(); 
        Document doc;

        try {
            doc = db.parse(file);
        } catch (SAXException e){
            Logger.log(e); queries.add(null); continue;
        } catch (IOException e){
            Logger.log(e); queries.add(null); continue;
        }

        TCTLAbstractProperty property;

        try{
            property = XMLQueryParser.parse(doc);
        } 
        catch (ParseException e){ 
            // TODO Implement custom exception in XMLQueryParser
            Logger.log(e); queries.add(null); continue; 
        }
                            
        // The number 9999 is the number of extra tokens allowed, 
        // this is set high s.t. we don't have to change it manually
        TAPNQuery query = new TAPNQuery(name, 9999, property, 
            TraceOption.NONE, SearchOption.HEURISTIC, ReductionOption.VerifyPN, 
            true, false, true, true, true, true, HashTableSize.MB_16, 
            ExtrapolationOption.AUTOMATIC, new InclusionPlaces());
                        
        RenameTemplateVisitor rt = new RenameTemplateVisitor("", 
            network.activeTemplates().get(0).name());

        query.getProperty().accept(rt, null);
                
        queries.add(query);

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
