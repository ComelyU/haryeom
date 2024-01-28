import { useState, ChangeEvent } from 'react';
import axios from 'axios';
import styled from 'styled-components';
import SelectForm from '@/components/commons/SelectForm';
import InputForm from '@/components/commons/InputForm';

const TutoringApplicationForm = () => {
    const [subjectId, setSubjectId] = useState<number>(1); // 과목 ID
    const [hourlyRate, setHourlyRate] = useState<string>(''); // 수업료
    const optionList = [
        { id: 1, name: '수학' },
        { id: 2, name: '과학' }
    ];

    const handleSubjectChange = (name: string, selectedOption: string | number) => {
        if (typeof selectedOption === 'string') {
            // 문자열인 경우: 옵션의 이름으로 과목 ID 찾기
            const selectedSubject = optionList.find(option => option.name === selectedOption);
            if (selectedSubject) {
                setSubjectId(selectedSubject.id);
            }
        } else if (typeof selectedOption === 'number') {
            // 숫자인 경우: 직접 과목 ID 설정
            setSubjectId(selectedOption);
        }
    };

    

    const handleHourlyRateChange = (event: ChangeEvent<HTMLInputElement>) => {
        setHourlyRate(event.target.value);
    };

    const handleSubmit = async () => {
        const chatRoomId = 1; // 채팅방 ID, 실제 적용 시 적절한 값을 사용하세요.

        try {
            const response = await axios.post('http://localhost:8080/api/matching/request', {
                chatRoomId,
                subjectId,
                hourlyRate
            });
            console.log(response.data); // 요청에 대한 응답 처리
        } catch (error) {
            console.error('과외 신청 요청 중 오류 발생:', error);
        }
    };

    const optionListStrings = optionList.map(option => option.name);

    return (
        <ApplyTutoringForm>
            <ApplyTutoringFormHeader>과외 신청서</ApplyTutoringFormHeader>
            <SelectSubjectHeader>과목 선택</SelectSubjectHeader>
            <SelectSubject>
            <SelectForm
            label={'선택'}
            name={'subject'}
            optionList={optionListStrings}
            handleSelect={handleSubjectChange}
            height="40px"
            />
            </SelectSubject>
            <InputTutoringFeeHeader>수업료</InputTutoringFeeHeader>
            <InputTutoringFee>
                <InputForm label={''} name={'hourlyRate'} handleChange={handleHourlyRateChange} />
                <span>만원</span>
            </InputTutoringFee>
            <SubmitButton onClick={handleSubmit}>신청하기</SubmitButton>
        </ApplyTutoringForm>
    );
};

const ApplyTutoringForm = styled.div`
    width: 400px;
    /* height: 400px; */
    padding: 2em;
    background-color: white;
    display: flex;
    flex-direction: column;
    border-radius: 1em;
`;

const ApplyTutoringFormHeader = styled.header`
    width: 100%;
    height: 35px;
    text-align: center;
    font-size: 1.4em;
    font-weight: bold;
    margin-bottom: 0.4em;
`;

const SelectSubjectHeader = styled.header`
    font-size: 1.1em;
    font-weight: bold;
    padding: 0.5em;
    margin-bottom: 0.5em;
    border-bottom: 1px solid ${({ theme }) => theme.BORDER_LIGHT};
`;

const SelectSubject = styled.div`
    margin-bottom: 3em;
`;

const InputTutoringFeeHeader = styled.header`
    font-size: 1.1em;
    font-weight: bold;
    padding-left: 0.5em;
    padding-bottom: 0.5em;
    border-bottom: 1px solid ${({ theme }) => theme.BORDER_LIGHT};
`;

const InputTutoringFee = styled.div`
    display: flex;
    align-items: end;
    width: 150px;

    span {
        min-width: 100px;
        padding: 0 0 0.8em 0.1em;
    }
`;

const SubmitButton = styled.button`
    width: 100%;
    height: 35px;
    margin-top: 3.5em;
    border-radius: 0.6em;
    background-color: ${({ theme }) => theme.PRIMARY_LIGHT};
    color: white;

    &:hover {
        background-color: ${({ theme }) => theme.PRIMARY};
    }
`;

export default TutoringApplicationForm;
