package org.jenkinsci.plugins.codescene.Domain;

import org.apache.commons.lang.StringUtils;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

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
        final String formattedDelta = StringUtils.join(delta.getValuesAs(JsonString.class), ", ");

        return StringUtils.isEmpty(formattedDelta)? formattedDelta : "-";
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

    @Override
    public String toString() {
        return "CodeHealthDelta{" +
                "improvements='" + improvements + '\'' +
                ", degradations='" + degradations + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
