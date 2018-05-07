package ua.edu.chdtu.deanoffice.service.document.report.exam;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ua.edu.chdtu.deanoffice.entity.Course;
import ua.edu.chdtu.deanoffice.entity.CourseForGroup;
import ua.edu.chdtu.deanoffice.entity.Grade;
import ua.edu.chdtu.deanoffice.entity.Speciality;
import ua.edu.chdtu.deanoffice.entity.Student;
import ua.edu.chdtu.deanoffice.entity.StudentDegree;
import ua.edu.chdtu.deanoffice.entity.StudentGroup;
import ua.edu.chdtu.deanoffice.service.CourseForGroupService;
import ua.edu.chdtu.deanoffice.service.CurrentYearService;
import ua.edu.chdtu.deanoffice.service.GradeService;
import ua.edu.chdtu.deanoffice.service.document.DocumentIOService;
import ua.edu.chdtu.deanoffice.util.comparators.PersonFullNameComparator;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ua.edu.chdtu.deanoffice.service.document.TemplateUtil.findTable;
import static ua.edu.chdtu.deanoffice.service.document.TemplateUtil.getAllElementsFromObject;
import static ua.edu.chdtu.deanoffice.service.document.TemplateUtil.replaceInRow;
import static ua.edu.chdtu.deanoffice.service.document.TemplateUtil.replacePlaceholdersWithBlank;
import static ua.edu.chdtu.deanoffice.service.document.TemplateUtil.replaceTextPlaceholdersInTemplate;
import static ua.edu.chdtu.deanoffice.util.PersonUtil.makeInitials;

@Service
public class MultiGroupReportTemplateFillService {
    private static final int STARTING_ROW_INDEX = 7;
    private static final Logger log = LoggerFactory.getLogger(ExamReportTemplateFillService.class);

    private CourseForGroupService courseForGroupService;
    private DocumentIOService documentIOService;
    private GradeService gradeService;
    private CurrentYearService currentYearService;

    public MultiGroupReportTemplateFillService(CourseForGroupService courseForGroupService,
                                               DocumentIOService documentIOService,
                                               GradeService gradeService,
                                               CurrentYearService currentYearService) {
        this.courseForGroupService = courseForGroupService;
        this.documentIOService = documentIOService;
        this.gradeService = gradeService;
        this.currentYearService = currentYearService;
    }

    public WordprocessingMLPackage fillTemplate(String templateName, List<StudentGroup> groups, Course course)
            throws IOException, Docx4JException {
        CourseForGroup courseForGroup = new CourseForGroup();
        if (!groups.isEmpty()) {
            courseForGroup = courseForGroupService.getCourseForGroup(groups.get(0).getId(), course.getId());
        }
        WordprocessingMLPackage template = documentIOService.loadTemplate(templateName);
        fillTableWithStudentInitials(template, groups, course);
        Map<String, String> commonDict = new HashMap<>();
        commonDict.putAll(getGroupInfoReplacements(courseForGroup));
        commonDict.put("GroupName", getGroupNames(groups));
        commonDict.putAll(getCourseInfoReplacements(courseForGroup));

        replaceTextPlaceholdersInTemplate(template, commonDict);
        return template;
    }

    private void fillTableWithStudentInitials(WordprocessingMLPackage template, List<StudentGroup> studentGroups, Course course) {
        List<Object> tables = getAllElementsFromObject(template.getMainDocumentPart(), Tbl.class);
        String tableWithGradesKey = "№";
        Tbl tempTable = findTable(tables, tableWithGradesKey);
        if (tempTable == null) {
            log.warn("Couldn't find table that contains: " + tableWithGradesKey);
            return;
        }
        List<Object> gradeTableRows = getAllElementsFromObject(tempTable, Tr.class);

        int currentRowIndex = STARTING_ROW_INDEX;
        for (StudentGroup studentGroup : studentGroups) {
            currentRowIndex = fillWithStudentsInfo(studentGroup, course, gradeTableRows, currentRowIndex);
        }
        removeUnfilledPlaceholders(template);
    }

