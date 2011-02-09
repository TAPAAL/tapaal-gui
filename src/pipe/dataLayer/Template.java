package pipe.dataLayer;

import dk.aau.cs.util.Require;

public class Template<TNet> {
	private TNet net;
	private DataLayer guiModel;

	public Template(TNet net, DataLayer guiModel) {
		this.net = net;
		this.guiModel = guiModel;
	}

	@Override
	public String toString() {
		return net.toString();
	}

	public DataLayer guiModel() {
		return guiModel;
	}

	public TNet model() {
		return net;
	}

	public void setGuiModel(DataLayer guiModel) {
		Require.that(guiModel != null, "GuiModel cannot be null");
		this.guiModel = guiModel;
	}
}
