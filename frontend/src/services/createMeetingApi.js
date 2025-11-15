import apiClient from './apiClient';

/**
 * 새로운 모임을 생성합니다. (파일 업로드 기능 추가)
 * @param {object} meetingData - 폼 데이터 객체
 * @param {File|null} imageFile - 업로드할 이미지 파일
 * @returns {Promise<{success: boolean, data?: object, message?: string}>}
 */
export const createMeeting = async (meetingData, imageFile) => {
    const formData = new FormData();
    formData.append('request', new Blob([JSON.stringify(meetingData)], { type: "application/json" }));

    if (imageFile) {
        formData.append('imageFile', imageFile);
    }

    try {
        const response = await apiClient.post('/meetings', formData, {
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

        // axios 에러 객체의 상세 내용을 확인합니다.
        if (error.response) {
            // 서버가 응답을 했지만, 상태 코드가 2xx가 아닌 경우
            console.error('서버 응답 데이터:', error.response.data);
            console.error('서버 응답 상태:', error.response.status);
            console.error('서버 응답 헤더:', error.response.headers);
            // 서버가 보낸 에러 메시지를 우선적으로 사용합니다.
            const serverMessage = error.response.data?.message || '서버에서 오류가 발생했습니다.';
            return { success: false, message: serverMessage };
        } else if (error.request) {
            // 요청은 이루어졌으나, 응답을 받지 못한 경우 (네트워크 문제 등)
            console.error('서버로부터 응답을 받지 못했습니다:', error.request);
            return { success: false, message: '서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요.' };
        } else {
            // 요청을 설정하는 중에 에러가 발생한 경우
            console.error('Axios 요청 설정 오류:', error.message);
            return { success: false, message: '요청을 보내는 중 문제가 발생했습니다.' };
        }
    }
};