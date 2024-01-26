import { ChangeEvent, MouseEvent, useEffect, useState } from 'react';
import styled from 'styled-components';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import ChatStatus from './ChatStatus';

interface ChattingProps {
    chatRoomId: number;
}

const Chatting = ({ chatRoomId }: ChattingProps) => {
    const [stompClient, setStompClient] = useState<Stomp.Client>();
    const [message, setMessage] = useState('');
    const [chatList, setChatList] = useState<{ text: string }[]>([{ text: '첫 번째 메시지' }]);

    useEffect(() => {
        const socket = new SockJS('http://localhost:8080/');
        const stomp = Stomp.over(socket);
        setStompClient(stomp);

        const connectAndSubscribe = (() => {
            stomp.connect({}, () => {
                console.log('Connected to WebSocket');
                stomp.subscribe('/api/chat', (message) => {
                    const data = JSON.parse(message.body);
                    const { text } = data;
                    setChatList((prev) => [...prev, { text }]);
                });
            });
        })();
    }, []);

    const sendMessage = () => {
        if (!stompClient) return;
        stompClient.send('/api/chat', {}, JSON.stringify(message));
        setMessage('');
    };

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        setMessage(e.target.value);
    };

    return (
        <StyledChatting>
            <ChatStatus chatRoomId={chatRoomId} />
            <TestForm>
                <input type="text" onChange={handleChange} />
                <button onClick={sendMessage}>전송</button>
            </TestForm>
            <ChatList>
                {chatList.map((chat) => {
                    return <div key={`chat_${chat.text}`}>{chat.text}</div>;
                })}
            </ChatList>
        </StyledChatting>
    );
};

const StyledChatting = styled.div`
    width: 100%;
    height: 100%;
`;

const TestForm = styled.div``;

const ChatList = styled.div``;

export default Chatting;
