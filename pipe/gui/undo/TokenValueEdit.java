package pipe.gui.undo;

import dk.aau.cs.gui.undo.Command;
import pipe.dataLayer.colors.ColoredToken;
import pipe.dataLayer.colors.IntOrConstant;

public class TokenValueEdit extends Command {

	private IntOrConstant oldValue;
	private IntOrConstant newValue;
	private ColoredToken token;
	
	public TokenValueEdit(ColoredToken token, IntOrConstant oldValue, IntOrConstant newValue){
		this.token = token;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	@Override
	public void redo() {
		token.setColor(newValue);
	}

	@Override
	public void undo() {
		token.setColor(oldValue);
	}

}
