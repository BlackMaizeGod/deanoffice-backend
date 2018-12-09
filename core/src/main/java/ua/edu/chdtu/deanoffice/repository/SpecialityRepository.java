package ua.edu.chdtu.deanoffice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.edu.chdtu.deanoffice.entity.Speciality;

import java.util.List;

public interface SpecialityRepository extends JpaRepository<Speciality, Integer> {
    @Query(value =
            "select s.* from speciality as s " +
            "inner join specialization as sz on s.id = sz.speciality_id " +
            "where sz.faculty_id = :faculty_id and s.active = true " +
            "group by s.id " +
            "order by s.name, s.code",
            nativeQuery = true)
    List<Speciality> findAllActive(@Param("faculty_id") int facultyId);

    @Query(value =
            "select s.* from speciality as s " +
            "inner join specialization as sz on s.id = sz.speciality_id " +
            "where sz.faculty_id = :faculty_id " +
            "group by s.id " +
            "order by s.name, s.code",
            nativeQuery = true)
    List<Speciality> findAll(@Param("faculty_id") int facultyId);

    @Query("SELECT s from Speciality s " +
            "where s.code = :code " +
            "and upper(s.name) = upper(:name)")
    Speciality getSpecialityByCodeAndName(@Param("code") String code, @Param("name") String name);

    List<Speciality> findAllByActiveOrderByName(boolean active);

    Speciality getSpecialityByName(String name);

    Speciality getSpecialityByCode(String code);

    @Query("SELECT s.id FROM speciality s " +
            "INNER JOIN specialization sz ON s.id = sz.speciality_id " +
            "WHERE sz.faculty_id = :faculty_id " +
            "AND s.active = true " +
            "AND concat(s.code, ' ', s.name) = :codeAndName")
    Speciality findByCodeAndNameAndFacultyId(@Param("codeAndName") String codeAndName, @Param("facultyId") int facultyId);
}
