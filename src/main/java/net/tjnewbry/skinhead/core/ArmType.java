package net.tjnewbry.skinhead.core;

public enum ArmType {
    CLASSIC("classic", 4),
    SLIM("slim", 3);

    private final String apiName;
    private final int pixels;

    ArmType(String apiName, int pixels) {
        this.apiName = apiName;
        this.pixels = pixels;
    }

    /** Value sent to the Mojang API. */
    public String apiName() {
        return apiName;
    }

    /** Arm width in pixels, for UI labels. */
    public int pixels() {
        return pixels;
    }

    public ArmType toggle() {
        return this == CLASSIC ? SLIM : CLASSIC;
    }
}
