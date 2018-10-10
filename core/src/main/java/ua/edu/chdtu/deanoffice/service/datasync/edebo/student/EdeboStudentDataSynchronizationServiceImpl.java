package ua.edu.chdtu.deanoffice.service.datasync.edebo.student;

import com.google.common.base.Strings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.Worksheet;
import ua.edu.chdtu.deanoffice.entity.*;
import ua.edu.chdtu.deanoffice.entity.superclasses.Sex;
import ua.edu.chdtu.deanoffice.service.*;
import ua.edu.chdtu.deanoffice.service.datasync.edebo.student.beans.StudentDegreePrimaryDataBean;
import ua.edu.chdtu.deanoffice.service.datasync.edebo.student.beans.MissingPrimaryDataRedMessageBean;
import ua.edu.chdtu.deanoffice.service.document.DocumentIOService;
import ua.edu.chdtu.deanoffice.util.StringUtil;
import ua.edu.chdtu.deanoffice.util.comparators.EntityUtil;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

@Service
public class EdeboStudentDataSynchronizationServiceImpl implements EdeboStudentDataSyncronizationService {
    private static final String SPECIALIZATION_REGEXP = "([\\d]+\\.[\\d]+)\\s([\\w\\W]+)";
    private static final String SPECIALITY_REGEXP_OLD = "([\\d]\\.[\\d]+)\\s([\\w\\W]+)";
    private static final String SPECIALITY_REGEXP_NEW = "([\\d]{3})\\s([\\w\\W]+)";
    private static final String ADMISSION_REGEXP ="Номер[\\s]+наказу[\\s:]+([\\w\\W]+);[\\W\\w]+Дата[\\s]+наказу[\\s:]*([0-9]{2}.[0-9]{2}.[0-9]{4})";
    private static final String SECONDARY_STUDENT_DEGREE_FIELDS_TO_COMPARE[] = {"payment", "previousDiplomaNumber", "previousDiplomaDate",
            "previousDiplomaType", "previousDiplomaIssuedBy", "supplementNumber", "admissionDate","admissionOrderNumber","admissionOrderDate"};
    private static final String SECONDARY_STUDENT_FIELDS_TO_COMPARE[] = {
            "surnameEng", "nameEng"};
    private static Logger log = LoggerFactory.getLogger(EdeboStudentDataSynchronizationServiceImpl.class);
    private final DocumentIOService documentIOService;
    private final StudentService studentService;
    private final StudentDegreeService studentDegreeService;
    private final DegreeService degreeService;
    private final FacultyService facultyService;
    private final SpecialityService specialityService;
    private final SpecializationService specializationService;

    @Autowired
    public EdeboStudentDataSynchronizationServiceImpl(DocumentIOService documentIOService, StudentService studentService, StudentDegreeService studentDegreeService,
                                                      DegreeService degreeService, SpecialityService specialityService, SpecializationService specializationService,
                                                      FacultyService facultyService) {
        this.documentIOService = documentIOService;
        this.studentService = studentService;
        this.studentDegreeService = studentDegreeService;
        this.degreeService = degreeService;
        this.specialityService = specialityService;
        this.specializationService = specializationService;
        this.facultyService = facultyService;
    }

    private List<ImportedData> getStudentDegreesFromStream(InputStream xlsxInputStream) throws IOException, Docx4JException {
        return getEdeboStudentDegreesInfo(xlsxInputStream);
    }

    private List<ImportedData> getEdeboStudentDegreesInfo(Object source) throws IOException, Docx4JException {
        SpreadsheetMLPackage xlsxPkg;
        if (source instanceof String) {
            xlsxPkg = documentIOService.loadSpreadsheetDocument((String) source);
        } else {
            xlsxPkg = documentIOService.loadSpreadsheetDocument((InputStream) source);
        }
        return getImportedDataFromXlsxPkg(xlsxPkg);
    }

