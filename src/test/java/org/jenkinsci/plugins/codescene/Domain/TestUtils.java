package org.jenkinsci.plugins.codescene.Domain;

import javax.json.Json;
import javax.json.JsonObject;

public class TestUtils {

    public static JsonObject DELTA_ANALYSIS_RESULT = deltaAnalysisResult();

    public static JsonObject DELTA_ANALYSIS_RESULT_WITH_NEW_FILES = deltaAnalysisWithNewFileWarnings();

    private static JsonObject deltaAnalysisResult() {
        return Json.createReader(TestUtils.class.getClassLoader().getResourceAsStream("delta-analysis-result.json"))
                .readObject();
    }

    private static JsonObject deltaAnalysisWithNewFileWarnings() {
        return Json.createReader(TestUtils.class.getClassLoader().getResourceAsStream("delta-analysis-result-with-new-files.json"))
                .readObject();
    }
}
