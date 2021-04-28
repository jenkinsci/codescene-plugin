package org.jenkinsci.plugins.codescene.Domain;

import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class QualityGatesTest {

    @Test
    public void gatesPass() {
        QualityGates qg = new QualityGates(resultPassesGates(), allGatesEnabled());

        assertFalse(qg.goalHasFailed());
        assertFalse(qg.codeHealthDeclined());
    }

    @Test
    public void allGatesFail() {
        QualityGates qg = new QualityGates(resultFailsAllGates(), allGatesEnabled());

        assertTrue(qg.goalHasFailed());
        assertTrue(qg.codeHealthDeclined());
    }

    @Test
    public void codeHealthGatesFail() {
        QualityGates qg = new QualityGates(resultFailsCodeHealthGate(), allGatesEnabled());

        assertFalse(qg.goalHasFailed());
        assertTrue(qg.codeHealthDeclined());
    }

    @Test
    public void goalsGatesFail() {
        QualityGates qg = new QualityGates(resultFailsGoalsGate(), allGatesEnabled());

        assertTrue(qg.goalHasFailed());
        assertFalse(qg.codeHealthDeclined());
    }

    @Test
    public void gatesAreDisabledByConfig() {
        QualityGates qg = new QualityGates(resultFailsAllGates(), gatesDisabled());

        assertFalse(qg.goalHasFailed());
        assertFalse(qg.codeHealthDeclined());
    }

    private static JsonObject resultFailsAllGates() {
        return failGatesMatching(true, true);
    }

    private static JsonObject resultPassesGates() {
        return failGatesMatching(false, false);
    }

    private static JsonObject resultFailsCodeHealthGate() {
        return failGatesMatching(true, false);
    }

    private static JsonObject resultFailsGoalsGate() {
        return failGatesMatching(false, true);
    }

    private static JsonObject failGatesMatching(boolean triggerCodeHealth, boolean triggerGoal) {
        return Json.createObjectBuilder()
                .add("quality-gates", Json.createObjectBuilder()
                        .add("degrades-in-code-health", triggerCodeHealth)
                        .add("violates-goal", triggerGoal))
                .build();
    }

    private static Configuration allGatesEnabled() {
        return new Configuration(
                null,
                new CodeSceneUser("my name", "123"),
                new Repository("testing-repo"),
                80, // coupling threshold
                true, // use biomarkers
                true, // letBuildPassOnFailedAnalysis,
                true, // failOnFailedGoal,
                true, // failOnDecliningCodeHealth,
                "localhost", // origin URL
                "master", // changeRef
                null);
    }

    private static Configuration gatesDisabled() {
        return new Configuration(
                null,
                new CodeSceneUser("my name", "123"),
                new Repository("testing-repo"),
                80, // coupling threshold
                true, // use biomarkers
                true, // letBuildPassOnFailedAnalysis,
                false, // failOnFailedGoal,
                false, // failOnDecliningCodeHealth,
                "localhost", // origin URL
                "master", // changeRef
                null);
    }
}
