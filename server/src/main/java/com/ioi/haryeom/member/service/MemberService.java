package com.ioi.haryeom.member.service;


import com.ioi.haryeom.advice.exception.ConflictException;
import com.ioi.haryeom.auth.service.AuthService;
import com.ioi.haryeom.auth.service.TokenService;
import com.ioi.haryeom.aws.S3Upload;
import com.ioi.haryeom.common.repository.SubjectRepository;
import com.ioi.haryeom.member.domain.Member;
import com.ioi.haryeom.member.domain.Student;
import com.ioi.haryeom.member.domain.Teacher;
import com.ioi.haryeom.member.domain.TeacherSubject;
import com.ioi.haryeom.member.domain.type.Role;
import com.ioi.haryeom.member.dto.CodeCertifyRequest;
import com.ioi.haryeom.member.dto.EmailCertifyRequest;
import com.ioi.haryeom.member.dto.StudentInfoResponse;
import com.ioi.haryeom.member.dto.StudentRequest;
import com.ioi.haryeom.member.dto.SubjectInfo;
import com.ioi.haryeom.member.dto.TeacherInfoResponse;
import com.ioi.haryeom.member.dto.TeacherRequest;
import com.ioi.haryeom.member.exception.EmailCertifyException;
import com.ioi.haryeom.member.exception.MemberNotFoundException;
import com.ioi.haryeom.member.exception.StudentNotFoundException;
import com.ioi.haryeom.member.exception.SubjectNotFoundException;
import com.ioi.haryeom.member.exception.TeacherNotFoundException;
import com.ioi.haryeom.member.repository.MemberRepository;
import com.ioi.haryeom.member.repository.StudentRepository;
import com.ioi.haryeom.member.repository.TeacherRepository;
import com.ioi.haryeom.member.repository.TeacherSubjectRepository;
import com.univcert.api.UnivCert;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final S3Upload s3Upload;

    @Value("${spring.univcert.api-key}")
    private String univKey;

    private final AuthService authService;
    private final TokenService tokenService;

    private final MemberRepository memberRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final SubjectRepository subjectRepository;


    public void certifyEmail(EmailCertifyRequest certifyRequest) {
        try {
            Map<String, Object> response = UnivCert.certify(univKey, certifyRequest.getEmail(),
                certifyRequest.getUnivName(), true);

            boolean success = Boolean.parseBoolean(String.valueOf(response.get("success")));
            if (!success) {
                throw new EmailCertifyException(String.valueOf(response.get("message")));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void certifyCode(CodeCertifyRequest certifyRequest) {
        try {
            Map<String, Object> response = UnivCert.certifyCode(univKey, certifyRequest.getEmail(),
                certifyRequest.getUnivName(), certifyRequest.getCode());

            boolean success = Boolean.parseBoolean(String.valueOf(response.get("success")));
            if (!success) {
                throw new EmailCertifyException(String.valueOf(response.get("message")));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void certifyUniv(String univName) {
        try {
            Map<String, Object> response = UnivCert.check(univName);

            boolean success = Boolean.parseBoolean(String.valueOf(response.get("success")));
            if (!success) {
                throw new EmailCertifyException(String.valueOf(response.get("message")));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public Long createStudent(Long userId, MultipartFile profileImg,
        StudentRequest studentRequest) {
        try {
            Member member = findMemberById(userId);

            if (member.getRole() != Role.GUEST) {
                throw new ConflictException("학생 등록을 중복으로 할 수 없습니다.");
            }

            String profileUrl = member.getProfileUrl();

            if (profileImg != null && profileImg.getSize() != 0) {
                profileUrl = s3Upload.uploadFile(String.valueOf(userId),
                    profileImg.getInputStream(), profileImg.getSize(), profileImg.getContentType());
            }

            Student student = Student.builder()
                .member(member)
                .grade(studentRequest.getGrade())
                .school(studentRequest.getSchool())
                .build();

            member.create(Role.STUDENT, profileUrl,
                studentRequest.getName(), studentRequest.getPhone());

            studentRepository.save(student);

            return member.getId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public StudentInfoResponse getStudent(Long memberId) {

        Member member = findMemberById(memberId);
        Student student = findStudentByMember(member);

        return StudentInfoResponse.builder()
            .profileUrl(member.getProfileUrl())
            .name(member.getName())
            .phone(member.getPhone())
            .grade(student.getGrade())
            .school(student.getSchool())
            .build();
    }

    @Transactional
    public void updateStudent(Long memberId, MultipartFile profileImg,
        StudentRequest studentRequest) {
        try {
            Member member = findMemberById(memberId);

            String profileUrl = member.getProfileUrl();
            if (profileImg != null && profileImg.getSize() != 0) {
                profileUrl = s3Upload.uploadFile(String.valueOf(memberId),
                    profileImg.getInputStream(), profileImg.getSize(), profileImg.getContentType());
            }

            Student student = findStudentByMember(member);
            student.updateStudent(studentRequest.getGrade(), studentRequest.getSchool());

            member.update(profileUrl,
                studentRequest.getName(), studentRequest.getPhone());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public Long createTeacher(Long userId, MultipartFile profileImg,
        TeacherRequest teacherRequest) {
        try {
            Member member = findMemberById(userId);

            if (member.getRole() != Role.GUEST) {
                throw new ConflictException("선생님 등록을 중복으로 할 수 없습니다.");
            }

            String profileUrl = member.getProfileUrl();
            if (profileImg != null && profileImg.getSize() != 0) {
                profileUrl = s3Upload.uploadFile(String.valueOf(userId),
                    profileImg.getInputStream(), profileImg.getSize(), profileImg.getContentType());
            }

            Teacher teacher = Teacher.builder()
                .profileStatus(teacherRequest.getProfileStatus())
                .college(teacherRequest.getCollege())
                .collegeEmail(teacherRequest.getCollegeEmail())
                .gender(teacherRequest.getGender())
                .salary(teacherRequest.getSalary())
                .career(teacherRequest.getCareer())
                .introduce(teacherRequest.getIntroduce())
                .build();

            member.create(Role.TEACHER, profileUrl,
                teacherRequest.getName(), teacherRequest.getPhone());
            teacherRepository.save(teacher);

            List<SubjectInfo> subjects = teacherRequest.getSubjects();

            subjects.forEach(subjectInfo -> {
                TeacherSubject teacherSubject = TeacherSubject.builder()
                    .teacher(teacher)
                    .subject(subjectRepository.findById(subjectInfo.getSubjectId())
                        .orElseThrow(
                            () -> new SubjectNotFoundException(subjectInfo.getSubjectId())))
                    .build();

                teacherSubjectRepository.save(teacherSubject);
            });

            return member.getId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TeacherInfoResponse getTeacher(Long memberId) {
        Member member = findMemberById(memberId);
        Teacher teacher = findTeacherByMember(member);

        return TeacherInfoResponse.builder()
            .profileUrl(member.getProfileUrl())
            .name(member.getName())
            .phone(member.getPhone())
            .profileStatus(teacher.getProfileStatus())
            .college(teacher.getCollege())
            .collegeEmail(teacher.getCollegeEmail())
            .gender(teacher.getGender())
            .salary(teacher.getSalary())
            .career(teacher.getCareer())
            .subjects(findSubjectsById(teacher.getId()))
            .introduce(teacher.getIntroduce())
            .build();
    }

    @Transactional
    public void updateTeacher(Long memberId, MultipartFile profileImg,
        TeacherRequest teacherRequest) {
        try {
            Member member = findMemberById(memberId);

            String profileUrl = member.getProfileUrl();
            if (profileImg != null && profileImg.getSize() != 0) {
                profileUrl = s3Upload.uploadFile(String.valueOf(memberId),
                    profileImg.getInputStream(), profileImg.getSize(), profileImg.getContentType());
            }

            member.update(profileUrl, teacherRequest.getName(),
                teacherRequest.getPhone());

            Teacher teacher = findTeacherByMember(member);

            List<TeacherSubject> teacherSubjects = teacher.getTeacherSubjects();

            teacherSubjectRepository.deleteAllInBatch(teacherSubjects);

            List<SubjectInfo> subjects = teacherRequest.getSubjects();
            for (SubjectInfo subjectInfo : subjects) {
                TeacherSubject teacherSubject = TeacherSubject.builder()
                    .teacher(teacher)
                    .subject(
                        subjectRepository.findById(subjectInfo.getSubjectId()).orElseThrow(
                            () -> new SubjectNotFoundException(subjectInfo.getSubjectId())
                        )
                    )
                    .build();

                teacherSubjectRepository.save(teacherSubject);
            }

            teacher.updateTeacher(teacherRequest.getProfileStatus(),
                teacherRequest.getCollege(),
                teacherRequest.getCollegeEmail(),
                teacherRequest.getGender(),
                teacherRequest.getSalary(),
                teacherRequest.getCareer(),
                teacherRequest.getIntroduce());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void deleteMember(Long userId, HttpServletResponse response) throws IOException {
        Member member = findMemberById(userId);
        member.delete();
        authService.oauthLogout(userId, "KAKAO");
        tokenService.resetHeader(response);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    private List<SubjectInfo> findSubjectsById(Long teacherId) {
        return teacherSubjectRepository
            .findByTeacherId(teacherId)
            .stream()
            .map(TeacherSubject::getSubject)
            .map(SubjectInfo::from)
            .collect(Collectors.toList());
    }

    private Student findStudentByMember(Member member) {
        return studentRepository.findByMember(member).orElseThrow(() -> new StudentNotFoundException(member.getId()));
    }

    private Teacher findTeacherByMember(Member member) {
        return teacherRepository.findByMember(member).orElseThrow(() -> new TeacherNotFoundException(member.getId()));
    }
}