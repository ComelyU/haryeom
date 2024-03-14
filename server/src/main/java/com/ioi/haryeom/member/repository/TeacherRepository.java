package com.ioi.haryeom.member.repository;

import com.ioi.haryeom.member.domain.Member;
import com.ioi.haryeom.member.domain.Teacher;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByMember(Member teacherMember);

    @Query("SELECT t FROM Teacher t " +
        "LEFT JOIN FETCH t.teacherSubjects ts " +
        "LEFT JOIN FETCH ts.subject WHERE t.id = :teacherId")
    Optional<Teacher> findByIdWithSubjects(@Param("teacherId") Long teacherId);
}
