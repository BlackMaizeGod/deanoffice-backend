package ua.edu.chdtu.deanoffice.service;

import org.springframework.stereotype.Service;
import ua.edu.chdtu.deanoffice.entity.ApplicationUser;
import ua.edu.chdtu.deanoffice.entity.StudentGroup;
import ua.edu.chdtu.deanoffice.exception.UnauthorizedFacultyDataException;
import ua.edu.chdtu.deanoffice.repository.CurrentYearRepository;
import ua.edu.chdtu.deanoffice.repository.StudentGroupRepository;

import java.util.List;

import static ua.edu.chdtu.deanoffice.Constants.FACULTY_ID;

@Service
public class StudentGroupService {

    private final StudentGroupRepository studentGroupRepository;
    private final CurrentYearRepository currentYearRepository;

    public StudentGroupService(
            StudentGroupRepository studentGroupRepository,
            CurrentYearRepository currentYearRepository
    ) {
        this.studentGroupRepository = studentGroupRepository;
        this.currentYearRepository = currentYearRepository;
    }

    public StudentGroup getById(Integer studentGroupId) {
        return this.studentGroupRepository.findOne(studentGroupId);
    }

    public List<StudentGroup> getGroupsByCourse(int courseId, int facultyId) {
        return studentGroupRepository.findAllByCourse(courseId, facultyId);
    }

    public List<StudentGroup> getGraduateGroups(Integer degreeId, int facultyId) {
        return studentGroupRepository.findGraduateByDegree(degreeId, getCurrentYear(), facultyId);
    }

    private int getCurrentYear() {
        return currentYearRepository.findOne(1).getCurrYear();
    }

    public List<StudentGroup> getGroupsByDegreeAndYear(int degreeId, int year, int facultyId) {
        return studentGroupRepository.findGroupsByDegreeAndYear(degreeId, year, getCurrentYear(), facultyId);
    }

    public List<StudentGroup> getAllByActive(boolean onlyActive, int facultyId) {
        if (onlyActive) {
            return this.studentGroupRepository.findAllActiveByFaculty(facultyId);
        }
        return this.studentGroupRepository.findAllByFaculty(facultyId);
    }

    public List<StudentGroup> getAllGroups(boolean onlyActive) {
        if (onlyActive) {
            return this.studentGroupRepository.findAllActive();
        }
        return this.studentGroupRepository.findAll();
    }

    public StudentGroup save(StudentGroup studentGroup) {
        return studentGroupRepository.save(studentGroup);
    }

    public List<StudentGroup> getByIds(Integer[] groupIds) {
        return studentGroupRepository.findAllByIds(groupIds);
    }

    public void delete(List<StudentGroup> studentGroups) {
        studentGroups.forEach(studentGroup -> studentGroup.setActive(false));
        studentGroupRepository.save(studentGroups);
    }

    public static void verifyAccess(ApplicationUser user, StudentGroup studentGroup) throws UnauthorizedFacultyDataException {
        if (user.getFaculty().getId() != studentGroup.getSpecialization().getFaculty().getId()) {
            throw new UnauthorizedFacultyDataException("Група знаходить в недоступному факультеті для поточного користувача");
        }
    }
}
