package org.jenkinsci.plugins.codescene.Domain;


import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class NewFiles {

    private boolean infoAvailable = false;
    private String summary = "-";
    private List<ReviewOfNewFile> reviews = new ArrayList<>();

    public NewFiles(final JsonObject deltaResult) {
        final JsonObject newFiles = deltaResult.getJsonObject("new-files-info");

        // Not available in older versions of PRs without any added content
        if (null != newFiles) {
            summary = newFiles.getString("summary");

            final JsonArray ws = newFiles.getJsonArray("warnings");

            if (null != ws) {
                for (int i = 0; i < ws.size(); i++) {
                    final JsonObject w = ws.getJsonObject(i);

                    reviews.add(new ReviewOfNewFile(w));
                }
            }

            infoAvailable = true;
        }
    }

    public boolean hasNewFileInfo() { return infoAvailable; }

    public String getSummary() { return summary; }

    public List<ReviewOfNewFile> getReviews() { return reviews; }
}
