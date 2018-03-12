package ua.edu.chdtu.deanoffice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.edu.chdtu.deanoffice.entity.CourseForGroup;
import ua.edu.chdtu.deanoffice.repository.CourseForGroupRepository;

import java.util.List;

@Service
public class CourseForGroupService {
    private final CourseForGroupRepository courseForGroupRepository;
    @Autowired
    public CourseForGroupService(CourseForGroupRepository courseForGroupRepository) {
        this.courseForGroupRepository = courseForGroupRepository;
    }

    public List<CourseForGroup> getCourseForGroup(int idGroup) {
        List<CourseForGroup> courseForGroup = courseForGroupRepository.findAllByStudentGroupId(idGroup);
        return courseForGroup;
    }
    public List<CourseForGroup> getCourseForGroupBySemester(int idGroup, int semester) {
        List<CourseForGroup> courseForGroup = courseForGroupRepository.findAllByStudentGroupIdAndCourse_Semester(idGroup, semester);
        return courseForGroup;
    }

    public List<CourseForGroup> getCourseForGroupBySpecialization(int specialization){
        return courseForGroupRepository.findAllBySpecialization(specialization);
    }
}
