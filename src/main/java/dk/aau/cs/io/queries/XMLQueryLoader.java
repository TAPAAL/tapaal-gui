package dk.aau.cs.io.queries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import dk.aau.cs.TCTL.XMLParsing.XMLHyperLTLQueryParser;
import dk.aau.cs.TCTL.XMLParsing.XMLLTLQueryParser;
import dk.aau.cs.io.LoadedQueries;
import dk.aau.cs.verification.SMCSettings;
import net.tapaal.gui.petrinet.TAPNLens;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import net.tapaal.gui.petrinet.verification.TAPNQuery.ExtrapolationOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.HashTableSize;
import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;
import org.w3c.dom.Element;
import pipe.gui.TAPAALGUI;
import net.tapaal.gui.petrinet.verification.InclusionPlaces;
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
import pipe.gui.petrinet.PetriNetTab;

public class XMLQueryLoader extends QueryLoader{

    private final File file;
    private final List<TAPNQuery.QueryCategory> queryCategories;
    private final ArrayList<QueryWrapper> faultyQueries = new ArrayList<QueryWrapper>();

    public XMLQueryLoader(File file, TimedArcPetriNetNetwork network){
        this(file, network, null);
    }

    public XMLQueryLoader(File file, TimedArcPetriNetNetwork network, List<TAPNQuery.QueryCategory> queryCategories){
        super(network);
        this.file = file;
        this.queryCategories = queryCategories;
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

        for (int i = 0; i < propList.getLength(); i++) {
            Node prop = propList.item(i);
            QueryWrapper queryWrapper = new QueryWrapper();

            // Save query for later use in dialog window
            this.faultyQueries.add(queryWrapper);

            TAPNLens lens = TAPAALGUI.getCurrentTab().getLens();
            boolean isTimed = (lens != null && lens.isTimed()) || network.isTimed();
            boolean isKnownGame = (lens != null && lens.isGame()); // XXX: This is a hack, not sure why network does not know if it a game, also control tag should used to check if query is a game
            boolean isStochastic = (lens != null && lens.isStochastic());
            boolean canBeCTL = isTimed || canBeCTL(prop);
            boolean canBeLTL = !isTimed && !isKnownGame && canBeLTL(prop);
            boolean canBeHyperLTL = !isTimed && !isKnownGame && canBeHyperLTL(prop);
            boolean hasSmcTag = TAPNQueryLoader.hasSmcTag(prop);
            boolean isSmc = isStochastic && hasSmcTag;

            int counter = 0;
            if (canBeCTL) counter += 1;
            if (canBeLTL) counter += 1;
            if (canBeHyperLTL) counter += 1;

            if (counter > 1 && choice == -1 && queryCategories == null && !isSmc) {
                choice = JOptionPane.showOptionDialog(TAPAALGUI.getApp(),
                    "There were some queries that can be classified as CTL, LTL or HyperLTL. \nHow do you want to import them?",
                    "Choose query category",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{"Import all as CTL", "Import all as LTL", "Import all as HyperLTL", "Cancel"},
                    0);
            } else if (!canBeCTL && !canBeLTL && !canBeHyperLTL) {
                JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
                    "One or more queries do not have the correct format.");
            }
            if (choice == 3) return null;

            boolean isCTL = (canBeCTL && counter == 1) || (counter > 1 && choice == 0);
            boolean isLTL = (canBeLTL && counter == 1) || (counter > 1 && choice == 1);
            boolean isHyperLTL = (canBeHyperLTL && counter == 1) || (counter > 1  && choice == 2);

            if (queryCategories != null && queryCategories.size() > i) {
                if (queryCategories.get(i) == TAPNQuery.QueryCategory.CTL) {
                    isCTL = true;
                } else if (queryCategories.get(i) == TAPNQuery.QueryCategory.LTL) {
                    isLTL = true;
                } else if (queryCategories.get(i) == TAPNQuery.QueryCategory.HyperLTL) {
                    isHyperLTL = true;
                } else if (queryCategories.get(i) == TAPNQuery.QueryCategory.SMC) {
                    isSmc = true;
                }
            }

            // Update queryWrapper name and property
            if (isCTL && !isSmc) {
                if (!XMLCTLQueryParser.parse(prop, queryWrapper)) {
                    queries.add(null);
                    continue;
                }
            } else if (isLTL) {
                if (!XMLLTLQueryParser.parse(prop, queryWrapper)) {
                    queries.add(null);
                    continue;
                }
            } else if (isHyperLTL) {
                if (!XMLHyperLTLQueryParser.parse(prop, queryWrapper)) {
                    queries.add(null);
                    continue;
                }
            } else if (isSmc) {
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
                HashTableSize.MB_16, ExtrapolationOption.AUTOMATIC, new InclusionPlaces(), network.isColored());

            RenameTemplateVisitor rt = new RenameTemplateVisitor("", 
                network.activeTemplates().get(0).name());

            query.setCategory(TAPNQueryLoader.detectCategory(queryWrapper.getProp(), isCTL, isLTL, isHyperLTL, isSmc));
            
            if(query.getCategory() == TAPNQuery.QueryCategory.CTL || query.getCategory() == TAPNQuery.QueryCategory.LTL){
            	query.setSearchOption(SearchOption.DFS);
            	query.setUseReduction(true);
            } else if(query.getCategory() == TAPNQuery.QueryCategory.HyperLTL) {
                query.setSearchOption(SearchOption.DFS);
                query.setTraceList(queryWrapper.getTraceList());
                query.setUseStubbornReduction(false);
                query.setUseReduction(false);
            } else if(query.getCategory() == TAPNQuery.QueryCategory.SMC) {
                query.setReductionOption(ReductionOption.VerifyDTAPN);
                SMCSettings smcSettings = SMCSettings.Default();
                if(hasSmcTag) {
                    Element smcTag = (Element) ((Element) prop).getElementsByTagName("smc").item(0);
                    smcSettings = TAPNQueryLoader.parseSmcSettings(smcTag);
                }
                query.setSmcSettings(smcSettings);
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

    public static boolean canBeHyperLTL(Node prop) {
        NodeList children = prop.getChildNodes();
        int hyperLTLNodesCount = 0;

        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            hyperLTLNodesCount += countHyperLTLNodes(child);

            if (hyperLTLNodesCount > 0){
                return true;
            }
        }

        return hyperLTLNodesCount > 0;
    }

    public static int countHyperLTLNodes(Node prop) {
        NodeList children = prop.getChildNodes();
        int count = 0;
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if(child.getNodeName().equals("path-scope")) {
                return 1;
            }

            if(child.getNodeName().equals("exists-path") || child.getNodeName().equals("all-paths")) {
                if(child.getAttributes().getLength() > 0) {
                    return 1;
                }
            }
            count += countHyperLTLNodes(child);
        }
        return count;
    }

    public static boolean canBeLTL(Node prop) {
        NodeList children = prop.getChildNodes();
        int counter = 0;

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("formula")) {
                counter += countSupportedPaths(child);
            }
        }
        return counter == 1;
    }

    private static int countSupportedPaths(Node prop) {
        NodeList children = prop.getChildNodes();
        int counter = 0;

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("all-paths") || child.getNodeName().equals("exists-path")) {
                counter++;
            } else if (child.getNodeName().equals("deadlock")){
                return 100;
            }
            counter += countSupportedPaths(child);
        }
        return counter;
    }

    public static void importQueries(File file, TimedArcPetriNetNetwork network, PetriNetTab tab){
        XMLQueryLoader loader = new XMLQueryLoader(file, network);

        // Suppress default error message
        loader.showErrorMessage = false;
        LoadedQueries loadedQueries = loader.parseQueries();
        if (loadedQueries == null) return;
	
        for(TAPNQuery query : loadedQueries.getQueries()){
            tab.addQuery(query);

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
        JOptionPane.showMessageDialog(TAPAALGUI.getApp(), text,
            header, JOptionPane.ERROR_MESSAGE);
    }
}
