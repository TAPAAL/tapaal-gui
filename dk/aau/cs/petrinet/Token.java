package dk.aau.cs.petrinet;

public class Token {
	private Place place;
	
	public Token(Place place){
		this.place = place;
	}
	
	public Place getPlace(){
		return place;
	}
	
	public void setPlace(Place newPlace){
		this.place = newPlace;
	}
}
