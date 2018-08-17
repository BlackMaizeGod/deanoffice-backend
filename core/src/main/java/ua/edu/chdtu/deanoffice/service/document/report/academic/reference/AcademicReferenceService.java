package ua.edu.chdtu.deanoffice.service.document.report.academic.reference;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.edu.chdtu.deanoffice.entity.Grade;
import ua.edu.chdtu.deanoffice.entity.Student;
import ua.edu.chdtu.deanoffice.entity.StudentDegree;
import ua.edu.chdtu.deanoffice.entity.StudentExpel;
import ua.edu.chdtu.deanoffice.service.GradeService;
import ua.edu.chdtu.deanoffice.service.StudentExpelService;
import ua.edu.chdtu.deanoffice.service.StudentGroupService;
import ua.edu.chdtu.deanoffice.service.document.DocumentIOService;
import ua.edu.chdtu.deanoffice.service.document.FileFormatEnum;
import ua.edu.chdtu.deanoffice.util.PersonUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static ua.edu.chdtu.deanoffice.service.document.DocumentIOService.TEMPLATES_PATH;
import static ua.edu.chdtu.deanoffice.service.document.TemplateUtil.*;
import static ua.edu.chdtu.deanoffice.util.LanguageUtil.transliterate;

@Service
public class AcademicReferenceService {

    private static final String TEMPLATE = TEMPLATES_PATH + "AcademicCertificate.docx";

    private static final int INDEX_OF_TABLE_WITH_GRADES = 10;

    private static final String DOCUMENT_DELIMITER = "/";

    @Autowired
    private DocumentIOService documentIOService;

    @Autowired
    private StudentGroupService studentGroupService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private StudentExpelService studentExpelService;

    public File formDocument(int studentExpelId) throws Docx4JException, IOException {
        StudentExpel studentExpel = studentExpelService.getById(studentExpelId);
        StudentDegree studentDegree = studentExpel.getStudentDegree();
        Student student = studentDegree.getStudent();
        List<List<Grade>> grades = gradeService.getGradesByStudentDegreeId(studentDegree.getId());
        StudentSummaryForAcademicReference studentSummary = new StudentSummaryForAcademicReference(studentDegree, grades);
        WordprocessingMLPackage resultTemplate = formDocument(TEMPLATE, studentSummary, studentExpel);
        String fileName = transliterate(student.getName() + " " + student.getSurname());
        return documentIOService.saveDocumentToTemp(resultTemplate, fileName, FileFormatEnum.DOCX);
    }

    private WordprocessingMLPackage formDocument(String templateFilepath, StudentSummaryForAcademicReference studentSummary, StudentExpel studentExpel)
            throws Docx4JException {
        WordprocessingMLPackage template = documentIOService.loadTemplate(templateFilepath);
        prepareTable(template, studentSummary);
        replaceTextPlaceholdersInTemplate(template, getStudentInfoDictionary(studentSummary, studentExpel));
        return template;
    }


    private HashMap<String, String> getStudentInfoDictionary(StudentSummaryForAcademicReference studentSummary, StudentExpel studentExpel) {
        HashMap<String, String> result = new HashMap();
        StudentDegree studentDegree = studentSummary.getStudentDegree();
        Student student = studentDegree.getStudent();
        result.put("studentNameUkr", student.getFullNameUkr());
        String studentNameEng;
        if (student.getNameEng() == null || student.getSurnameEng() == null) {
            studentNameEng = transliterate(student.getName() + " " + student.getSurname());
        } else {
            studentNameEng = student.getSurnameEng() + " " + student.getNameEng();
        }
        result.put("studentNameEng", studentNameEng);
        result.put("facultyNameUkr", studentDegree.getSpecialization().getDepartment().getFaculty().getName());
        result.put("facultyNameEng", studentDegree.getSpecialization().getDepartment().getFaculty().getNameEng());
        String code = studentDegree.getSpecialization().getSpeciality().getCode();
        result.put("specialityUkr", code + " " + studentDegree.getSpecialization().getSpeciality().getName());
        result.put("specialityEng", code + " " + studentDegree.getSpecialization().getSpeciality().getNameEng());
        result.put("educationalProgramUkr", studentDegree.getSpecialization().getName());
        result.put("educationalProgramEng", studentDegree.getSpecialization().getNameEng());
        result.put("birthDate", formatDate(student.getBirthDate()));
        result.put("individualNumber",studentDegree.getSupplementNumber());
        result.put("dean", PersonUtil.makeInitialsSurnameLast(studentDegree.getSpecialization().getDepartment().getFaculty().getDean()));
        result.put("deanEng", PersonUtil.makeInitialsSurnameLast(studentDegree.getSpecialization().getDepartment().getFaculty().getDeanEng()));
        result.put("programHeadNameUkr", studentDegree.getSpecialization().getEducationalProgramHeadName());
        result.put("programHeadInfoUkr", studentDegree.getSpecialization().getEducationalProgramHeadInfo());
        result.put("programHeadNameEng", studentDegree.getSpecialization().getEducationalProgramHeadNameEng());
        result.put("programHeadInfoEng", studentDegree.getSpecialization().getEducationalProgramHeadInfoEng());

        result.put("startStudy", formatDate(studentDegree.getAdmissionDate()));
        result.put("endStudy", formatDate(studentExpel.getExpelDate()));
        result.put("expelReasonUkr", studentExpel.getOrderReason().getName());
        result.put("expelReasonEng", studentExpel.getOrderReason().getNameEng());
        result.put("orderUkr", " від "+formatDate(studentExpel.getOrderDate())+" № "+studentExpel.getOrderNumber());
        result.put("orderEng", formatDate(studentExpel.getOrderDate())+", № "+studentExpel.getOrderNumber());
        result.put("today", formatDate(new Date()));
        return result;
    }

