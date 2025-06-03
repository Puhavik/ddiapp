package com.puhavik.model;

public class DDIEntry {
    private final String drug1;
    private final String drug2;
    private final String interaction;
    private final String effect;
    private final String severity;
    private final String recommendation;

    public DDIEntry(String drug1, String drug2, String interaction, String effect, String severity, String recommendation) {
        this.drug1 = drug1;
        this.drug2 = drug2;
        this.interaction = interaction;
        this.effect = effect;
        this.severity = severity;
        this.recommendation = recommendation;
    }

    public String getDrug1() {
        return drug1;
    }

    public String getDrug2() {
        return drug2;
    }

    public String getEffect() {
        return effect;
    }

    public String getInteraction() {
        return interaction;
    }

    public String getSeverity() {
        return severity;
    }

    public String getRecommendation() {
        return recommendation;
    }
}
