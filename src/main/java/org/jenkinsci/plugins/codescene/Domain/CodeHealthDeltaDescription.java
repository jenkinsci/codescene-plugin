package org.jenkinsci.plugins.codescene.Domain;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class CodeHealthDeltaDescription {

    private List<CodeHealthDelta> deltas = new ArrayList<>();

    public CodeHealthDeltaDescription(final JsonObject deltaResult) {
        final JsonArray deltaDescriptors = deltaResult.getJsonArray("code-health-delta-descriptions");

        if (null != deltaDescriptors) {
            for (int i = 0; i < deltaDescriptors.size(); i++) {
                deltas.add(new CodeHealthDelta(deltaDescriptors.getJsonObject(i)));
            }
        }
    }

    public List<CodeHealthDelta> deltaDescriptions() {
        return deltas;
    }
}
