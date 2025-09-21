import apiClient from './apiClient';

/**
 * 모임 상세 정보 조회
 * @param {number|string} meetingId - 모임 ID
 * @returns {Promise<Object>} API 응답
 */
export const getMeetingDetail = async (meetingId) => {
    try {
        const response = await apiClient.get(`/meetings/search/${meetingId}`);

        if (response.data && response.data.success) {
            return {
                success: true,
                data: response.data.data,
                message: response.data.message
            };
        } else {
            return {
                success: false,
                message: response.data?.message || '모임 정보를 불러올 수 없습니다.',
                data: null
            };
        }
    } catch (error) {
        console.error('모임 상세 조회 API 호출 실패:', error);

        if (error.response?.status === 404) {
            return {
                success: false,
                message: '존재하지 않는 모임입니다.',
                data: null
            };
        }

        if (error.response?.status === 401) {
            return {
                success: false,
                message: '로그인이 필요합니다.',
                data: null
            };
        }

        return {
            success: false,
            message: '서버와 통신 중 오류가 발생했습니다.',
            data: null
        };
    }
};

/**
 * 모임 참여 신청
 * @param {number|string} meetingId - 모임 ID
 * @returns {Promise<Object>} API 응답
 */
export const joinMeeting = async (meetingId) => {
    try {
        const response = await apiClient.post(`/meetings/${meetingId}/join`);

        if (response.data && response.data.success) {
            return {
                success: true,
                message: response.data.message || '모임 참여 신청이 완료되었습니다.',
                data: response.data.data
            };
        } else {
            return {
                success: false,
                message: response.data?.message || '참여 신청에 실패했습니다.',
                data: null
            };
        }
    } catch (error) {
        console.error('모임 참여 신청 API 호출 실패:', error);

        if (error.response?.status === 400) {
            return {
                success: false,
                message: error.response.data?.message || '참여 신청을 할 수 없습니다.',
                data: null
            };
        }

        // 403 상태 코드 처리 추가 (거절된 사용자)
        if (error.response?.status === 403) {
            return {
                success: false,
                message: error.response.data?.message || '참여 승인이 거절되었습니다! 다른 모임을 신청해주세요.',
                data: null
            };
        }

        if (error.response?.status === 409) {
            return {
                success: false,
                message: '이미 참여했거나 신청한 모임입니다.',
                data: null
            };
        }

        if (error.response?.status === 401) {
            return {
                success: false,
                message: '로그인이 필요합니다.',
                data: null
            };
        }

        return {
            success: false,
            message: '참여 신청 중 오류가 발생했습니다.',
            data: null
        };
    }
};

/**
 * 모임 탈퇴
 * @param {number|string} meetingId - 모임 ID
 * @returns {Promise<Object>} API 응답
 */
export const leaveMeeting = async (meetingId) => {
    try {
        const response = await apiClient.post(`/meetings/${meetingId}/leave`);

        if (response.data && response.data.success) {
            return {
                success: true,
                message: response.data.message || '모임에서 탈퇴했습니다.',
                data: response.data.data
            };
        } else {
            return {
                success: false,
                message: response.data?.message || '탈퇴에 실패했습니다.',
                data: null
            };
        }
    } catch (error) {
        console.error('모임 탈퇴 API 호출 실패:', error);

        if (error.response?.status === 400) {
            return {
                success: false,
                message: error.response.data?.message || '탈퇴할 수 없습니다.',
                data: null
            };
        }

        if (error.response?.status === 401) {
            return {
                success: false,
                message: '로그인이 필요합니다.',
                data: null
            };
        }

        return {
            success: false,
            message: '탈퇴 중 오류가 발생했습니다.',
            data: null
        };
    }
};

/**
 * 모임 댓글 조회
 * @param meetingId
 * @returns {Promise<null>}
 */
