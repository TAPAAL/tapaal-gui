package dk.aau.cs.model.tapn;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.NTA.trace.TraceToken;
import pipe.gui.TAPAALGUI;
import dk.aau.cs.model.tapn.simulation.FiringMode;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.TAPNComposer;

public class NetworkMarking implements TimedMarking {
	private final Map<TimedArcPetriNet, LocalTimedMarking> markings = new HashMap<TimedArcPetriNet, LocalTimedMarking>();
	private final Map<TimedPlace, List<TimedToken>> sharedPlacesTokens = new HashMap<TimedPlace, List<TimedToken>>();

	public NetworkMarking() {
	}

	public void addMarking(TimedArcPetriNet tapn, LocalTimedMarking marking) {
		Require.that(tapn != null, "tapn must not be null");
		Require.that(!markings.containsKey(tapn), "There is already a marking for that tapn");

		marking.setNetworkMarking(this);
		markings.put(tapn, marking);
	}
	
	public void removeMarkingFor(TimedArcPetriNet tapn) {
		Require.that(tapn != null, "tapn must be non-null");

		if (markings.containsKey(tapn)) {
		    for (TimedPlace place : tapn.places()) {
		        place.resetNumberOfTokens();
            }
			LocalTimedMarking marking = markings.remove(tapn);
			marking.setNetworkMarking(null);
		}
	}

    public void updateMarking(LocalTimedMarking newMarking, Map<TimedPlace, List<TimedToken>> sharedPlacesToTokensMap) {
        for (TimedPlace place : newMarking.getPlacesToTokensMap().keySet()) {
            TimedArcPetriNet tapn = ((LocalTimedPlace)place).model();
            markings.put(tapn, newMarking);
        }

        sharedPlacesTokens.clear();
        for (TimedPlace place : sharedPlacesToTokensMap.keySet()) {
            sharedPlacesTokens.put(place, sharedPlacesToTokensMap.get(place));
        }
    }
	
	public LocalTimedMarking getMarkingFor(TimedArcPetriNet tapn) {
		return markings.get(tapn);
	}

    public Map<TimedArcPetriNet, LocalTimedMarking> getMarkingMap() {
        return markings;
    }

    public Map<TimedPlace, List<TimedToken>> getSharedPlacesTokens() {
        return sharedPlacesTokens;
    }

	public NetworkMarking clone() {
		return delay(BigDecimal.ZERO);
	}

	public boolean isDelayPossible(BigDecimal delay) {
		for(Entry<TimedPlace, List<TimedToken>> entry: sharedPlacesTokens.entrySet()){
			if(TAPAALGUI.getCurrentTab().network().isSharedPlaceUsedInTemplates((SharedPlace)entry.getKey())){
				for(TimedToken token : entry.getValue()){
					TimeInvariant invariant = token.place().invariant();
					if (!invariant.isSatisfied(token.age().add(delay))) {
						return false;
					}
				}
			}
		}
		
		
		for(Entry<TimedArcPetriNet, LocalTimedMarking> pair : markings.entrySet()){
			if(pair.getKey().isActive()){
				if (!pair.getValue().isDelayPossible(delay))
					return false;
			}
		}
		return true;
	}
	
	public List<TimedPlace> getBlockingPlaces(BigDecimal delay){
		List<TimedPlace> result = new ArrayList<TimedPlace>();
		for(Entry<TimedPlace, List<TimedToken>> entry: sharedPlacesTokens.entrySet()){
			if(TAPAALGUI.getCurrentTab().network().isSharedPlaceUsedInTemplates((SharedPlace)entry.getKey())){
				for(TimedToken token : entry.getValue()){
					TimeInvariant invariant = token.place().invariant();
					if (!invariant.isSatisfied(token.age().add(delay))) {
						if(!result.contains(token.place())){
							result.add(token.place());
						}
					}
				}
			}
		}
		
		for(Entry<TimedArcPetriNet, LocalTimedMarking> pair : markings.entrySet()){
			if(pair.getKey().isActive()){
				result.addAll(pair.getValue().getBlockingPlaces(delay));
			}
		}

		return result;
	}
	
	public NetworkMarking delay(BigDecimal amount) {
		Require.that(amount != null, "Delay must not be null");
		Require.that(isDelayPossible(amount), "Delay breaks invariant.");

		NetworkMarking newMarking = new NetworkMarking();
		for(Entry<TimedPlace, List<TimedToken>> entry : sharedPlacesTokens.entrySet()){
			List<TimedToken> newTokens = new ArrayList<TimedToken>(entry.getValue().size());
			for(TimedToken token : entry.getValue()){
				newTokens.add(token.delay(amount));
			}
			newMarking.sharedPlacesTokens.put(entry.getKey(), newTokens);
		}
		
		for (Entry<TimedArcPetriNet, LocalTimedMarking> entry : markings.entrySet()) {
			if(entry.getKey().isActive()){
				newMarking.addMarking(entry.getKey(), entry.getValue().delay(amount));
			}
		}
		return newMarking;
	}

