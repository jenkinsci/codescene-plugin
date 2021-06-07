package org.jenkinsci.plugins.codescene.Domain;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;

/**
 * CodeScene reviews new files to make sure they have an acceptable code health.
 * Any issues are reported here.
 */
public class ReviewOfNewFile {

    private final String name;
    private final List<String> reviewResults;

    public ReviewOfNewFile(final JsonObject newFileWarning) {
        if (null == newFileWarning) {
            throw new IllegalArgumentException("The new file warning cannot be null. This should be checked in the calling context");
        }

        name = newFileWarning.getString("name", "Missing Name");
        reviewResults = parseReviewResultsFrom(newFileWarning);
    }

    private List<String> parseReviewResultsFrom(final JsonObject w) {
        final List<String> reviewResults = new ArrayList<>();
        final JsonArray reviews = w.getJsonArray("review");

        if (null != reviews) {
            for (int i = 0; i < reviews.size(); i++) {
                final JsonObject r = reviews.getJsonObject(i);
                reviewResults.add(r.getString("title") + ": " + parseDescription(r.get("description")));
            }
        }
        return reviewResults;
    }

    private String parseDescription(JsonValue description){
        StringBuilder sb = new StringBuilder();
        if(description != null){
            switch (description.getValueType()){
                case ARRAY: {
                    JsonArray array = ((JsonArray)description);
                    for(int i = 0; i < array.size(); i++){
                        if(sb.length() > 0){
                            sb.append(" ");
                        }
                        sb.append(array.getString(i));
                    }
                } break;
                case STRING: {
                    sb.append(description);
                } break;
            }
        }
        return sb.toString();
    }

    public String getName() { return name; }
    public List<String> getReviewResults() { return reviewResults; }
}
