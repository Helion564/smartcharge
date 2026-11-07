import { useNavigate } from 'react-router-dom'
import { Zap, MapPin, Navigation, IndianRupee } from 'lucide-react'
import { formatCurrency, formatDistance } from '../../../utils/formatters'
import { Badge } from '../../../components/ui'

export default function StationCard({ station, onSelect, isSelected }) {
  const navigate = useNavigate()

  const available = station.availableConnectors ?? 0
  const total = station.totalConnectors ?? (station.connectors?.length || 0)

  return (
    <div
      onClick={() => onSelect?.(station)}
      className={`p-4 rounded-xl border transition-all cursor-pointer ${
        isSelected
          ? 'border-emerald-500 bg-emerald-50/40 shadow-md ring-1 ring-emerald-500'
          : 'border-slate-200 bg-white hover:border-slate-300 hover:shadow-sm'
      }`}
    >
      <div className="flex items-start justify-between gap-2">
        <div>
          <h4 className="font-bold text-slate-900 text-sm">{station.name}</h4>
          <p className="text-xs text-slate-500 flex items-center gap-1 mt-0.5">
            <MapPin className="w-3 h-3 flex-shrink-0" />
            <span className="truncate max-w-[200px]">{station.city}, {station.state}</span>
          </p>
        </div>
        <Badge variant={available > 0 ? 'success' : 'danger'}>
          {available}/{total} Free
        </Badge>
      </div>

      <div className="mt-3 flex items-center justify-between text-xs pt-2 border-t border-slate-100">
        <span className="font-semibold text-slate-700 flex items-center gap-0.5">
          <Zap className="w-3.5 h-3.5 text-emerald-600 fill-emerald-600" />
          {station.operatorName || 'Public Network'}
        </span>

        {station.distanceKm != null && (
          <span className="text-slate-500 flex items-center gap-1">
            <Navigation className="w-3 h-3 text-slate-400" />
            {formatDistance(station.distanceKm)}
          </span>
        )}
      </div>

      <div className="mt-3 flex items-center gap-2">
        <button
          onClick={(e) => {
            e.stopPropagation()
            navigate(`/stations/${station.id}`)
          }}
          className="flex-1 py-1.5 px-3 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold shadow-sm transition"
        >
          View & Reserve
        </button>
      </div>
    </div>
  )
}
