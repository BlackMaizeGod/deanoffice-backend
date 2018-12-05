package ua.edu.chdtu.deanoffice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.edu.chdtu.deanoffice.entity.Course;
import ua.edu.chdtu.deanoffice.entity.Grade;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Integer> {

    @Query("Select grade From Grade grade" +
            " Join grade.course course" +
            " Where grade.studentDegree.id = :studentDegreeId" +
            " and course.knowledgeControl.id in (:KnowledgeControlIds)" +
            " and course.id in (:courseIds)" +
            " Order by course.courseName.name")
    List<Grade> getByStudentDegreeIdAndCoursesAndKCTypes(
            @Param("studentDegreeId") Integer studentDegreeId,
            @Param("courseIds") List<Integer> courseIds,
            @Param("KnowledgeControlIds") List<Integer> knowledgeControlsIds
    );

    @Query("select grade from Grade grade " +
            "join grade.course course " +
            "where grade.studentDegree.id in (:studentIds)" +
            "and course.id in (:courseIds)")
    List<Grade> findGradesByCourseAndBySemesterForStudents(
            @Param("studentIds") List<Integer> studentIds,
            @Param("courseIds") List<Integer> courseIds);

    Grade getByStudentDegreeIdAndCourseId(Integer studentDegreeId, Integer courseId);

    @Query("select gr from Grade gr where gr.course.id = :courseId and gr.studentDegree.studentGroup.id = :groupId")
    List<Grade> findByCourseAndGroup(@Param("courseId") int courseId, @Param("groupId") int groupId);

    @Query(value =
            "SELECT CAST(CAST(COUNT(*) AS INT) AS BOOLEAN) AS is_good_mark" +
            "  FROM student_degree" +
            "        LEFT JOIN grade ON student_degree.id = grade.student_degree_id" +
            " WHERE student_degree.id = :studentDegreeId " +
            "       AND student_degree.student_group_id = :studentGroupId " +
            "       AND grade.points >= 60 " +
            "       AND course_id IN (SELECT similar_course.id " +
            "                           FROM course similar_course " +
            "                          WHERE course_name_id IN (SELECT course_name.course_name_id " +
            "                                                     FROM course course_name " +
            "                                                    WHERE course_name.id = :courseId) " +
            "                                AND kc_id IN (SELECT course_kc.kc_id " +
            "                                                FROM course course_kc " +
            "                                               WHERE course_kc.id = :courseId) " +
            "                                AND hours IN (SELECT course_hours.hours " +
            "                                                FROM course course_hours " +
            "                                               WHERE course_hours.id = :courseId)) ",
            nativeQuery = true)
    boolean isStudentHaveGoodMarkFromCourse(@Param("studentDegreeId") Integer studentDegreeId,
                                            @Param("studentGroupId") Integer studentGroupId,
                                            @Param("courseId") Integer courseId);

}
