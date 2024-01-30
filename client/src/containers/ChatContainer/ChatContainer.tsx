import { useRecoilState } from 'recoil';
import styled from 'styled-components';
import Chatting from '@/components/Chatting';
import Chat from '@/components/icons/Chat';
import Close from '@/components/icons/Close';
import GoBack from '@/components/icons/GoBack';
import chatSessionAtom from '@/recoil/atoms/chat';
import ChatRoomList from '@/components/ChatRoomList';

const ChatContainer = () => {
    const [chatSession, setChatSession] = useRecoilState(chatSessionAtom);

    if (!chatSession.openChat) {
        return (
            <ChatButton
                onClick={() =>
                    setChatSession((prev) => {
                        return { ...prev, openChat: true };
                    })
                }
            >
                <Chat />
            </ChatButton>
        );
    }
    return (
        <StyledChatContainer>
            <CloseChatButton
                onClick={() =>
                    setChatSession((prev) => {
                        return { ...prev, openChat: false };
                    })
                }
            >
                <Close />
            </CloseChatButton>
            <>
                {chatSession.chatRoomId ? (
                    <>
                        <Header>
                            <GoChatRoomListButton
                                onClick={() =>
                                    setChatSession((prev) => {
                                        return { ...prev, chatRoomId: null };
                                    })
                                }
                            >
                                <GoBack />
                            </GoChatRoomListButton>
                            <span>김성은 선생님</span>
                        </Header>
                        <Chatting />
                    </>
                ) : (
                    <>
                        <Header>채팅</Header>
                        <ChatRoomList />
                    </>
                )}
            </>
        </StyledChatContainer>
    );
};

const ChatButton = styled.button`
    position: fixed;
    bottom: 3em;
    right: 3em;
    width: 65px;
    height: 65px;
    z-index: 100;
    background-color: ${({ theme }) => theme.PRIMARY_LIGHT};
    border-radius: 1em;
`;

const StyledChatContainer = styled.div`
    position: absolute;
    bottom: 30px;
    right: 30px;
    width: 350px;
    height: 550px;
    z-index: 100;
    border-radius: 1.6em;
    background-color: white;
    border: 2px solid ${({ theme }) => theme.PRIMARY_LIGHT};
    box-shadow: 0px 0px 20px rgba(105, 105, 105, 0.25);
    padding: 2.5em;
`;

const CloseChatButton = styled.button`
    position: absolute;
    top: 27px;
    right: 27px;
    width: 13px;
    height: 13px;
`;

const Header = styled.header`
    font-size: 1.2em;
    font-weight: bold;
    padding-bottom: 0.8em;
    border-bottom: 1px solid ${({ theme }) => theme.BORDER_LIGHT};
    display: flex;
`;

const GoChatRoomListButton = styled.button`
    margin-right: 12px;
`;

export default ChatContainer;
