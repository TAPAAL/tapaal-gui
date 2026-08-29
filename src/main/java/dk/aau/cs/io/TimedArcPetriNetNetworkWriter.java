package dk.aau.cs.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import dk.aau.cs.TCTL.visitors.SMCQueryVisitor;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.model.tapn.TAPNQuery.QueryCategory;
import dk.aau.cs.TCTL.visitors.LTLQueryVisitor;
import dk.aau.cs.TCTL.visitors.HyperLTLQueryVisitor;
import net.tapaal.gui.petrinet.TAPNLens;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import net.tapaal.gui.petrinet.verification.TAPNQuery;
import net.tapaal.gui.petrinet.Template;
import net.tapaal.gui.petrinet.verification.InclusionPlaces.InclusionPlacesOption;
import dk.aau.cs.TCTL.visitors.CTLQueryVisitor;
import dk.aau.cs.util.Require;

public class TimedArcPetriNetNetworkWriter implements NetWriter {

	private final Iterable<TAPNQuery> queries;
    private final writeTACPN writeTACPN;
    private final TapnModelXmlWriter modelWriter;
    private final TapnTemplateXmlWriter templateWriter;
    private final TAPNLens lens;
	private boolean saveConstantNames;

    public TimedArcPetriNetNetworkWriter(
			TimedArcPetriNetNetwork network, 
			Iterable<Template> templates,
			Iterable<TAPNQuery> queries,
			Iterable<Constant> constants
    ) {
        this.queries = queries;
        writeTACPN = new writeTACPN(network);
		modelWriter = new TapnModelXmlWriter(network, constants, writeTACPN);
		templateWriter = new TapnTemplateXmlWriter(templates, TAPNLens.Default, writeTACPN);
		this.lens = TAPNLens.Default;
	}

    public TimedArcPetriNetNetworkWriter(
        TimedArcPetriNetNetwork network,
        Iterable<Template> templates,
        Iterable<TAPNQuery> queries,
        Iterable<Constant> constants,
        TAPNLens lens
    ) {
        this.queries = queries;
        writeTACPN = new writeTACPN(network);
        modelWriter = new TapnModelXmlWriter(network, constants, writeTACPN);
		templateWriter = new TapnTemplateXmlWriter(templates, lens, writeTACPN);
        this.lens = lens;
    }
	
	public ByteArrayOutputStream savePNML() throws ParserConfigurationException, DOMException, TransformerException {
		Document document = null;
		Transformer transformer = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		// Build a Petri Net XML Document
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		document = builder.newDocument();

		Element pnmlRootNode = document.createElement("pnml"); // PNML Top Level
		document.appendChild(pnmlRootNode);
		Attr pnmlAttr = document.createAttribute("xmlns"); // PNML "xmlns"
		pnmlAttr.setValue("http://www.informatik.hu-berlin.de/top/pnml/ptNetb");
		pnmlRootNode.setAttributeNode(pnmlAttr);

		modelWriter.appendNetworkData(
			document,
			pnmlRootNode,
			lens.isColored(),
			lens.isStochastic(),
			saveConstantNames
		);
		templateWriter.appendTemplates(document, pnmlRootNode, saveConstantNames);

        appendQueries(document, pnmlRootNode);
		appendFeature(document, pnmlRootNode);

		document.normalize();
		// Create Transformer with XSL Source File
		transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(document);

		StreamResult result = new StreamResult(os);
		transformer.transform(source, result);
		
		return os;
	}

	public void savePNML(File file) throws IOException, ParserConfigurationException, DOMException, TransformerException {
		savePNML(file, true);
	}

	public void savePNML(File file, boolean saveConstantNames) throws IOException, ParserConfigurationException, DOMException, TransformerException {
		this.saveConstantNames = saveConstantNames;

		Require.that(file != null, "Error: file to save to was null");
		
		try {
			ByteArrayOutputStream os = savePNML();
			FileOutputStream fs = new FileOutputStream(file);
			fs.write(os.toByteArray());
			fs.close();
		} catch (ParserConfigurationException e) {
			System.out
					.println("ParserConfigurationException thrown in savePNML() "
							+ ": dataLayerWriter Class : dataLayer Package: filename=\"");
		} catch (DOMException e) {
			System.out
					.println("DOMException thrown in savePNML() "
							+ ": dataLayerWriter Class : dataLayer Package: filename=\""
							+ file.getCanonicalPath() + "\" transformer=\"");
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			System.out
					.println("TransformerConfigurationException thrown in savePNML() "
							+ ": dataLayerWriter Class : dataLayer Package: filename=\""
							+ file.getCanonicalPath()
							+ "\" transformer=\"");
		} catch (TransformerException e) {
			System.out
					.println("TransformerException thrown in savePNML() : dataLayerWriter Class : dataLayer Package: filename=\""
							+ file.getCanonicalPath()
							+ "\"" + e);
		}
	}

