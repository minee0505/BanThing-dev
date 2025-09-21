import apiClient from './apiClient';

export const AuthService = {
  me: () => apiClient.get('/users/me'),
  refresh: () => apiClient.post('/auth/refresh', {}, { skipAuthRefresh: true }),
  logout: () => apiClient.post('/auth/logout'),
  updateAgreement: () => apiClient.put('/users/me/agreement'), // 약관 동의 API 함수
};
