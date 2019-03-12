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

    // emulate named parameters to get more calling context
    private String useBiomarkers = "";
    private boolean enableUserBiomarkers = false;
    private static boolean enableFailedGoalGate = false;
    private static boolean enableCodeHealthGate = false;

    @Test
    public void serializesRequestAsJson() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac51bf48ff5a206f0854ace2b67734ea66")),
                userConfigFrom(GIT_REPO, COUPLING_THRESHOLD, enableUserBiomarkers = true));

        assertEqualPayload("{\"commits\":[\"b75943ac51bf48ff5a206f0854ace2b67734ea66\"],", useBiomarkers = "true", request);
    }

    @Test
    public void serializesRequestWithMultipleCommitsAsJson() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac5"), new Commit("9822ac")),
                userConfigFrom(GIT_REPO, COUPLING_THRESHOLD, enableUserBiomarkers = false));

        assertEqualPayload("{\"commits\":[\"b75943ac5\",\"9822ac\"],", useBiomarkers = "false", request);
    }

    @Test
    public void enablesBiomarkersWhenFailedGoalGateEnabled() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac51bf48ff5a206f0854ace2b67734ea66")),
                userConfigFrom(GIT_REPO, COUPLING_THRESHOLD,
                        enableUserBiomarkers = false,
                        enableFailedGoalGate = true,
                        enableCodeHealthGate = false));

        assertEqualPayload("{\"commits\":[\"b75943ac51bf48ff5a206f0854ace2b67734ea66\"],", useBiomarkers = "true", request);
    }

    @Test
    public void enablesBiomarkersWhenDecliningCodeHealthGateEnabled() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac51bf48ff5a206f0854ace2b67734ea66")),
                userConfigFrom(GIT_REPO, COUPLING_THRESHOLD,
                        enableUserBiomarkers = false,
                        enableFailedGoalGate = false,
                        enableCodeHealthGate = true));

        assertEqualPayload("{\"commits\":[\"b75943ac51bf48ff5a206f0854ace2b67734ea66\"],", useBiomarkers = "true", request);
    }

    @Test
    public void enablesBiomarkersWhenAllGatesEnabled() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac51bf48ff5a206f0854ace2b67734ea66")),
                userConfigFrom(GIT_REPO, COUPLING_THRESHOLD,
                        enableUserBiomarkers = false,
                        enableFailedGoalGate = true,
                        enableCodeHealthGate = true));

        assertEqualPayload("{\"commits\":[\"b75943ac51bf48ff5a206f0854ace2b67734ea66\"],", useBiomarkers = "true", request);
    }

    @Test
    public void enablesBiomarkersWhenRequestedTogetherWithAllGatesEnabled() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac51bf48ff5a206f0854ace2b67734ea66")),
                userConfigFrom(GIT_REPO, COUPLING_THRESHOLD,
                        enableUserBiomarkers = true,
                        enableFailedGoalGate = true,
                        enableCodeHealthGate = true));

        assertEqualPayload("{\"commits\":[\"b75943ac51bf48ff5a206f0854ace2b67734ea66\"],", useBiomarkers = "true", request);
    }

    private static void assertEqualPayload(final String serializedCommits,
                                           final String enabledBiomarkers,
                                           final DeltaAnalysisRequest request) {
        assertEquals(serializedCommits +
                        "\"repository\":\"codescene-ui\"," +
                        "\"coupling_threshold_percent\":65," +
                        "\"use_biomarkers\":" + enabledBiomarkers + "}",
                request.asJson().toString());
    }

    private static Configuration userConfigFrom(
            Repository repo,
            int couplingThreshold,
            boolean useBiomarkers)
    {
        return userConfigFrom(repo, couplingThreshold, useBiomarkers, enableFailedGoalGate = false, enableCodeHealthGate = false);
    }

    private static Configuration userConfigFrom(
            Repository repo,
            int couplingThreshold,
            boolean useBiomarkers,
            boolean failOnFailedGoal,
            boolean failOnDecliningCodeHealth) {
        final boolean letBuildPassOnFailedAnalysis = false;

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