    private void prepareTable(WordprocessingMLPackage template, StudentSummaryForAcademicReference studentSummary) {
        Tbl table = (Tbl) getAllElementsFromObject(template.getMainDocumentPart(), Tbl.class).get(INDEX_OF_TABLE_WITH_GRADES);
        prepareRows(table, studentSummary);
    }

    private void prepareRows(Tbl table, StudentSummaryForAcademicReference studentSummary) {
        List<Tr> tableRows = (List<Tr>) (Object) getAllElementsFromObject(table, Tr.class);
        Tr rowWithSignature = tableRows.get(1);
        Tr rowWithCourse = tableRows.get(2);
        int currentSemester = 1;
        int currentRow = 2;
        for (SemesterDetails semesterDetails : studentSummary.getSemesters()) {
            Tr newRowWithSignature = XmlUtils.deepCopy(rowWithSignature);
            replaceInRow(newRowWithSignature, getSignatureDictionary(currentSemester));
            table.getContent().add(currentRow, newRowWithSignature);
            currentRow++;
            for (Grade grade : semesterDetails.getGrades().get(0)) {
                Tr newRowWithCourse = XmlUtils.deepCopy(rowWithCourse);
                replaceInRow(newRowWithCourse, getCourseDictionary(grade));
                table.getContent().add(currentRow, newRowWithCourse);
                currentRow++;
            }
            currentSemester++;
        }
        table.getContent().remove(1);
        table.getContent().remove(table.getContent().size()-1);
    }

    private Map<String, String> getSignatureDictionary(int semester) {
        HashMap<String, String> result = new HashMap<>();
        int coursePart = (semester - 1) / 2 + 1;
        int semesterPart = (semester - 1) % 2 + 1;
        String semesterDisplay;
        if (semesterPart == 1) {
            semesterDisplay = "I";
        } else {
            semesterDisplay = "ІI";
        }
        String ukrainianPart = coursePart + " курс" + " " + semesterDisplay + " семестр";
        String englishPart = coursePart + " year" + " " + semesterDisplay + " semester";
        result.put("n", ukrainianPart + DOCUMENT_DELIMITER + englishPart);
        return result;
    }

    private Map<String, String> getCourseDictionary(Grade grade) {
        HashMap<String, String> result = new HashMap<>();
        result.put("s", grade.getCourse().getCourseName().getName() + DOCUMENT_DELIMITER + grade.getCourse().getCourseName().getNameEng());
        result.put("c", grade.getCourse().getCredits().toString());
        result.put("g", getGradeDisplay(grade));
        return result;
    }

    private String getGradeDisplay(Grade grade) {
        String result = "";
        result += grade.getPoints() + DOCUMENT_DELIMITER;
        result += grade.getEcts() + DOCUMENT_DELIMITER;
        result += grade.getEcts().getNationalGradeUkr(grade) + DOCUMENT_DELIMITER;
        result += grade.getEcts().getNationalGradeEng(grade);
        return result;
    }

    private String formatDate(Date date) {
        String result;
        if (date != null) {
            result = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
        } else {
            result = "";
        }
        return result;
    }
}
