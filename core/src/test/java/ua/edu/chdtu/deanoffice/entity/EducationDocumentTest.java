package ua.edu.chdtu.deanoffice.entity;


import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class EducationDocumentTest {
    private void assertGetPreviousDiplomaType(Integer degreeId, EducationDocument expectedEducationDocument) {
        assertEquals(EducationDocument.getPreviousDiplomaType(degreeId), expectedEducationDocument);
    }

    @Test
    public void getPreviousDiplomaType() {
        assertGetPreviousDiplomaType(1, EducationDocument.SECONDARY_SCHOOL_CERTIFICATE);
        assertGetPreviousDiplomaType(4, EducationDocument.SECONDARY_SCHOOL_CERTIFICATE);
    }

    @Test
    public void isExist() {
        assertTrue(EducationDocument.isExist(EducationDocument.BACHELOR_DIPLOMA));
    }
}
