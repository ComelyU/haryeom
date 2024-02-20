package com.ioi.haryeom.member.repository;

import com.ioi.haryeom.member.domain.Member;
import com.ioi.haryeom.member.domain.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByMember(Member member);
}
