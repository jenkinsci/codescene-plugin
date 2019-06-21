package org.jenkinsci.plugins.codescene.Domain;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class DeltaAnalysisResult {
    private final String viewUrl;
    private final Commits commits;
    private final RiskClassification risk;
    private final Improvements improvements;
    private final Warnings warnings;
    private final RiskDescription description;
    private final QualityGates gates;

    public DeltaAnalysisResult(final Commits commits, final Configuration userConfig, final JsonObject result) {
        ensureTheVersionIsSupported(result);

        final JsonObject deltaResult = result.getJsonObject("result");

        viewUrl = result.getString("view");
        risk = riskFrom(deltaResult);
        improvements = Improvements.in(deltaResult);
        warnings = warningsFrom(deltaResult);
        description = descriptionOfRiskFrom(deltaResult, versionOf(result));
        this.commits = commits;
        gates = triggeredQualityGatesFrom(deltaResult, userConfig);
    }

    private QualityGates triggeredQualityGatesFrom(JsonObject deltaResult, final Configuration userConfig) {
        if (deltaResult.containsKey("quality-gates")) {
            return new QualityGates(deltaResult, userConfig);
        }

        return QualityGates.none();
    }

    private RiskClassification riskFrom(JsonObject deltaResult) {
        return new RiskClassification(deltaResult.getJsonNumber("risk").intValue());
    }

    private static Warnings warningsFrom(JsonObject deltaResult) {
        final Warnings ws = new Warnings();

        final JsonArray jsonWarnings = deltaResult.getJsonArray("warnings");

        for (int i = 0; i < jsonWarnings.size(); ++i) {
            final JsonObject w = jsonWarnings.getJsonObject(i);

            final WarningCategory category = new WarningCategory(w.getString("category"));
            final JsonArray jsonDetails = w.getJsonArray("details");

            List<String> ds = new ArrayList<>();
            for (int j=0; j < jsonDetails.size(); j++) {
                ds.add(jsonDetails.getString(j));
            }

            final List<String> details = new ArrayList<>(ds);

            ws.add(new Warning(category, details));
        }

        return ws;
    }

    private RiskDescription descriptionOfRiskFrom(JsonObject deltaResult, final String version) {
        if (version.equals("1")) {
            return new RiskDescription("No risk description available: upgrade CodeScene");
        }

        return new RiskDescription(deltaResult.getString("description"));
    }

    private void ensureTheVersionIsSupported(JsonObject result) {
        final String version = versionOf(result);

        if (! (version.equals("1") || version.equals("2"))) {
            throw new RuntimeException("The CodeScene API reports version " + version + ", which we don't support. You need to upgrade CodeScene.");
        }
    }

    private static String versionOf(JsonObject result) {
        return result.getString("version");
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public Commits getCommits() {
        return commits;
    }

    public RiskClassification getRisk() {
        return risk;
    }

    public Improvements improvements() { return improvements;}

    public RiskDescription getRiskDescription() { return description; }

    public Warnings getWarnings() {
        return warnings;
    }

    public QualityGates qualityGatesState() { return gates; }
}
