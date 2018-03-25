package ua.edu.chdtu.deanoffice.api.group;

import com.fasterxml.jackson.annotation.JsonView;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.chdtu.deanoffice.api.group.dto.StudentGroupDTO;
import ua.edu.chdtu.deanoffice.api.group.dto.StudentGroupView;
import ua.edu.chdtu.deanoffice.entity.StudentGroup;
import ua.edu.chdtu.deanoffice.service.CourseForGroupService;
import ua.edu.chdtu.deanoffice.service.GroupService;
import ua.edu.chdtu.deanoffice.service.document.diploma.supplement.GraduateService;

import java.lang.reflect.Type;
import java.util.List;

@RestController
@RequestMapping("/groups")
public class StudentGroupController {
    private GraduateService graduateService;
    private GroupService groupService;
    private CourseForGroupService courseForGroupService;

    @Autowired
    public StudentGroupController(
            GraduateService graduateService,
            GroupService groupService,
            CourseForGroupService courseForGroupService
    ) {
        this.graduateService = graduateService;
        this.groupService = groupService;
        this.courseForGroupService = courseForGroupService;
    }

    @JsonView(StudentGroupView.WithStudents.class)
    @GetMapping("/graduates")
    public ResponseEntity getGraduateGroups(@RequestParam Integer degreeId) {
        List<StudentGroup> groups = graduateService.getGraduateGroups(degreeId);
        return ResponseEntity.ok(parseToStudentGroupDTO(groups));
    }

    private List<StudentGroupDTO> parseToStudentGroupDTO(List<StudentGroup> studentGroupList) {
        ModelMapper modelMapper = new ModelMapper();
        Type listType = new TypeToken<List<StudentGroupDTO>>() {}.getType();
        return modelMapper.map(studentGroupList, listType);
    }

    @GetMapping()
    @JsonView(StudentGroupView.GroupData.class)
    public ResponseEntity getGroups() {
        List<StudentGroup> studentGroups = groupService.getGroups();
        return ResponseEntity.ok(parseToStudentGroupDTO(studentGroups));
    }

    @GetMapping("/year")
    @JsonView(StudentGroupView.WithStudents.class)
    public ResponseEntity getGroupsByDegreeAndYear(
            @RequestParam Integer degreeId,
            @RequestParam Integer year
    ) {
        List<StudentGroup> groups = groupService.getGroupsByDegreeAndYear(degreeId, year);
        return ResponseEntity.ok(parseToStudentGroupDTO(groups));
    }
}
