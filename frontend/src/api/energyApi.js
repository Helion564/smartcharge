import api from './axiosConfig.js'

export const energyApi = {
  getDemandPrediction: (stationId, hours = 24) =>
    api
      .get('/api/v1/energy/demand-prediction', { params: { stationId, hours } })
      .then((r) => r.data),

  getStationAnalytics: (stationId, period = '7d') =>
    api
      .get(`/api/v1/energy/stations/${stationId}/analytics`, { params: { period } })
      .then((r) => r.data),

  getOptimizationStatus: () =>
    api.get('/api/v1/energy/optimization/status').then((r) => r.data),

  runOptimization: () =>
    api.post('/api/v1/energy/optimization/run').then((r) => r.data),

  getSystemAnalytics: (params = {}) =>
    api.get('/api/v1/energy/analytics', { params }).then((r) => r.data),

  getCO2Savings: () =>
    api.get('/api/v1/energy/co2-savings').then((r) => r.data),
}
