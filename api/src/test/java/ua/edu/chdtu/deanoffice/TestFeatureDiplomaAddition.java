package ua.edu.chdtu.deanoffice;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ua.edu.chdtu.deanoffice.entity.*;
import ua.edu.chdtu.deanoffice.repository.GradeRepository;
import ua.edu.chdtu.deanoffice.repository.StudentRepository;
import ua.edu.chdtu.deanoffice.service.GradeService;
import ua.edu.chdtu.deanoffice.service.StudentService;
import ua.edu.chdtu.deanoffice.service.document.diploma.supplement.DiplomaSupplementService;
import ua.edu.chdtu.deanoffice.service.document.diploma.supplement.StudentSummary;
import ua.edu.chdtu.deanoffice.webstarter.Application;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DiplomaSupplementService.class, StudentService.class, GradeService.class,
        StudentRepository.class, GradeRepository.class})
@EnableAutoConfiguration
public class TestFeatureDiplomaAddition {


    private DiplomaSupplementService diplomaSupplementService;

    @Autowired
    public void setDiplomaSupplementService(DiplomaSupplementService diplomaSupplementService) {
        this.diplomaSupplementService = diplomaSupplementService;
    }

    private static Logger log = LoggerFactory.getLogger(Application.class);

    private static final String TEMPLATE = "DiplomaSupplementTemplate nc.docx";

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

    private static List<List<Grade>> createGrades(Student student) {
        List<List<Grade>> grades = new ArrayList<>();
        grades.add(new ArrayList<>());
        grades.add(new ArrayList<>());
        grades.add(new ArrayList<>());
        grades.add(new ArrayList<>());

        CourseName courseName1 = new CourseName();
        courseName1.setName("Багатосеместровий курс 1");
        courseName1.setNameEng("Multiple Semester course 1");

        Course course11 = createCourse(courseName1, true);
        Course course12 = createCourse(courseName1, true);
        Course course13 = createCourse(courseName1, false);

        Grade grade11 = createGrade(course11, student, 89);
        Grade grade12 = createGrade(course12, student, 75);
        Grade grade13 = createGrade(course13, student, 90);

        grades.get(0).addAll(Arrays.asList(grade11, grade12, grade13));

        CourseName courseName2 = new CourseName();
        courseName2.setName("Курсова робота 1");
        courseName2.setNameEng("Course work 1");

        Course course2 = createCourse(courseName2, true);

        Grade grade2 = createGrade(course2, 84);

        grades.get(1).add(grade2);


        CourseName courseName3 = new CourseName();
        courseName3.setName("Практика 1");
        courseName3.setNameEng("Practice 1");

        Course course3 = createCourse(courseName3, true);

        Grade grade3 = createGrade(course3, 90);

        grades.get(2).add(grade3);


        CourseName courseName4 = new CourseName();
        courseName4.setName("Дипломна робота 1");
        courseName4.setNameEng("Diploma work 1");

        Course course4 = createCourse(courseName4, true);

        Grade grade4 = createGrade(course4, 90);

        grades.get(3).add(grade4);

        return grades;
    }

    @Test
    public void testStudentSummary() {
        Student student = createStudent();
        StudentSummary summary = new StudentSummary(student, createGrades(student));

//        Assert.assertEquals(82, summary.getTotalGrade(), 0.1);
//        Assert.assertEquals("Добре", summary.getTotalNationalGradeUkr());
//        Assert.assertEquals("Good", summary.getTotalNationalGradeEng());
//        Assert.assertEquals(0, summary.getTotalHours());
//        Assert.assertEquals(0, summary.getTotalCredits(30), 0.1);
    }

    @Test
    public void testFillWithStudentInformation() {
        //Student student = diplomaSupplementService.formDiplomaSupplement();

        Student student = createStudent();
        diplomaSupplementService.setStudentSummary(new StudentSummary(student, createGrades(student)));
        Assert.assertEquals(true, diplomaSupplementService
                .fillWithStudentInformation(TEMPLATE).getAbsolutePath().endsWith(".docx"));
    }

    @Test
    public void testAdjustAverage() {
        double[] expectedResult1 = {5, 90};
        int[] actualResult1 = StudentSummary.adjustAverageGradeAndPoints(5, 89);
        Assert.assertEquals(expectedResult1[0], actualResult1[0], 0.01);
        Assert.assertEquals(expectedResult1[1], actualResult1[1], 0.01);

        double[] expectedResult2 = {4, 83};
        int[] actualResult2 = StudentSummary.adjustAverageGradeAndPoints(4.5, 83);
        Assert.assertEquals(expectedResult2[0], actualResult2[0], 0.01);
        Assert.assertEquals(expectedResult2[1], actualResult2[1], 0.01);
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

    private static Grade createGrade(Course course, Student student, int points) {
        Grade grade = createGrade(course, points);
        grade.setStudent(student);
        return grade;
    }

    private static Course createCourse(boolean knowledgeControlHasGrade) {
        Course course = new Course();
        KnowledgeControl kc = new KnowledgeControl();
        course.setHours(90);
        kc.setHasGrade(knowledgeControlHasGrade);
        if (knowledgeControlHasGrade)
            kc.setName("іспит");
        else kc.setName("залік");
        course.setKnowledgeControl(kc);
        return course;
    }

    private static Course createCourse(CourseName courseName, boolean knowledgeControlHasGrade) {
        Course c = createCourse(knowledgeControlHasGrade);
        c.setCourseName(courseName);
        return c;
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
