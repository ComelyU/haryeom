package com.ioi.haryeom.matching.dto;

import com.ioi.haryeom.chat.domain.ChatRoom;
import com.ioi.haryeom.common.domain.Subject;
import com.ioi.haryeom.common.dto.SubjectResponse;
import com.ioi.haryeom.member.domain.Member;
import lombok.Getter;

@Getter
public class CreateMatchingResponse {

    private final String matchingId;
    private final Long recipientMemberId;
    private final String senderName;
    private final SubjectResponse subject;
    private final Integer hourlyRate;

    private CreateMatchingResponse(String matchingId, Long recipientMemberId, String senderName,
        Subject subject, Integer hourlyRate) {
        this.matchingId = matchingId;
        this.recipientMemberId = recipientMemberId;
        this.senderName = senderName;
        this.subject = new SubjectResponse(subject);
        this.hourlyRate = hourlyRate;
    }

    public static CreateMatchingResponse of(String matchingId, ChatRoom chatRoom, Member member, Subject subject, Integer hourlyRate) {
        return new CreateMatchingResponse(
            matchingId,
            chatRoom.getOppositeMember(member).getId(),
            member.getName(),
            subject,
            hourlyRate
        );
    }

}