    private void appendFeature(Document document, Element root) {
        String isTimed = "true";
        String isGame = "true";
        String isColored = "true";
        String isStochastic = "true";
        if (!lens.isTimed()) {
            isTimed = "false";
        }
        if (!lens.isGame()) {
            isGame = "false";
        }
        if(!lens.isColored()){
            isColored = "false";
        }
        if(!lens.isStochastic()) {
            isStochastic = "false";
        }
        root.appendChild(createFeatureElement(isTimed, isGame, isColored, isStochastic, document));
    }

    private Element createFeatureElement(String isTimed, String isGame, String isColored, String isStochastic, Document document) {
        Require.that(document != null, "Error: document was null");
        Element feature = document.createElement("feature");

        feature.setAttribute("isTimed", isTimed);
        feature.setAttribute("isGame", isGame);
        feature.setAttribute("isColored", isColored);
        feature.setAttribute("isStochastic", isStochastic);

        return feature;
    }

	private void appendQueries(Document document, Element root) {
		for (TAPNQuery query : queries) {
			Element newQuery;
			if (query.getCategory() == QueryCategory.LTL){
                newQuery = createLTLQueryElement(query, document);
            } else if(query.getCategory() == QueryCategory.HyperLTL) {
                newQuery = createHyperLTLQueryElement(query, document);
            } else if(query.getCategory() == QueryCategory.SMC) {
                newQuery = createSMCQueryElement(query, document);
            }else {
				newQuery = createCTLQueryElement(query, document);
			}
			root.appendChild(newQuery);
		}
	}
	
	private Element createCTLQueryElement(TAPNQuery query, Document document) {
		Require.that(query != null, "Error: query was null");
		Require.that(document != null, "Error: document was null");

		Element queryElement = document.createElement("query");

		CTLQueryVisitor ctlQueryVisitor = new CTLQueryVisitor();
		ctlQueryVisitor.buildXMLQuery(query.getProperty(), query.getName(), false);

		Node queryFormula = XMLQueryStringToElement(ctlQueryVisitor.getXMLQuery().toString());
		queryElement.appendChild(document.importNode(queryFormula, true));
		
		queryElement.setAttribute("name", query.getName());
		queryElement.setAttribute("type", query.getCategory().toString());
		queryElement.setAttribute("capacity", "" + query.getCapacity());
		queryElement.setAttribute("traceOption", ""	+ query.getTraceOption());
		queryElement.setAttribute("searchOption", "" + query.getSearchOption());
		queryElement.setAttribute("hashTableSize", "" + query.getHashTableSize());
		queryElement.setAttribute("extrapolationOption", "" + query.getExtrapolationOption());
        queryElement.setAttribute("reductionOption", ""	+ query.getReductionOption());
        queryElement.setAttribute("coloredReduction", ""	+ query.useColoredReduction());
		queryElement.setAttribute("symmetry", "" + query.useSymmetry());
		queryElement.setAttribute("gcd", "" + query.useGCD());
		queryElement.setAttribute("timeDarts", "" + query.useTimeDarts());
		queryElement.setAttribute("pTrie", "" + query.usePTrie());
		queryElement.setAttribute("discreteInclusion", String.valueOf(query.discreteInclusion()));
		queryElement.setAttribute("active", "" + query.isActive());
		queryElement.setAttribute("inclusionPlaces", getInclusionPlacesString(query));
		queryElement.setAttribute("overApproximation", "" + query.useOverApproximation());
		queryElement.setAttribute("reduction", "" + query.useReduction());
		queryElement.setAttribute("enableOverApproximation", "" + query.isOverApproximationEnabled());
		queryElement.setAttribute("enableUnderApproximation", "" + query.isUnderApproximationEnabled());
		queryElement.setAttribute("approximationDenominator", "" + query.approximationDenominator());
		queryElement.setAttribute("algorithmOption", "" + query.getAlgorithmOption());
		queryElement.setAttribute("useSiphonTrapAnalysis", "" + query.isSiphontrapEnabled());
		queryElement.setAttribute("useQueryReduction", "" + query.isQueryReductionEnabled());
		queryElement.setAttribute("useStubbornReduction", "" + query.isStubbornReductionEnabled());
		queryElement.setAttribute("useTarOption", "" + query.isTarOptionEnabled());
        queryElement.setAttribute("useTarjan", "" + query.isTarjan());
		queryElement.setAttribute("partitioning", "" + query.usePartitioning());
		queryElement.setAttribute("colorFixpoint", "" + query.useColorFixpoint());
        queryElement.setAttribute("symmetricVars", "" + query.useSymmetricVars());
        queryElement.setAttribute("useExplicitSearch" , "" + query.useExplicitSearch());

        return queryElement;
	}

