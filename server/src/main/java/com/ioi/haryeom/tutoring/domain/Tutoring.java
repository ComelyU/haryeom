package com.ioi.haryeom.tutoring.domain;

import com.ioi.haryeom.chat.domain.ChatRoom;
import com.ioi.haryeom.common.domain.BaseTimeEntity;
import com.ioi.haryeom.common.domain.Subject;
import com.ioi.haryeom.member.domain.Member;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Tutoring extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "chat_room_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

    @JoinColumn(name = "subject_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Subject subject;

    @Column
    private Integer hourlyRate;

    @Enumerated(EnumType.STRING)
    private TutoringStatus status = TutoringStatus.IN_PROGRESS;

    @JoinColumn(name = "student_member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member studentMember;

    @JoinColumn(name = "teacher_member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member teacherMember;


    @Builder
    public Tutoring(ChatRoom chatRoom, Subject subject, Integer hourlyRate, Member studentMember, Member teacherMember) {
        this.chatRoom = chatRoom;
        this.subject = subject;
        this.hourlyRate = hourlyRate;
        this.studentMember = studentMember;
        this.teacherMember = teacherMember;
    }

    public void update(ChatRoom chatRoom, Subject subject, Integer hourlyRate,
        TutoringStatus status, Member studentMember, Member teacherMember) {
        this.chatRoom = chatRoom;
        this.subject = subject;
        this.hourlyRate = hourlyRate;
        this.status = status;
        this.studentMember = studentMember;
        this.teacherMember = teacherMember;
    }

    public void end() {
        this.status = TutoringStatus.CLOSED;
    }


    public boolean isMemberPartOfTutoring(Member member) {
        return (studentMember.equals(member) || teacherMember.equals(member));
    }

}