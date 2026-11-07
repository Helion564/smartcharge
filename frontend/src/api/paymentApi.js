import api from './axiosConfig.js'

export const paymentApi = {
  getMyPayments: (params = {}) =>
    api.get('/api/v1/payments', { params }).then((r) => r.data),

  getWallet: () =>
    api.get('/api/v1/wallet').then((r) => r.data),

  topupWallet: (amount, paymentMethod) =>
    api
      .post('/api/v1/wallet/topup', { amount, paymentMethod })
      .then((r) => r.data),

  initiatePayment: (data) =>
    api.post('/api/v1/payments/initiate', data).then((r) => r.data),

  verifyPayment: (paymentId, data) =>
    api.post(`/api/v1/payments/${paymentId}/verify`, data).then((r) => r.data),
}
