package ua.edu.chdtu.deanoffice.service;

import com.google.common.base.Strings;
import org.springframework.stereotype.Service;
import ua.edu.chdtu.deanoffice.entity.Grade;
import ua.edu.chdtu.deanoffice.entity.StudentDegree;
import ua.edu.chdtu.deanoffice.repository.GradeRepository;
import ua.edu.chdtu.deanoffice.repository.StudentDegreeRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudentDegreeService {
    private final StudentDegreeRepository studentDegreeRepository;
    private final CurrentYearService currentYearService;
    private final GradeRepository gradeRepository;

    public StudentDegreeService(StudentDegreeRepository studentDegreeRepository, CurrentYearService currentYearService,
                                GradeRepository gradeRepository) {
        this.studentDegreeRepository = studentDegreeRepository;
        this.currentYearService = currentYearService;
        this.gradeRepository = gradeRepository;
    }

    public StudentDegree getById(Integer id) {
        return studentDegreeRepository.getById(id);
    }

    public List<StudentDegree> getAllByActive(boolean active, int facultyId) {
        return studentDegreeRepository.findAllByActive(active, facultyId);
    }

    public StudentDegree getFirst(Integer studentId) {
        List<StudentDegree> studentDegrees = this.studentDegreeRepository.findAllByStudentId(studentId);
        return (studentDegrees.isEmpty()) ? null : studentDegrees.get(0);
    }

    public List<StudentDegree> getAllByGroupId(Integer groupId) {
        return this.studentDegreeRepository.findStudentDegreeByStudentGroupIdAndActive(groupId, true);
    }

    public List<StudentDegree> getAllActiveByStudent(Integer studentId) {
        return this.studentDegreeRepository.findAllActiveByStudentId(studentId);
    }

    private String checkGraduateFieldValuesAvailability(StudentDegree studentDegree) {
        String message = "";
        message += Strings.isNullOrEmpty(studentDegree.getDiplomaNumber()) ? "Номер диплома. " : "";
        message += (studentDegree.getDiplomaDate()==null) ? "Дата диплома. " : "";
        message += (studentDegree.getPreviousDiplomaDate()==null) ? "Попередня дата диплома. " : "";
        message += Strings.isNullOrEmpty(studentDegree.getPreviousDiplomaNumber()) ? "Попередній номер диплома. " : "";
        message += Strings.isNullOrEmpty(studentDegree.getPreviousDiplomaIssuedBy()) ? "Попередній диплом виданий. " : "";
        message += (studentDegree.getAdmissionDate()==null) ? "Дата вступу. " : "";
        message += (studentDegree.getProtocolDate()==null) ? "Дата протокола. " : "";
        message += Strings.isNullOrEmpty(studentDegree.getProtocolNumber()) ? "Номер протокола. " : "";
        message += (studentDegree.getSupplementDate()==null) ? "Дата додатка. " : "";
        message += Strings.isNullOrEmpty(studentDegree.getSupplementNumber()) ? "Номер диплома. " : "";
        message += Strings.isNullOrEmpty(studentDegree.getThesisName()) ? "Тема дипломної роботи. " : "";
        message += Strings.isNullOrEmpty(studentDegree.getThesisNameEng()) ? "Тема дипломної роботи англійською. " : "";
        message += Strings.isNullOrEmpty(studentDegree.getStudent().getSurnameEng()) ? "Прізвище англійською мовою. " : "";
        message += Strings.isNullOrEmpty(studentDegree.getStudent().getNameEng()) ? "Ім'я англійською мовою. " : "";
        message += Strings.isNullOrEmpty(studentDegree.getStudent().getPatronimicEng()) ? "По батькові англійською мовою. " : "";
        checkStudentGradesForSupplement(studentDegree);
        return message;
    }

    private String checkStudentGradesForSupplement(StudentDegree studentDegree) {
        List<Grade> grades = gradeRepository.getByCheckStudentGradesForSupplement(studentDegree.getId());
        if (grades == null)
            return "";
        final StringBuilder message = new StringBuilder();
        grades.forEach(grade -> message.append(grade.getCourse().getCourseName().getName() + ", " + grade.getCourse().getSemester() + "сем; "));
        return message.toString();
    }

    public Map<StudentDegree, String> checkAllGraduatesData(int facultyId, int degreeId) {
        int year = currentYearService.getYear();
        List<StudentDegree> studentDegrees = studentDegreeRepository.findAllGraduates(year, facultyId, degreeId);
        return studentDegrees
                .stream()
                .filter(sd -> !checkGraduateFieldValuesAvailability(sd).equals(""))
                .collect(Collectors.toMap(sd -> sd, this::checkGraduateFieldValuesAvailability));
    }

    public Map<StudentDegree, String> checkAllGraduatesGrades(int facultyId, int degreeId) {
        int year = currentYearService.getYear();
        List<StudentDegree> studentDegrees = studentDegreeRepository.findAllGraduates(year, facultyId, degreeId);
        return studentDegrees
                .stream()
                .filter(sd -> !checkStudentGradesForSupplement(sd).equals(""))
                .collect(Collectors.toMap(sd -> sd, this::checkStudentGradesForSupplement));
    }

    public StudentDegree getByStudentIdAndSpecializationId(boolean active,Integer studentId, Integer specializationId){
        return this.studentDegreeRepository.findByStudentIdAndSpecialityId(active,studentId,specializationId);
    }

    public StudentDegree save(StudentDegree studentDegree) {
        return this.studentDegreeRepository.save(studentDegree);
    }

    public void update(List<StudentDegree> studentDegree) {
        studentDegreeRepository.save(studentDegree);
    }

    public List<StudentDegree> getAllNotInImportData(List<Integer> ids, int facultyId, int degreeId, int specialityId){
        return studentDegreeRepository.findAll(StudentDegreeSpecification.getAbsentStudentDegreeInImportData(ids, facultyId, degreeId, specialityId));
    }
}
