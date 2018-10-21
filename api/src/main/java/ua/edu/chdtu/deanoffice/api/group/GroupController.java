package ua.edu.chdtu.deanoffice.api.group;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.chdtu.deanoffice.api.general.ExceptionHandlerAdvice;
import ua.edu.chdtu.deanoffice.api.general.ExceptionToHttpCodeMapUtil;
import ua.edu.chdtu.deanoffice.api.general.dto.NamedDTO;
import ua.edu.chdtu.deanoffice.api.general.mapper.Mapper;
import ua.edu.chdtu.deanoffice.api.group.dto.StudentGroupDTO;
import ua.edu.chdtu.deanoffice.api.group.dto.StudentGroupShortDTO;
import ua.edu.chdtu.deanoffice.api.group.dto.StudentGroupView;
import ua.edu.chdtu.deanoffice.entity.ApplicationUser;
import ua.edu.chdtu.deanoffice.entity.Specialization;
import ua.edu.chdtu.deanoffice.entity.StudentDegree;
import ua.edu.chdtu.deanoffice.entity.StudentGroup;
import ua.edu.chdtu.deanoffice.entity.superclasses.BaseEntity;
import ua.edu.chdtu.deanoffice.exception.NotFoundException;
import ua.edu.chdtu.deanoffice.exception.OperationCannotBePerformedException;
import ua.edu.chdtu.deanoffice.exception.UnauthorizedFacultyDataException;
import ua.edu.chdtu.deanoffice.service.CurrentYearService;
import ua.edu.chdtu.deanoffice.service.SpecializationService;
import ua.edu.chdtu.deanoffice.service.StudentDegreeService;
import ua.edu.chdtu.deanoffice.service.StudentGroupService;
import ua.edu.chdtu.deanoffice.webstarter.security.CurrentUser;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


import static java.util.Arrays.asList;
import static ua.edu.chdtu.deanoffice.api.general.Util.getNewResourceLocation;

@RestController
public class GroupController {
    private final StudentGroupService studentGroupService;
    private final SpecializationService specializationService;
    private final CurrentYearService currentYearService;
    private final StudentDegreeService studentDegreeService;

    @Autowired
    public GroupController(
            StudentGroupService studentGroupService,
            SpecializationService specializationService,
            CurrentYearService currentYearService,
            StudentDegreeService studentDegreeService
    ) {
        this.studentGroupService = studentGroupService;
        this.currentYearService = currentYearService;
        this.specializationService = specializationService;
        this.studentDegreeService = studentDegreeService;
    }

