import api from './axiosConfig.js'

export const sessionApi = {
  startSession: (data) =>
    api.post('/api/v1/sessions/start', data).then((r) => r.data),

  stopSession: (sessionId) =>
    api.post(`/api/v1/sessions/${sessionId}/stop`).then((r) => r.data),

  getMySession: () =>
    api.get('/api/v1/sessions/active').then((r) => r.data),

  getMySessions: (params = {}) =>
    api.get('/api/v1/sessions', { params }).then((r) => r.data),

  getSessionById: (sessionId) =>
    api.get(`/api/v1/sessions/${sessionId}`).then((r) => r.data),

  getSessionStats: () =>
    api.get('/api/v1/sessions/stats').then((r) => r.data),
}