export const getComments = async (meetingId) => {
    try {
        const response = await apiClient.get(`/meetings/${meetingId}/comments`);
        // console.log("comments",response)
        if (response.data) {
            return {
                success: true,
                data: response.data.comments,
                message: response.data.message
            };
        } else {
            return {
                success: false,
                message: response.data?.message || '댓글 목록을 불러올 수 없습니다.',
                data: null
            }
        }
    }catch (error) {
        console.error('댓글 조회 API 호출 실패:', error);

        if (error.response?.status === 404) {
            return {
                success: false,
                message: '존재하지 않는 댓글입니다.',
                data: null
            };
        }

        if (error.response?.status === 401) {
            return {
                success: false,
                message: '로그인이 필요합니다.',
                data: null
            };
        }

        return {
            success: false,
            message: '서버와 통신 중 오류가 발생했습니다.',
            data: null
        };
    }
}

/**
 * 모임 댓글 작성
 * @param meetingId
 * @param comment
 * @returns {Promise<{success: boolean, data: *, message}>}
 */
export const postComment = async (meetingId, comment) => {
    try {
        const response = await apiClient.post(`/meetings/${meetingId}/comments`, comment);
        // console.log("postComment",response)
        if (response.data) {
            return {
                success: true,
                data: response.data.comments,
                message: response.data.message
            };
        } else {
            return {
                success: false,
                message: response.data?.message || '댓글 작성을 할 수 없습니다.',
                data: null
            }
        }
    } catch (error) {
        console.error('댓글 작성 API 호출 실패:', error);

        if (error.response?.status === 400) {
            return {
                success: false,
                message: error.response.data?.message || '댓글 작성을 할 수 없습니다.',
                data: null
            };
        }

        if (error.response?.status === 401) {
            return {
                success: false,
                message: '로그인이 필요합니다.',
                data: null
            };
        }

        return {
            success: false,
            message: '댓글 작성 중 오류가 발생했습니다.',
            data: null
        };
    }
}


export const updateComments = async (meetingId,commentId, comment) => {
    try {
        const response = await apiClient.put(`/meetings/${meetingId}/comments/${commentId}`, comment);
        console.log("postComment",response)
        if (response.data) {
            return {
                success: true,
                data: response.data.comments,
                message: response.data.message
            };
        } else {
            return {
                success: false,
                message: response.data?.message || '댓글 수정을 할 수 없습니다.',
                data: null
            }
        }
    } catch (error) {
        console.error('댓글 수정 API 호출 실패:', error);

        if (error.response?.status === 400) {
            return {
                success: false,
                message: error.response.data?.message || '댓글 수정을 할 수 없습니다.',
                data: null
            };
        }

        if (error.response?.status === 401) {
            return {
                success: false,
                message: '로그인이 필요합니다.',
                data: null
            };
        }

        return {
            success: false,
            message: '댓글 수정 중 오류가 발생했습니다.',
            data: null
        };
    }
}


/**
 * 모임 참여자 목록 조회
 * @param {number|string} meetingId - 모임 ID
 * @returns {Promise<Object>} API 응답
 */
export const getParticipants = async (meetingId) => {
    try {
        const response = await apiClient.get(`/meetings/${meetingId}/participants`);

        if (response.data && response.data.success) {
            return {
                success: true,
                data: response.data.data,
                message: response.data.message
            };
        } else {
            return {
                success: false,
                message: response.data?.message || '참여자 목록을 불러올 수 없습니다.',
                data: { approved: [], pending: [] }
            };
        }
    } catch (error) {
        console.error('참여자 목록 조회 API 호출 실패:', error);

        if (error.response?.status === 403) {
            return {
                success: false,
                message: '참여자 목록을 볼 권한이 없습니다.',
                data: { approved: [], pending: [] }
            };
        }

        if (error.response?.status === 401) {
            return {
                success: false,
                message: '로그인이 필요합니다.',
                data: { approved: [], pending: [] }
            };
        }

        return {
            success: false,
            message: '참여자 목록 조회 중 오류가 발생했습니다.',
            data: { approved: [], pending: [] }
        };
    }
};

/**
 * 참여자 승인 (호스트 전용)
 * @param {number|string} meetingId - 모임 ID
 * @param {number|string} participantId - 참여자 ID
 * @returns {Promise<Object>} API 응답
 */
