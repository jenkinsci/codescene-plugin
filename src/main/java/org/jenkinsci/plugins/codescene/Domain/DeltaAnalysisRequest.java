package org.jenkinsci.plugins.codescene.Domain;

import javax.json.*;


/**
 * Example of a complete delta analysis api request
 * <pre>
 * curl
 *  -u 'Bot:bot-password'
 *  -H "content-type: application/json"
 *  -d
 * '{"commits": ["e69c60944dec4f0b1213cfbbd65067c57ef84e60"],
 *   "repository": "poptavka",
 *   "use_biomarkers": true,
 *   "origin_url": "ssh://admin@localhost:29418/poptavka",
 *   "change_ref": "refs/changes/02/2/1"}'
 *   http://localhost:3003/projects/1/delta-analysis
 *
 * </pre>
 */
public class DeltaAnalysisRequest {

    private final JsonObject value;

    public DeltaAnalysisRequest(final Commits commits, final Configuration userConfig) {
        final JsonArray cs = serialize(commits);

        JsonObjectBuilder b = Json.createObjectBuilder();

        b.add("commits", cs);
        b.add("repository", userConfig.gitRepositoryToAnalyze().value());
        b.add("coupling_threshold_percent", userConfig.couplingThresholdPercent());
        b.add("use_biomarkers", enableBiomarkersDependingOn(userConfig));

        // gerrit support - CodeScene will fetch from given url and refspec if they are not null
        // strictly speaking we could allow changeRef to be null, but the same "and" is required by delta api too
        if (userConfig.originUrl() != null && userConfig.changeRef() != null) {
            b.add("origin_url", userConfig.originUrl());
            b.add("change_ref", userConfig.changeRef());
        }

        if (userConfig.currentCommit() != null) {
            b.add("delta_branch_head", userConfig.currentCommit().toString());
        }

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
