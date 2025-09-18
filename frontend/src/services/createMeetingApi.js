import apiClient from './apiClient';

/**
 * 새로운 모임을 생성합니다. (파일 업로드 기능 추가)
 * @param {object} meetingData - 폼 데이터 객체
 * @param {File|null} imageFile - 업로드할 이미지 파일
 * @returns {Promise<{success: boolean, data?: object, message?: string}>}
 */
export const createMeeting = async (meetingData, imageFile) => {
    // FormData 객체를 생성합니다.
    const formData = new FormData();

    // 1. DTO 데이터를 JSON 문자열로 변환하여 'request' 파트에 추가합니다.
    //    CORS 문제 등을 피하기 위해 Blob으로 감싸서 보냅니다.
    formData.append('request', new Blob([JSON.stringify(meetingData)], { type: "application/json" }));

    // 2. 이미지 파일이 있으면 'imageFile' 파트에 추가합니다.
    if (imageFile) {
        formData.append('imageFile', imageFile);
    }

    try {
        const response = await apiClient.post('/meetings', formData, {
            // FormData를 전송할 때는 Content-Type을 'multipart/form-data'로 설정해야 합니다.
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });

        if (response.data && response.data.success) {
            return { success: true, data: response.data.data };
        } else {
            return { success: false, message: response.data.message || '모임을 생성하지 못했습니다.' };
        }
    } catch (error) {
        console.error('모임 생성 API 호출 실패:', error);
        return { success: false, message: '서버와 통신 중 오류가 발생했습니다.' };
    }
};