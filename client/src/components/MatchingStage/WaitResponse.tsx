import styled from 'styled-components';
import { useRecoilValue } from 'recoil';
import userSessionAtom from '@/recoil/atoms/userSession';

const WaitResponse = () => {
    const userSession = useRecoilValue(userSessionAtom);

    return (
        <>
            <StyledWaitResponse>
                <RequestMessageHeader>김성은 학생이 과외를 신청했어요.</RequestMessageHeader>
                <RequestMessage>
                    <TutoringSubject>과목: 수학</TutoringSubject>
                    <TutoringFee>수강료: 30000원</TutoringFee>
                </RequestMessage>
            </StyledWaitResponse>
            {userSession?.role === 'TEACHER' && (
                <ResponseButtons>
                    <ResponseButton>수락</ResponseButton>
                    <ResponseButton>거절</ResponseButton>
                </ResponseButtons>
            )}
        </>
    );
};

const StyledWaitResponse = styled.div`
    padding: 1.5em 1em;
    display: flex;
    flex-direction: column;
    align-items: center;
    background-color: ${({ theme }) => theme.PRIMARY_LIGHT};
`;

const RequestMessageHeader = styled.span`
    margin-bottom: 10px;
`;

const RequestMessage = styled.div`
    display: flex;
    gap: 1em;
`;

const TutoringSubject = styled.div``;

const TutoringFee = styled.div``;

const ResponseButtons = styled.div`
    width: 100%;
    padding: 1em 0;
    display: flex;
    align-items: center;
    border-radius: 0 0 0.4em 0.4em;
    background-color: white;
    border: 1px solid ${({ theme }) => theme.PRIMARY};
`;

const ResponseButton = styled.button`
    width: 100%;
    border-right: 1px solid ${({ theme }) => theme.PRIMARY};

    &:last-child {
        border-right: none;
    }

    &:hover {
        text-decoration: underline;
    }
`;

export default WaitResponse;