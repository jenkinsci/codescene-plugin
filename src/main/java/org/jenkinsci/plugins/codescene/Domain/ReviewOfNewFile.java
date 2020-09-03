package org.jenkinsci.plugins.codescene.Domain;

import org.apache.commons.lang.StringUtils;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * CodeScene reviews new files to make sure they have an acceptable code health.
 * Any issues are reported here.
 */
public class ReviewOfNewFile {

    private String name;
    private String reviewResults;

    public ReviewOfNewFile(final JsonObject newFileWarning) {
        if (null == newFileWarning) {
            throw new IllegalArgumentException("The new file warning cannot be null. This should be checked in the calling context");
        }

        name = newFileWarning.getString("name", "Missing Name");
        reviewResults = parseReviewResultsFrom(newFileWarning);
    }

    private static String parseReviewResultsFrom(final JsonObject w) {
        final List<String> reviewResults = new ArrayList<>();
        final JsonArray reviews = w.getJsonArray("review");

        if (null != reviews) {
            for (int i = 0; i < reviews.size(); i++) {
                final JsonObject r = reviews.getJsonObject(i);
                reviewResults.add(r.getString("title") + ": " + r.getString("description"));
            }
        }

        return StringUtils.join(reviewResults, ", ");
    }

    public String getName() { return name; }
    public String getReviewResults() { return reviewResults; }
}
