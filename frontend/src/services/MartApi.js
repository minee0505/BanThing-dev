import apiClient from './apiClient';

/**
 * 서버에 등록된 모든 마트 목록을 조회합니다.
 * @returns {Promise<{success: boolean, data?: object[], message?: string}>}
 */
export const getAllMarts = async () => {
    try {
        const response = await apiClient.get('/marts');

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