    @JsonView(StudentGroupView.WithStudents.class)
    @GetMapping("/groups/graduates")
    public ResponseEntity getGraduateGroups(@RequestParam int degreeId, @CurrentUser ApplicationUser user) {
        try {
            List<StudentGroup> groups = studentGroupService.getGraduateGroups(degreeId, user.getFaculty().getId());
            return ResponseEntity.ok(Mapper.map(groups, StudentGroupShortDTO.class));
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    @GetMapping("/groups/filter")
    @JsonView(StudentGroupView.WithStudents.class)
    public ResponseEntity getGroupsByDegreeAndYear(
            @RequestParam Integer degreeId,
            @RequestParam Integer year,
            @CurrentUser ApplicationUser user
    ) {
        try {
            List<StudentGroup> groups = studentGroupService.getGroupsByDegreeAndYear(degreeId, year, user.getFaculty().getId());
            return ResponseEntity.ok(Mapper.map(groups, StudentGroupDTO.class));
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    @GetMapping("courses/{courseId}/groups")
    public ResponseEntity getGroupsByCourse(@PathVariable int courseId, @CurrentUser ApplicationUser user) {
        try {
            List<StudentGroup> studentGroups = studentGroupService.getGroupsByCourse(courseId, user.getFaculty().getId());
            return ResponseEntity.ok(Mapper.map(studentGroups, NamedDTO.class));
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    @GetMapping("/groups")
    @JsonView(StudentGroupView.AllGroupData.class)
    public ResponseEntity getActiveGroups(
            @RequestParam(value = "only-active", required = false, defaultValue = "true") boolean onlyActive,
            @CurrentUser ApplicationUser user
    ) {
        try {
            List<StudentGroup> studentGroups = studentGroupService.getAllByActive(onlyActive, user.getFaculty().getId());
            return ResponseEntity.ok(Mapper.map(studentGroups, StudentGroupDTO.class));
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    @JsonView(StudentGroupView.AllGroupData.class)
    @PostMapping("/groups")
    public ResponseEntity createGroup(
            @RequestBody StudentGroupDTO studentGroupDTO,
            @CurrentUser ApplicationUser user) {
        if (studentGroupDTO == null) {
            String exceptionMessage = "Не було отримано жодних даних про групу. " +
                    "Зверніться до адміністратора чи розробника системи.";
            return handleException(new OperationCannotBePerformedException(exceptionMessage));
        }
        if (studentGroupDTO.getId() != null && !studentGroupDTO.getId().equals(0)) {
            String exceptionMessage = "Створити групу [" + studentGroupDTO.getName() + "] неможливо. " +
                    "Зверніться до адміністратора чи розробника системи.";
            return handleException(new OperationCannotBePerformedException(exceptionMessage));
        }
        try {
            StudentGroup studentGroup = create(studentGroupDTO);
            if (studentGroup.getSpecialization().getFaculty().getId() != user.getFaculty().getId()) {
                String exceptionMessage = "Неможливо створити групу в іншому факультеті.";
                throw new UnauthorizedFacultyDataException(exceptionMessage);
            }
            studentGroup.setActive(true);
            StudentGroup studentGroupAfterSaving = studentGroupService.save(studentGroup);
            if (studentGroupAfterSaving != null) {
                URI location = getNewResourceLocation(studentGroup.getId());
                return ResponseEntity.created(location).body(studentGroup);
            } else
                throw new OperationCannotBePerformedException("Групу не вдалося зберегти.");
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    private StudentGroup create(StudentGroupDTO studentGroupDTO) {
        StudentGroup studentGroup = (StudentGroup) Mapper.strictMap(studentGroupDTO, StudentGroup.class);
        Specialization specialization = specializationService.getById(studentGroupDTO.getSpecialization().getId());
        studentGroup.setSpecialization(specialization);
        return studentGroup;
    }

    @JsonView(StudentGroupView.AllGroupData.class)
    @GetMapping("/groups/{group_id}")
    public ResponseEntity getGroupById(@PathVariable(value = "group_id") Integer groupId) {
        try {
            StudentGroup studentGroup = studentGroupService.getById(groupId);
            return ResponseEntity.ok(Mapper.map(studentGroup, StudentGroupDTO.class));
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    @PutMapping("/groups")
    public ResponseEntity updateGroup(@RequestBody StudentGroupDTO studentGroupDTO,
                                      @CurrentUser ApplicationUser user) {
        if (studentGroupDTO == null) {
            String exceptionMessage = "Не було отримано жодних даних про групу. " +
                    "Зверніться до адміністратора чи розробника системи.";
            return handleException(new OperationCannotBePerformedException(exceptionMessage));
        }
        if (studentGroupDTO.getId() == null) {
            String exceptionMessage = "Оновити групу [" + studentGroupDTO.getName() + "] неможливо. " +
                    "Зверніться до адміністратора чи розробника системи.";
            return handleException(new OperationCannotBePerformedException(exceptionMessage));
        } else if (studentGroupDTO.getId().equals(0)) {
            String exceptionMessage = "Оновити групу [" + studentGroupDTO.getName() + "] неможливо. " +
                    "Зверніться до адміністратора чи розробника системи.";
            return handleException(new OperationCannotBePerformedException(exceptionMessage));
        }
        if (!studentGroupDTO.isActive()) {
            String exceptionMessage = "Не можливо оновити дані про групу [" + studentGroupDTO.getName() + "], бо вона не активна.";
            return handleException(new OperationCannotBePerformedException(exceptionMessage));
        }
        try {
            StudentGroup studentGroup = create(studentGroupDTO);
            if (studentGroup.getSpecialization().getFaculty().getId() != user.getFaculty().getId()) {
                String exceptionMessage = "Не можливо оновити дані про групу [" + studentGroup.getName() + "], " +
                        "тому що вона знаходиться в недоступному для поточного користувача факультеті.";
                return handleException(new UnauthorizedFacultyDataException(exceptionMessage));
            }
            List<StudentDegree> studentDegrees = studentDegreeService.getAllByGroupId(studentGroup.getId());
            studentGroup.setStudentDegrees(studentDegrees);
            StudentGroup studentGroupAfterSave = studentGroupService.save(studentGroup);
            if (studentGroupAfterSave == null) {
                throw new OperationCannotBePerformedException("Групу не вдалося зберегти.");
            }
            return ResponseEntity.ok().build();
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    @DeleteMapping("/groups/{group_ids}")
    public ResponseEntity deleteGroup(@PathVariable("group_ids") Integer[] groupIds,
                                      @CurrentUser ApplicationUser user) {
        List<StudentGroup> studentGroups = studentGroupService.getByIds(groupIds);
        if (studentGroups.stream().filter(studentGroup -> studentGroup.getSpecialization().getFaculty().getId() != user.getFaculty().getId()).findAny().isPresent()) {
            String exceptionMessage = "Неможливо видаляти групи із інших факультетів.";
            return handleException(new UnauthorizedFacultyDataException(exceptionMessage));
        }
        if (studentGroups.size() != groupIds.length) {
            return handleException(
                new NotFoundException("Групи не були знайдені "
                        + Arrays.toString(findNotFoundStudentGroups(studentGroups, asList(groupIds))))
            );
        }
        try {
            if (hasInactiveStudentGroup(studentGroups)) {
                String exceptionMessage = "Групи " + Arrays.toString(findInactiveStudentGroups(studentGroups).toArray())
                        + " наразі не активні. Видалення неактивних груп неможливе.";
                return handleException(new OperationCannotBePerformedException(exceptionMessage));
            }
            List<StudentGroup> groupsToDelete = findStudentGroupsInWhichAllStudentsAreInactive(studentGroups);
            studentGroupService.delete(groupsToDelete);
            return ResponseEntity.ok(Mapper.map(groupsToDelete, StudentGroupDTO.class));
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    private List<StudentGroup> findStudentGroupsInWhichAllStudentsAreInactive(List<StudentGroup> studentGroups) {
        return studentGroups.stream()
                .filter(studentGroup -> studentGroup.getActiveStudents().size() == 0)
                .collect(Collectors.toList());
    }

    private Integer[] findNotFoundStudentGroups(List<StudentGroup> found, List<Integer> initial) {
        List<Integer> foundIds = found.stream().map(BaseEntity::getId).collect(Collectors.toList());
        return (Integer[]) initial.stream().filter(integer -> findNouFoundStudentGroup(integer, foundIds)).toArray();
    }

    private boolean findNouFoundStudentGroup(Integer initialId, List<Integer> foundIds) {
        foundIds = foundIds.stream().filter(integer -> integer.equals(initialId)).collect(Collectors.toList());
        return foundIds.size() == 0;
    }

    private boolean hasInactiveStudentGroup(List<StudentGroup> studentGroups) {
        return findInactiveStudentGroups(studentGroups).size() != 0;
    }

    private List<StudentGroup> findInactiveStudentGroups(List<StudentGroup> studentGroups) {
        return studentGroups.stream().filter(studentGroup -> !studentGroup.isActive()).collect(Collectors.toList());
    }

    private ResponseEntity handleException(Exception exception) {
        return ExceptionHandlerAdvice.handleException(exception, GroupController.class, ExceptionToHttpCodeMapUtil.map(exception));
    }
}
