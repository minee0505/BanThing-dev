
import apiClient from './apiClient';


/**
 * 특정 모임의 참여자 목록을 조회하는 API
 * @param {number} meetingId 모임 ID
 */
export const getParticipants = async (meetingId) => {
    const response = await apiClient.get(`/meetings/${meetingId}/participants`);
    return response.data;
};

/**
 * 모임 참여 신청을 승인하는 API
 * @param {number} meetingId 모임 ID
 * @param {number} participantId 승인할 참여자의 ID
 */
export const approveParticipant = async (meetingId, participantId) => {
    const response = await apiClient.post(`/meetings/${meetingId}/participants/${participantId}/approve`);
    return response.data;
};

/**
 * 모임 참여 신청을 거절하는 API
 * @param {number} meetingId 모임 ID
 * @param {number} participantId 거절할 참여자의 ID
 */
export const rejectParticipant = async (meetingId, participantId) => {
    const response = await apiClient.delete(`/meetings/${meetingId}/participants/${participantId}/reject`);
    return response.data;
};