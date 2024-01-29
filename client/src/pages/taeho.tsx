import { registUser } from '@/apis/user/regist-user';
import styled from 'styled-components';

const taeho = () => {
    return <StyledDiv onClick={() => registUser('STUDENT')}>유저 등록 요청</StyledDiv>;
};

const StyledDiv = styled.div`
    position: absolute;
    top: 10px;
    left: 10px;
    border: 1px solid black;
    padding: 1em;
`;

export default taeho;
