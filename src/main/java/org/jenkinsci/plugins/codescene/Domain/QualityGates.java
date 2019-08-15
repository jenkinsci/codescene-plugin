package org.jenkinsci.plugins.codescene.Domain;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class QualityGates {
    private boolean goalHasFailed;
    private boolean codeHealthDeclined;
    private boolean gatesEnabled;
    private String codeOwners;

    public QualityGates(final JsonObject deltaResult, final Configuration userConfig) {
        final JsonObject gatesField = deltaResult.getJsonObject("quality-gates");
        if (null == gatesField) {
            throw new IllegalArgumentException("The quality gates field cannot be null. This should be checked in the calling context");
        }

        goalHasFailed = userConfig.failOnFailedGoal() && gatesField.getBoolean("violates-goal");
        codeHealthDeclined = userConfig.failOnDecliningCodeHealth() && gatesField.getBoolean("degrades-in-code-health");

        gatesEnabled = userConfig.failOnFailedGoal() || userConfig.failOnDecliningCodeHealth();
        codeOwners = parseOptionalOwnersFrom(deltaResult);
    }

    private String parseOptionalOwnersFrom(final JsonObject deltaResult) {
        if (!deltaResult.containsKey("code-owners-for-quality-gates")) {
            return "-";
        }

        final JsonArray owners = deltaResult.getJsonArray("code-owners-for-quality-gates");

        StringBuilder formattedOwners = new StringBuilder();
        String separator = "";

        for (int i = 0; i < owners.size(); ++i) {
            formattedOwners.append(separator);
            formattedOwners.append(owners.getString(i));
            separator = ", ";
        }

        return formattedOwners.toString();
    }

    private QualityGates(boolean enableFailedGoalGate, boolean enableCodeHealthGate) {
        this.goalHasFailed = enableFailedGoalGate;
        this.codeHealthDeclined = enableCodeHealthGate;
        this.codeOwners = "-";
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

    public String codeOwners() { return codeOwners; }

    public boolean hasCodeOwners() { return !codeOwners.isEmpty() && !codeOwners.equals("-");}
}
