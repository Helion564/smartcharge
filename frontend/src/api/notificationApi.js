import api from './axiosConfig.js'

export const notificationApi = {
  getNotifications: (params = {}) =>
    api.get('/api/v1/notifications', { params }).then((r) => r.data),

  markAsRead: (notificationId) =>
    api
      .patch(`/api/v1/notifications/${notificationId}/read`)
      .then((r) => r.data),

  markAllAsRead: () =>
    api.patch('/api/v1/notifications/read-all').then((r) => r.data),

  getUnreadCount: () =>
    api.get('/api/v1/notifications/unread-count').then((r) => r.data),
}
