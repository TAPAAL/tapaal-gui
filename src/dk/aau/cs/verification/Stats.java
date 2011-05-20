package dk.aau.cs.verification;

public class Stats {
	private long discovered;
	private long explored;
	private long stored;
	
	public Stats(long discovered, long explored, long stored)
	{
		this.discovered = discovered;
		this.explored = explored;
		this.stored = stored;	
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Discovered markings: ");
		buffer.append(discovered);
		buffer.append(System.getProperty("line.separator"));
		
		buffer.append("Explored markings: ");
		buffer.append(explored);
		buffer.append(System.getProperty("line.separator"));
		
		buffer.append("Stored markings: ");
		buffer.append(stored);
		buffer.append(System.getProperty("line.separator"));
		buffer.append(System.getProperty("line.separator"));
		return buffer.toString();
	}
}
