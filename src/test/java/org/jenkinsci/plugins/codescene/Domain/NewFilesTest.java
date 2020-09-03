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
        assertEquals("Long Method Detected: The longest function (initialize_rb_class_with_no_args) has 174 lines of code. The recommended maximum length is 70 lines., Bumpy Road Ahead: The code is complex to read due to its nesting with multiple logical blocks. The most complex function is putmsg with 3 logical blocks. A bumpy road like putmsg indicates a lack of encapsulation. Consider to extract smaller, cohesive functions from the bumpy functions., File Size Issue: This module seems to grow quite large with 1444 lines of code (comments stripped away). Watch it carefully and consider to modularize it., Excess function arguments: The function put_ruby_value has 7 arguments, which is above the threshold of 5 arguments. This indicates either low cohesion or a missing abstraction that encapsulates those arguments., Brain Method Detected: The function putmsg has a McCabe complexity of 39 logical paths. The recommended complexity threshold is 9.",
                reviews.get(0).getReviewResults());
    }
}
