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
            doc = db.parse(file);
        } catch(ParserConfigurationException e){
            Logger.log(e);
            createDialogBox(e.getMessage(), "Parsing Exception");
            return queries;
        } catch (SAXException e){
            Logger.log(e);
            createDialogBox(e.getMessage(), "Parsing Exception");
            return queries;
        } catch (IOException e){
            Logger.log(e);
            createDialogBox(e.getMessage(), "Parsing Exception");
            return queries;
        }

        // Get all properties from DOM
        NodeList propList = doc.getElementsByTagName("property");

        for(int i = 0; i < propList.getLength(); i++){
            Node prop = propList.item(i);
            QueryWrapper queryWrapper = new QueryWrapper();

            // Save query for later use in dialog window
            addQueryToList(queryWrapper);

            // Update queryWrapper name and property
            if(!XMLQueryParser.parse(prop, queryWrapper)){
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

            if(loader.faultyQueries.size() == 1){
                errorMessage.append("The following query could not be imported:");
            } else{
                errorMessage.append("The following queries could not be imported:");
            }
            
            errorMessage.append(System.lineSeparator());

            for(QueryWrapper q : loader.faultyQueries){
                errorMessage.append(System.lineSeparator());
                errorMessage.append(q.getNameAndException());
                if(!q.hasException()){
                    errorMessage.append("  Reason: place not found in model");
                }
            }

            createDialogBox(errorMessage.toString(), "Error Parsing Queries");
        }
    }

    private void createDialogBox(String text, String header){
	    JOptionPane.showMessageDialog(CreateGui.getApp(), text, 
                header, JOptionPane.ERROR_MESSAGE);
    }

    private void addQueryToList(QueryWrapper queryWrapper){
        this.faultyQueries.add(queryWrapper);
    }
}
