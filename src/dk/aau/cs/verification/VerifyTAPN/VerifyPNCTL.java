package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.Messenger;
import pipe.gui.FileFinder;

public class VerifyPNCTL extends VerifyPN{
    public VerifyPNCTL(FileFinder fileFinder, Messenger messenger) {
        super(fileFinder, messenger);
    }

    @Override
    public String getStatsExplanation(){
        StringBuffer buffer = new StringBuffer("<html>");
        buffer.append("The number of configurations, markings and hyper-edges explored during<br />" +
                "the on-the-fly generation of the dependency graph for the given net and<br />" +
                "query before a conclusive answer was reached.");
        buffer.append("</html>");
        return buffer.toString();
    }
}