export const approveParticipant = async (meetingId, participantId) => {
    try {
        const response = await apiClient.post(`/meetings/${meetingId}/participants/${participantId}/approve`);

        if (response.data && response.data.success) {
            return {
                success: true,
                message: response.data.message || '참여자를 승인했습니다.',
                data: response.data.data
            };
        } else {
            return {
                success: false,
                message: response.data?.message || '참여자 승인에 실패했습니다.',
                data: null
            };
        }
    } catch (error) {
        console.error('참여자 승인 API 호출 실패:', error);

        if (error.response?.status === 403) {
            return {
                success: false,
                message: '호스트만 참여자를 승인할 수 있습니다.',
                data: null
            };
        }

        if (error.response?.status === 401) {
            return {
                success: false,
                message: '로그인이 필요합니다.',
                data: null
            };
        }

        return {
            success: false,
            message: '참여자 승인 중 오류가 발생했습니다.',
            data: null
        };
    }
};

/**
 * 모집 마감 (호스트 전용)
 * @param {number|string} meetingId - 모임 ID
 * @returns {Promise<Object>} API 응답
 */
export const closeRecruitment = async (meetingId) => {
    try {
        const response = await apiClient.post(`/meetings/${meetingId}/close-recruitment`);

        if (response.data && response.data.success) {
            return {
                success: true,
                message: response.data.message || '모집을 마감했습니다.',
                data: response.data.data
            };
        } else {
            return {
                success: false,
                message: response.data?.message || '모집 마감에 실패했습니다.',
                data: null
            };
        }
    } catch (error) {
        console.error('모집 마감 API 호출 실패:', error);

        if (error.response?.status === 403) {
            return {
                success: false,
                message: '호스트만 모집을 마감할 수 있습니다.',
                data: null
            };
        }

        if (error.response?.status === 401) {
            return {
                success: false,
                message: '로그인이 필요합니다.',
                data: null
            };
        }

        return {
            success: false,
            message: '모집 마감 중 오류가 발생했습니다.',
            data: null
        };
    }
};

/**
 * 모임 완료 (호스트 전용)
 * @param {number|string} meetingId - 모임 ID
 * @returns {Promise<Object>} API 응답
 */
export const completeMeeting = async (meetingId) => {
    try {
        const response = await apiClient.post(`/meetings/${meetingId}/complete`);

        if (response.data && response.data.success) {
            return {
                success: true,
                message: response.data.message || '모임을 종료했습니다.',
                data: response.data.data
            };
        } else {
            return {
                success: false,
                message: response.data?.message || '모임 종료에 실패했습니다.',
                data: null
            };
        }
    } catch (error) {
        console.error('모임 완료 API 호출 실패:', error);

        if (error.response?.status === 403) {
            return {
                success: false,
                message: '호스트만 모임을 종료할 수 있습니다.',
                data: null
            };
        }

        if (error.response?.status === 401) {
            return {
                success: false,
                message: '로그인이 필요합니다.',
                data: null
            };
        }

        return {
            success: false,
            message: '모임 종료 중 오류가 발생했습니다.',
            data: null
        };
    }
};

/**
 * 모임 수정 (호스트 전용)
 * @param {number|string} meetingId - 모임 ID
 * @param {Object} meetingData - 수정할 모임 데이터
 * @returns {Promise<Object>} API 응답
 */
export const updateMeeting = async (meetingId, meetingData) => {
    try {
        const response = await apiClient.put(`/meetings/update/${meetingId}`, meetingData);

        if (response.data && response.data.success) {
            return {
                success: true,
                message: response.data.message || '모임 정보를 수정했습니다.',
                data: response.data.data
            };
        } else {
            return {
                success: false,
                message: response.data?.message || '모임 수정에 실패했습니다.',
                data: null
            };
        }
    } catch (error) {
        console.error('모임 수정 API 호출 실패:', error);

        if (error.response?.status === 403) {
            return {
                success: false,
                message: '호스트만 모임을 수정할 수 있습니다.',
                data: null
            };
        }

        if (error.response?.status === 401) {
            return {
                success: false,
                message: '로그인이 필요합니다.',
                data: null
            };
        }

        if (error.response?.status === 400) {
            return {
                success: false,
                message: error.response.data?.message || '입력 정보를 확인해주세요.',
                data: null
            };
        }

        return {
            success: false,
            message: '모임 수정 중 오류가 발생했습니다.',
            data: null
        };
    }
};

/**
 * 모임 삭제 (호스트 전용)
 * @param {number|string} meetingId - 모임 ID
 * @returns {Promise<Object>} API 응답
 */
