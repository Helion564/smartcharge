import { useState, useEffect } from 'react'
import { MapContainer, TileLayer, useMap } from 'react-leaflet'
import 'leaflet/dist/leaflet.css'
import { Search, Filter, Navigation, Zap, Layers } from 'lucide-react'
import { stationApi } from '../../api/stationApi'
import StationMarker from './components/StationMarker'
import StationCard from './components/StationCard'
import { LoadingSpinner } from '../../components/ui'

// Helper component to recenter map
function MapController({ center, zoom }) {
  const map = useMap()
  useEffect(() => {
    map.setView(center, zoom)
  }, [center, zoom, map])
  return null
}

const INDIAN_CITIES = [
  { name: 'All India', lat: 20.5937, lng: 78.9629, zoom: 5 },
  { name: 'Mumbai', lat: 19.0760, lng: 72.8777, zoom: 12 },
  { name: 'Delhi NCR', lat: 28.6139, lng: 77.2090, zoom: 11 },
  { name: 'Bengaluru', lat: 12.9716, lng: 77.5946, zoom: 12 },
  { name: 'Hyderabad', lat: 17.3850, lng: 78.4867, zoom: 12 },
  { name: 'Pune', lat: 18.5204, lng: 73.8567, zoom: 12 },
  { name: 'Ahmedabad', lat: 23.0225, lng: 72.5714, zoom: 12 },
]

export default function MapPage() {
  const [stations, setStations] = useState([])
  const [loading, setLoading] = useState(true)
  const [selectedStation, setSelectedStation] = useState(null)
  const [mapCenter, setMapCenter] = useState([19.0760, 72.8777]) // Mumbai default
  const [mapZoom, setMapZoom] = useState(12)
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedCity, setSelectedCity] = useState('Mumbai')

  useEffect(() => {
    loadStations()
  }, [])

  const loadStations = async (lat = 19.0760, lng = 72.8777, radius = 50) => {
    setLoading(true)
    try {
      const res = await stationApi.getNearbyStations(lat, lng, radius)
      setStations(res.stations || [])
    } catch (err) {
      console.warn('Failed to fetch from backend, loading fallback Indian stations:', err)
      // Fallback mock stations if backend is starting
      setStations([
        {
          id: 'a1111111-1111-1111-1111-111111111101',
          name: 'Tata Power EZ Charge - BKC Hub',
          operatorName: 'Tata Power',
          city: 'Mumbai',
          state: 'Maharashtra',
          latitude: 19.0657,
          longitude: 72.8687,
          availableConnectors: 3,
          totalConnectors: 3,
          distanceKm: 1.2,
        },
        {
          id: 'a1111111-1111-1111-1111-111111111110',
          name: 'Magenta ChargeGrid - Inorbit Vashi',
          operatorName: 'Magenta Mobility',
          city: 'Navi Mumbai',
          state: 'Maharashtra',
          latitude: 19.0664,
          longitude: 72.9984,
          availableConnectors: 2,
          totalConnectors: 2,
          distanceKm: 14.5,
        },
        {
          id: 'a1111111-1111-1111-1111-111111111102',
          name: 'Statiq Hub - Cyber City',
          operatorName: 'Statiq',
          city: 'Gurugram',
          state: 'Haryana',
          latitude: 28.4950,
          longitude: 77.0895,
          availableConnectors: 2,
          totalConnectors: 2,
        },
        {
          id: 'a1111111-1111-1111-1111-111111111103',
          name: 'ChargeZone Fast Charging - Indiranagar',
          operatorName: 'ChargeZone',
          city: 'Bengaluru',
          state: 'Karnataka',
          latitude: 12.9716,
          longitude: 77.6412,
          availableConnectors: 2,
          totalConnectors: 2,
        }
      ])
    } finally {
      setLoading(false)
    }
  }

  const handleCityChange = (city) => {
    setSelectedCity(city.name)
    setMapCenter([city.lat, city.lng])
    setMapZoom(city.zoom)
    loadStations(city.lat, city.lng, city.name === 'All India' ? 2000 : 35)
  }

  const filteredStations = stations.filter(
    (s) =>
      s.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.city?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.operatorName?.toLowerCase().includes(searchQuery.toLowerCase())
  )

  return (
    <div className="flex h-[calc(100vh-4rem)] overflow-hidden">
      {/* Sidebar */}
      <aside className="w-full md:w-[420px] bg-white border-r border-slate-200 flex flex-col z-10 shadow-lg">
        {/* Search & City Filter */}
        <div className="p-4 border-b border-slate-100 space-y-3">
          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
            <input
              type="text"
              placeholder="Search station, city, operator..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
            />
          </div>

          {/* Quick city pills */}
          <div className="flex items-center gap-1.5 overflow-x-auto pb-1 scrollbar-none">
            {INDIAN_CITIES.map((c) => (
              <button
                key={c.name}
                onClick={() => handleCityChange(c)}
                className={`px-3 py-1 rounded-full text-xs font-semibold whitespace-nowrap transition ${
                  selectedCity === c.name
                    ? 'bg-emerald-600 text-white'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                {c.name}
              </button>
            ))}
          </div>
        </div>

        {/* Stations List */}
        <div className="flex-1 overflow-y-auto p-4 space-y-3">
          {loading ? (
            <LoadingSpinner />
          ) : filteredStations.length === 0 ? (
            <div className="text-center py-12 text-slate-500 text-sm">
              No charging stations found for this query.
            </div>
          ) : (
            filteredStations.map((station) => (
              <StationCard
                key={station.id}
                station={station}
                isSelected={selectedStation?.id === station.id}
                onSelect={(st) => {
                  setSelectedStation(st)
                  setMapCenter([st.latitude, st.longitude])
                  setMapZoom(15)
                }}
              />
            ))
          )}
        </div>
      </aside>

      {/* Map View */}
      <div className="flex-1 relative">
        <MapContainer
          center={mapCenter}
          zoom={mapZoom}
          className="w-full h-full"
          zoomControl={true}
        >
          <MapController center={mapCenter} zoom={mapZoom} />
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          {filteredStations.map((station) => (
            <StationMarker key={station.id} station={station} />
          ))}
        </MapContainer>
      </div>
    </div>
  )
}
