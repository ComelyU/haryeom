package com.ioi.haryeom.chat.domain;

import com.ioi.haryeom.member.domain.Member;
import javax.persistence.Entity;
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
public class ChatRoomState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private Integer unreadCount;

    private Long lastReadMessageId;

    private Boolean isDeleted;

    @Builder
    public ChatRoomState(Long id, ChatRoom chatRoom, Member member, Integer unreadCount,
        Long lastReadMessageId, Boolean isDeleted) {
        this.id = id;
        this.chatRoom = chatRoom;
        this.member = member;
        this.unreadCount = unreadCount;
        this.lastReadMessageId = lastReadMessageId;
        this.isDeleted = isDeleted;
    }
}