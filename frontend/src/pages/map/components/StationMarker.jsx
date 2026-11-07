import { Marker, Popup } from 'react-leaflet'
import L from 'leaflet'
import { Link } from 'react-router-dom'
import { Zap } from 'lucide-react'

// Custom Leaflet DivIcon
const createCustomIcon = (isAvailable) => {
  return L.divIcon({
    className: 'custom-ev-marker',
    html: `
      <div style="
        width: 34px;
        height: 34px;
        background: ${isAvailable ? '#059669' : '#dc2626'};
        border: 3px solid white;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 4px 10px rgba(0,0,0,0.25);
        color: white;
      ">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="white" stroke="white" stroke-width="1.5">
          <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
        </svg>
      </div>
    `,
    iconSize: [34, 34],
    iconAnchor: [17, 17],
    popupAnchor: [0, -20],
  })
}

export default function StationMarker({ station }) {
  const isAvailable = (station.availableConnectors ?? 0) > 0
  const icon = createCustomIcon(isAvailable)

  return (
    <Marker position={[station.latitude, station.longitude]} icon={icon}>
      <Popup className="custom-popup">
        <div className="p-1 min-w-[200px]">
          <h4 className="font-bold text-slate-900 text-sm">{station.name}</h4>
          <p className="text-xs text-slate-500 mt-0.5">{station.operatorName}</p>
          <div className="mt-2 text-xs flex items-center justify-between font-semibold">
            <span className={isAvailable ? 'text-emerald-600' : 'text-red-600'}>
              {station.availableConnectors || 0} Connectors Available
            </span>
          </div>
          <Link
            to={`/stations/${station.id}`}
            className="mt-3 block text-center py-1.5 px-3 bg-emerald-600 text-white rounded text-xs font-semibold hover:bg-emerald-700"
          >
            Station Details & Slots
          </Link>
        </div>
      </Popup>
    </Marker>
  )
}
