import {profileMeetings} from "../services/profileApi.js";

// 프로필 최초 진입했을때 스켈레톤 몇 개 보여줄지 계산용 로더 함수
// 페이지에 진입하기 전에 실행됨.
export const profileMeetingLoader = async () => {
  // 한 페이지에 보여지는 모임 개수는 최대 4개
  const meetingsPerPage = 4;

  // 첫 페이지(page=0, APPROVED 조건)
  const result = await profileMeetings(0, 'APPROVED');

  if (result.success) {
    const totalElements = result.data.totalElements;
    if (totalElements === 0) {
      return 1;
    }
    return Math.min(meetingsPerPage, totalElements); // 첫 페이지 개수
  } else {
    return meetingsPerPage; // 실패하면 기본 4개 보여주기
  }
};