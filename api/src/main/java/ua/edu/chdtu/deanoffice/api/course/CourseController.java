package ua.edu.chdtu.deanoffice.api.course;

import com.fasterxml.jackson.annotation.JsonView;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.edu.chdtu.deanoffice.api.course.dto.CourseDTO;
import ua.edu.chdtu.deanoffice.api.course.dto.CourseForGroupDTO;
import ua.edu.chdtu.deanoffice.api.course.dto.CourseForGroupView;
import ua.edu.chdtu.deanoffice.api.general.ExceptionHandlerAdvice;
import ua.edu.chdtu.deanoffice.entity.Course;
import ua.edu.chdtu.deanoffice.entity.CourseForGroup;
import ua.edu.chdtu.deanoffice.entity.StudentGroup;
import ua.edu.chdtu.deanoffice.entity.Teacher;
import ua.edu.chdtu.deanoffice.service.CourseForGroupService;
import ua.edu.chdtu.deanoffice.service.CourseService;
import ua.edu.chdtu.deanoffice.service.StudentGroupService;
import ua.edu.chdtu.deanoffice.service.TeacherService;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequestMapping("/")
@RestController
public class CourseController {
    private CourseForGroupService courseForGroupService;
    private CourseService courseService;
    private StudentGroupService studentGroupService;
    private TeacherService teacherService;

    @Autowired
    public CourseController(CourseForGroupService courseForGroupService, CourseService courseService,
                            StudentGroupService studentGroupService, TeacherService teacherService) {
        this.courseForGroupService = courseForGroupService;
        this.courseService = courseService;
        this.studentGroupService = studentGroupService;
        this.teacherService = teacherService;
    }

    @GetMapping("/courses")
    public ResponseEntity getCoursesBySemester(@RequestParam(value = "semester") int semester) {
        List<Course> courses = courseService.getCoursesBySemester(semester);
        return ResponseEntity.ok(parseToCourseDTO(courses));
    }

    private List<CourseDTO> parseToCourseDTO(List<Course> courses) {
        Type listType = new TypeToken<List<CourseDTO>>() {}.getType();
        return new ModelMapper().map(courses, listType);
    }

    @GetMapping("/groups/{groupId}/courses")
    @JsonView(CourseForGroupView.Basic.class)
    public ResponseEntity getCoursesByGroupAndSemester(@PathVariable int groupId, @RequestParam int semester) {
        List<CourseForGroup> coursesForGroup = courseForGroupService.getCoursesForGroupBySemester(groupId, semester);
        return ResponseEntity.ok(parseToCourseForGroupDTO(coursesForGroup));
    }

    private List<CourseForGroupDTO> parseToCourseForGroupDTO(List<CourseForGroup> courseForGroupList) {
        Type listType = new TypeToken<List<CourseForGroupDTO>>() {}.getType();
        return new ModelMapper().map(courseForGroupList, listType);
    }

    @PostMapping(value = "/groups/{groupId}/courses")
    public ResponseEntity.BodyBuilder addCoursesForGroup(@RequestBody Map<String,List> body,
//            @RequestBody List<CourseForGroupDTO> newCourses,
//            @RequestBody List<CourseForGroupDTO> updatedCourses,
//            @RequestBody List<Integer> deleteCoursesIds,
            @PathVariable Integer groupId) {

        List<CourseForGroupDTO> newCourses = body.get("newCourses");
        List<CourseForGroupDTO> updatedCourses = body.get("updatedCourses");
        List<Integer> deleteCoursesIds = body.get("deleteCoursesIds");

        if (newCourses == null || updatedCourses == null || deleteCoursesIds == null)
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY);
        Set<CourseForGroup> newCoursesForGroup = new HashSet<CourseForGroup>();
        Set<CourseForGroup> updatedCoursesForGroup = new HashSet<CourseForGroup>();
        for (CourseForGroupDTO newCourseForGroup: newCourses) {
            CourseForGroup courseForGroup = new CourseForGroup();
            Course course = courseService.getCourse(newCourseForGroup.getCourse().getId());
            StudentGroup studentGroup = studentGroupService.getById(groupId);
            Teacher teacher = teacherService.getTeacher(newCourseForGroup.getTeacher().getId());
            courseForGroup.setCourse(course);
            courseForGroup.setStudentGroup(studentGroup);
            courseForGroup.setTeacher(teacher);
            courseForGroup.setExamDate(newCourseForGroup.getExamDate());
            newCoursesForGroup.add(courseForGroup);
        }
        for (CourseForGroupDTO updatedCourseForGroup: updatedCourses) {
            CourseForGroup courseForGroup = courseForGroupService.getCourseForGroup(updatedCourseForGroup.getId());
            Teacher teacher = teacherService.getTeacher(updatedCourseForGroup.getTeacher().getId());
            courseForGroup.setTeacher(teacher);
            courseForGroup.setExamDate(updatedCourseForGroup.getExamDate());
            updatedCoursesForGroup.add(courseForGroup);
        }
        courseForGroupService.addCourseForGroupAndNewChanges(newCoursesForGroup, updatedCoursesForGroup, deleteCoursesIds);
        return ResponseEntity.ok();
    }

    @GetMapping("/groups/{groupId}/courses/all")
    @JsonView(CourseForGroupView.Course.class)
    public ResponseEntity getCourses(@PathVariable int groupId) {
        List<CourseForGroup> courseForGroups = courseForGroupService.getCoursesForOneGroup(groupId);
        return ResponseEntity.ok(parseToCourseForGroupDTO(courseForGroups));
    }

    @GetMapping("/specialization/{id}/courses")
    @JsonView(CourseForGroupView.Basic.class)
    public ResponseEntity getCoursesBySpecialization(@PathVariable int id, @RequestParam("semester") int semester) {
        List<CourseForGroup> courseForGroups = courseForGroupService.getCourseForGroupBySpecialization(id, semester);
        return ResponseEntity.ok(parseToCourseForGroupDTO(courseForGroups));
    }

    @PostMapping("/courses")
    @ResponseBody
    public ResponseEntity createCourse(@RequestBody Course course){
        try {
            this.courseService.createCourse(course);
            return new ResponseEntity(HttpStatus.CREATED);
        }
        catch (DataIntegrityViolationException e){
            return ExceptionHandlerAdvice.handleException(e, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
}
