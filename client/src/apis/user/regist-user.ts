import axios from 'axios';
import { IUserRole } from './user';

const path = '/members';

export const registUser = async (role: IUserRole) => {
    try {
        const studentInfo = {
            profile_url: 'https://avatars.githubusercontent.com/u/88070657?v=4',
            name: '새로운 유저',
            phone: '010-1234-5678',
            grade: '중학교 3학년',
            school: '솔빛중학교',
        };

        const createFileFromImageUrl = async (imageUrl: string, fileName: string) => {
            const response = await fetch(imageUrl);
            const blob = await response.blob();
            const file = new File([blob], fileName, { type: blob.type });
            return file;
        };

        const imageUrl = 'https://avatars.githubusercontent.com/u/88070657?v=4';
        const fileName = 'image.jpg';

        const imageFile = await createFileFromImageUrl(imageUrl, fileName);
        const formData = new FormData();
        formData.append(
            'request',
            new Blob([JSON.stringify(studentInfo)], { type: 'application/json' })
        );
        formData.append('profileImg', imageFile);

        const res = await axios.post(
            `https://i10a807.p.ssafy.io:8080/api${path}/students`,
            formData,
            {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
                withCredentials: true,
            }
        );
        console.log('등록 성공');
    } catch {
        /**
         * 유저 등록 실패
         */
        // refreshAccessToken()
        console.log('등록 실패');
    }
};
