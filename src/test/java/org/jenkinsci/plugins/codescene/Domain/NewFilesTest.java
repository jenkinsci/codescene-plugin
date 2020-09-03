package org.jenkinsci.plugins.codescene.Domain;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.jenkinsci.plugins.codescene.Domain.TestUtils.*;

public class NewFilesTest {

    @Test
    public void containsSummary() {
        assertTrue(new NewFiles(DELTA_ANALYSIS_RESULT_WITH_NEW_FILES).hasNewFileInfo());
        assertEquals("1 new files where the code health is above the lower threshold for new code, 8.1",
                new NewFiles(DELTA_ANALYSIS_RESULT_WITH_NEW_FILES).getSummary());
    }

    @Test
    public void deltaAnalysesThatDoNotContainNewFilesOmitThisInfo() {
        assertFalse(new NewFiles(DELTA_ANALYSIS_RESULT).hasNewFileInfo());
    }

    @Test
    public void performsCodeReviewOfEachFile() {
        final List<ReviewOfNewFile> reviews = new NewFiles(DELTA_ANALYSIS_RESULT_WITH_NEW_FILES).getReviews();

        assertEquals(1, reviews.size());
        assertEquals("rename-test/one_more_new_src/more_new_files2.c", reviews.get(0).getName());

        final List<String> reviewResults = reviews.get(0).getReviewResults();
        assertEquals(5, reviewResults.size());
    }
}
