package dk.aau.cs.io.queries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

import dk.aau.cs.TCTL.XMLParsing.XMLLTLQueryParser;
import dk.aau.cs.io.LoadedQueries;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.HashTableSize;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.CreateGui;
import pipe.gui.widgets.InclusionPlaces;
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

    private final File file;
    private final ArrayList<QueryWrapper> faultyQueries = new ArrayList<QueryWrapper>();

    public XMLQueryLoader(File file, TimedArcPetriNetNetwork network){
        super(network);
        this.file = file;
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
        } catch (SAXException | IllegalArgumentException | IOException e){
            Logger.log(e);
            createDialogBox(e.getMessage(), "Parse Exception");
            return queries;
        }

        // Get all properties from DOM
        NodeList propList = doc.getElementsByTagName("property");
        int choice = -1;

        for(int i = 0; i < propList.getLength(); i++){
            Node prop = propList.item(i);
            QueryWrapper queryWrapper = new QueryWrapper();

            // Save query for later use in dialog window
            this.faultyQueries.add(queryWrapper);

            boolean canBeCTL = canBeCTL(prop);
            boolean canBeLTL = canBeLTL(prop);

            if (canBeCTL && canBeLTL && choice == -1) {
                choice = JOptionPane.showOptionDialog(CreateGui.getApp(),
                    "There were some queries that can be classified both as LTL and CTL. \nHow do you want to import them?",
                    "Choose query category",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{"Import all as CTL", "Import all as LTL", "Cancel"},
                    0);
            } else if (!canBeCTL && !canBeLTL) {
                JOptionPane.showMessageDialog(CreateGui.getApp(),
                    "One or more queries do not have the correct format.");
            }
                if (choice == 2) return null;


            boolean isCTL = (canBeCTL && !canBeLTL) || (canBeCTL && canBeLTL && choice == 0);
            boolean isLTL = (!canBeCTL && canBeLTL) || (canBeCTL && canBeLTL && choice == 1 );


            // Update queryWrapper name and property
            if (isCTL) {
                if (!XMLCTLQueryParser.parse(prop, queryWrapper)) {
                    queries.add(null);
                    continue;
                }
            } else if (isLTL) {
                if (!XMLLTLQueryParser.parse(prop, queryWrapper)) {
                    queries.add(null);
                    continue;
                }
            }

            // The number 9999 is the number of extra tokens allowed,
            // this is set high s.t. we don't have to change it manually
            TAPNQuery query = new TAPNQuery(queryWrapper.getName(), 9999,
                queryWrapper.getProp(),TraceOption.NONE, SearchOption.HEURISTIC, 
                ReductionOption.VerifyPN, true, false, true, true, true, true, 
                HashTableSize.MB_16, ExtrapolationOption.AUTOMATIC, new InclusionPlaces());

            RenameTemplateVisitor rt = new RenameTemplateVisitor("", 
                network.activeTemplates().get(0).name());

            query.setCategory(TAPNQueryLoader.detectCategory(queryWrapper.getProp(), isCTL, isLTL));
            
            if(query.getCategory() == TAPNQuery.QueryCategory.CTL || query.getCategory() == TAPNQuery.QueryCategory.LTL){
            	query.setSearchOption(SearchOption.DFS);
            	query.setUseReduction(true);
            }
            
            query.getProperty().accept(rt, null);
                    
            queries.add(query);
        }

        return queries;
    }

    public static boolean canBeCTL(Node prop) {
        NodeList children = prop.getChildNodes();
        boolean correctQuantifiers = false;

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("formula")) {
                correctQuantifiers = checkQuantifiers(child);
            }
        }
        return correctQuantifiers;
    }

    private static boolean checkQuantifiers(Node prop) {
        NodeList children = prop.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("finally") || child.getNodeName().equals("globally") ||
                child.getNodeName().equals("next") || child.getNodeName().equals("until")) {
                Node parent = child.getParentNode();
                if (parent == null || !(parent.getNodeName().equals("all-paths") || parent.getNodeName().equals("exists-path"))) {
                    return false;
                }
            }
            if (!checkQuantifiers(child)) return false;
        }
        return true;
    }

    public static boolean canBeLTL(Node prop) {
        NodeList children = prop.getChildNodes();
        int allPathsCounter = 0;

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("formula")) {
                allPathsCounter += countAllPaths(child);
            }
        }
        return allPathsCounter == 1;
    }

    private static int countAllPaths(Node prop) {
        NodeList children = prop.getChildNodes();
        int allPathsCounter = 0;

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("all-paths")) {
                allPathsCounter++;
            } else if (child.getNodeName().equals("exists-path") || child.getNodeName().equals("deadlock")){
                return 100;
            }
            allPathsCounter += countAllPaths(child);
        }
        return allPathsCounter;
    }

    public static void importQueries(File file, TimedArcPetriNetNetwork network){
        XMLQueryLoader loader = new XMLQueryLoader(file, network);

        // Suppress default error message
        loader.showErrorMessage = false;
        LoadedQueries loadedQueries = loader.parseQueries();
        if (loadedQueries == null) return;
	
        for(TAPNQuery query : loadedQueries.getQueries()){
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
