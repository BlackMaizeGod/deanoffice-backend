package ua.edu.chdtu.deanoffice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.edu.chdtu.deanoffice.entity.StudentGroup;

import java.util.List;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Integer> {

    @Query("select sg from StudentGroup as sg " +
            "where sg.active = true and sg.specialization.faculty.id = :facultyId " +
            "order by sg.name")
    List<StudentGroup> findAllActiveByFaculty(@Param("facultyId") int facultyId);

    @Query("SELECT sg FROM StudentGroup AS sg " +
            "WHERE sg.active = TRUE " +
            "ORDER BY sg.name")
    List<StudentGroup> findAllActive();

    @Query("select sg from StudentGroup as sg " +
            "where sg.specialization.faculty.id = :facultyId " +
            "order by sg.name")
    List<StudentGroup> findAllByFaculty(@Param("facultyId") int facultyId);

    @Query("select cfg.studentGroup from CourseForGroup as cfg " +
            "where cfg.course.id = :courseId " +
            "and cfg.studentGroup.specialization.faculty.id = :faculty_id")
    List<StudentGroup> findAllByCourse(@Param("courseId") int courseId, @Param("faculty_id") int facultyId);

    @Query(value = "SELECT * FROM student_group sg " +
            "INNER JOIN specialization s ON s.id = sg.specialization_id " +
            "WHERE sg.active = TRUE AND s.degree_id = :degreeId " +
            "AND floor(sg.creation_year + sg.study_years - 0.1) = :currYear " +
            "AND s.faculty_id = :faculty_id " +
            "ORDER BY sg.tuition_form DESC, sg.name", nativeQuery = true)
    List<StudentGroup> findGraduateByDegree(
            @Param("degreeId") Integer degreeId,
            @Param("currYear") Integer currYear,
            @Param("faculty_id") int facultyId
    );

    @Query("select sg from StudentGroup sg " +
            "where sg.active = true " +
            "and sg.specialization.degree.id = :degree_id " +
            "and :curr_year - sg.creationYear + sg.beginYears = :study_year " +
            "and sg.specialization.faculty.id = :faculty_id " +
            "order by sg.tuitionForm desc, sg.name")
    List<StudentGroup> findGroupsByDegreeAndYear(
            @Param("degree_id") Integer degreeId,
            @Param("study_year") Integer studyYear,
            @Param("curr_year") Integer currYear,
            @Param("faculty_id") int facultyId
    );

    @Query("select sg from StudentGroup sg " +
            "where sg.id in :group_ids")
    List<StudentGroup> findAllByIds(@Param("group_ids") Integer[] groupIds);

    @Query(value =
            "SELECT * " +
            "FROM student_group " +
            "   INNER JOIN courses_for_groups cfg ON student_group.id = cfg.student_group_id " +
            "   INNER JOIN specialization sp ON specialization_id = sp.id " +
            "   INNER JOIN course c ON cfg.course_id = c.id " +
            "WHERE cfg.course_id IN (SELECT course.id " +
            "                       FROM course " +
            "                       WHERE course.hours IN (SELECT c2.hours FROM course c2 WHERE c2.id = :course_id) " +
            "                           AND course.course_name_id IN " +
            "                               (SELECT c3.course_name_id FROM course c3 WHERE c3.id = :course_id) " +
            "                           AND course.kc_id IN (SELECT c4.kc_id FROM course c4 WHERE c4.id = :course_id)) " +
            "   AND sp.faculty_id = :faculty_id " +
            "   AND c.semester = ((((SELECT curr_year FROM current_year) - student_group.creation_year) * 2 + 1) + " +
            "       cast((NOT date_part('year', CURRENT_DATE) = (SELECT curr_year FROM current_year)) AS int))", nativeQuery = true)
    List<StudentGroup> findGroupsThatAreStudyingSameCourseTo(
            @Param("course_id") Integer courseId,
            @Param("faculty_id") Integer facultyId
    );
}
