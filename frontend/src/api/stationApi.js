import api from './axiosConfig.js'

export const stationApi = {
  getNearbyStations: (lat, lng, radius = 10, params = {}) =>
    api
      .get('/api/v1/stations/nearby', { params: { lat, lng, radius, ...params } })
      .then((r) => r.data),

  getStation: (stationId) =>
    api.get(`/api/v1/stations/${stationId}`).then((r) => r.data),

  searchStations: (params) =>
    api.get('/api/v1/stations/search', { params }).then((r) => r.data),

  getStationAvailability: (stationId) =>
    api.get(`/api/v1/stations/${stationId}/availability`).then((r) => r.data),

  updateConnectorStatus: (stationId, connectorId, status) =>
    api
      .patch(`/api/v1/stations/${stationId}/connectors/${connectorId}/status`, { status })
      .then((r) => r.data),

  getStationsInBounds: (bounds, params = {}) =>
    api
      .get('/api/v1/stations', {
        params: {
          northEastLat: bounds.northEast.lat,
          northEastLng: bounds.northEast.lng,
          southWestLat: bounds.southWest.lat,
          southWestLng: bounds.southWest.lng,
          ...params,
        },
      })
      .then((r) => r.data),

  getOperatorStations: () =>
    api.get('/api/v1/stations/my').then((r) => r.data),

  createStation: (data) =>
    api.post('/api/v1/stations', data).then((r) => r.data),

  updateStation: (stationId, data) =>
    api.put(`/api/v1/stations/${stationId}`, data).then((r) => r.data),

  getAllStations: (params = {}) =>
    api.get('/api/v1/admin/stations', { params }).then((r) => r.data),

  approveStation: (stationId) =>
    api.post(`/api/v1/admin/stations/${stationId}/approve`).then((r) => r.data),
}
