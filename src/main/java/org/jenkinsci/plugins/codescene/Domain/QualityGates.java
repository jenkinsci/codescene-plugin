package org.jenkinsci.plugins.codescene.Domain;

import javax.json.JsonObject;

public class QualityGates {
    private boolean goalHasFailed;
    private boolean codeHealthDeclined;
    private boolean gatesEnabled;

    public QualityGates(final JsonObject gatesField, final Configuration userConfig) {
        if (null == gatesField) {
            throw new IllegalArgumentException("The quality gates field cannot be null. This should be checked in the calling context");
        }

        goalHasFailed = userConfig.failOnFailedGoal() && gatesField.getBoolean("degrades-in-code-health");
        codeHealthDeclined = userConfig.failOnDecliningCodeHealth() && gatesField.getBoolean("violates-goal");
        gatesEnabled = userConfig.failOnFailedGoal() || userConfig.failOnDecliningCodeHealth();
    }

    private QualityGates(boolean enableFailedGoalGate, boolean enableCodeHealthGate) {
        this.goalHasFailed = enableFailedGoalGate;
        this.codeHealthDeclined = enableCodeHealthGate;
    }

    public static QualityGates none() {
        return new QualityGates(false, false);
    }

    public boolean goalHasFailed() {
        return goalHasFailed;
    }

    public boolean codeHealthDeclined() {
        return codeHealthDeclined;
    }

    public boolean enabled() { return gatesEnabled; }
}
