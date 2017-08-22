package dk.aau.cs.io.queries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JOptionPane;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.HashTableSize;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.CreateGui;
import pipe.gui.widgets.InclusionPlaces;
import dk.aau.cs.TCTL.XMLParsing.XMLQueryParser;
import dk.aau.cs.TCTL.XMLParsing.QueryWrapper;
import dk.aau.cs.TCTL.XMLParsing.XMLCTLQueryParser;
import dk.aau.cs.TCTL.visitors.RenameTemplateVisitor;
import dk.aau.cs.debug.Logger;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.translations.ReductionOption;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class XMLQueryLoader extends QueryLoader{

    private File file;
    private ArrayList<QueryWrapper> faultyQueries;

    public XMLQueryLoader(File file, TimedArcPetriNetNetwork network){
        super(network);
        this.file = file;
        this.faultyQueries = new ArrayList<QueryWrapper>();
    }

    @Override
    protected ArrayList<TAPNQuery> getQueries(){
        ArrayList<TAPNQuery> queries = new ArrayList<TAPNQuery>();

        // Instantiate DOM builder components and build DOM
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document doc;
        
        try {
            db = dbf.newDocumentBuilder();
        } catch(ParserConfigurationException e){
            Logger.log(e);
            createDialogBox(e.getMessage(), "Parse Exception");
            return queries;
        }

        db.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException e) throws SAXException {}

            @Override
            public void fatalError(SAXParseException e) throws SAXException { throw e; }

            @Override
            public void error(SAXParseException e) throws SAXException { throw e; }
        });
            
        try{
            doc = db.parse(file);
        } catch (SAXException e){
            Logger.log(e);
            createDialogBox(e.getMessage(), "Parse Exception");
            return queries;
        } catch (IOException e){
            Logger.log(e);
            createDialogBox(e.getMessage(), "Parse Exception");
            return queries;
        } catch (IllegalArgumentException e){
            Logger.log(e);
            createDialogBox(e.getMessage(), "Parse Exception");
            return queries;
        }

        // Get all properties from DOM
        NodeList propList = doc.getElementsByTagName("property");

        for(int i = 0; i < propList.getLength(); i++){
            Node prop = propList.item(i);
            QueryWrapper queryWrapper = new QueryWrapper();

            // Save query for later use in dialog window
            this.faultyQueries.add(queryWrapper);

            // Update queryWrapper name and property
            if(!XMLCTLQueryParser.parse(prop, queryWrapper)){
                queries.add(null); 
                continue; 
            }

            // The number 9999 is the number of extra tokens allowed, 
            // this is set high s.t. we don't have to change it manually
            TAPNQuery query = new TAPNQuery(queryWrapper.getName(), 9999,
                queryWrapper.getProp(),TraceOption.NONE, SearchOption.HEURISTIC, 
                ReductionOption.VerifyPN, true, false, true, true, true, true, 
                HashTableSize.MB_16, ExtrapolationOption.AUTOMATIC, new InclusionPlaces());

            RenameTemplateVisitor rt = new RenameTemplateVisitor("", 
                network.activeTemplates().get(0).name());
            query.setCategory(TAPNQueryLoader.detectCategory(queryWrapper.getProp(), false));
            
            if(query.getCategory() == TAPNQuery.QueryCategory.CTL){
            	query.setSearchOption(SearchOption.DFS);
            	query.setUseReduction(true);
            }
            
            query.getProperty().accept(rt, null);
                    
            queries.add(query);
        }

        return queries;
    }

    public static void importQueries(File file, TimedArcPetriNetNetwork network){
        XMLQueryLoader loader = new XMLQueryLoader(file, network);

        // Suppress default error message
        loader.showErrorMessage = false;
        Collection<TAPNQuery> queries = loader.parseQueries();
	
        for(TAPNQuery query : queries){
            CreateGui.getCurrentTab().addQuery(query);

            // Remove successfully parsed queries from list
            for(QueryWrapper q : loader.faultyQueries){
                if(q.getName().equals(query.getName())){
                    loader.faultyQueries.remove(q);
                    break;
                }
            }
        }

        loader.createErrorDialog(loader);
    }

    private void createErrorDialog(XMLQueryLoader loader){
        /* Show error message indicating which queries could not be parsed.
         * Remaining queries in "faultyQueries" are not parsable.
         * Queries that references places not in the model, is found in
         * loader.parseQueries() method.
         */

        if(loader.faultyQueries.size() > 0){
            StringBuilder errorMessage = new StringBuilder();

            errorMessage.append("We can parse and import only the reachability cardinality and reachability deadlock XML queries.");
            
            errorMessage.append(System.lineSeparator());

            for(QueryWrapper q : loader.faultyQueries){
                errorMessage.append(System.lineSeparator());
                errorMessage.append(q.getNameAndException());
                if(!q.hasException() && loader.queryUsingNonexistentPlaceFound){
                    errorMessage.append("  Reason: place not found in model");
                } else if (!q.hasException() && loader.queryUsingNonexistentTransitionFound){
                    errorMessage.append("  Reason: transition not found in model");
                }
            }

            createDialogBox(errorMessage.toString(), "Error Parsing Queries");
        }
    }

    private void createDialogBox(String text, String header){
        JOptionPane.showMessageDialog(CreateGui.getApp(), text, 
            header, JOptionPane.ERROR_MESSAGE);
    }
}