export const deleteMeeting = async (meetingId) => {
    try {
        const response = await apiClient.delete(`/meetings/delete/${meetingId}`);

        if (response.data && response.data.success) {
            return {
                success: true,
                message: response.data.message || '모임을 삭제했습니다.',
                data: response.data.data
            };
        } else {
            return {
                success: false,
                message: response.data?.message || '모임 삭제에 실패했습니다.',
                data: null
            };
        }
    } catch (error) {
        console.error('모임 삭제 API 호출 실패:', error);

        if (error.response?.status === 403) {
            return {
                success: false,
                message: '호스트만 모임을 삭제할 수 있습니다.',
                data: null
            };
        }

        if (error.response?.status === 401) {
            return {
                success: false,
                message: '로그인이 필요합니다.',
                data: null
            };
        }

        return {
            success: false,
            message: '모임 삭제 중 오류가 발생했습니다.',
            data: null
        };
    }
};

/**
 * 피드백 생성 API 호출 함수
 * @param {number} meetingId - 모임 ID
 * @param {number} receiverId - 피드백을 받을 사용자 ID
 * @param {number} giverId - 피드백을 주는 사용자 ID
 * @param {string} feedbackType - 피드백 타입 ('POSITIVE' 또는 'NEGATIVE')
 * @returns {Promise<object>} API 응답 결과
 */
export const postFeedback = async (meetingId, receiverId, giverId, feedbackType) => {
        try {
            const requestData = {
                meetingId: meetingId,
                giverId: giverId,
                receiverId: receiverId,
                feedbackType: feedbackType // 'POSITIVE' 또는 'NEGATIVE'
            };

            console.log('피드백 생성 요청 데이터:', requestData);

            const response = await apiClient.post('/feedbacks', requestData);

            return {
                success: true,
                data: response.data.data, // CommonResponse 구조에서 실제 데이터
                message: response.data.message
            };
        } catch (error) {
            console.error('피드백 생성 실패:', error);

            // 에러 응답 처리
            if (error.response && error.response.data) {
                return {
                    success: false,
                    message: error.response.data.message || '피드백 생성에 실패했습니다.',
                    error: error.response.data
                };
            }

            return {
                success: false,
                message: '네트워크 오류가 발생했습니다.',
                error: error.message
            };
        }
    };

/**
 * 사용자별 피드백 조회 API 호출 함수
 * @param {number} userId - 사용자 ID
 * @param {string} type - 피드백 타입 ('RECEIVED' 또는  'GIVEN')
 * @returns {Promise<object>} API 응답 결과
 */
export const getUserFeedbacks = async (userId, type = 'RECEIVED') => {
    try {
        const response = await apiClient.get(`/feedbacks/users/${userId}?type=${type}`);

        return {
            success: true,
            data: response.data.data,
            message: response.data.message
        };
    } catch (error) {
        console.error('사용자 피드백 조회 실패:', error);

        if (error.response && error.response.data) {
            return {
                success: false,
                message: error.response.data.message || '피드백 조회에 실패했습니다.',
                error: error.response.data
            };
        }

        return {
            success: false,
            message: '네트워크 오류가 발생했습니다.',
            error: error.message
        };
    }
};

/**
 * 사용자 신뢰도 점수 조회 API 호출 함수
 * @param {number} userId - 사용자 ID
 * @returns {Promise<object>} API 응답 결과
 */
export const getUserScore = async (userId) => {
    try {
        const response = await apiClient.get(`/feedbacks/users/${userId}/score`);

        return {
            success: true,
            data: response.data.data,
            message: response.data.message
        };
    } catch (error) {
        console.error('사용자 점수 조회 실패:', error);

        if (error.response && error.response.data) {
            return {
                success: false,
                message: error.response.data.message || '점수 조회에 실패했습니다.',
                error: error.response.data
            };
        }

        return {
            success: false,
            message: '네트워크 오류가 발생했습니다.',
            error: error.message
        };
    }
};


export default {
    getMeetingDetail,
    joinMeeting,
    leaveMeeting,
    getParticipants,
    approveParticipant,
    closeRecruitment,
    completeMeeting,
    updateMeeting,
    deleteMeeting
};