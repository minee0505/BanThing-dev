import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:9000/api';

const martApi = axios.create({
    baseURL: API_URL,
    withCredentials: true,
});

export const getAllMarts = async () => {
    try {
        const response = await martApi.get('/marts');
        // 백엔드의 ApiResponse 형식에 맞춰 실제 데이터(data.data)를 반환
        if (response.data && response.data.success) {
            return { success: true, data: response.data.data };
        } else {
            return { success: false, message: response.data.message || '마트 목록을 불러오지 못했습니다.' };
        }
    } catch (error) {
        console.error('마트 목록 조회 API 호출 실패:', error);
        return { success: false, message: '서버와 통신 중 오류가 발생했습니다.' };
    }
};