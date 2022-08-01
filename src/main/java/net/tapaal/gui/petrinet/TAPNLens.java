package net.tapaal.gui.petrinet;

public final class TAPNLens {
    public static final TAPNLens Default = new TAPNLens(true, true, true);

    public boolean isTimed() {
        return timed;
    }

    public boolean isGame() {
        return game;
    }

    public boolean isColored() {
        return colored;
    }

    private final boolean timed;
    private final boolean game;
    private final boolean colored;

    public TAPNLens(boolean timed, boolean game, boolean colored) {
        this.timed = timed;
        this.game = game;
        this.colored = colored;
    }
}