    private Element createHyperLTLQueryElement(TAPNQuery query, Document document) {
        Require.that(query != null, "Error: query was null");
        Require.that(document != null, "Error: document was null");

        Element queryElement = document.createElement("query");

        Node queryFormula = XMLQueryStringToElement(new HyperLTLQueryVisitor().getXMLQueryFor(query.getProperty(), query.getName()));
        queryElement.appendChild(document.importNode(queryFormula, true));

        String traces = String.join(",", query.getTraceList());

        queryElement.setAttribute("name", query.getName());
        queryElement.setAttribute("type", query.getCategory().toString());
        queryElement.setAttribute("traces", traces);
        queryElement.setAttribute("capacity", "" + query.getCapacity());
        queryElement.setAttribute("traceOption", ""	+ query.getTraceOption());
        queryElement.setAttribute("searchOption", "" + query.getSearchOption());
        queryElement.setAttribute("hashTableSize", "" + query.getHashTableSize());
        queryElement.setAttribute("extrapolationOption", "" + query.getExtrapolationOption());
        queryElement.setAttribute("reductionOption", ""	+ query.getReductionOption());
        queryElement.setAttribute("symmetry", "" + query.useSymmetry());
        queryElement.setAttribute("gcd", "" + query.useGCD());
        queryElement.setAttribute("timeDarts", "" + query.useTimeDarts());
        queryElement.setAttribute("pTrie", "" + query.usePTrie());
        queryElement.setAttribute("discreteInclusion", String.valueOf(query.discreteInclusion()));
        queryElement.setAttribute("active", "" + query.isActive());
        queryElement.setAttribute("inclusionPlaces", getInclusionPlacesString(query));
        queryElement.setAttribute("overApproximation", "" + query.useOverApproximation());
        queryElement.setAttribute("reduction", "" + query.useReduction());
        queryElement.setAttribute("enableOverApproximation", "" + query.isOverApproximationEnabled());
        queryElement.setAttribute("enableUnderApproximation", "" + query.isUnderApproximationEnabled());
        queryElement.setAttribute("approximationDenominator", "" + query.approximationDenominator());
        queryElement.setAttribute("algorithmOption", "" + query.getAlgorithmOption());
        queryElement.setAttribute("useSiphonTrapAnalysis", "" + query.isSiphontrapEnabled());
        queryElement.setAttribute("useQueryReduction", "" + query.isQueryReductionEnabled());
        queryElement.setAttribute("useStubbornReduction", "" + query.isStubbornReductionEnabled());
        queryElement.setAttribute("useTarOption", "" + query.isTarOptionEnabled());
        queryElement.setAttribute("useTarjan", "" + query.isTarjan());

        return queryElement;
    }

    private Element createLTLQueryElement(TAPNQuery query, Document document) {
        Require.that(query != null, "Error: query was null");
        Require.that(document != null, "Error: document was null");

        Element queryElement = document.createElement("query");

        LTLQueryVisitor ltlQueryVisitor = new LTLQueryVisitor();
        ltlQueryVisitor.buildXMLQuery(query.getProperty(), query.getName());

        Node queryFormula = XMLQueryStringToElement(ltlQueryVisitor.getXMLQuery().toString());
        queryElement.appendChild(document.importNode(queryFormula, true));

        queryElement.setAttribute("name", query.getName());
        queryElement.setAttribute("type", query.getCategory().toString());
        queryElement.setAttribute("capacity", "" + query.getCapacity());
        queryElement.setAttribute("traceOption", ""	+ query.getTraceOption());
        queryElement.setAttribute("searchOption", "" + query.getSearchOption());
        queryElement.setAttribute("hashTableSize", "" + query.getHashTableSize());
        queryElement.setAttribute("extrapolationOption", "" + query.getExtrapolationOption());
        queryElement.setAttribute("reductionOption", ""	+ query.getReductionOption());
        queryElement.setAttribute("symmetry", "" + query.useSymmetry());
        queryElement.setAttribute("gcd", "" + query.useGCD());
        queryElement.setAttribute("timeDarts", "" + query.useTimeDarts());
        queryElement.setAttribute("pTrie", "" + query.usePTrie());
        queryElement.setAttribute("discreteInclusion", String.valueOf(query.discreteInclusion()));
        queryElement.setAttribute("active", "" + query.isActive());
        queryElement.setAttribute("inclusionPlaces", getInclusionPlacesString(query));
        queryElement.setAttribute("overApproximation", "" + query.useOverApproximation());
        queryElement.setAttribute("reduction", "" + query.useReduction());
        queryElement.setAttribute("enableOverApproximation", "" + query.isOverApproximationEnabled());
        queryElement.setAttribute("enableUnderApproximation", "" + query.isUnderApproximationEnabled());
        queryElement.setAttribute("approximationDenominator", "" + query.approximationDenominator());
        queryElement.setAttribute("algorithmOption", "" + query.getAlgorithmOption());
        queryElement.setAttribute("useSiphonTrapAnalysis", "" + query.isSiphontrapEnabled());
        queryElement.setAttribute("useQueryReduction", "" + query.isQueryReductionEnabled());
        queryElement.setAttribute("useStubbornReduction", "" + query.isStubbornReductionEnabled());
        queryElement.setAttribute("useTarOption", "" + query.isTarOptionEnabled());
        queryElement.setAttribute("useTarjan", "" + query.isTarjan());

        return queryElement;
    }

