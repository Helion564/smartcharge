import api from './axiosConfig.js'

export const bookingApi = {
  createBooking: (data) =>
    api.post('/api/v1/bookings', data).then((r) => r.data),

  getMyBookings: (params = {}) =>
    api.get('/api/v1/bookings', { params }).then((r) => r.data),

  getBooking: (bookingId) =>
    api.get(`/api/v1/bookings/${bookingId}`).then((r) => r.data),

  cancelBooking: (bookingId, reason) =>
    api
      .post(`/api/v1/bookings/${bookingId}/cancel`, { reason })
      .then((r) => r.data),

  checkIn: (bookingId) =>
    api.post(`/api/v1/bookings/${bookingId}/check-in`).then((r) => r.data),
}
