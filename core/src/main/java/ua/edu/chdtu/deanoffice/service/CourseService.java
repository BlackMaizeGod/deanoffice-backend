package ua.edu.chdtu.deanoffice.service;

import org.springframework.stereotype.Service;
import ua.edu.chdtu.deanoffice.entity.Course;
import ua.edu.chdtu.deanoffice.repository.CourseRepository;

import java.util.List;

@Service
public class CourseService {
    private final CourseRepository courseRepository;

    public Course getCourse(int courseId) {
        return courseRepository.findOne(courseId);
    }

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> getCoursesBySemester(int semester) {
        return courseRepository.findAllBySemester(semester);
    }

    public void createCourse(Course course){
        this.courseRepository.save(course);
    }

}