    private Element createSMCQueryElement(TAPNQuery query, Document document) {
        Require.that(query != null, "Error: query was null");
        Require.that(document != null, "Error: document was null");

        Element queryElement = document.createElement("query");
        SMCQueryVisitor smcQueryVisitor = new SMCQueryVisitor();
        smcQueryVisitor.buildXMLQuery(query.getProperty(), query.getName(), query.getSmcSettings());

        try {
            Element doc = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(smcQueryVisitor.getXMLQuery().toString().getBytes()))
                .getDocumentElement();
            Node smcTag = doc.getElementsByTagName("smc").item(0);
            Node formula = doc.getElementsByTagName("formula").item(0);
            Node observations = doc.getElementsByTagName("observations").item(0);
            queryElement.appendChild(document.importNode(smcTag, true));
            if(observations != null) {
				queryElement.appendChild(document.importNode(observations, true));
			}
            queryElement.appendChild(document.importNode(formula, true));
        } catch (SAXException | ParserConfigurationException | IOException e) {
            System.out.println(e + " thrown in savePNML() "
                + ": dataLayerWriter Class : dataLayer Package: filename=\"");
            return null;
        }

        queryElement.setAttribute("name", query.getName());
        queryElement.setAttribute("type", query.getCategory().toString());
        queryElement.setAttribute("capacity", "" + query.getCapacity());
        queryElement.setAttribute("traceOption", ""	+ query.getTraceOption());
        queryElement.setAttribute("reductionOption", ""	+ query.getReductionOption());
        queryElement.setAttribute("active", "" + query.isActive());
        queryElement.setAttribute("algorithmOption", "" + query.getAlgorithmOption());
        queryElement.setAttribute("timeDarts", "" + query.useTimeDarts());
        queryElement.setAttribute("gcd", "" + query.useGCD());
        queryElement.setAttribute("parallel", "" + query.isParallel());
        queryElement.setAttribute("overApproximation", ""	+ false);
        queryElement.setAttribute("verificationType", query.getVerificationType().toString());
        queryElement.setAttribute("numberOfTraces", "" + query.getNumberOfTraces());
        queryElement.setAttribute("smcTraceType", query.getSmcTraceType().toString());

        query.getSmcSettings().getSmcSeed().ifPresent(seed -> queryElement.setAttribute("smcSeed", Long.toUnsignedString(seed)));

        return queryElement;
    }
	
	private Node XMLQueryStringToElement(String formulaString){
		
		try {
			return DocumentBuilderFactory
			    .newInstance()
			    .newDocumentBuilder()
			    .parse(new ByteArrayInputStream(formulaString.getBytes()))
			    .getDocumentElement().getElementsByTagName("formula").item(0);
		} catch (SAXException | ParserConfigurationException | IOException e) {
			System.out.println(e + " thrown in savePNML() "
					+ ": dataLayerWriter Class : dataLayer Package: filename=\"");
		}

        return null;
	}

	private String getInclusionPlacesString(TAPNQuery query) {
		if(!query.discreteInclusion() || (query.inclusionPlaces().inclusionOption() == InclusionPlacesOption.UserSpecified && query.inclusionPlaces().inclusionPlaces().isEmpty()))
			return "*NONE*";
		
		if(query.inclusionPlaces().inclusionOption() == InclusionPlacesOption.AllPlaces)
			return "*ALL*";
		
		boolean first = true;
		StringBuilder s = new StringBuilder();
		for(TimedPlace p : query.inclusionPlaces().inclusionPlaces()) {
			if(!first) s.append(',');
			
			s.append(p.toString());
			if(first) first = false;
		}
		
		return s.toString();
	}
















}
