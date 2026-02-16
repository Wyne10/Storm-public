package org.bigcraft.storm.api;

public enum EffectSource {
    PURCHASE("purchase"),
    APPLY("apply");

    private final String source;

    EffectSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }
}
