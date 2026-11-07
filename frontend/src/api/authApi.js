import api from './axiosConfig.js'

export const authApi = {
  login: (credentials) =>
    api.post('/api/v1/auth/login', credentials).then((r) => r.data),

  register: (data) =>
    api.post('/api/v1/auth/register', data).then((r) => r.data),

  logout: () =>
    api.post('/api/v1/auth/logout').then((r) => r.data),

  refreshToken: (refreshToken) =>
    api.post('/api/v1/auth/refresh', { refreshToken }).then((r) => r.data),

  changePassword: (data) =>
    api.post('/api/v1/auth/change-password', data).then((r) => r.data),

  me: () =>
    api.get('/api/v1/auth/me').then((r) => r.data),
}