	public Tuple<NetworkMarking, List<TimedToken>> fireTransition(TimedTransition transition, FiringMode firingMode) {
		Require.that(transition != null, "transition cannot be null");
		Require.that(firingMode != null, "firingMode cannot be null");
		
		if(transition.isShared()) return fireSharedTransition(transition.sharedTransition(), firingMode);

		NetworkMarking clone = clone(); // TODO: try to conserve memory by reusing unchanged markings (they are immutable wrt. transition firing and delay)
		Tuple<LocalTimedMarking, List<TimedToken>> newMarking = clone.getMarkingFor(transition.model()).fireTransition(transition, firingMode);

		clone.removeMarkingFor(transition.model());
		clone.addMarking(transition.model(), newMarking.value1());
		
		return new Tuple<NetworkMarking, List<TimedToken>> (clone, newMarking.value2());
	}

	private Tuple<NetworkMarking, List<TimedToken>> fireSharedTransition(SharedTransition sharedTransition, FiringMode firingMode) {
		// validity of arguments already checked above
		NetworkMarking clone = clone();
		Tuple<LocalTimedMarking, List<TimedToken>> ltm;
		List<TimedToken> consumedTokens = new ArrayList<TimedToken>();

		for(TimedTransition transition : sharedTransition.transitions()){
			if(transition.model().isActive()) {
				ltm = clone.getMarkingFor(transition.model()).fireTransition(transition, firingMode);
				consumedTokens.addAll(ltm.value2());
				clone.removeMarkingFor(transition.model());
				clone.addMarking(transition.model(), ltm.value1());
			}
		}
		
		return new Tuple<NetworkMarking, List<TimedToken>> (clone, consumedTokens);
	}

	private NetworkMarking fireSharedTransition(SharedTransition sharedTransition, List<TimedToken> tokensToConsume) {
		HashMap<TimedTransition, List<TimedToken>> tokensPerTransition = distributeTokensToIndividualTransitions(sharedTransition, tokensToConsume);
		
		NetworkMarking clone = clone();
		for(TimedTransition transition : sharedTransition.transitions()){
			if(transition.model().isActive()) {
				LocalTimedMarking ltm = clone.getMarkingFor(transition.model()).fireTransition(transition, tokensPerTransition.get(transition));
				
				clone.removeMarkingFor(transition.model());
				clone.addMarking(transition.model(), ltm);
			}
		}
		
		return clone;
	}

	private HashMap<TimedTransition, List<TimedToken>> distributeTokensToIndividualTransitions(SharedTransition sharedTransition, List<TimedToken> tokensToConsume) {
		HashMap<TimedTransition, List<TimedToken>> distributedTokens = new HashMap<TimedTransition, List<TimedToken>>();
		
		for(TimedTransition transition : sharedTransition.transitions()){
			distributedTokens.put(transition, new ArrayList<TimedToken>());
		}
		
		for(TimedToken token : tokensToConsume){
			for(TimedTransition transition : sharedTransition.transitions()){
				if(transition.model().isActive()) {
					if(transition.model().equals(((LocalTimedPlace)token.place()).model())){
						distributedTokens.get(transition).add(token);
						break;
					}
				}
			}
		}
		
		return distributedTokens;
	}

	public NetworkMarking fireTransition(TimedTransition transition, List<TimedToken> tokensToConsume) {
		Require.that(transition != null, "transition cannot be null");
		Require.that(tokensToConsume != null, "Must specify a list of tokens");
		
		if(transition.isShared()) return fireSharedTransition(transition.sharedTransition(), tokensToConsume);
		
		NetworkMarking clone = clone(); // TODO: Try to conserve memory by reusing unchanged markings (they are immutable wrt. transition firing and delay)
										// cannot be done right now because the fireTransition call on local marking needs the reference setup properly to the new network marking
		LocalTimedMarking newMarking = clone.getMarkingFor(transition.model()).fireTransition(transition, tokensToConsume);

		clone.removeMarkingFor(transition.model());
		clone.addMarking(transition.model(), newMarking);

		return clone;
	}


	public int size() {
		int size = 0;
		for (LocalTimedMarking marking : markings.values()) {
			size += marking.size();
		}
		
		for(List<TimedToken> tokens : sharedPlacesTokens.values()){
			size += tokens.size();
		}
		return size;
	}

	public void add(TimedToken token) {
		if(token.place().isShared()){
			addTokenToSharedPlace(token);
		}else{
			getMarkingFor(((LocalTimedPlace)token.place()).model()).add(token); // TODO: ugly cast, but only way to get model?
		}
	}

