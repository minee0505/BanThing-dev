// File: frontend/src/services/chatbotApi.js
import axios from 'axios';

// Vite 환경에서는 import.meta.env 사용
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:9000/api';

const chatbotApi = axios.create({
    baseURL: API_URL,
    timeout: 10000,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json'
    }
});

chatbotApi.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export const sendMessageToChatbot = async (message) => {
    try {
        const response = await chatbotApi.post('/chatbot/message', { message });

        if (response.data.success) {
            return { success: true, data: response.data.data, message: response.data.message };
        } else {
            throw new Error(response.data.message || '응답 처리 중 오류가 발생했습니다.');
        }
    } catch (error) {
        console.error('챗봇 메시지 전송 실패:', error);
        let errorMessage = '일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';

        if (error.response) {
            switch (error.response.status) {
                case 400:
                    errorMessage = '잘못된 요청입니다. 메시지를 확인해주세요.';
                    break;
                case 401:
                    errorMessage = '로그인이 필요합니다.';
                    break;
                case 429:
                    errorMessage = '요청이 너무 많습니다. 잠시 후 다시 시도해주세요.';
                    break;
                case 500:
                    errorMessage = 'AI 서비스에 일시적인 문제가 있습니다.';
                    break;
                default:
                    errorMessage = error.response.data?.message || errorMessage;
            }
        } else if (error.request) {
            errorMessage = '네트워크 연결을 확인해주세요.';
        }

        return { success: false, error: errorMessage };
    }
};

export const getChatbotHistory = async () => {
    try {
        const response = await chatbotApi.get('/chatbot/history');

        if (response.data.success) {
            return { success: true, data: response.data.data, message: response.data.message };
        } else {
            throw new Error(response.data.message || '대화 기록 조회 중 오류가 발생했습니다.');
        }
    } catch (error) {
        console.error('챗봇 대화 기록 조회 실패:', error);
        return { success: false, error: '대화 기록을 불러올 수 없습니다.', data: [] };
    }
};

export const isUserAuthenticated = () => {
    return document.cookie.includes('ACCESS_TOKEN');
};

export default chatbotApi;