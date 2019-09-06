package org.jenkinsci.plugins.codescene.Domain;

import static org.jenkinsci.plugins.codescene.Domain.TestUtils.DELTA_ANALYSIS_RESULT;
import static org.junit.Assert.*;

import org.junit.Test;

public class CodeHealthDeltaDescriptionTest {

    @Test
    public void testCodeHealthDescriptions() {
        assertEquals(
                "[CodeHealthDelta{improvements='-', degradations=',Deep, Nested Complexity - new issue', name='ClientDemandsModuleRPCServiceImpl.java'}, " +
                 "CodeHealthDelta{improvements='-', degradations='Lines of Code in a Single File - new issue, Large Brain Method - new issue, Brain Method - new issue, Overall Code Complexity - new issue, Deep, Nested Complexity - new issue', name='AdminRPCServiceImpl.java'}, " +
                 "CodeHealthDelta{improvements='-', degradations='Brain Method - new issue, Deep, Nested Complexity - new issue', name='HomeDemandsPresenter.java'}]",
                new CodeHealthDeltaDescription(DELTA_ANALYSIS_RESULT).deltaDescriptions().toString());
    }
}