	private void addTokenToSharedPlace(TimedToken token) {
		Require.that(token.place().isShared(), "Token must be located in a shared place");
		if(!sharedPlacesTokens.containsKey(token.place())){
			sharedPlacesTokens.put(token.place(), new ArrayList<TimedToken>());
		}

		sharedPlacesTokens.get(token.place()).add(token);
	}

	public List<TimedToken> getTokensFor(TimedPlace place){
		if(place.isShared()){
			if(!sharedPlacesTokens.containsKey(place)) return new ArrayList<TimedToken>();
			return sharedPlacesTokens.get(place);
		}else{
			LocalTimedPlace timedPlace = (LocalTimedPlace)place;
			return getMarkingFor(timedPlace.model()).getTokensFor(timedPlace);
		}
	}
	
	public void remove(TimedToken token) {
		TimedPlace place = token.place();
		if(place.isShared()){
			if(sharedPlacesTokens.containsKey(place)){
				List<TimedToken> tokens = sharedPlacesTokens.get(place); 
				if(!tokens.remove(token) && token instanceof TraceToken){
					for(TimedToken t : tokens){
						if(t.age().compareTo(token.age()) >= 0){
							tokens.remove(t);
							break;
						}
					}
				}
			}
		}else{
			getMarkingFor(((LocalTimedPlace)place).model()).remove(token); // TODO: this is ugly but only way to obtain the model?
		}
	}

	public void removePlaceFromMarking(TimedPlace place) {
		if(place.isShared()){
            sharedPlacesTokens.remove(place);
		}else{
			getMarkingFor(((LocalTimedPlace)place).model()).removePlaceFromMarking(place);	
		}
		
	}
	
	/**
	 * Removes all tokens from marking
	 */
	public void clear(){
		sharedPlacesTokens.clear();
		for(TimedArcPetriNet key : markings.keySet()){
			markings.get(key).clear();
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)	return true;
		if(!(obj instanceof NetworkMarking))	return false;
		NetworkMarking other = (NetworkMarking) obj;
		if(markings.size() != other.markings.size())	return false;
		for(TimedArcPetriNet key : markings.keySet()){
			if(other.markings.get(key) == null || !other.markings.get(key).equals(markings.get(key)))	return false;
		}
		if(sharedPlacesTokens.size() != other.sharedPlacesTokens.size())	return false;
		for(TimedPlace key : sharedPlacesTokens.keySet()){
			if(other.sharedPlacesTokens.get(key) == null)	return false;
			for(TimedToken t : other.sharedPlacesTokens.get(key)){
				if(!t.equals(sharedPlacesTokens.get(key)))	return false;
			}
		}
		return true;
	}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<TimedArcPetriNet, LocalTimedMarking> entry : markings.entrySet()) {
            sb.append(entry.getKey().name()).append(": ").append(entry.getValue().toString()).append("\n");
        }

        if (sharedPlacesTokens.isEmpty()) {
            return sb.toString();
        }
        
        sb.append("Shared Places:\n");
        for (Entry<TimedPlace, List<TimedToken>> entry : sharedPlacesTokens.entrySet()) {
            sb.append(entry.getKey().name()).append(" -> ");
            for (TimedToken token : entry.getValue()) {
                sb.append(token.toString()).append(" ");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public String toXmlStr(TAPNComposer composer) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document document = builder.newDocument();
            
            Element markingElement = toXmlElement(document, composer);
            document.appendChild(markingElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            return writer.getBuffer().toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }   
    }

    public Element toXmlElement(Document document, TAPNComposer composer) {
        Map<TimedPlace, List<TimedToken>> allPlaces = new HashMap<>();
        for (Entry<TimedArcPetriNet, LocalTimedMarking> entry : markings.entrySet()) {
            allPlaces.putAll(entry.getValue().getPlacesToTokensMap());
        }
    
        allPlaces.putAll(sharedPlacesTokens);

        Element markingElement = document.createElement("marking");
        for (Entry<TimedPlace, List<TimedToken>> entry : allPlaces.entrySet()) {
            Element placeElement = document.createElement("place");
            placeElement.setAttribute("id", composer.composedPlaceName(entry.getKey()));
            
            Map<Color, List<TimedToken>> colorToTokensMap = entry.getValue().stream()
                    .collect(Collectors.groupingBy(TimedToken::color));
            
            for (Map.Entry<Color, List<TimedToken>> colorEntry : colorToTokensMap.entrySet()) {
                Element tokenElement = document.createElement("token");
                tokenElement.setAttribute("count", String.valueOf(colorEntry.getValue().size()));
                
                Element colorElement = document.createElement("color");
                colorElement.setTextContent(colorEntry.getKey().toString());
                tokenElement.appendChild(colorElement);
                placeElement.appendChild(tokenElement);
            }

            markingElement.appendChild(placeElement);
        }

        return markingElement;
    }
}
