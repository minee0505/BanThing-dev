import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:9000/api';

const api = axios.create({
    baseURL: API_URL,
    withCredentials: true,
});

export const getAllMeetings = async () => {
    try {
        const response = await api.get('/meetings/search');
        if (response.data && response.data.success) {
            return { success: true, data: response.data.data };
        }
        return { success: false, message: '모임 목록을 불러오지 못했습니다.' };
    } catch (error) {
        console.error('모임 목록 조회 API 호출 실패:', error);
        return { success: false, message: '서버와 통신 중 오류가 발생했습니다.' };
    }
};