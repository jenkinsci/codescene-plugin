package org.jenkinsci.plugins.codescene.Domain;

import javax.json.*;


public class DeltaAnalysisRequest {

    private final JsonObject value;

    public DeltaAnalysisRequest(final Commits commits, final Configuration userConfig) {
        final JsonArray cs = serialize(commits);

        JsonObjectBuilder b = Json.createObjectBuilder();
        b.add("commits", cs);
        b.add("repository", userConfig.gitRepositoryToAnalyze().value());
        b.add("coupling_threshold_percent", userConfig.couplingThresholdPercent());
        b.add("use_biomarkers", enableBiomarkersDependingOn(userConfig));

        value = b.build();
    }

    private static boolean enableBiomarkersDependingOn(final Configuration userConfig) {
        return userConfig.useBiomarkers() || anyQualityGateEnabled(userConfig);
    }

    private static boolean anyQualityGateEnabled(final Configuration userConfig) {
        return userConfig.failOnFailedGoal() || userConfig.failOnDecliningCodeHealth();
    }

    private static JsonArray serialize(final Commits commits) {
        final JsonArrayBuilder b = Json.createArrayBuilder();

        for (Commit c : commits.value()) {
            b.add(c.value());
        }

        return b.build();
    }

    public JsonObject asJson() {
        return value;
    }
}
