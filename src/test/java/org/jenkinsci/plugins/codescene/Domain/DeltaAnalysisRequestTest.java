package org.jenkinsci.plugins.codescene.Domain;

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
                userConfigFrom(true));

        assertEqualPayload("{\"commits\":[\"b75943ac51bf48ff5a206f0854ace2b67734ea66\"],", "true", request);
    }

    @Test
    public void serializesRequestWithMultipleCommitsAsJson() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac5"), new Commit("9822ac")),
                userConfigFrom(false));

        assertEqualPayload("{\"commits\":[\"b75943ac5\",\"9822ac\"],", "false", request);
    }

    @Test
    public void enablesBiomarkersWhenFailedGoalGateEnabled() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac51bf48ff5a206f0854ace2b67734ea66")),
                userConfigFrom(false, true, false));

        assertEqualPayload("{\"commits\":[\"b75943ac51bf48ff5a206f0854ace2b67734ea66\"],", "true", request);
    }

    @Test
    public void enablesBiomarkersWhenDecliningCodeHealthGateEnabled() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac51bf48ff5a206f0854ace2b67734ea66")),
                userConfigFrom(false, false, true));

        assertEqualPayload("{\"commits\":[\"b75943ac51bf48ff5a206f0854ace2b67734ea66\"],", "true", request);
    }

    @Test
    public void enablesBiomarkersWhenAllGatesEnabled() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac51bf48ff5a206f0854ace2b67734ea66")),
                userConfigFrom(false, true, true));

        assertEqualPayload("{\"commits\":[\"b75943ac51bf48ff5a206f0854ace2b67734ea66\"],", "true", request);
    }

    @Test
    public void enablesBiomarkersWhenRequestedTogetherWithAllGatesEnabled() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac51bf48ff5a206f0854ace2b67734ea66")),
                userConfigFrom(true, true, true));

        assertEqualPayload("{\"commits\":[\"b75943ac51bf48ff5a206f0854ace2b67734ea66\"],", "true", request);
    }


    @Test
    public void gerritSpecificRequest() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac51bf48ff5a206f0854ace2b67734ea66")),
                commonUserConfig()
                        .originUrl("ssh://admin@localhost:29418/poptavka")
                        .changeRef("refs/changes/10/10/1")
                .build()
        );

        assertEquals("{\"commits\":[\"b75943ac51bf48ff5a206f0854ace2b67734ea66\"]," +
                        "\"repository\":\"codescene-ui\"," +
                        "\"coupling_threshold_percent\":65," +
                        "\"use_biomarkers\":false," +
                        "\"origin_url\":\"ssh://admin@localhost:29418/poptavka\",\"change_ref\":\"refs/changes/10/10/1\"}",
                request.asJson().toString());
    }

     @Test
    public void gerritSpecificRequestWithOnlyOriginUrlIsNotEnough() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac51bf48ff5a206f0854ace2b67734ea66")),
                commonUserConfig()
                        .originUrl("ssh://admin@localhost:29418/poptavka")
                .build()
        );

        // origin_url won't be included unless the changeRef is specified too
        assertEquals("{\"commits\":[\"b75943ac51bf48ff5a206f0854ace2b67734ea66\"]," +
                        "\"repository\":\"codescene-ui\"," +
                        "\"coupling_threshold_percent\":65," +
                        "\"use_biomarkers\":false}",
                request.asJson().toString());
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

    private static Configuration userConfigFrom(boolean useBiomarkers) {
        return userConfigFrom(useBiomarkers, false, false);
    }

    private static Configuration userConfigFrom(boolean useBiomarkers, boolean failOnFailedGoal, boolean failOnDecliningCodeHealth) {
        final boolean letBuildPassOnFailedAnalysis = false;
        return commonUserConfig()
                .useBiomarkers(useBiomarkers)
                .letBuildPassOnFailedAnalysis(letBuildPassOnFailedAnalysis)
                .failOnFailedGoal(failOnFailedGoal)
                .failOnDecliningCodeHealth(failOnDecliningCodeHealth)
                .build();
    }

    private static ConfigurationBuilder commonUserConfig() {
        try {
            return new ConfigurationBuilder().codeSceneUrl(new URL("https://empear.com/")).user(new CodeSceneUser("CodeScene user name", "hashed"))
                    .gitRepositoryToAnalyze(DeltaAnalysisRequestTest.GIT_REPO)
                    .couplingThresholdPercent(DeltaAnalysisRequestTest.COUPLING_THRESHOLD);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}