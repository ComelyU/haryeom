package com.ioi.haryeom.textbook.controller;

import com.ioi.haryeom.common.util.AuthMemberId;
import com.ioi.haryeom.member.dto.SubjectInfo;
import com.ioi.haryeom.textbook.dto.StudentAssignmentRequest;
import com.ioi.haryeom.textbook.dto.TextbookListByTutoringResponse;
import com.ioi.haryeom.textbook.dto.TextbookRequest;
import com.ioi.haryeom.textbook.dto.TextbookResponse;
import com.ioi.haryeom.textbook.dto.TextbookWithStudentsResponse;
import com.ioi.haryeom.textbook.service.TextbookService;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/textbook")
@RequiredArgsConstructor
@RestController
public class TextbookController {

    private final TextbookService textbookService;

    // 학습자료 추가
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> createTextbook(@RequestPart("file") MultipartFile file, @RequestPart("request") TextbookRequest request,
        @AuthMemberId Long teacherMemberId) {

        Long textbookId = textbookService.createTextbook(file, request, teacherMemberId);
        return ResponseEntity.created(URI.create("textbook/" + textbookId)).build();
    }

    // 과와별 학습자료 리스트 조회
    @GetMapping("/tutoring/{tutoringId}")
    public ResponseEntity<List<TextbookListByTutoringResponse>> getTextbookList(@PathVariable Long tutoringId) {

        List<TextbookListByTutoringResponse> textbookList = textbookService.getTextbookListByTutoring(tutoringId);
        return ResponseEntity.ok(textbookList);
    }

    // 선생님 학습자료 리스트 조회
    @GetMapping("/teachers/{memberId}")
    public ResponseEntity<List<TextbookWithStudentsResponse>> getTeacherTextbookList(@PathVariable Long memberId,
        @AuthMemberId Long teacherMemberId) {
        List<TextbookWithStudentsResponse> textbooks = textbookService.getTextbooksWithStudents(memberId, teacherMemberId);
        return ResponseEntity.ok(textbooks);
    }

    // 학습자료 삭제
    @DeleteMapping
    public ResponseEntity<Void> deleteTextbook(@RequestParam("textbookIds") List<Long> textbookIds, @AuthMemberId Long teacherMemberId) {

        textbookService.deleteTextbook(textbookIds, teacherMemberId);
        return ResponseEntity.noContent().build();
    }

    // 학습자료 불러오기
    @GetMapping("/{textbookId}")
    public ResponseEntity<TextbookResponse> getTextbook(@PathVariable Long textbookId) {

        TextbookResponse textbook = textbookService.getTextbook(textbookId);
        return ResponseEntity.ok(textbook);
    }

    // 학습자료별 지정 가능 학생 리스트 조회
    @GetMapping("/{textbookId}/students")
    public ResponseEntity<List<TextbookWithStudentsResponse.StudentInfo>> getAssignableStudent(@PathVariable Long textbookId,
        @AuthMemberId Long teacherMemberId) {
        List<TextbookWithStudentsResponse.StudentInfo> studentInfos = textbookService.getAssignableStudent(textbookId, teacherMemberId);

        return ResponseEntity.ok(studentInfos);
    }

    // 학습자료 학생 지정
    @PutMapping("/{textbookId}/students")
    public ResponseEntity<Void> putAssignment(@PathVariable Long textbookId, @RequestBody StudentAssignmentRequest request,
        @AuthMemberId Long teacherMemberId) {

        textbookService.putAssignment(textbookId, request.getStudentMemberIds(), teacherMemberId);
        return ResponseEntity.noContent().build();
    }

    // 선생님 과목 조회
    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectInfo>> getTeacherSubjects(@AuthMemberId Long teacherMemberId) {
        List<SubjectInfo> subjects = textbookService.getTeacherSubjects(teacherMemberId);

        return ResponseEntity.ok(subjects);
    }

    // 학습자료 학생 지정 해제
    @DeleteMapping("/{textbookId}/students")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long textbookId, @RequestParam("studentMemberIds") List<Long> studentMemberIds,
        @AuthMemberId Long teacherMemberId) {

        textbookService.deleteAssignment(textbookId, studentMemberIds, teacherMemberId);
        return ResponseEntity.noContent().build();
    }
}
