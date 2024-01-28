import React, { useState, useEffect } from 'react';
import Stomp from 'stompjs';
import { IUserRole } from '@/apis/user/user';
import axios from 'axios';

interface MatchingProps {
  memberId: number; // 현재 사용자의 ID
  chatRoomId: number;
  stompClient: Stomp.Client;
  userRole: IUserRole; // 사용자의 역할
}

const Matching: React.FC<MatchingProps> = ({ memberId, chatRoomId, stompClient, userRole }) => {
    const [matchingRequest, setMatchingRequest] = useState<any>(null); // 매칭 요청 데이터
    const [requestStatus, setRequestStatus] = useState<'pending' | 'accepted' | 'rejected'>('pending'); // 매칭 요청 상태

  useEffect(() => {
    // 매칭 요청 구독 설정
    const requestSubscription = stompClient.subscribe(`/topic/chatroom/${chatRoomId}/request`, (message) => {
      const data = JSON.parse(message.body);
      if (userRole == 'TEACHER') {
        setMatchingRequest(data);
      }
    });

    // 매칭 응답 구독 설정
    const responseSubscription = stompClient.subscribe(`/topic/chatroom/${chatRoomId}/response`, (message) => {
      const data = JSON.parse(message.body);
      setRequestStatus(data.isAccepted ? 'accepted' : 'rejected');
    });

    return () => {
      requestSubscription.unsubscribe();
      responseSubscription.unsubscribe();
    };
  }, [memberId, chatRoomId, stompClient, userRole]);

  // 매칭 요청 처리 함수
  const handleResponse = async (isAccepted: boolean) => {
    
    const responsePayload = {
        matchingId: matchingRequest.matchingId,
        isAccepted: isAccepted,
    };
    try {
    const response = await axios.post('/api/matching/response', responsePayload);
    console.log('매칭 응답 처리 결과:', response.data);

    } catch (error) {
    console.error('매칭 응답 처리 중 오류 발생', error);
    }
  };

  // 선생님 화면
  if (userRole == 'TEACHER' && matchingRequest) {
    return (
      <div>
        <div>매칭 요청: {matchingRequest.matchingId}</div>
        <button onClick={() => handleResponse(true)}>수락</button>
        <button onClick={() => handleResponse(false)}>거부</button>
      </div>
    );
  }

  // 학생 화면
  return <div>매칭 상태: {requestStatus}</div>;
};

export default Matching;
