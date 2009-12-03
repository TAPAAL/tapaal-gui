package dk.aau.cs.petrinet;

public class Token {
	private TAPNPlace place;
	
	public Token(TAPNPlace place){
		this.place = place;
	}
	
	public TAPNPlace getPlace(){
		return place;
	}
	
	public void setPlace(TAPNPlace newPlace){
		this.place = newPlace;
	}
}
