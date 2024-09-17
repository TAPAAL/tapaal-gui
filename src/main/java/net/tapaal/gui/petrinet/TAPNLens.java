package net.tapaal.gui.petrinet;

public final class TAPNLens {
    public static final TAPNLens Default = new TAPNLens(true, true, true, false);

    public boolean isTimed() {
        return timed;
    }

    public boolean isGame() {
        return game;
    }

    public boolean isColored() {
        return colored;
    }

    public boolean isStochastic() { return stochastic; }

    public boolean canBeStochastic() {
        return timed && (!game); // && (!colored);
    }

    private final boolean timed;
    private final boolean game;
    private final boolean colored;
    private final boolean stochastic;

    public TAPNLens(boolean timed, boolean game, boolean colored, boolean stochastic) {
        this.timed = timed;
        this.game = game;
        this.colored = colored;
        this.stochastic = stochastic && canBeStochastic();
    }
}
