package com.ioi.haryeom.tutoring.dto;

import com.ioi.haryeom.common.dto.SubjectResponse;
import com.ioi.haryeom.member.domain.Student;
import com.ioi.haryeom.tutoring.domain.Tutoring;
import lombok.Getter;

@Getter
public class TeacherTutoringResponse {

    private Long tutoringId;

    private Long studentMemberId;
    private String studentName;
    private String studentProfileUrl;
    private String studentSchool;
    private String studentGrade;

    private SubjectResponse subject;

    public TeacherTutoringResponse(Tutoring tutoring, Student student) {
        this.tutoringId = tutoring.getId();
        this.studentMemberId = tutoring.getStudentMember().getId();
        this.studentName = tutoring.getStudentMember().getName();
        this.studentProfileUrl = tutoring.getStudentMember().getProfileUrl();
        this.studentSchool = student.getSchool();
        this.studentGrade = student.getGrade();
        this.subject = new SubjectResponse(tutoring.getSubject());
    }
}