    private int fillWithStudentsInfo(StudentGroup studentGroup, Course course, List<Object> gradeTableRows, int currentRowIndex) {
        List<StudentDegree> studentDegrees = studentGroup.getStudentDegrees();
        List<Student> students = studentDegrees.stream().filter(studentDegree -> {
            Grade grade = gradeService.getGradeForStudentAndCourse(studentDegree.getId(), course.getId());
            return grade == null
                    || grade.getPoints() == null || grade.getPoints() < 60
                    || grade.getGrade() == null || grade.getGrade() < 3;
        }).map(StudentDegree::getStudent).sorted(new PersonFullNameComparator()).collect(Collectors.toList());

        Tr currentRow = (Tr) gradeTableRows.get(currentRowIndex);
        Map<String, String> groupNameReplacement = new HashMap<>();
        groupNameReplacement.put("StudentInitials", studentGroup.getName());
        replaceInRow(currentRow, groupNameReplacement);
        currentRowIndex++;

        for (Student student : students) {
            currentRow = (Tr) gradeTableRows.get(currentRowIndex);
            Map<String, String> replacements = new HashMap<>();
            replacements.put("StudentInitials", student.getInitialsUkr());
            replacements.put("RecBook", studentGroup.getStudentDegrees().stream().filter(studentDegree ->
                    studentDegree.getStudent().equals(student)).findFirst().get().getRecordBookNumber());
            replaceInRow(currentRow, replacements);
            currentRowIndex++;
        }
        return currentRowIndex;
    }

    private void removeUnfilledPlaceholders(WordprocessingMLPackage template) {
        Set<String> placeholdersToRemove = new HashSet<>();
        placeholdersToRemove.add("#StudentInitials");
        placeholdersToRemove.add("#RecBook");
        replacePlaceholdersWithBlank(template, placeholdersToRemove);
    }

    private Map<String, String> getCourseInfoReplacements(CourseForGroup courseForGroup) {
        Course course = courseForGroup.getCourse();
        Map<String, String> result = new HashMap<>();
        result.put("CourseName", course.getCourseName().getName());
        result.put("Hours", String.format("%d", course.getHours()));

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        if (courseForGroup.getExamDate() != null) {
            result.put("ExamDate", dateFormat.format(courseForGroup.getExamDate()));
        } else {
            result.put("ExamDate", "");
        }
        result.put("Course", String.format("%d", Calendar.getInstance().get(Calendar.YEAR) - courseForGroup.getStudentGroup().getCreationYear()));
        result.put("KCType", course.getKnowledgeControl().getName());
        result.put("TeacherName", courseForGroup.getTeacher().getFullNameUkr());
        result.put("TeacherInitials", courseForGroup.getTeacher().getInitialsUkr());
        result.put("Semester", String.format("%d-й", courseForGroup.getCourse().getSemester()));

        return result;
    }

    private Map<String, String> getGroupInfoReplacements(CourseForGroup courseForGroup) {
        Map<String, String> result = new HashMap<>();
        StudentGroup studentGroup = courseForGroup.getStudentGroup();
        Speciality speciality = studentGroup.getSpecialization().getSpeciality();
        result.put("Specialization", speciality.getCode() + " " + speciality.getName());
        result.put("DeanInitials", makeInitials(studentGroup.getSpecialization().getDepartment().getFaculty().getDean()));
        result.put("Degree", studentGroup.getSpecialization().getDegree().getName());
        result.put("StudyYear", getStudyYear());

        return result;
    }

    private String getGroupNames(List<StudentGroup> studentGroups) {
        StringBuilder result = new StringBuilder();
        for (StudentGroup group : studentGroups) {
            result.append(group.getName()).append((studentGroups.indexOf(group) != studentGroups.size() - 1 ? ", " : ""));
        }
        return result.toString();
    }

    private String getStudyYear() {
        int currentYear = currentYearService.get().getCurrYear();
        return String.format("%4d-%4d", currentYear, currentYear + 1);
    }
}