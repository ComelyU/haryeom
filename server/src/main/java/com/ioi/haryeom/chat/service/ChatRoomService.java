package com.ioi.haryeom.chat.service;

import com.ioi.haryeom.auth.exception.AuthorizationException;
import com.ioi.haryeom.chat.document.ChatMessage;
import com.ioi.haryeom.chat.domain.ChatRoom;
import com.ioi.haryeom.chat.domain.ChatRoomState;
import com.ioi.haryeom.chat.dto.ChatMessageResponse;
import com.ioi.haryeom.chat.dto.ChatRoomResponse;
import com.ioi.haryeom.chat.exception.ChatRoomNotFoundException;
import com.ioi.haryeom.chat.exception.ChatRoomStateNotFoundException;
import com.ioi.haryeom.chat.exception.SelfChatroomException;
import com.ioi.haryeom.chat.repository.ChatMessageRepository;
import com.ioi.haryeom.chat.repository.ChatRoomRepository;
import com.ioi.haryeom.chat.repository.ChatRoomStateRepository;
import com.ioi.haryeom.common.domain.Subject;
import com.ioi.haryeom.common.dto.SubjectResponse;
import com.ioi.haryeom.member.domain.Member;
import com.ioi.haryeom.member.domain.Teacher;
import com.ioi.haryeom.member.domain.TeacherSubject;
import com.ioi.haryeom.member.exception.MemberNotFoundException;
import com.ioi.haryeom.member.exception.TeacherNotFoundException;
import com.ioi.haryeom.member.repository.MemberRepository;
import com.ioi.haryeom.member.repository.TeacherRepository;
import com.ioi.haryeom.tutoring.domain.Tutoring;
import com.ioi.haryeom.tutoring.domain.TutoringStatus;
import com.ioi.haryeom.tutoring.dto.TutoringResponse;
import com.ioi.haryeom.tutoring.repository.TutoringRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChatRoomService {

    private final TeacherRepository teacherRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomStateRepository chatRoomStateRepository;
    private final TutoringRepository tutoringRepository;
    private final ChatMessageRepository chatMessageRepository;

    // 채팅방 생성
    // 선생님이 선생님 찾기를 통해서 연락할 수 있다.
    @Transactional
    public Long createOrGetChatRoom(Long teacherId, Long memberId) {

        Teacher teacher = findTeacherById(teacherId);
        Member teacherMember = teacher.getMember();
        Member studentMember = findMemberById(memberId);
        validateSelfChatRoom(teacherMember, studentMember);

        log.info("[CREAT OR GET CHATROOM] teacherId : {}, teacherMemberId : {}, studentMemberId : {}", teacher.getId(), teacherMember.getId(),
            studentMember.getId());

        return chatRoomRepository.findByTeacherMemberAndStudentMemberAndIsDeletedFalse(teacherMember, studentMember)
            .map(ChatRoom::getId)
            .orElseGet(() -> createNewChatRoom(teacherMember, studentMember));
    }

    private Long createNewChatRoom(Member teacherMember, Member studentMember) {

        ChatRoom chatRoom = ChatRoom.builder()
            .teacherMember(teacherMember)
            .studentMember(studentMember)
            .build();

        ChatRoomState studentChatRoomState = ChatRoomState.builder()
            .chatRoom(chatRoom)
            .member(studentMember)
            .build();

        ChatRoomState teacherChatRoomState = ChatRoomState.builder()
            .chatRoom(chatRoom)
            .member(teacherMember)
            .build();

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        chatRoomStateRepository.save(studentChatRoomState);
        chatRoomStateRepository.save(teacherChatRoomState);
        log.info("[CREAT CHATROOM] chatRoomId : {}, teacherMemberId : {}, studentMemberId : {}", savedChatRoom.getId(), teacherMember.getId(),
            studentMember.getId());
        return savedChatRoom.getId();
    }


    // 채팅방 목록 조회
    public List<ChatRoomResponse> getChatRoomList(Long memberId) {

        Member member = findMemberById(memberId);
        List<ChatRoomState> chatRoomStates = chatRoomStateRepository.findAllByMemberAndIsDeletedIsFalse(member);

        log.info("[GET CHATROOM LIST] chatRoomStates size : {} ", chatRoomStates.size());

        return chatRoomStates.stream()
            .map(chatRoomState -> createChatRoomResponse(chatRoomState, member))
            .collect(Collectors.toList());
    }

    // 채팅방 나가기
    @Transactional
    public void exitChatRoom(Long memberId, Long chatRoomId) {
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        Member member = findMemberById(memberId);

        validateMemberInChatRoom(chatRoom, member);

        ChatRoomState chatRoomState = chatRoomStateRepository.findByChatRoomAndMemberAndIsDeletedIsFalse(chatRoom,
            member).orElseThrow(ChatRoomStateNotFoundException::new);

        chatRoomState.delete();
    }

    // 채팅방 메시지 목록 조회
    public List<ChatMessageResponse> getChatMessageList(Long chatRoomId, String lastMessageId, Integer size, Long memberId) {

        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        Member member = findMemberById(memberId);

        validateMemberInChatRoom(chatRoom, member);

        Pageable pageable = createPageable(size);

        Page<ChatMessage> chatMessagePage = getChatMessages(lastMessageId, chatRoomId, pageable);

        return chatMessagePage.getContent()
            .stream()
            .map(ChatMessageResponse::from)
            .collect(Collectors.toList());
    }

    // 채팅방 구성원 과외 조회
    public List<TutoringResponse> getChatRoomMembersTutoringList(Long chatRoomId, Long memberId) {

        ChatRoom chatRoom = findChatRoomById(chatRoomId);

        Member member = findMemberById(memberId);

        validateMemberInChatRoom(chatRoom, member);

        List<Tutoring> tutoringList = tutoringRepository.findAllByChatRoomAndStatus(chatRoom,
            TutoringStatus.IN_PROGRESS);

        return tutoringList.stream()
            .map(tutoring -> new TutoringResponse(tutoring.getId(), tutoring.getSubject()))
            .collect(Collectors.toList());
    }

    // 신청 가능한 과목 조회
    public List<SubjectResponse> getAvailableSubjectsForEnrollment(Long chatRoomId, Long memberId) {

        ChatRoom chatRoom = findChatRoomById(chatRoomId);

        Member member = findMemberById(memberId);

        validateMemberInChatRoom(chatRoom, member);

        Member teacherMember = chatRoom.getTeacherMember();
        Teacher teacher = teacherRepository.findByMember(teacherMember)
            .orElseThrow(() -> new TeacherNotFoundException("선생님을 찾을 수 없습니다."));

        // 선생님 과목 조회
        Set<Subject> teacherSubjects = teacher.getTeacherSubjects().stream()
            .map(TeacherSubject::getSubject)
            .collect(Collectors.toSet());

        // 채팅방 구성원 신청한 과목 조회하여 제거
        tutoringRepository.findAllByChatRoomAndStatus(chatRoom, TutoringStatus.IN_PROGRESS)
            .stream()
            .map(Tutoring::getSubject)
            .forEach(teacherSubjects::remove);

        return teacherSubjects.stream()
            .map(SubjectResponse::new)
            .collect(Collectors.toList());
    }

    private ChatRoomResponse createChatRoomResponse(ChatRoomState chatRoomState, Member member) {
        String lastReadMessageId = chatRoomState.getLastReadMessageId();
        Long chatRoomId = chatRoomState.getChatRoom().getId();

        ChatMessage lastChatMessage = chatMessageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
        Integer unreadMessageCount = chatMessageRepository.countAllByChatRoomIdAndIdGreaterThan(chatRoomId, new ObjectId(lastReadMessageId));
        Member oppositeMember = chatRoomState.getChatRoom().getOppositeMember(member);

        return ChatRoomResponse.of(chatRoomId, lastChatMessage, oppositeMember, unreadMessageCount);
    }

    private void validateSelfChatRoom(Member teacherMember, Member studentMember) {
        if (teacherMember.equals(studentMember)) {
            throw new SelfChatroomException();
        }
    }

    private Pageable createPageable(Integer size) {
        return PageRequest.of(0, size, Sort.by("id").descending());
    }

    private Page<ChatMessage> getChatMessages(String lastMessageId, Long chatRoomId, Pageable pageable) {
        Page<ChatMessage> chatMessagePage;
        if (lastMessageId == null) {
            chatMessagePage = chatMessageRepository.findByChatRoomId(chatRoomId, pageable);
        } else {
            chatMessagePage = chatMessageRepository.findByChatRoomIdAndIdLessThan(chatRoomId, new ObjectId(lastMessageId), pageable);
        }
        return chatMessagePage;
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    private ChatRoom findChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));
    }

    private Teacher findTeacherById(Long teacherId) {
        return teacherRepository.findById(teacherId)
            .orElseThrow(() -> new TeacherNotFoundException(teacherId));
    }

    private void validateMemberInChatRoom(ChatRoom chatRoom, Member member) {
        if (!chatRoom.isMemberPartOfChatRoom(member)) {
            throw new AuthorizationException(member.getId());
        }
    }
}
