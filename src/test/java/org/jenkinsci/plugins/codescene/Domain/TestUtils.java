package org.jenkinsci.plugins.codescene.Domain;

import javax.json.Json;
import javax.json.JsonObject;

public class TestUtils {

    public static JsonObject DELTA_ANALYSIS_RESULT = deltaAnalysisResult();

    private static JsonObject deltaAnalysisResult() {
        return Json.createReader(TestUtils.class.getClassLoader().getResourceAsStream("delta-analysis-result.json"))
                .readObject();
    }

}
