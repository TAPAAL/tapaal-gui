package pipe.gui;

public interface ModelChecker {
	boolean setup();
	String getVersion();
	boolean isCorrectVersion();
	String getPath(); // TODO: MJ -- Delete me when refactoring is done
}
