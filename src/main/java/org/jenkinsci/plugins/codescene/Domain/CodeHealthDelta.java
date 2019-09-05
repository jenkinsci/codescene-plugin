package org.jenkinsci.plugins.codescene.Domain;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class CodeHealthDelta {

    private String improvements;
    private String degradations;

    private String name;

    public CodeHealthDelta(final JsonObject deltaTrendPart) {
        if (null == deltaTrendPart) {
            throw new IllegalArgumentException("The code health delta trend cannot be null. This should be checked in the calling context");
        }

        name = deltaTrendPart.getString("name", "Missing Name");
        improvements = formatDeltaIn(deltaTrendPart.getJsonArray("improved"));
        degradations = formatDeltaIn(deltaTrendPart.getJsonArray("degraded"));
    }

    private static String formatDeltaIn(final JsonArray delta) {
        StringBuilder formattedDelta = new StringBuilder();
        String separator = "";

        for (int i = 0; i < delta.size(); ++i) {
            formattedDelta.append(separator);
            formattedDelta.append(delta.getString(i));
            separator = ", ";
        }

        return formattedDelta.length() > 0 ? formattedDelta.toString() : "-";
    }

    public String getName() {
        return name;
    }

    public String getImprovements() {
        return improvements;
    }

    public String getDegradations() {
        return degradations;
    }
}