    private List<ImportedData> getImportedDataFromXlsxPkg(SpreadsheetMLPackage xlsxPkg) {
        try {
            WorkbookPart workbookPart = xlsxPkg.getWorkbookPart();
            WorksheetPart sheetPart = workbookPart.getWorksheet(0);
            Worksheet worksheet = sheetPart.getContents();
            org.xlsx4j.sml.SheetData sheetData = worksheet.getSheetData();
            DataFormatter formatter = new DataFormatter();
            SheetData sd = new SheetData();
            List<ImportedData> importedData = new ArrayList<>();
            String cellValue;

            for (Row r : sheetData.getRow()) {
                log.debug("importing row: " + r.getR());
                for (Cell c : r.getC()) {
                    cellValue = "";
                    try {
                        cellValue = StringUtil.replaceSingleQuotes(formatter.formatCellValue(c));
                    } catch (Exception e) {
                        log.debug(e.getMessage());
                    }
                    if (r.getR() == 1) {
                        sd.assignHeader(cellValue, c.getR());
                    } else {
                        sd.setCellData(c.getR(), cellValue.trim());
                    }
                }
                if (r.getR() == 1) {
                    continue;
                }
                importedData.add(sd.getStudentData());
                sd.cleanStudentData();
            }
            return importedData;
        } catch (Exception e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public EdeboStudentDataSynchronizationReport getEdeboDataSynchronizationReport(InputStream xlsxInputStream, String facultyName) throws Exception {
        if (xlsxInputStream == null)
            throw new Exception("Помилка читання файлу");
        try {
            List<ImportedData> importedData = getStudentDegreesFromStream(xlsxInputStream);
            Objects.requireNonNull(importedData);
            EdeboStudentDataSynchronizationReport edeboDataSyncronizationReport = new EdeboStudentDataSynchronizationReport();
            for (ImportedData data : importedData) {
                addSynchronizationReportForImportedData(data, edeboDataSyncronizationReport, facultyName);
            }
            return edeboDataSyncronizationReport;
        } catch (Docx4JException e) {
            e.printStackTrace();
            throw new Exception("Помилка обробки файлу");
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Помилка читання файлу");
        } finally {
            xlsxInputStream.close();
        }
    }

    @Override
    public boolean isCriticalDataAvailable(StudentDegree studentDegree) throws RuntimeException {
        List<String> necessaryNotEmptyStudentDegreeData = new ArrayList<>();
        Specialization specialization = studentDegree.getSpecialization();
        Student student = studentDegree.getStudent();
        String specialityName = specialization.getSpeciality().getName();
        necessaryNotEmptyStudentDegreeData.add(student.getSurname());
        necessaryNotEmptyStudentDegreeData.add(student.getName());
        necessaryNotEmptyStudentDegreeData.add(specialization.getDegree().getName());
        necessaryNotEmptyStudentDegreeData.add(specialization.getFaculty().getName());
        for (String s : necessaryNotEmptyStudentDegreeData) {
            if (Strings.isNullOrEmpty(s)) {
                return false;
            }
        }
        if (student.getBirthDate() == null || Strings.isNullOrEmpty(specialityName) ||
                studentDegree.getSpecialization().getDegree() == null) {
            return false;
        }
        return true;
    }

    private boolean isSpecializationPatternMatch(ImportedData importedData) {
        String specialityName = importedData.getFullSpecialityName();
        String specializationName = importedData.getFullSpecializationName();
        String programName = importedData.getProgramName();
        Pattern specialityPattern = Pattern.compile(SPECIALITY_REGEXP_OLD);
        Matcher specialityMatcher = specialityPattern.matcher(specialityName);
        if (specialityMatcher.matches()) {
            return true;
        } else {
            specialityPattern = Pattern.compile(SPECIALITY_REGEXP_NEW);
            specialityMatcher = specialityPattern.matcher(specialityName);
            if (specialityMatcher.matches()) {
                Pattern specializationPattern = Pattern.compile(SPECIALIZATION_REGEXP);
                Matcher specializationMatcher = specializationPattern.matcher(specializationName);
                if (specializationMatcher.matches()) {
                    return true;
                } else {
                    if (Strings.isNullOrEmpty(specializationName) && !Strings.isNullOrEmpty(programName)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
    }

    @Override
    public Student getStudentFromData(ImportedData data) {
        Student student = new Student();
        Date birthDate = parseDate(data.getBirthday());
        student.setBirthDate(birthDate);
        student.setName(data.getFirstName());
        student.setSurname(data.getLastName());
        student.setPatronimic(data.getMiddleName());
        student.setNameEng(data.getFirstNameEn());
        student.setSurnameEng(data.getLastNameEn());
        student.setPatronimicEng(data.getMiddleNameEn());
        student.setSex("Чоловіча".equals(data.getPersonsSexName()) ? Sex.MALE : Sex.FEMALE);
        return student;
    }

    @Override
    public Speciality getSpecialityFromData(ImportedData data) {
        Speciality speciality = new Speciality();
        String specialityNameWithCode = data.getFullSpecialityName();
        String specialityCode = "";
        String specialityName = "";
        Pattern specialityPattern;
        if (specialityNameWithCode.matches(SPECIALITY_REGEXP_OLD)) {
            specialityPattern = Pattern.compile(SPECIALITY_REGEXP_OLD);
        } else {
            specialityPattern = Pattern.compile(SPECIALITY_REGEXP_NEW);
        }
        Matcher matcher = specialityPattern.matcher(specialityNameWithCode);
        if (matcher.matches() && matcher.groupCount() > 1) {
            specialityCode = matcher.group(1).trim();
            specialityName = matcher.group(2).trim();
        }
        speciality.setCode(specialityCode);
        speciality.setName(specialityName);
        return speciality;
    }

    @Override
    public Specialization getSpecializationFromData(ImportedData data) {
        Specialization specialization = new Specialization();
        specialization.setName("");
        specialization.setCode("");
        Speciality speciality = getSpecialityFromData(data);
        specialization.setSpeciality(speciality);
        Faculty faculty = new Faculty();
        String specializationName = data.getFullSpecializationName();
        if ((speciality.getCode()+" "+speciality.getName()).matches(SPECIALITY_REGEXP_NEW)) {
            if (!Strings.isNullOrEmpty(specializationName)) {
                Pattern specializationPattern = Pattern.compile(SPECIALIZATION_REGEXP);
                Matcher spMatcher = specializationPattern.matcher(specializationName);
                if (spMatcher.matches() && spMatcher.groupCount() > 1) {
                    specialization.setCode(spMatcher.group(1));
                    specialization.setName(spMatcher.group(2));
                }
            } else {
                specialization.setName(data.getProgramName());
            }
        }
        specialization.setDegree(DegreeEnum.getDegreeFromEnumByName(data.getQualificationGroupName()));
        faculty.setName(data.getFacultyName());
        specialization.setFaculty(faculty);
        return specialization;
    }

    @Override
    public StudentDegree getStudentDegreeFromData(ImportedData data) {
        Student student = getStudentFromData(data);
        Specialization specialization = getSpecializationFromData(data); //getSpeciality inside
        StudentDegree studentDegree = new StudentDegree();
        studentDegree.setActive(true);
        studentDegree.setStudent(student);
        studentDegree.setSpecialization(specialization);

        studentDegree.setSupplementNumber(data.getEducationId());
        studentDegree.setPayment(Payment.getPaymentFromUkrName(data.getPersonEducationPaymentTypeName()));
        studentDegree.setPreviousDiplomaDate(parseDate(data.getDocumentDateGet2()));
        studentDegree.setPreviousDiplomaIssuedBy(data.getDocumentIssued2());
        studentDegree.setPreviousDiplomaNumber(data.getDocumentSeries2() + " № " + data.getDocumentNumbers2());
        studentDegree.setPreviousDiplomaType(EducationDocument.getEducationDocumentByName(data.getPersonDocumentTypeName()));
        studentDegree.setAdmissionDate(parseDate(data.getEducationDateBegin()));
        Map<String,Object> admissionOrderNumberAndDate = getAdmissionOrderNumberAndDate(data.getRefillInfo());
        studentDegree.setAdmissionOrderNumber((String)admissionOrderNumberAndDate.get("admissionOrderNumber"));
        studentDegree.setAdmissionOrderDate((Date)admissionOrderNumberAndDate.get("admissionOrderDate"));
        return studentDegree;
    }

    public Map<String,Object> getAdmissionOrderNumberAndDate(String refillInfo) {
        Map<String,Object> admissionOrderNumberAndDate = new HashMap<>();
        DateFormat admissionOrderDateFormatter = new SimpleDateFormat("dd.MM.yyyy");
        Pattern admissionPattern = Pattern.compile(ADMISSION_REGEXP);
        try {
            Matcher matcher = admissionPattern.matcher(refillInfo);
            if (matcher.find()) {
                admissionOrderNumberAndDate.put("admissionOrderNumber",matcher.groupCount() > 0 ? matcher.group(1) : "");
                Date admissionOrderDate = matcher.groupCount() > 1 ? admissionOrderDateFormatter.parse(matcher.group(2)) : null;
                admissionOrderNumberAndDate.put("admissionOrderDate",admissionOrderDate);
                return admissionOrderNumberAndDate;
            }
        } catch (ParseException e) {
            log.debug(e.getMessage());
        } catch (IllegalStateException e) {
            log.debug(e.getMessage());
        }
        return admissionOrderNumberAndDate;
    }

    @Override
    public void addSynchronizationReportForImportedData(ImportedData importedData, EdeboStudentDataSynchronizationReport edeboDataSyncronizationReport, String facultyName) {
        if (!(facultyName.toUpperCase().equals(importedData.getFacultyName().toUpperCase())))
            return;
        StudentDegree studentDegreeFromData;
        if (isSpecializationPatternMatch(importedData)) {
            studentDegreeFromData = getStudentDegreeFromData(importedData);
            if (!isCriticalDataAvailable(studentDegreeFromData)) {
                String message = "Недостатньо інформації для синхронізації";
                edeboDataSyncronizationReport.addMissingPrimaryDataRed(new MissingPrimaryDataRedMessageBean(message, new StudentDegreePrimaryDataBean(importedData)));
                return;
            }
        } else {
            String message = "Неправильна спеціалізація";
            edeboDataSyncronizationReport.addMissingPrimaryDataRed(new MissingPrimaryDataRedMessageBean(message, new StudentDegreePrimaryDataBean(importedData)));
            return;
        }

        Specialization specializationFromData = studentDegreeFromData.getSpecialization();
        Faculty facultyFromDb = facultyService.getByName(specializationFromData.getFaculty().getName());
        if (facultyFromDb == null){
            String message = "Даний факультет відсутній";
            edeboDataSyncronizationReport.addMissingPrimaryDataRed(new MissingPrimaryDataRedMessageBean(message, new StudentDegreePrimaryDataBean(importedData)));
            return;
        }

        Speciality specialityFromDb = specialityService.findSpecialityByCodeAndName(specializationFromData.getSpeciality().getCode(),
                specializationFromData.getSpeciality().getName());
        if (specialityFromDb == null){
            String message = "Дана спеціальність відсутня";
            edeboDataSyncronizationReport.addMissingPrimaryDataRed(new MissingPrimaryDataRedMessageBean(message, new StudentDegreePrimaryDataBean(importedData)));
            return;
        }
        Specialization specializationFromDB = specializationService.getByNameAndDegreeAndSpecialityAndFaculty(
                specializationFromData.getName(),
                specializationFromData.getDegree().getId(),
                specialityFromDb.getId(),
                facultyFromDb.getId());
        if (specializationFromDB == null){
            String message = "Дана спеціалізація відсутня";
            edeboDataSyncronizationReport.addMissingPrimaryDataRed(new MissingPrimaryDataRedMessageBean(message, new StudentDegreePrimaryDataBean(importedData)));
            return;
        } else {
            studentDegreeFromData.setSpecialization(specializationFromDB);
        }

        Student studentFromData = studentDegreeFromData.getStudent();
        Student studentFromDB = studentService.searchByFullNameAndBirthDate(
                studentFromData.getName(),
                studentFromData.getSurname(),
                studentFromData.getPatronimic(),
                studentFromData.getBirthDate()
        );

        if (studentFromDB == null) {
            edeboDataSyncronizationReport.addNoSuchStudentOrStudentDegreeInDbOrange(studentDegreeFromData);
            return;
        }

        StudentDegree studentDegreeFromDb = studentDegreeService.getByStudentIdAndSpecializationId(true, studentFromDB.getId(), specializationFromDB.getId());
        if (studentDegreeFromDb == null){
            edeboDataSyncronizationReport.addNoSuchStudentOrStudentDegreeInDbOrange(studentDegreeFromData);
            return;
        }

        if (isSecondaryFieldsMatch(studentDegreeFromData, studentDegreeFromDb)) {
            edeboDataSyncronizationReport.addSyncohronizedDegreeGreen(new StudentDegreePrimaryDataBean(studentDegreeFromData));
        } else {
            edeboDataSyncronizationReport.addUnmatchedSecondaryDataStudentDegreeBlue(studentDegreeFromData,studentDegreeFromDb);
        }
    }

    public boolean isSecondaryFieldsMatch(StudentDegree studentDegreeFromFile, StudentDegree studentDegreeFromDb) {
        return (EntityUtil.isValuesOfFieldsReturnedByGettersMatch(studentDegreeFromFile, studentDegreeFromDb, SECONDARY_STUDENT_DEGREE_FIELDS_TO_COMPARE) &&
               EntityUtil.isValuesOfFieldsReturnedByGettersMatch(studentDegreeFromFile.getStudent(),studentDegreeFromDb.getStudent(),SECONDARY_STUDENT_FIELDS_TO_COMPARE));
    }

    private Date parseDate(String fileDate) {
        try {
            DateFormat formatter = new SimpleDateFormat("M/dd/yy H:mm");
            return formatter.parse(fileDate);
        } catch (ParseException e) {
            log.debug(e.getMessage());
        }
        return null;
    }
}