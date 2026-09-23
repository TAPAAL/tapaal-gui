package net.tapaal.gui.petrinet.undo;

import net.tapaal.gui.petrinet.verification.TAPNQuery;
import net.tapaal.gui.petrinet.widgets.QueryPane;

public class EditQueryCommand implements Command {
    private final QueryPane queryPane;
    private final TAPNQuery oldQuery;
    private final TAPNQuery newQuery;
    private final int index;

    public EditQueryCommand(QueryPane queryPane, TAPNQuery oldQuery, TAPNQuery newQuery, int index) {
        this.queryPane = queryPane;
        this.oldQuery = oldQuery;
        this.newQuery = newQuery;
        this.newQuery.setActive(oldQuery.isActive());
        this.index = index;
    }

    @Override
    public void undo() {
        queryPane.replaceQuery(index, oldQuery);
    }

    @Override
    public void redo() {
        queryPane.replaceQuery(index, newQuery);
    }
}
