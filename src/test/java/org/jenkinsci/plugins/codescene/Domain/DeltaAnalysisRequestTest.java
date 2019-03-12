package org.jenkinsci.plugins.codescene.Domain;

import org.apache.commons.jelly.tags.fmt.Config;
import org.apache.mina.core.RuntimeIoException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

public class DeltaAnalysisRequestTest {

    private static final Repository GIT_REPO = new Repository("codescene-ui");
    private static final int COUPLING_THRESHOLD = 65;

    @Test
    public void serializesRequestAsJson() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac51bf48ff5a206f0854ace2b67734ea66")),
                userConfigFrom(GIT_REPO, COUPLING_THRESHOLD, true));

        assertEquals("{\"commits\":[\"b75943ac51bf48ff5a206f0854ace2b67734ea66\"]," +
                        "\"repository\":\"codescene-ui\"," +
                        "\"coupling_threshold_percent\":65," +
                        "\"use_biomarkers\":true}",
                request.asJson().toString());
    }

    @Test
    public void serializesRequestWithMultipleCommitsAsJson() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac5"), new Commit("9822ac")),
                userConfigFrom(GIT_REPO, COUPLING_THRESHOLD, false));

        assertEquals("{\"commits\":[\"b75943ac5\",\"9822ac\"]," +
                        "\"repository\":\"codescene-ui\"," +
                        "\"coupling_threshold_percent\":65," +
                        "\"use_biomarkers\":false}",
                request.asJson().toString());
    }

    private static Configuration userConfigFrom(Repository repo, int couplingThreshold, boolean useBiomarkers) {
        final boolean letBuildPassOnFailedAnalysis = false;
        final boolean failOnFailedGoal = true;
        final boolean failOnDecliningCodeHealth = true;

        try {
            return new Configuration(
                    new URL("https://empear.com/"),
                    new CodeSceneUser("CodeScene user name", "hashed"),
                    repo,
                    couplingThreshold,
                    useBiomarkers,
                    letBuildPassOnFailedAnalysis,
                    failOnFailedGoal,
                    failOnDecliningCodeHealth);
        } catch (MalformedURLException e) {
            throw new RuntimeIoException(e);
        }
    }
}