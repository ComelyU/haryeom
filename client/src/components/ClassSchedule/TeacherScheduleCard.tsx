import styled from 'styled-components';
import { ITeacherSchedule } from '@/apis/tutoring/tutoring';
import { addMinutesToTime, getHourMin } from '@/utils/time';

interface TeacherScheduleCardProps {
    schedule: ITeacherSchedule;
}

const TeacherScheduleCard = ({ schedule }: TeacherScheduleCardProps) => {
    return (
        <StyledTeacherScheduleCard>
            <div>
                <ScheduledTime>
                    <StartTime>{getHourMin(schedule.startTime)}</StartTime>
                    <Duration>~ {addMinutesToTime(schedule.startTime, schedule.duration)}</Duration>
                </ScheduledTime>
                <ClassInfo>
                    <Subject>{schedule.subject.name}</Subject>
                    <Curriculum>| {schedule.title}</Curriculum>
                </ClassInfo>
            </div>
            {/* <ClassState>종료</ClassState> */}
        </StyledTeacherScheduleCard>
    );
};

const StyledTeacherScheduleCard = styled.div`
    width: 100%;
    padding: 0.8em;
    margin-bottom: 0.9em;
    border-radius: 0.8em;
    display: flex;
    align-items: center;
    font-size: 0.9em;
    border: 2px solid ${({ theme }) => theme.PRIMARY};
    background-color: ${({ theme }) => theme.SECONDARY};
`;

const ScheduledTime = styled.div`
    padding-left: 0.5em;
    margin-bottom: 4px;
    display: flex;
    align-items: center;
    justify-content: start;
    font-size: 0.9em;
`;

const StartTime = styled.div``;

const Duration = styled.div`
    margin-left: 4px;
`;

const ClassInfo = styled.div`
    display: flex;
    align-items: center;
`;

const Subject = styled.div`
    padding: 0.35em;
    font-size: 1.2em;
    font-weight: bold;
`;

const Curriculum = styled.div`
    margin-left: 0.2em;
`;

const ClassState = styled.div`
    margin-left: auto;
`;

export default TeacherScheduleCard;
