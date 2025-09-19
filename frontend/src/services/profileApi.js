import apiClient from './apiClient.js';

export const profileMeetings = async (page, condition) => {
  if (condition !== 'APPROVED' && condition !== 'PENDING') {
    console.error('잘못된 상태 조회:', condition);
    return { success: false, message: '잘못된 요청입니다.' };
  }
  try {
    const response = await apiClient.get(`/meetings/condition?page=${page}&size=${4}&status=${condition}`);

    if (response.data && response.data.success) {
      return { success: true, data: response.data.data };
    }

    return { success: false, message: '모임 목록을 불러오지 못했습니다.' };
  } catch (error) {
    console.error('프로필 모임 목록 조회 API 호출 실패:', error);
    return { success: false, message: '서버와 통신 중 오류가 발생했습니다.' };
  }
};