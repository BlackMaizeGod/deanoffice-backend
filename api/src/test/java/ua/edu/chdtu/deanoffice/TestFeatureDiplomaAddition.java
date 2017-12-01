package ua.edu.chdtu.deanoffice;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.edu.chdtu.deanoffice.entity.*;
import ua.edu.chdtu.deanoffice.service.document.diploma.supplement.DiplomaSupplementService;
import ua.edu.chdtu.deanoffice.service.document.diploma.supplement.StudentSummary;
import ua.edu.chdtu.deanoffice.webstarter.Application;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class TestFeatureDiplomaAddition {

    private static Logger log = LoggerFactory.getLogger(Application.class);

    private static final String TEMPLATE = "DiplomaSupplementTemplate.docx";

    private static DateFormat dateOfBirthFormat = new SimpleDateFormat("dd.MM.yyyy");

    private static Speciality createSpeciality() {
        Speciality speciality = new Speciality();
        speciality.setName("Спеціальність");
        speciality.setNameEng("Speciality");
        speciality.setCode("123");
        return speciality;
    }

    private static Specialization createSpecialization() {
        Specialization specialization = new Specialization();
        specialization.setName("Спеціалізація");
        specialization.setNameEng("Specialization");
        specialization.setQualification("Кваліфікація 1  Кваліфікація 2");
        specialization.setQualificationEng("Qualification 1  Qualification 2");
        return specialization;
    }

    private static StudentGroup createStudentGroup() {
        StudentGroup studentGroup = new StudentGroup();
        studentGroup.setName("АБ-123");
        studentGroup.setActive(true);
        studentGroup.setStudySemesters(8);
        studentGroup.setStudyYears(new BigDecimal(4));
        studentGroup.setBeginYears(1);
        studentGroup.setTuitionForm('f');
        studentGroup.setTuitionTerm('f');
        return studentGroup;
    }

    private static Student createStudent() {
        Student student = new Student();

        student.setName("Іван");
        student.setSurname("Іванов");
        student.setPatronimic("Іванович");

        student.setNameEng("Ivan");
        student.setSurnameEng("Ivanov");
        student.setPatronimicEng("Ivanovich");

        try {
            student.setBirthDate(dateOfBirthFormat.parse("01.01.2000"));
        } catch (ParseException e) {
            log.error("Wrong date. Should never happen", e);
        }

        StudentGroup studentGroup = createStudentGroup();
        student.setStudentGroup(studentGroup);

        Specialization specialization = createSpecialization();
        studentGroup.setSpecialization(specialization);

        Speciality speciality = createSpeciality();
        specialization.setSpeciality(speciality);

        Degree degree = new Degree("Ступінь", "Degree");
        specialization.setDegree(degree);

        return student;
    }

    @Test
    public void testFillWithStudentInformation() {
        Student student = createStudent();
        StudentSummary studentSummary = new StudentSummary(student, new ArrayList<>());
        Assert.assertEquals(true, DiplomaSupplementService
                .fillWithStudentInformation(TEMPLATE, studentSummary).getAbsolutePath().endsWith(".docx"));
    }

    @Test
    public void testAdjustAverage() {
        double[] expectedResult1 = {5, 90};
        double[] actualResult1 = StudentSummary.adjustAverageGradeAndPoints(5, 89);
        Assert.assertArrayEquals(expectedResult1, actualResult1, 0.01);

        double[] expectedResult2 = {4, 83};
        double[] actualResult2 = StudentSummary.adjustAverageGradeAndPoints(4.5, 83);
        Assert.assertArrayEquals(expectedResult2, actualResult2, 0.01);
    }

    @Test
    public void testGetGradeFromPoints() {
        Assert.assertEquals("A", StudentSummary.getECTSGrade(95));
        Assert.assertEquals("B", StudentSummary.getECTSGrade(82));
        Assert.assertEquals("D", StudentSummary.getECTSGrade(67));
        Assert.assertEquals("C", StudentSummary.getECTSGrade(78));
    }


    private static Grade createGrade(Course course, int points) {
        Grade grade = new Grade();
        grade.setPoints(points);
        grade.setCourse(course);
        grade.setGrade(StudentSummary.getGradeFromPoints(points));
        grade.setEcts(StudentSummary.getECTSGrade(points));
        return grade;
    }

    private static Course createCourse(boolean knowledgeControlHasGrade) {
        Course course = new Course();
        KnowledgeControl kc = new KnowledgeControl();
        kc.setHasGrade(knowledgeControlHasGrade);
        course.setKnowledgeControl(kc);
        return course;
    }

    @Test
    public void testGradeSetting() {
        Grade grade1 = createGrade(createCourse(true), 90);
        Assert.assertEquals("Відмінно", StudentSummary.getNationalGradeUkr(grade1));
        Assert.assertEquals("Excellent", StudentSummary.getNationalGradeEng(grade1));
        Assert.assertEquals("A", grade1.getEcts());
        Assert.assertEquals(5, grade1.getGrade());

        Grade grade2 = createGrade(createCourse(true), 76);
        Assert.assertEquals("Добре", StudentSummary.getNationalGradeUkr(grade2));
        Assert.assertEquals("Good", StudentSummary.getNationalGradeEng(grade2));
        Assert.assertEquals("C", grade2.getEcts());
        Assert.assertEquals(4, grade2.getGrade());

        Grade grade3 = createGrade(createCourse(true), 65);
        Assert.assertEquals("Задовільно", StudentSummary.getNationalGradeUkr(grade3));
        Assert.assertEquals("Satisfactory", StudentSummary.getNationalGradeEng(grade3));
        Assert.assertEquals("D", grade3.getEcts());
        Assert.assertEquals(3, grade3.getGrade());

        Grade grade4 = createGrade(createCourse(false), 90);
        Assert.assertEquals("Зараховано", StudentSummary.getNationalGradeUkr(grade4));
        Assert.assertEquals("Passed", StudentSummary.getNationalGradeEng(grade4));
        Assert.assertEquals("A", grade4.getEcts());
        Assert.assertEquals(5, grade4.getGrade());

        Grade grade5 = createGrade(createCourse(false), 59);
        Assert.assertEquals("Не зараховано", StudentSummary.getNationalGradeUkr(grade5));
        Assert.assertEquals("Fail", StudentSummary.getNationalGradeEng(grade5));
        Assert.assertEquals("Fx", grade5.getEcts());
    }

}
