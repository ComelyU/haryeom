package com.ioi.haryeom.chat.dto;

import com.ioi.haryeom.chat.domain.ChatMessageBefore;
import com.ioi.haryeom.chat.domain.ChatRoomState;
import com.ioi.haryeom.member.domain.Member;
import com.ioi.haryeom.member.domain.type.Role;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ChatRoomResponse {

    private final Long chatRoomId;
    private final OppositeMemberResponse oppositeMember;
    private final String lastMessage;
    private final LocalDateTime lastMessageCreatedAt;
    private final Integer unreadMessageCount;

    private ChatRoomResponse(Long chatRoomId, OppositeMemberResponse oppositeMember, String lastMessage,
        LocalDateTime lastMessageCreatedAt, Integer unreadMessageCount) {
        this.chatRoomId = chatRoomId;
        this.oppositeMember = oppositeMember;
        this.lastMessage = lastMessage;
        this.lastMessageCreatedAt = lastMessageCreatedAt;
        this.unreadMessageCount = unreadMessageCount;
    }

    public static ChatRoomResponse of(ChatRoomState chatRoomState, ChatMessageBefore lastChatMessageBefore, Member oppositeMember,
        Integer unreadMessageCount) {
        Long chatRoomId = chatRoomState.getChatRoom().getId();
        OppositeMemberResponse oppositeMemberResponse = new OppositeMemberResponse(oppositeMember);

        String lastMessage = (lastChatMessageBefore != null) ? lastChatMessageBefore.getMessageContent() : null;
        LocalDateTime lastMessageCreatedAt = (lastChatMessageBefore != null) ? lastChatMessageBefore.getCreatedAt() : null;

        return new ChatRoomResponse(chatRoomId, oppositeMemberResponse, lastMessage, lastMessageCreatedAt,
            unreadMessageCount);
    }

    @Getter
    public static class OppositeMemberResponse {

        private final Role role;
        private final String profileUrl;
        private final String name;

        private OppositeMemberResponse(Member oppositeMember) {
            this.role = oppositeMember.getRole();
            this.profileUrl = oppositeMember.getProfileUrl();
            this.name = oppositeMember.getName();
        }
    }
}
