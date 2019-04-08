package ua.edu.chdtu.deanoffice.service.security;

import org.springframework.stereotype.Service;
import ua.edu.chdtu.deanoffice.entity.ApplicationUser;
import ua.edu.chdtu.deanoffice.entity.Department;
import ua.edu.chdtu.deanoffice.entity.StudentDegree;
import ua.edu.chdtu.deanoffice.entity.StudentGroup;
import ua.edu.chdtu.deanoffice.exception.UnauthorizedFacultyDataException;
import ua.edu.chdtu.deanoffice.repository.DepartmentRepository;
import ua.edu.chdtu.deanoffice.repository.StudentDegreeRepository;
import ua.edu.chdtu.deanoffice.repository.TeacherRepository;

import java.util.List;

@Service
public class FacultyAuthorizationService {
    private final StudentDegreeRepository studentDegreeRepository;
    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;

    public FacultyAuthorizationService(StudentDegreeRepository studentDegreeRepository,
                                       DepartmentRepository departmentRepository,
                                       TeacherRepository teacherRepository) {
        this.studentDegreeRepository = studentDegreeRepository;
        this.departmentRepository = departmentRepository;
        this.teacherRepository = teacherRepository;
    }

    public void verifyAccessibilityOfStudentGroup(ApplicationUser user, StudentGroup studentGroup) throws UnauthorizedFacultyDataException {
        if (user.getFaculty().getId() != studentGroup.getSpecialization().getFaculty().getId()) {
            throw new UnauthorizedFacultyDataException("Група знаходить в недоступному факультеті для поточного користувача");
        }
    }

    public void verifyAccessibilityOfGroupAndStudents(
            ApplicationUser user, List<StudentDegree> studentDegrees,
            StudentGroup studentGroup) throws UnauthorizedFacultyDataException
    {
        verifyAccessibilityOfStudentGroup(user, studentGroup);
        verifyAccessibilityOfStudentDegrees(user, studentDegrees);
    }

    public void verifyAccessibilityOfStudentDegrees(ApplicationUser user, List<StudentDegree> studentDegrees) throws UnauthorizedFacultyDataException {
        if (studentDegrees.stream().anyMatch(studentDegree -> studentDegree.getSpecialization().getFaculty().getId() != user.getFaculty().getId())) {
            throw new UnauthorizedFacultyDataException("Вибрані студенти навчаються на недоступному для користувача факультеті");
        }
    }

    public void verifyAccessibilityOfStudentDegrees(List<Integer> studentDegreeIds, ApplicationUser user) throws UnauthorizedFacultyDataException {
        List <Integer> studentDegreeIdFromDb = studentDegreeRepository.findIdsByIdsAndFacultyId(studentDegreeIds,user.getFaculty().getId());
        if (studentDegreeIdFromDb.size() != 0)
            throw new UnauthorizedFacultyDataException("Вибрані студенти навчаються на недоступному для користувача факультеті");

    }

    public void verifyAccessibilityOfDepartment(ApplicationUser user, Department department) throws UnauthorizedFacultyDataException {
        if (user.getFaculty().getId() != department.getFaculty().getId())
            throw new UnauthorizedFacultyDataException("Вибрана кафедра є недоступною для даного користувача!");
    }

    public void verifyAccessibilityOfDepartments(ApplicationUser user, List<Integer> teacherIds) throws UnauthorizedFacultyDataException {
        List<Integer> teacherIdsFromDb = teacherRepository.findIdsEveryoneWhoDoesNotBelongToThisFacultyId(user.getFaculty().getId(), teacherIds);
        if (teacherIdsFromDb.size() != 0)
            throw new UnauthorizedFacultyDataException("Тут присутні ідентифікатори викладачів, які не відносяться до поточного факультету!");
    }
}
