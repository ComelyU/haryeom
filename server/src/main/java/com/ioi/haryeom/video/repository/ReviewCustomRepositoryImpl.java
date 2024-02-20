package com.ioi.haryeom.video.repository;

import static com.ioi.haryeom.common.domain.QSubject.subject;
import static com.ioi.haryeom.homework.domain.QHomework.homework;
import static com.ioi.haryeom.textbook.domain.QTextbook.textbook;
import static com.ioi.haryeom.tutoring.domain.QTutoring.tutoring;
import static com.ioi.haryeom.tutoring.domain.QTutoringSchedule.tutoringSchedule;
import static com.ioi.haryeom.video.domain.QVideo.video;

import com.ioi.haryeom.common.dto.SubjectResponse;
import com.ioi.haryeom.homework.domain.HomeworkStatus;
import com.ioi.haryeom.homework.dto.HomeworkResponse;
import com.ioi.haryeom.textbook.dto.TextbookResponse;
import com.ioi.haryeom.video.dto.VideoResponse;
import com.ioi.haryeom.video.dto.VideoResponseForQuery;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class ReviewCustomRepositoryImpl implements ReviewCustomRepository {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<TextbookResponse> findAllByAssignmentByTutoringByMember(Long memberId) {
        return queryFactory
            .select(
                Projections.constructor(TextbookResponse.class,
                    textbook.id.as("textbookId"), textbook.textbookName, textbook.textbookUrl, textbook.totalPage)
            ).from(textbook)
            .where(textbook.id.in(
                JPAExpressions
                    .select(homework.textbook.id)
                    .from(homework)
                    .where(homework.tutoring.id.in(
                        JPAExpressions
                            .select(tutoring.id)
                            .from(tutoring)
                            .where(tutoring.studentMember.id.eq(memberId))
                    ))
            ))
            .fetch();
    }

    @Override
    public Page<HomeworkResponse> findAllByTextbookByTutoringByMember(Long textbookId,
        Long memberId, Pageable pageable) {
        QueryResults<HomeworkResponse> results = queryFactory
            .select(
                Projections.constructor(HomeworkResponse.class,
                    homework.id, homework.textbook.id,
                    homework.textbook.textbookName, homework.startPage, homework.endPage, homework.status.stringValue(), homework.deadline)
            ).from(homework)
            .leftJoin(textbook).on(textbook.id.eq(homework.textbook.id))
            .fetchJoin()
            .where(textbook.id.eq(textbookId), homework.tutoring.studentMember.id.eq(memberId), homework.status.eq(HomeworkStatus.COMPLETED))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(homework.deadline.desc())
            .fetchResults();
        List<HomeworkResponse> content = results.getResults();
        Long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<SubjectResponse> findAllByTutoringByMember(Long memberId) {

        return queryFactory
            .select(Projections.constructor(SubjectResponse.class,
                subject.id.as("subjectId"), subject.name))
            .from(subject)
            .where(subject.id.in(
                JPAExpressions
                    .select(tutoring.subject.id)
                    .from(tutoring)
                    .where(tutoring.studentMember.id.eq(memberId))
            ))
            .fetch();
    }

    @Override
    public Page<VideoResponse> findAllByTutoringIdByMemberId(Long tutoringId,
        Long memberId, Pageable pageable) {
        QueryResults<VideoResponseForQuery> results = queryFactory
            .select(
                Projections.constructor(VideoResponseForQuery.class,
                    video.id, video.tutoringSchedule.title, video.tutoringSchedule.scheduleDate,
                    video.startTime, video.endTime, video.tutoringSchedule.id, tutoring.teacherMember.id))
            .from(video)
            .leftJoin(video.tutoringSchedule, tutoringSchedule)
            .leftJoin(tutoringSchedule.tutoring, tutoring)
            .where(tutoring.id.eq(tutoringId), tutoring.studentMember.id.eq(memberId))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(video.createdAt.desc())
            .fetchResults();

        //content 옮겨담기 - second to HH:mm:ss
        List<VideoResponseForQuery> content = results.getResults();
        List<VideoResponse> response = new ArrayList<VideoResponse>();
        for (VideoResponseForQuery query : content) {
            Long videoId = query.getVideoId();
            String title = query.getTitle();
            LocalDate scheduleDate = query.getScheduleDate();
            String duration = null;
            if (query.getStartTime() != null && query.getEndTime() != null) {
                Long seconds = Duration.between(query.getStartTime(), query.getEndTime()).toSeconds();
                if (seconds < 0) {
                    seconds += 60 * 60 * 24;
                }
                duration = String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
            }
            Long tutoringScheduleId = query.getTutoringScheduleId();
            Long teacherMemberId = query.getTeacherMemberId();
            VideoResponse resp = new VideoResponse(videoId, title, scheduleDate, duration, tutoringScheduleId, teacherMemberId);
            response.add(resp);
        }
        Long total = results.getTotal();
        return new PageImpl<>(response, pageable, total);
    }
}
