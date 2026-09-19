package me.wyne.storm.api;

/**
 * Identifies where a {@link StormEffectManager#setEffect} call originated from. Recorded in the
 * player's effect history and reported on {@link me.wyne.storm.api.event.StormEffectApplyEvent}.
 */
public enum EffectSource {
    /** The effect was granted through a purchase. */
    PURCHASE("purchase"),
    /** The effect was applied directly, outside of a purchase. */
    APPLY("apply");

    private final String source;

    EffectSource(String source) {
        this.source = source;
    }

    /**
     * Returns the machine-readable name of this source, as stored in the effect history.
     */
    public String getSource() {
        return source;
    }
}
